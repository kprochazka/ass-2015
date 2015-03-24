package ass.prochka6;

import org.junit.AfterClass;
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

    private static final int PORT = 8585;

    @BeforeClass
    public static void beforeClass() throws IOException {
        TCPEchoServerThreadPool.main(new String[]{"8585", "4"});
    }

    @AfterClass
    public static void afterClass() {
        TCPEchoServerThreadPool.close();
    }

    @Test
    public void happyDay() {
        final String word = "<hi>Kamile, tvoje jmeno pozpatku je:<flip>kamil</flip>. Co na to rikas, huste co?<fin>";
        String response = getResponse(word);
        assertEquals("Kamile, tvoje jmeno pozpatku je:limak. Co na to rikas, huste co?closes", response);
    }

    @Test
    public void noInput() {
        final String word = "<hi><fin>";
        String response = getResponse(word);
        assertEquals("closes", response);
    }

    @Test
    public void immediateFlip() {
        final String word = "<hi><flip>abc</flip> <fin>";
        String response = getResponse(word);
        assertEquals("cba closes", response);
    }

    @Test
    public void incorrectTagPair() {
        final String word = "<hi>Test </flip>abc<flip><fin>";
        String response = getResponse(word);
        assertEquals("Test closes", response);
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
        assertEquals("Test unknown tag closes", response);
    }

    @Test
    public void unknownTag2() {
        final String word = "<hi>Test 3 < 1 > 5 = 8 end tag <fin>";
        String response = getResponse(word);
        assertEquals("Test 3 closes", response);
    }

    @Test
    public void ignoredCharsAfterFinTag() {
        final String word = "<hi>Test end tag <fin> uff";
        String response = getResponse(word);
        assertEquals("Test end tag closes", response);
    }

    @Test
    public void allowedGtTag() {
        final String word = "<hi>Test > end tag <fin>";
        String response = getResponse(word);
        assertEquals("Test > end tag closes", response);
    }

    private String getResponse(String word) {
        TCPEchoClientExtended
            localhost =
            new TCPEchoClientExtended("localhost", word, 8585);
        localhost.run();
        String response = localhost.getResponse();
        return response;
    }

}
