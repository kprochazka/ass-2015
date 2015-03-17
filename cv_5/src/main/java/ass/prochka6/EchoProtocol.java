package ass.prochka6;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Logger;

public class EchoProtocol implements Runnable {

    private static final int BUFFER_SIZE = 32;

    private final Logger logger;
    private final Socket clientSocket;

    public EchoProtocol(Socket clntSock, Logger logger) {
        this.clientSocket = clntSock;
        this.logger = logger;
    }

    public static void handleEchoClient(Socket clntSock, Logger logger) {
        new EchoProtocol(clntSock, logger).run();
    }

    @Override
    public void run() {
        try {
            int recvMsgSize;   // Size of received message
            byte[] receiveBuffer = new byte[BUFFER_SIZE];  // Receive buffer

            SocketAddress clientAddress = clientSocket.getRemoteSocketAddress();
            logger.info("Handling client at " + clientAddress);

            InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();

            // Receive until client closes connection, indicated by -1 return
            while ((recvMsgSize = in.read(receiveBuffer)) != -1) {

                out.write(receiveBuffer, 0, recvMsgSize);

                // internal work
                if (recvMsgSize >= 4) { // for quit
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    break;
                }

            }

            out.flush();

            // Close the socket.  We are done with this client!
            logger.info("Closing client at " + clientAddress);
            clientSocket.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
