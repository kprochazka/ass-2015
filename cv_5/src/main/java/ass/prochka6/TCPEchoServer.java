package ass.prochka6;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class TCPEchoServer {

    public static void main(String[] args) throws IOException {

        // Test for correct # of args
        if (args.length != 1) {
            throw new IllegalArgumentException("Parameter(s): <Port>");
        }

        int serverPort = Integer.parseInt(args[0]);

        // Create a server socket to accept client connection requests
        ServerSocket serverSocket = new ServerSocket(serverPort);

        Logger logger = Logger.getLogger("practical");

        while (true) { // Run forever, accepting and servicing connections
            Socket clientSocket = serverSocket.accept();     // Get client connection

            EchoProtocol.handleEchoClient(clientSocket, logger);
        }
    }

}
