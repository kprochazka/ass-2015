package ass.prochka6;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link HttpRequest}.
 *
 * @author Kamil Prochazka
 */
public class HttpRequestTest extends TestBase {

    @Test
    public void testParseRequest() throws IOException {
        // test data preparation
        HttpRequest request = new HttpRequest(null, new ByteArrayInputStream(validHeader()));

        // execute the tested method
        request.parse();

        // result and execution verifications
        assertEquals(Method.GET, request.getMethod());
        assertEquals("/javase/7/docs/api/javax/activation/MimetypesFileTypeMap.html", request.getUri());
        assertEquals("test=1&test=2", request.getQueryParameterString());
        assertEquals(11, request.getHeaders().size());
        assertEquals("2", request.getParam("test"));
        assertEquals(2, request.getParams().get("test").size());
        assertEquals(5, request.getCookies().size());
    }

    @Test
    public void testParseRequestSeznam() throws IOException {
        // test data preparation
        HttpRequest request = new HttpRequest(null, new ByteArrayInputStream(validHeaderSeznam()));

        // execute the tested method
        request.parse();

        // result and execution verifications
        assertEquals(Method.GET, request.getMethod());
        assertEquals("/", request.getUri());
        assertEquals(7, request.getHeaders().size());
    }

    @Test(expected = InvalidRequestException.class)
    public void testInvalidRequest() throws IOException {
        // test data preparation
        HttpRequest request = new HttpRequest(null, new ByteArrayInputStream(invalidHeader()));

        // execute the tested method
        request.parse();
    }

    private byte[] validHeader() throws UnsupportedEncodingException {
        String headerText = "GET /javase/7/docs/api/javax/activation/MimetypesFileTypeMap.html?test=1&test=2 HTTP/1.1\n"
                            + "Host: docs.oracle.com\n"
                            + "Connection: keep-alive\n"
                            + "Cache-Control: max-age=0\n"
                            + "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\n"
                            + "User-Agent: Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2405.0 Safari/537.36\n"
                            + "Referer: https://www.google.cz/\n"
                            + "Accept-Encoding: gzip, deflate, sdch\n"
                            + "Accept-Language: en,cs;q=0.8\n"
                            + "Cookie: s_cc=true; s_sq=%5B%5BB%5D%5D; s_nr=1432016987653; gpv_p24=http%3A//docs.oracle.com/javase/7/docs/api/javax/activation/MimetypesFileTypeMap.html; gpw_e24=http%3A//docs.oracle.com/javase/7/docs/api/javax/activation/MimetypesFileTypeMap.html\n"
                            + "If-None-Match: \"a80e0bc65ab00912b4860e9aeba2ac0b:1413991911\"\n"
                            + "If-Modified-Since: Sat, 04 Oct 2014 01:04:43 GMT\r\n"
                            + "\r\n";
        return headerText.getBytes("utf-8");
    }

    private byte[] validHeaderSeznam() throws UnsupportedEncodingException {
        String headerText = "GET / HTTP/1.1\n"
                            + "Host: www.seznam.cz\n"
                            + "Connection: keep-alive\n"
                            + "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\n"
                            + "User-Agent: Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2405.0 Safari/537.36\n"
                            + "Accept-Encoding: gzip, deflate, sdch\n"
                            + "Accept-Language: en,cs;q=0.8\n"
                            + "If-Modified-Since: Sat, 04 Oct 2014 01:04:43 GMT\r\n"
                            + "\r\n";
        return headerText.getBytes("utf-8");
    }

    private byte[] invalidHeader() throws UnsupportedEncodingException {
        String headerText = "/javase/7/docs/api/javax/activation/MimetypesFileTypeMap.html?test=1&test=2 \n";
        return headerText.getBytes("utf-8");
    }

}
