package ass.prochka6.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ass.prochka6.ServerContext;

/**
 *
 * @author Kamil Prochazka
 */
public class HttpServer {

    private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class);

    /**
     * The runnable that will be used for the main listening thread.
     */
    private class ServerPortListener implements Runnable {

        private final int timeout;

        private ServerPortListener(int timeout) {
            this.timeout = timeout;
        }

        @Override
        public void run() {
            LOG.info("ServerPortListener started");
            do {
                Socket clientSocket = null;
                try {
                    clientSocket = HttpServer.this.serverSocket.accept();

                    LOG.debug("Client Socket accepted {}", clientSocket);

                    if (this.timeout > 0) {
                        clientSocket.setSoTimeout(this.timeout);
                    }
                    InputStream inputStream = clientSocket.getInputStream();
                    HttpProtocolHandler httpProtocolHandler = new HttpProtocolHandler(inputStream, clientSocket);
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

    /**
     * Pseudo-Parameter to use to store the actual query string in the
     * parameters map for later re-processing.
     */
    private static final String QUERY_STRING_PARAMETER = "ass.prochka6.HttpServer.QUERY_STRING";

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
        executorService = Executors.newFixedThreadPool(executionThreads);
    }

    /**
     * Decode parameters from a URL, handing the case where a single parameter
     * name might have been supplied several times, by return lists of values.
     * In general these lists will contain a single element.
     *
     * @param params original parameters values, as passed to the <code>serve()</code> method.
     * @return a map of <code>String</code> (parameter name) to <code>List&lt;String&gt;</code> (a list of the values supplied).
     */
    protected Map<String, List<String>> decodeParameters(Map<String, String> params) {
        return this.decodeParameters(params.get(QUERY_STRING_PARAMETER));
    }

    /**
     * Decode parameters from a URL, handing the case where a single parameter
     * name might have been supplied several times, by return lists of values.
     * In general these lists will contain a single element.
     *
     * @param queryString a query string pulled from the URL.
     * @return a map of <code>String</code> (parameter name) to <code>List&lt;String&gt;</code> (a list of the values supplied).
     */
    protected Map<String, List<String>> decodeParameters(String queryString) {
        Map<String, List<String>> params = new HashMap<>();

        if (queryString != null) {
            StringTokenizer st = new StringTokenizer(queryString, "&");
            while (st.hasMoreTokens()) {
                String element = st.nextToken();
                int separator = element.indexOf('=');
                String propertyName = separator >= 0 ? Util.decodePercent(element.substring(0, separator)).trim() : Util.decodePercent(element).trim();
                if (!params.containsKey(propertyName)) {
                    params.put(propertyName, new ArrayList<String>());
                }
                String propertyValue = separator >= 0 ? Util.decodePercent(element.substring(separator + 1)) : null;
                if (propertyValue != null) {
                    params.get(propertyName).add(propertyValue);
                }
            }
        }

        return params;
    }


    public final int getListeningPort() {
        return this.serverSocket == null ? -1 : this.serverSocket.getLocalPort();
    }

    public final boolean isAlive() {
        return isStarted() && !this.serverSocket.isClosed() && this.serverThread.isAlive();
    }

    /**
     * Override this to customize the server.
     * <p/>
     * (By default, this returns a 404 "Not Found" plain text error response.)
     *
     * @param httpRequest the HTTP httpRequest
     * @return HTTP response, see class Response for details
     */
    public static Response serve(HttpRequest httpRequest) {
        Method method = httpRequest.getMethod();
        Map<String, String> params = httpRequest.getParams();
        params.put(QUERY_STRING_PARAMETER, httpRequest.getQueryParameterString());
        return serve(httpRequest.getUri(), method, httpRequest.getHeaders(), params);
    }

    /**
     * Override this to customize the server.
     * <p/>
     * <p/>
     * (By default, this returns a 404 "Not Found" plain text error response.)
     *
     * @param uri Percent-decoded URI without parameters, for example "/index.cgi"
     * @param method only GET is supported
     * @param params Parsed, percent decoded parameters from URI.
     * @param headers header entries, percent decoded
     * @return HTTP response, see class Response for details
     */
    public static Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> params) {
//        return Response.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Not Found");
        return Response.newFixedLengthResponse(Status.OK, "text/plain", "Příliš žluťoučký kůň úpěl ďábelské ódy!");
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
     * @param timeout  timeout to use for socket connections.
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
