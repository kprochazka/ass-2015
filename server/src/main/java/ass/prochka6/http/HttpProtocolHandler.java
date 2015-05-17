package ass.prochka6.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * The runnable that will be used for every new client connection.
 */
class HttpProtocolHandler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(HttpProtocolHandler.class);

    private final Socket socket;
    private final InputStream inputStream;

    public HttpProtocolHandler(InputStream inputStream, Socket socket) {
        this.inputStream = inputStream;
        this.socket = socket;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        OutputStream outputStream = null;
        try {
            outputStream = this.socket.getOutputStream();
            HttpRequest request = new HttpRequest(this.inputStream, outputStream, this.socket.getInetAddress());
            if (!socket.isClosed()) {
                request.execute();
            } else {
                LOG.info("Socked is closed()!");
            }
        } catch (Throwable ex) {
            LOG.error("Error occurred during Request processing on Socket ({})!", socket.getRemoteSocketAddress(), ex);
        } finally {
            Util.safeClose(outputStream);
            Util.safeClose(this.inputStream);
            Util.safeClose(this.socket);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Socket ({}) request processed in {}ms", socket.getRemoteSocketAddress(), System.currentTimeMillis() - start);
            }
        }
    }

}
