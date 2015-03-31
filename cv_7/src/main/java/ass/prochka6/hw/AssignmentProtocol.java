package ass.prochka6.hw;

import com.google.gson.Gson;

import de.odysseus.el.util.SimpleContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

/**
 * Lesson 7 - HW
 *
 * @author Kamil Prochazka
 */
public class AssignmentProtocol implements Runnable, Closeable {

    static final Charset DEFAULT_CHARSET = Charset.forName("utf-8");

    static final CloseableHttpClient HTTP_CLIENT = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
    static final String
        YANDEX_URL =
        "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20150324T000012Z.0d3623b79d11051f.f0a83426b1bea436890f37794c2c542ff7cf393d";

    static final ExpressionFactory EXPRESSION_FACTORY = ExpressionFactory.newInstance();

    // protocol keyword
    static final String TRANSLATE_MATH = "<TRANSLATE-MATH>";
    static final String GREETINGS = "<GREETINGS>";
    static final String EN_CS = "<EN-CS>";
    static final String TRANSLATION = "<TRANSLATION>";
    static final String CS_EN = "<CS-EN>";
    static final String MATH = "<MATH>";
    static final String RESULT = "<RESULT>";
    static final String BYE = "<BYE>";
    static final String FAIL = "<FAIL>";
    //

    private final Logger logger;
    private final Socket clientSocket;

    private BufferedReader reader;
    private PrintWriter writer;

    public AssignmentProtocol(Socket clientSocket, Logger logger) {
        this.logger = logger;
        this.clientSocket = clientSocket;

        try {
            this.clientSocket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(30L));
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), DEFAULT_CHARSET));
            writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), DEFAULT_CHARSET), true);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception occurred during stream initialization.", e);
        }
    }

    @Override
    public void run() {
        try {
            SocketAddress clientAddress = clientSocket.getRemoteSocketAddress();
            logger.info("Handling client at " + clientAddress);

            // clear client screen
//            writer.println();

            String line = reader.readLine();
            if (line == null || !line.startsWith(TRANSLATE_MATH)) {
                logger.warning("Client doesn't send first required protocol message with starting tag " + TRANSLATE_MATH);
                writer.println(FAIL);
                return;
            } else {
                writer.println(GREETINGS);
            }

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(EN_CS)) {
                    String text = line.replace(EN_CS, "").trim();
                    writer.println(TRANSLATION + " " + translate(true, text));
                } else if (line.startsWith(CS_EN)) {
                    String text = line.replace(CS_EN, "").trim();
                    writer.println(TRANSLATION + " " + translate(false, text));
                } else if (line.startsWith(MATH)) {
                    String input = line.replace(MATH, "").trim();
                    try {
                        SimpleContext context = new SimpleContext();
                        ValueExpression expression = EXPRESSION_FACTORY.createValueExpression(context, "${" + input + "}", Number.class);
                        Number result = (Number) expression.getValue(context);
                        writer.println(RESULT + " " + result);
                    } catch (ClassCastException | ELException e) {
                        logger.warning("Incorrect <MATH> input: " + input);
                        writer.println(FAIL);
                        break;
                    }
                } else if (line.startsWith(BYE)) {
                    logger.info("Ending communication.");
                    break;
                } else {
                    logger.warning("Unknown command: " + line);
                    writer.println(FAIL);
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    private String translate(boolean english, String text) {
        try {
            URIBuilder uriBuilder = new URIBuilder(YANDEX_URL);
            if (english) {
                uriBuilder.addParameter("lang", "en-cs");
            } else {
                uriBuilder.addParameter("lang", "cs-en");
            }
            uriBuilder.addParameter("text", text);

            HttpResponse response = HTTP_CLIENT.execute(new HttpGet(uriBuilder.build()));
            Map<String, Object> parsedResponseMap = new Gson().fromJson(new InputStreamReader(response.getEntity().getContent(), DEFAULT_CHARSET), Map.class);
            List<String> translations = (List<String>) parsedResponseMap.get("text");
            return translations.isEmpty() ? "" : translations.get(0);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void close() {
        // Close the socket.  We are done with this client!
        logger.info("Closing client at " + clientSocket.getRemoteSocketAddress());

        writer.print("");
        writer.flush();
        writer.close();

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
