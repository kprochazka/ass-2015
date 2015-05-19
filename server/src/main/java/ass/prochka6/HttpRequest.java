package ass.prochka6;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Represents Http request. This class is responsible for parsing request content.
 *
 * @author Kamil Prochazka
 */
class HttpRequest {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequest.class);

    /**
     * Input buffer size of 8MB should be enough for whole request size.
     */
    public static final int BUFFER_SIZE = 8 * 1024;

    private final PushbackInputStream inputStream;

    private String remoteIp;

    private String uri;

    private Method method;

    private Map<String, List<String>> params = new HashMap<>();

    private Map<String, String> headers = new HashMap<>();

    private Map<String, String> cookies = new HashMap<>();

    private String queryParameterString;

    private Map<String, Object> attributes = new HashMap<>();

    private final ServerContext serverContext;

    public HttpRequest(ServerContext serverContext, InputStream inputStream) {
        this.serverContext = serverContext;
        this.inputStream = new PushbackInputStream(inputStream, HttpRequest.BUFFER_SIZE);
    }

    public HttpRequest(ServerContext serverContext, InputStream inputStream, InetAddress inetAddress) {
        this.serverContext = serverContext;
        this.inputStream = new PushbackInputStream(inputStream, HttpRequest.BUFFER_SIZE);
        this.remoteIp = inetAddress.isLoopbackAddress() || inetAddress.isAnyLocalAddress() ? "127.0.0.1" : inetAddress.getHostAddress().toString();
    }

    /**
     * Decodes the sent headers and loads the data into Key/value pairs
     */
    protected void decodeHeader(BufferedReader in) throws InvalidRequestException {
        try {
            // Read the request line
            String inLine = in.readLine();
            if (inLine == null) {
                return;
            }

            StringTokenizer st = new StringTokenizer(inLine);
            if (!st.hasMoreTokens()) {
                throw new InvalidRequestException(Status.BAD_REQUEST, "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
            }

            this.method = Method.lookup(st.nextToken());
            if (this.method == null) {
                throw new InvalidRequestException(Status.BAD_REQUEST, "BAD REQUEST: Syntax error. Missing or unrecognized HTTP method name.");
            }
            if (!Method.GET.equals(this.method)) {
                throw new InvalidRequestException(Status.METHOD_NOT_ALLOWED, "METHOD NOT ALLOWED: Only GET method is supported.");
            }

            if (!st.hasMoreTokens()) {
                throw new InvalidRequestException(Status.BAD_REQUEST, "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
            }

            String uri = st.nextToken();

            // Decode parameters from the URI
            int queryParamStart = uri.indexOf('?');
            if (queryParamStart >= 0) {
                decodeParams(uri.substring(queryParamStart + 1));
                uri = Util.decodePercent(uri.substring(0, queryParamStart));
            } else {
                uri = Util.decodePercent(uri);
            }

            // If there's another token, its protocol version,
            // followed by HTTP headers. Ignore version but parse headers.
            // NOTE: this now forces header names lower case since they are
            // case insensitive and vary by client.
            if (!st.hasMoreTokens()) {
                LOG.debug("no protocol version specified, strange...");
            }

            // Now parse the header params
            String line = in.readLine();
            while (line != null && line.trim().length() > 0) {
                int delim = line.indexOf(':');
                if (delim >= 0) {
                    headers.put(line.substring(0, delim).trim().toLowerCase(Locale.US), line.substring(delim + 1).trim());
                }
                line = in.readLine();
            }

            this.uri = uri;
        } catch (IOException ioe) {
            throw new InvalidRequestException(Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage(), ioe);
        }
    }

    /**
     * Decodes parameters in percent-encoded URI-format ( e.g.
     * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given.
     * Support multi same name parameters stored in List of values.
     */
    protected void decodeParams(String queryString) {
        if (queryString == null) {
            this.queryParameterString = "";
            return;
        }

        this.queryParameterString = queryString;

        StringTokenizer st = new StringTokenizer(queryString, "&");
        while (st.hasMoreTokens()) {
            String element = st.nextToken();
            int separator = element.indexOf('=');
            String propertyName = separator >= 0 ? Util.decodePercent(element.substring(0, separator)).trim() : Util.decodePercent(element).trim();
            if (!params.containsKey(propertyName)) {
                params.put(propertyName, new ArrayList<>());
            }
            String propertyValue = separator >= 0 ? Util.decodePercent(element.substring(separator + 1)) : null;
            if (propertyValue != null) {
                params.get(propertyName).add(propertyValue);
            }
        }
    }

    protected void decodeCookies() {
        String raw = headers.get("cookie");
        if (raw != null) {
            String[] tokens = raw.split(";");
            for (String token : tokens) {
                String[] data = token.trim().split("=");
                if (data.length == 2) {
                    this.cookies.put(data[0], data[1]);
                }
            }
        }
    }

    protected void parse() throws SocketException, IOException, InvalidRequestException {
        // Read whole header in 8MB
        byte[] buffer = new byte[BUFFER_SIZE];
        int headerSplitByte = 0;
        int readlength = 0;

        int read = -1;
        read = this.inputStream.read(buffer, 0, BUFFER_SIZE);

        if (read == -1) {
            throw new InvalidRequestException(Status.BAD_REQUEST, "Socket has been closed or no data was sent!");
        }

        // find end of HTTP header section
        while (read > 0) {
            readlength += read;
            headerSplitByte = findHeaderEnd(buffer, readlength);
            if (headerSplitByte > 0) {
                break;
            }
            read = this.inputStream.read(buffer, readlength, BUFFER_SIZE - readlength);
        }

        // Push back bytes of headers, because we need whole HTTP header to start parsing
        if (headerSplitByte < readlength) {
            this.inputStream.unread(buffer, headerSplitByte, readlength - headerSplitByte);
        }

        if (null != this.remoteIp) {
            this.headers.put("remote-addr", this.remoteIp);
            this.headers.put("http-client-ip", this.remoteIp);
        }

        // Create a BufferedReader for parsing the header.
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer, 0, readlength), "utf-8"));

        // Decode the header into params and header java properties
        decodeHeader(reader);

        // Decode Cookies from headers
        decodeCookies();
    }

    /**
     * Find byte index separating header from body. It must be the last byte
     * of the first two sequential new lines.
     */
    protected int findHeaderEnd(final byte[] buf, int rlen) {
        int splitByte = 0;
        while (splitByte + 3 < rlen) {
            if (buf[splitByte] == '\r' && buf[splitByte + 1] == '\n' && buf[splitByte + 2] == '\r' && buf[splitByte + 3] == '\n') {
                return splitByte + 4;
            }
            splitByte++;
        }
        return 0;
    }

    //
    // Public API
    //

    public Map<String, String> getHeaders() {
        return this.headers;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public Method getMethod() {
        return this.method;
    }

    public Map<String, List<String>> getParams() {
        return this.params;
    }

    public String getParam(String name) {
        List<String> values = params.get(name);
        if (values != null) {
            // There was no associated value, return empty string
            if (values.isEmpty()) {
                return "";
            }

            // Return last added element <-> last win!
            return values.get(values.size() - 1);
        }
        return null;
    }

    public String getQueryParameterString() {
        return this.queryParameterString;
    }

    public final String getUri() {
        return this.uri;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public ServerContext getServerContext() {
        return serverContext;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

}
