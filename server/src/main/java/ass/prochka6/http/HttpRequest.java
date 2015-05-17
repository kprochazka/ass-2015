package ass.prochka6.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Handles one HTTP request, i.e. parses the HTTP request and returns the response.
 */
class HttpRequest {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequest.class);

    public static final int BUFFER_SIZE = 8 * 1024;

    private final OutputStream outputStream;

    private final PushbackInputStream inputStream;

    private String uri;

    private Method method;

    private Map<String, String> params;

    private Map<String, String> headers;

    private CookieHandler cookies;

    private String queryParameterString;

    private String remoteIp;

    public HttpRequest(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = new PushbackInputStream(inputStream, HttpRequest.BUFFER_SIZE);
        this.outputStream = outputStream;
    }

    public HttpRequest(InputStream inputStream, OutputStream outputStream, InetAddress inetAddress) {
        this.inputStream = new PushbackInputStream(inputStream, HttpRequest.BUFFER_SIZE);
        this.outputStream = outputStream;
        this.remoteIp = inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress() ? "127.0.0.1" : inetAddress.getHostAddress().toString();
        this.headers = new HashMap<String, String>();
    }

    /**
     * Decodes the sent headers and loads the data into Key/value pairs
     */
    private void decodeHeader(BufferedReader in, Map<String, String> pre, Map<String, String> parms, Map<String, String> headers) throws ResponseException {
        try {
            // Read the request line
            String inLine = in.readLine();
            if (inLine == null) {
                return;
            }

            StringTokenizer st = new StringTokenizer(inLine);
            if (!st.hasMoreTokens()) {
                throw new ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
            }

            pre.put("method", st.nextToken());

            if (!st.hasMoreTokens()) {
                throw new ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
            }

            String uri = st.nextToken();

            // Decode parameters from the URI
            int qmi = uri.indexOf('?');
            if (qmi >= 0) {
                decodeParams(uri.substring(qmi + 1), parms);
                uri = Util.decodePercent(uri.substring(0, qmi));
            } else {
                uri = Util.decodePercent(uri);
            }

            // If there's another token, its protocol version,
            // followed by HTTP headers. Ignore version but parse headers.
            // NOTE: this now forces header names lower case since they are
            // case insensitive and vary by client.
            if (!st.hasMoreTokens()) {
                LOG.debug("no protocol version specified, strange..");
            }
            String line = in.readLine();
            while (line != null && line.trim().length() > 0) {
                int p = line.indexOf(':');
                if (p >= 0) {
                    headers.put(line.substring(0, p).trim().toLowerCase(Locale.US), line.substring(p + 1).trim());
                }
                line = in.readLine();
            }

            pre.put("uri", uri);
        } catch (IOException ioe) {
            throw new ResponseException(Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage(), ioe);
        }
    }

    /**
     * Decodes parameters in percent-encoded URI-format ( e.g.
     * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given
     * Map. NOTE: this doesn't support multiple identical keys due to the
     * simplicity of Map.
     */
    private void decodeParams(String params, Map<String, String> p) {
        if (params == null) {
            this.queryParameterString = "";
            return;
        }

        this.queryParameterString = params;
        StringTokenizer st = new StringTokenizer(params, "&");
        while (st.hasMoreTokens()) {
            String e = st.nextToken();
            int sep = e.indexOf('=');
            if (sep >= 0) {
                p.put(Util.decodePercent(e.substring(0, sep)).trim(), Util.decodePercent(e.substring(sep + 1)));
            } else {
                p.put(Util.decodePercent(e).trim(), "");
            }
        }
    }

    public void execute() throws IOException {
        try {
            // Read whole header in 8MB
            byte[] buffer = new byte[BUFFER_SIZE];
            int splitByte = 0;
            int rlen = 0;

            int read = -1;
            try {
                read = this.inputStream.read(buffer, 0, BUFFER_SIZE);
            } catch (Exception e) {
                Util.safeClose(this.inputStream);
                Util.safeClose(this.outputStream);
                throw new ProtocolSocketException("Request exception", e);
            }
            if (read == -1) {
                // socket has been closed
                Util.safeClose(this.inputStream);
                Util.safeClose(this.outputStream);
                throw new ProtocolSocketException("Socket has been closed or no data was transmitted!");
            }

            while (read > 0) {
                rlen += read;
                splitByte = findHeaderEnd(buffer, rlen);
                if (splitByte > 0) {
                    break;
                }
                read = this.inputStream.read(buffer, rlen, BUFFER_SIZE - rlen);
            }

            if (splitByte < rlen) {
                this.inputStream.unread(buffer, splitByte, rlen - splitByte);
            }

            this.params = new HashMap<String, String>();
            if (null == this.headers) {
                this.headers = new HashMap<String, String>();
            } else {
                this.headers.clear();
            }

            if (null != this.remoteIp) {
                this.headers.put("remote-addr", this.remoteIp);
                this.headers.put("http-client-ip", this.remoteIp);
            }

            // Create a BufferedReader for parsing the header.
            BufferedReader hin = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer, 0, rlen)));

            // Decode the header into params and header java properties
            Map<String, String> pre = new HashMap<String, String>();
            decodeHeader(hin, pre, this.params, this.headers);

            this.method = Method.lookup(pre.get("method"));
            if (this.method == null) {
                throw new ResponseException(Status.BAD_REQUEST, "BAD REQUEST: Syntax error.");
            }

            this.uri = pre.get("uri");

            this.cookies = new CookieHandler(this.headers);

            // Ok, now do the serve()
            Response response = HttpServer.serve(this);
//            Response response = null;
            if (response == null) {
                throw new ResponseException(Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: Serve() returned a null response.");
            } else {
                this.cookies.unloadQueue(response);
                response.setRequestMethod(this.method);
                response.send(this.outputStream);
            }
        } catch (SocketException e) {
            // throw it out to close socket object (finalAccept)
            throw e;
        } catch (SocketTimeoutException ste) {
            // treat socket timeouts the same way we treat socket exceptions
            // i.e. close the stream & finalAccept object by throwing the
            // exception up the call stack.
            throw ste;
        } catch (IOException ioe) {
            Response
                r =
                Response.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            r.send(this.outputStream);
            Util.safeClose(this.outputStream);
        } catch (ResponseException re) {
            Response r = Response.newFixedLengthResponse(re.getStatus(), "text/html", re.getMessage());
            r.send(this.outputStream);
            Util.safeClose(this.outputStream);
        } finally {
        }
    }

    /**
     * Find byte index separating header from body. It must be the last byte
     * of the first two sequential new lines.
     */
    private int findHeaderEnd(final byte[] buf, int rlen) {
        int splitByte = 0;
        while (splitByte + 3 < rlen) {
            if (buf[splitByte] == '\r' && buf[splitByte + 1] == '\n' && buf[splitByte + 2] == '\r' && buf[splitByte + 3] == '\n') {
                return splitByte + 4;
            }
            splitByte++;
        }
        return 0;
    }

    public CookieHandler getCookies() {
        return this.cookies;
    }

    public final Map<String, String> getHeaders() {
        return this.headers;
    }

    public final InputStream getInputStream() {
        return this.inputStream;
    }

    public final Method getMethod() {
        return this.method;
    }

    public final Map<String, String> getParams() {
        return this.params;
    }

    public String getQueryParameterString() {
        return this.queryParameterString;
    }

    public final String getUri() {
        return this.uri;
    }

}
