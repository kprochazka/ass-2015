package ass.prochka6.hw;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class TCPEchoServerExecutor implements Runnable {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) { // Test for correct # of args
            throw new IllegalArgumentException("Parameter(s): <Port>");
        }

        int echoServerPort = Integer.parseInt(args[0]); // Server port

        // Create a server socket to accept client connection requests
        ServerSocket serverSocket = new ServerSocket(echoServerPort);

        Logger logger = Logger.getLogger("executor");

        Executor service = Executors.newCachedThreadPool();

        // Run forever, accepting and spawning threads to service each connection
        while (true) {
            // Block waiting for connection
            Socket clientSocket = serverSocket.accept();
            service.execute(new AssignmentProtocol(clientSocket, logger));

            if (Thread.currentThread().isInterrupted()) {
                logger.info("Server interrupted.");
                break;
            }

        }

        serverSocket.close();
    }

    @Override
    public void run() {
        try {
            main(new String[]{"8080"});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
