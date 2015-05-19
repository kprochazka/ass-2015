package ass.prochka6;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link HttpResponse}.
 *
 * @author Kamil Prochazka
 */
public class HttpResponseTest extends TestBase {

    private ByteArrayOutputStream outputStream;

    @Before
    public void setUp() {
        outputStream = new ByteArrayOutputStream(8 * 1000 * 1024);
    }

    @Test
    public void testEmpty() throws IOException {
        // test data preparation
        HttpResponse response = new HttpResponse(outputStream);
        response.setMimeType("text/plain");
        response.setStatus(Status.OK);
        response.addCookie(new Cookie("cookie", "value", "expiry"));

        // execute the tested method
        response.sendHeader();

        // result and execution verifications
        byte[] contentBytes = outputStream.toByteArray();
        String content = new String(contentBytes, "utf-8");
        String[] lines = content.split("\\r?\\n");
        System.out.println(content);

        assertEquals("HTTP/1.1 200 OK", lines[0].trim());
        assertEquals("Content-Type: text/plain", lines[1].trim());
        assertTrue(lines[2].trim().startsWith("Date:"));
        assertEquals("Set-Cookie: cookie=value; expires=expiry", lines[3].trim());
        assertEquals("Connection: close", lines[4].trim());
        assertEquals("Content-Length: 0", lines[5].trim());
    }

    @Test
    public void testWithData() throws IOException {
        // test data preparation
        HttpResponse response = new HttpResponse(outputStream);
        response.setMimeType("text/plain");
        response.setStatus(Status.OK);

        String text = "Hello World!";
        byte[] bytes = text.getBytes("utf-8");
        response.setContentLength(bytes.length);

        // execute the tested method
        response.write(bytes);

        // result and execution verifications
        byte[] contentBytes = outputStream.toByteArray();
        String content = new String(contentBytes, "utf-8");
        String[] lines = content.split("\\r?\\n");
        System.out.println(content);

        assertEquals("HTTP/1.1 200 OK", lines[0].trim());
        assertEquals("Content-Type: text/plain", lines[1].trim());
        assertTrue(lines[2].trim().startsWith("Date:"));
        assertEquals("Connection: close", lines[3].trim());
        assertEquals("Content-Length: 12", lines[4].trim());

        assertEquals("Hello World!", lines[6].trim());
    }

}
