package ass.prochka6;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * HTTP response. Return one of these from serve().
 */
class HttpResponse extends OutputStream {

    private static final Logger LOG = LoggerFactory.getLogger(HttpResponse.class);

    /**
     * Flag that the header part was already submitted.
     */
    private boolean headSubmitted;

    /**
     * HTTP status code after processing, e.g. "200 OK", Status.OK
     */
    private Status status = Status.INTERNAL_ERROR;

    /**
     * MIME type of content, e.g. "text/html"
     */
    private String mimeType;

    private long contentLength;

    /**
     * Headers for the HTTP response. Use addHeader() to add lines.
     */
    private final Map<String, String> header = new HashMap<String, String>();

    private List<Cookie> cookies = new ArrayList<>(2);

    private final OutputStream outputStream;

    public HttpResponse(OutputStream outputStream) {
        if (outputStream == null) {
            throw new IllegalArgumentException("Output stream could not be NULL!");
        }
        this.outputStream = outputStream;
    }

    /**
     * Adds given line to the header.
     */
    public void addHeader(String name, String value) {
        this.header.put(name, value);
    }

    public String getHeader(String name) {
        return this.header.get(name);
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public void addCookie(Cookie cookie) {
        if (cookie != null) {
            if (headSubmitted) {
                throw new IllegalStateException("HTTP Header already sent!");
            }
            this.cookies.add(cookie);
        }
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void write(byte[] b) throws IOException {
        checkHeaderSend();
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkHeaderSend();
        outputStream.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        checkHeaderSend();
        outputStream.write(b);
    }

    protected void checkHeaderSend() throws IOException {
        if (!headSubmitted) {
            // First submit header part
            sendHeader();
        }
    }

    protected boolean isHeadSubmitted() {
        return headSubmitted;
    }

    protected boolean headerAlreadySent(Map<String, String> header, String name) {
        boolean alreadySent = false;
        for (Map.Entry<String, String> pair : header.entrySet()) {
            alreadySent |= pair.getKey().equalsIgnoreCase(name);
        }
        return alreadySent;
    }

    protected void sendHeader() throws IOException {
        if (headSubmitted) {
            return;
        }

        headSubmitted = true;

        if (this.status == null) {
            throw new IllegalStateException("HTTP Status can't be null.");
        }

        // Propagate Cookies
        setCookiesHeader();

        String mime = this.mimeType;
        SimpleDateFormat gmtFormatter = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        gmtFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8")), false);
            pw.print("HTTP/1.1 " + this.status.getDescription() + " \r\n");

            if (mime != null) {
                pw.print("Content-Type: " + mime + "\r\n");
            }

            if (this.header.get("Date") == null) {
                pw.print("Date: " + gmtFormatter.format(new Date()) + "\r\n");
            }

            for (Map.Entry<String, String> headerPair : this.header.entrySet()) {
                pw.print(headerPair.getKey() + ": " + headerPair.getValue() + "\r\n");
            }

            sendConnectionHeaderIfNotAlreadyPresent(pw, this.header);
            sendContentLengthHeaderIfNotAlreadyPresent(pw, this.header, this.contentLength);

            pw.print("\r\n");
            pw.flush();
            outputStream.flush();
        } catch (IOException ioe) {
            LOG.error("Could not send response to the client", ioe);
            throw ioe;
        }
    }

    protected void sendConnectionHeaderIfNotAlreadyPresent(PrintWriter pw, Map<String, String> header) {
        if (!headerAlreadySent(header, "connection")) {
            pw.print("Connection: close\r\n");
        }
    }

    protected long sendContentLengthHeaderIfNotAlreadyPresent(PrintWriter pw, Map<String, String> header, long size) {
        for (Map.Entry<String, String> pair : header.entrySet()) {
            if (pair.getKey().equalsIgnoreCase("content-length")) {
                try {
                    return Long.parseLong(pair.getValue());
                } catch (NumberFormatException ex) {
                    return size;
                }
            }
        }

        pw.print("Content-Length: " + size + "\r\n");
        return size;
    }

    protected void setCookiesHeader() {
        for (Cookie cookie : this.cookies) {
            this.addHeader("Set-Cookie", cookie.getHTTPHeader());
        }
    }

}
