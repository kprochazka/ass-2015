package ass.prochka6;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TCPEchoServerThreadPool {

    private static ExecutorService executorService;

    public static void main(String[] args) throws IOException {

        // Test for correct # of args
        if (args.length != 2) {
            throw new IllegalArgumentException("Parameter(s): <Port> <Threads>");
        }

        int echoServerPort = Integer.parseInt(args[0]);
        int threadPoolSize = Integer.parseInt(args[1]);

        // Create a server socket to accept client connection requests
        final ServerSocket serverSocket = new ServerSocket(echoServerPort);

        final Logger logger = Logger.getLogger("homework");

        // Spawn a fixed number of threads to service clients
        executorService = Executors.newFixedThreadPool(threadPoolSize, new ProtocolThreadThreadFactory());

        for (int i = 0; i < threadPoolSize; i++) {
            executorService.submit(() -> {
                while (true) {
                    try {
                        // Wait for a connection
                        Socket clientSocket = serverSocket.accept();
                        new HomeWorkProtocol(clientSocket, logger).run();
                    } catch (IOException ex) {
                        logger.log(Level.WARNING, "Client accept failed", ex);
                    }
                }
            });
        }

        executorService.shutdown();
    }

    static void close() {
        executorService.shutdownNow();
    }

    /**
     * The default thread factory
     */
    static class ProtocolThreadThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        ProtocolThreadThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "server-" +
                         poolNumber.getAndIncrement() +
                         "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                                  namePrefix + threadNumber.getAndIncrement(),
                                  0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

}
