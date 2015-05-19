package ass.prochka6;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Http server, the socket acceptor runs in separate ServerPortListener Thread.
 *
 * @author Kamil Prochazka
 */
class HttpServer {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class);

    /**
     * The runnable that will be used for the main listening thread.
     */
    private class ServerPortListener implements Runnable {

        private final int readTimeout;

        private final RequestHandlerChain chain;

        private ServerPortListener(int readTimeout) {
            this.readTimeout = readTimeout;
            chain = new RequestHandlerChainImpl(Arrays.asList(new FileResourceRequestHandler(HttpServer.this.serverContext)));
        }

        @Override
        public void run() {
            LOG.info("ServerPortListener started");
            do {
                Socket clientSocket = null;
                try {
                    clientSocket = HttpServer.this.serverSocket.accept();

                    LOG.debug("Client Socket accepted {}", clientSocket);

                    if (this.readTimeout > 0) {
                        clientSocket.setSoTimeout(this.readTimeout);
                    }
                    InputStream inputStream = clientSocket.getInputStream();
                    HttpProtocolHandler httpProtocolHandler = new HttpProtocolHandler(inputStream, clientSocket, HttpServer.this.serverContext, chain);
                    executorService.submit(httpProtocolHandler);
                } catch (IOException e) {
                    LOG.info("Communication with the client is broken :(", e);
                }
            } while (!HttpServer.this.serverSocket.isClosed());
            LOG.info("ServerPortListener shutdown");
        }
    }

    /**
     * Maximum time to wait on Socket.getInputStream().read() (in milliseconds)
     * This is required as the Keep-Alive HTTP connections would otherwise block
     * the socket reading thread forever (or as long the browser is open).
     */
    public static final int SOCKET_READ_TIMEOUT = 5000;

    private final String hostname;
    private final int port;

    private ServerSocket serverSocket;
    private Thread serverThread;

    private final ServerContext serverContext;

    private ExecutorService executorService;

    public HttpServer(int port) {
        this(null, port, 50);
    }

    public HttpServer(String hostname, int port, int executionThreads) {
        this.hostname = hostname;
        this.port = port;
        this.serverContext = new ServerContext();
//        executorService = Executors.newFixedThreadPool(executionThreads);
        executorService = Executors.newCachedThreadPool();
    }

    public int getListeningPort() {
        return this.serverSocket == null ? -1 : this.serverSocket.getLocalPort();
    }

    public boolean isAlive() {
        return isStarted() && !this.serverSocket.isClosed() && this.serverThread.isAlive();
    }

    public ServerContext getServerContext() {
        return serverContext;
    }

    /**
     * Start the server.
     *
     * @throws IOException if the socket is in use.
     */
    public void start() throws IOException {
        start(SOCKET_READ_TIMEOUT);
    }

    /**
     * Start the server.
     *
     * @param timeout  readTimeout to use for socket connections.
     * @throws IOException  if the socket is in use.
     */
    public void start(int timeout) throws IOException {
        this.serverSocket = new ServerSocket();
        this.serverSocket.setReuseAddress(true);
        this.serverSocket.bind(this.hostname != null ? new InetSocketAddress(this.hostname, this.port) : new InetSocketAddress(this.port));

        this.serverThread = new Thread(new ServerPortListener(timeout));
        this.serverThread.setDaemon(true);
        this.serverThread.setName("Server Main Listener");
        this.serverThread.start();
    }

    /**
     * Stop the server and shutdown all currently processing requests.
     */
    public void stop() {
        try {
            Util.safeClose(this.serverSocket);
            executorService.shutdownNow();
            if (this.serverThread != null) {
                this.serverThread.join();
            }
        } catch (Exception e) {
            LOG.error("Could not stop all connections", e);
        }
    }

    public boolean isStarted() {
        return this.serverSocket != null && this.serverThread != null;
    }

}
