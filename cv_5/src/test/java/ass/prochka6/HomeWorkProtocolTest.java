package ass.prochka6;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link ass.prochka6.HomeWorkProtocol}.
 *
 * This test suite first start server {@linkplain ass.prochka6.TCPEchoServerThreadPool} and run tests.
 *
 * @author Kamil Prochazka
 */
public class HomeWorkProtocolTest {

    private static final int PORT = 8080;

    @BeforeClass
    public static void beforeClass() throws IOException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    TCPEchoServerThreadPool.main(new String[]{"" + PORT, "50"});
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        thread.setDaemon(true);
        thread.start();

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void happyDay() {
        final String word = "<hi>Kamile, tvoje jmeno pozpatku je:<flip>kamil</flip>. Co na to rikas, huste co?<fin>";
        String response = getResponse(word);
        assertEquals("Kamile, tvoje jmeno pozpatku je:limak. Co na to rikas, huste co?", response);
    }

    @Test
    public void noInput() {
        final String word = "<hi><fin>";
        String response = getResponse(word);
        assertEquals("", response);
    }

    @Test
    public void immediateFlip() {
        final String word = "<hi><flip>abc</flip> <fin>";
        String response = getResponse(word);
        assertEquals("cba ", response);
    }

    @Test
    public void incorrectTagPair() {
        final String word = "<hi>Test </flip>abc<flip><fin>";
        String response = getResponse(word);
        assertEquals("Test ", response);
    }

    @Test
    public void incorrectStartTag() {
        final String word = "Test message<fin>";
        String response = getResponse(word);
        assertEquals("", response);
    }

    @Test
    public void unknownTag() {
        final String word = "<hi>Test unknown tag <marta> ted.<fin>";
        String response = getResponse(word);
        assertEquals("Test unknown tag ", response);
    }

    @Test
    public void unknownTag2() {
        final String word = "<hi>Test 3 < 1 > 5 = 8 end tag <fin>";
        String response = getResponse(word);
        assertEquals("Test 3 ", response);
    }

    @Test
    public void ignoredCharsAfterFinTag() {
        final String word = "<hi>Test end tag <fin> uff";
        String response = getResponse(word);
        assertEquals("Test end tag ", response);
    }

    @Test
    public void allowedGtTag() {
        final String word = "<hi>Test > end tag <fin>";
        String response = getResponse(word);
        assertEquals("Test > end tag ", response);
    }

    @Test
    public void test1() {
        final String word = "<hi>hello <flip>Ahoj</flip><fin>";
        String response = getResponse(word);
        assertEquals("hello johA", response);
    }

    @Test
    public void test2() {
        final String word = "hi";
        String response = getResponse(word);
        assertEquals("", response);
    }

    @Test
    public void test3() {
        final String word = "<hi>hello<frip>";
        String response = getResponse(word);
        assertEquals("hello", response);
    }

    @Test
    public void test4() {
        final String word = "<hi>hello<<<<flip>:-)</flip><fin>";
        String response = getResponse(word);
        assertEquals("hello<<<)-:", response);
    }

    @Test
    public void test6() {
        final String word = "<hi><flip><flip>";
        String response = getResponse(word);
        assertEquals("", response);
    }

    @Test
    public void test5() {
        final String word = "";
        String response = getResponse(word);
        assertEquals("", response);
    }

    private String getResponse(String word) {
        TCPEchoClientExtended
            localhost =
            new TCPEchoClientExtended("localhost", word, PORT);
        localhost.run();
        String response = localhost.getResponse();
        return response;
    }

}
