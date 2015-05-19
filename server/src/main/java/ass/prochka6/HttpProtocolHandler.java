package ass.prochka6;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

/**
 * The runnable that will be used for every new client connection.
 *
 * @author Kamil Prochazka
 */
class HttpProtocolHandler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(HttpProtocolHandler.class);

    private final Socket socket;
    private final InputStream inputStream;
    private final ServerContext serverContext;
    private final RequestHandlerChain chain;

    public HttpProtocolHandler(InputStream inputStream, Socket socket, ServerContext serverContext, RequestHandlerChain chain) {
        this.inputStream = inputStream;
        this.socket = socket;
        this.serverContext = serverContext;
        this.chain = new RequestHandlerChainImpl(chain.getRequestHandlers());
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        OutputStream outputStream = null;
        try {
            outputStream = this.socket.getOutputStream();

            if (socket.isClosed()) {
                LOG.debug("Socket is closed, stop processing.");
                return;
            }

            // Parse HttpRequest, create HttpResponse and pass to the chain
            HttpRequest request = new HttpRequest(this.serverContext, this.inputStream, this.socket.getInetAddress());
            request.parse();
            HttpResponse response = new HttpResponse(outputStream);

            chain.handle(request, response);

            // Force send header if none of the handler
            if (!response.isHeadSubmitted()) {
                if (response.getStatus() == null) {
                    response.setStatus(Status.NOT_FOUND);
                }
                response.sendHeader();
            }
        } catch (InvalidRequestException re) {
            LOG.error("InvalidRequestException", re);
            invalidRequestResponse(outputStream, re);
        } catch (Throwable ex) {
            LOG.error("Error occurred during Request processing on Socket ({})!", socket.getRemoteSocketAddress(), ex);
            internalServerErrorResponse(outputStream);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.flush();
                } catch (IOException ex) {
                    // ignore
                }
                Util.safeClose(outputStream);
                Util.safeClose(this.inputStream);
                Util.safeClose(this.socket);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Socket ({}) request processed in {}ms", socket.getRemoteSocketAddress(), System.currentTimeMillis() - start);
                }
            }
        }
    }

    private void invalidRequestResponse(OutputStream outputStream, InvalidRequestException re) {
        if (outputStream != null) {
            HttpResponse response = new HttpResponse(outputStream);
            response.setMimeType("text/plain");
            response.setStatus(re.getStatus());

            String message = re.getMessage();
            byte[] bytes = new byte[0];
            try {
                bytes = message.getBytes("utf-8");
            } catch (UnsupportedEncodingException e) {
                // ignore
            }
            response.setContentLength(bytes.length);
            try {
                response.write(bytes);
            } catch (IOException ex) {
                LOG.error("Error occurred during sending InvalidRequestException response!", ex);
            }
        }
    }

    private void internalServerErrorResponse(OutputStream outputStream) {
        if (outputStream != null) {
            HttpResponse response = new HttpResponse(outputStream);
            response.setMimeType("text/plain");

            try {
                response.sendHeader();
            } catch (IOException ex) {
                LOG.error("Error occurred during sending HttpResponse of internal server error.", ex);
            }
        }
    }

}
