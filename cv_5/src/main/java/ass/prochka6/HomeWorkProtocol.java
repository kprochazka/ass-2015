package ass.prochka6;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * &Uacute;loha 6 - doma
 *
 * Implementujte echo server protokol tak, &#382;e o&#269;ek&aacute;v&aacute; zna&#269;ku &lt;hi&gt; a pak ihned p&iacute;&scaron;e text zp&#283;t, jinak
 * zav&#345;e spojen&iacute;
 *
 * <p> &lt;fin&gt; - ukon&#269;&iacute; klienta
 *
 * <p> &lt;flip&gt; p&#345;epne m&oacute;d textu a na&#269;&iacute;t&aacute; a&#382; do &lt;/flip&gt; pak po&scaron;le text po sp&aacute;tku (jin&aacute;
 * zna&#269;ka zav&#345;e spojen&iacute;)
 *
 *
 * <p> Otestujte pres JUnit i fail sc&eacute;n&aacute;&#345;e Nap&#345;
 *
 * <br> # &lt;hi&gt; Hello Bob &lt;flip&gt;Damn&lt;/flip&gt; wow &lt;fin&gt; <br> &lt;# Hello Bob nmaD wow closes <br> # &lt;hi&gt; Hello &lt;b&gt;Bob&lt;b&gt;
 * &lt;flip&gt;Damn&lt;/flip&gt; wow &lt;fin&gt; <br> &lt;# Hello closes
 *
 * @author Kamil Prochazka
 */
public class HomeWorkProtocol implements Runnable, Closeable {

    static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");

    // protocol keyword
    static final String HI = "<hi>";
    static final String FIN = "<fin>";
    static final String FLIP_START = "<flip>";
    static final String FLIP_END = "</flip>";
    static final int LONGEST_TAG_LENGTH = FLIP_END.length();

    static final List<String> ALLOWED_ENTITIES = new ArrayList<>(Arrays.asList(HI, FIN, FLIP_START, FLIP_END));

    private final Logger logger;
    private final Socket clientSocket;

    private BufferedReader reader;
    private PrintWriter writer;

    private StringBuilder receivedChars = new StringBuilder();

    public HomeWorkProtocol(Socket clientSocket, Logger logger) {
        this.logger = logger;
        this.clientSocket = clientSocket;

        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), DEFAULT_CHARSET));
            writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), DEFAULT_CHARSET));
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception occurred during stream initialization.", e);
        }
    }

    @Override
    public void run() {
        try {
            SocketAddress clientAddress = clientSocket.getRemoteSocketAddress();
            logger.info("Handling client at " + clientAddress);

            StringBuilder tagBuffer = new StringBuilder();

            int inputChar = 0;
            // read <hi>
            while (receivedChars.length() < HI.length() && ((inputChar = reader.read()) != -1)) {
                receivedChars.append((char) inputChar);
                tagBuffer.append((char) inputChar);
            }

            if (inputChar == -1 || !HI.equals(tagBuffer.toString())) {
                writer.print("closes");
                logger.warning("Client doesn't send <hi> start command. Closing connection!");
                return;
            }

            tagBuffer = new StringBuilder();
            StringBuilder reverseBuffer = new StringBuilder();
            boolean isFlip = false;
            while ((inputChar = reader.read()) != -1) {
                receivedChars.append((char) inputChar);

                char character = (char) inputChar;

                if (character == '<') { // start entity tag
                    if (tagBuffer.length() > 1) {
                        // tag name could not contain char '<'
                        break;
                    } else {
                        tagBuffer.append(character);
                    }
                } else if (character == '>' && tagBuffer.length() > 0) { // end entity tag
                    tagBuffer.append(character);
                    String tag = tagBuffer.toString();
                    tagBuffer = new StringBuilder();

                    int index = ALLOWED_ENTITIES.indexOf(tag);
                    if (index == 2 && !isFlip) {
                        // flip start
                        isFlip = true;
                    } else if (index == 3 && isFlip) {
                        // flip end
                        writer.write(reverseBuffer.reverse().toString());
                        reverseBuffer = new StringBuilder();
                        isFlip = false;
                    } else {
                        // not found HI, FIN, incorrect tag
                        break;
                    }
                } else {
                    // reading tag
                    if (tagBuffer.length() > 0) {
                        tagBuffer.append(character);
                    } else if (isFlip) {
                        reverseBuffer.append(character);
                    } else {
                        writer.write(character);
                    }
                }
            }

            writer.print("closes");
            writer.flush();
            // Close the socket.  We are done with this client!
            logger.info("Closing client at " + clientAddress);
            logger.info("Received message from client: " + receivedChars.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.flush();
        writer.close();
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
