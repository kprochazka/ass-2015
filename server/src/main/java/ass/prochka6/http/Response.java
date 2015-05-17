package ass.prochka6.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * HTTP response. Return one of these from serve().
 */
public class Response {

    private static final Logger LOG = LoggerFactory.getLogger(Response.class);

    /**
     * HTTP status code after processing, e.g. "200 OK", Status.OK
     */
    private Status status;

    /**
     * MIME type of content, e.g. "text/html"
     */
    private String mimeType;

    /**
     * Data of the response, may be null.
     */
    private InputStream data;

    private long contentLength;

    /**
     * Headers for the HTTP response. Use addHeader() to add lines.
     */
    private final Map<String, String> header = new HashMap<String, String>();

    /**
     * The request method that spawned this response.
     */
    private Method requestMethod;

    /**
     * Use chunkedTransfer
     */
    private boolean chunkedTransfer;

    /**
     * Creates a fixed length response if totalBytes>=0, otherwise chunked.
     */
    public Response(Status status, String mimeType, InputStream data, long totalBytes) {
        this.status = status;
        this.mimeType = mimeType;
        if (data == null) {
            this.data = new ByteArrayInputStream(new byte[0]);
            this.contentLength = 0L;
        } else {
            this.data = data;
            this.contentLength = totalBytes;
        }
        this.chunkedTransfer = this.contentLength < 0;
    }

    /**
     * Adds given line to the header.
     */
    public void addHeader(String name, String value) {
        this.header.put(name, value);
    }

    public InputStream getData() {
        return this.data;
    }

    public String getHeader(String name) {
        return this.header.get(name);
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public Method getRequestMethod() {
        return this.requestMethod;
    }

    public Status getStatus() {
        return this.status;
    }

    private boolean headerAlreadySent(Map<String, String> header, String name) {
        boolean alreadySent = false;
        for (String headerName : header.keySet()) {
            alreadySent |= headerName.equalsIgnoreCase(name);
        }
        return alreadySent;
    }

    /**
     * Sends given response to the socket.
     */
    public void send(OutputStream outputStream) {
        String mime = this.mimeType;
        SimpleDateFormat gmtFormatter = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        gmtFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            if (this.status == null) {
                throw new IllegalStateException("sendResponse(): Status can't be null.");
            }

            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8")), false);
            pw.print("HTTP/1.1 " + this.status.getDescription() + " \r\n");

            if (mime != null) {
                pw.print("Content-Type: " + mime + "\r\n");
            }

            if (this.header == null || this.header.get("Date") == null) {
                pw.print("Date: " + gmtFormatter.format(new Date()) + "\r\n");
            }

            if (this.header != null) {
                for (String key : this.header.keySet()) {
                    String value = this.header.get(key);
                    pw.print(key + ": " + value + "\r\n");
                }
            }

            sendConnectionHeaderIfNotAlreadyPresent(pw, this.header);

            if (this.requestMethod != Method.HEAD && this.chunkedTransfer) {
                sendAsChunked(outputStream, pw);
            } else {
                long pending = this.data != null ? this.contentLength : 0;
                pending = sendContentLengthHeaderIfNotAlreadyPresent(pw, this.header, pending);
                pw.print("\r\n");
                pw.flush();
                sendAsFixedLength(outputStream, pending);
            }
            outputStream.flush();
            Util.safeClose(this.data);
        } catch (IOException ioe) {
            LOG.error("Could not send response to the client", ioe);
        }
    }

    private void sendAsChunked(OutputStream outputStream, PrintWriter pw) throws IOException {
        pw.print("Transfer-Encoding: chunked\r\n");
        pw.print("\r\n");
        pw.flush();
        int BUFFER_SIZE = 16 * 1024;
        byte[] CRLF = "\r\n".getBytes();
        byte[] buff = new byte[BUFFER_SIZE];
        int read;
        while ((read = this.data.read(buff)) > 0) {
            outputStream.write(String.format("%x\r\n", read).getBytes());
            outputStream.write(buff, 0, read);
            outputStream.write(CRLF);
        }
        outputStream.write(String.format("0\r\n\r\n").getBytes());
    }

    private void sendAsFixedLength(OutputStream outputStream, long pending) throws IOException {
        if (this.requestMethod != Method.HEAD && this.data != null) {
            long BUFFER_SIZE = 16 * 1024;
            byte[] buff = new byte[(int) BUFFER_SIZE];
            while (pending > 0) {
                int read = this.data.read(buff, 0, (int) (pending > BUFFER_SIZE ? BUFFER_SIZE : pending));
                if (read <= 0) {
                    break;
                }
                outputStream.write(buff, 0, read);
                pending -= read;
            }
        }
    }

    protected void sendConnectionHeaderIfNotAlreadyPresent(PrintWriter pw, Map<String, String> header) {
        if (!headerAlreadySent(header, "connection")) {
            pw.print("Connection: close\r\n");
        }
    }

    protected long sendContentLengthHeaderIfNotAlreadyPresent(PrintWriter pw, Map<String, String> header, long size) {
        for (String headerName : header.keySet()) {
            if (headerName.equalsIgnoreCase("content-length")) {
                try {
                    return Long.parseLong(header.get(headerName));
                } catch (NumberFormatException ex) {
                    return size;
                }
            }
        }

        pw.print("Content-Length: " + size + "\r\n");
        return size;
    }

    public void setChunkedTransfer(boolean chunkedTransfer) {
        this.chunkedTransfer = chunkedTransfer;
    }

    public void setData(InputStream data) {
        this.data = data;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setRequestMethod(Method requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Create a response with unknown length (using HTTP 1.1 chunking).
     */
    public static Response newChunkedResponse(Status status, String mimeType, InputStream data) {
        return new Response(status, mimeType, data, -1);
    }

    /**
     * Create a response with known length.
     */
    public static Response newFixedLengthResponse(Status status, String mimeType, InputStream data, long totalBytes) {
        return new Response(status, mimeType, data, totalBytes);
    }

    /**
     * Create a text response with known length.
     */
    public static Response newFixedLengthResponse(Status status, String mimeType, String txt) {
        if (txt == null) {
            return newFixedLengthResponse(status, mimeType, new ByteArrayInputStream(new byte[0]), 0);
        } else {
            byte[] bytes;
            try {
                bytes = txt.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOG.error("encoding problem, responding nothing", e);
                bytes = new byte[0];
            }
            return newFixedLengthResponse(status, mimeType, new ByteArrayInputStream(bytes), bytes.length);
        }
    }

    /**
     * Create a text response with known length.
     */
    public static Response newFixedLengthResponse(String msg) {
        return newFixedLengthResponse(Status.OK, "text/html", msg);
    }

}
