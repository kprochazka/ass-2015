package ass.prochka6.hw;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

/**
 * @author Kamil Prochazka
 */
public class AssignmentProtocolTest {

    private static final Charset UTF8 = Charset.forName("utf-8");
    static Thread SERVER;

    @BeforeClass
    public static void beforeClass() {
        SERVER = new Thread(new TCPEchoServerExecutor());
        SERVER.setDaemon(true);
        SERVER.start();

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void afterClass() {
        SERVER.interrupt();
    }

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;


    @Before
    public void setUp() throws IOException {
        socket = new Socket("localhost", 8080);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF8));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), UTF8), true);
    }

    @After
    public void tearDown() {
        writer.close();
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAssignment() throws IOException {
        // Client: <TRANSLATE-MATH>
        writer.println(AssignmentProtocol.TRANSLATE_MATH);
        // Server: <GREETINGS>
        assertEquals(AssignmentProtocol.GREETINGS, reader.readLine());

        // Client: <EN-CS> Hello Mrs Frog
        writer.println(AssignmentProtocol.EN_CS + " Hello Mrs Frog");
        // Server: <TRANSLATION> Dobrý Den, Paní Žába
        assertEquals(AssignmentProtocol.TRANSLATION + " Dobrý Den, Paní Žába", reader.readLine());

        // Client: <CS-EN> Kladivo spadlo na žábu
        writer.println(AssignmentProtocol.CS_EN + " Kladivo spadlo na žábu");
        // Server: <TRANSLATION> The hammer fell on a frog
        assertEquals(AssignmentProtocol.TRANSLATION + " The hammer fell on a frog", reader.readLine());

        // Client: <MATH> 3 + 3
        writer.println(AssignmentProtocol.MATH + " 3 + 3");
        // Server: <RESULT> 6
        assertEquals(AssignmentProtocol.RESULT + " 6", reader.readLine());

        // Client: <BYE>
        writer.println(AssignmentProtocol.BYE);
    }

    @Test
    public void testIncorrectProtocolInitiation() throws IOException {
        writer.println("Ahoj" + AssignmentProtocol.TRANSLATE_MATH);
        assertEquals(AssignmentProtocol.FAIL, reader.readLine());
    }

    @Test
    public void testIncorrectProtocolInitiation2() throws IOException {
        writer.println("Kamil");
        assertEquals(AssignmentProtocol.FAIL, reader.readLine());
    }

    @Test
    public void testIncorrectMathExpression() throws IOException {
        writer.println(AssignmentProtocol.MATH + " Kamil");
        assertEquals(AssignmentProtocol.FAIL, reader.readLine());
    }

    @Test
    public void testIncorrectMathExpression2() throws IOException {
        writer.println(AssignmentProtocol.MATH + " (5 * (6+3))   )");
        assertEquals(AssignmentProtocol.FAIL, reader.readLine());
    }

    @Test
    public void testUnknownCommand() throws IOException {
        writer.println(AssignmentProtocol.TRANSLATE_MATH);
        assertEquals(AssignmentProtocol.GREETINGS, reader.readLine());
        writer.println("<COMMAND>");
        assertEquals(AssignmentProtocol.FAIL, reader.readLine());
    }

    @Test
    public void testUnknownCommand2() throws IOException {
        writer.println(AssignmentProtocol.TRANSLATE_MATH);
        assertEquals(AssignmentProtocol.GREETINGS, reader.readLine());
        writer.println(AssignmentProtocol.TRANSLATE_MATH);
        assertEquals(AssignmentProtocol.FAIL, reader.readLine());
    }

}
