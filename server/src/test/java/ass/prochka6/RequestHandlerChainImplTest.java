package ass.prochka6;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mock;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Test for {@link RequestHandlerChainImpl}.
 *
 * @author Kamil Prochazka
 */
public class RequestHandlerChainImplTest extends TestBase {

    @Mock
    private RequestHandler requestHandlerMock;

    @After
    public void after() {
        verifyNoMoreInteractions(requestHandlerMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotNull() {
        // execute the tested method
        new RequestHandlerChainImpl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotEmpty() {
        // execute the tested method
        new RequestHandlerChainImpl(Collections.emptyList());
    }

    @Test
    public void test() throws Exception {
        // test data preparation
        HttpRequest request = new HttpRequest(null, null);
        HttpResponse response = new HttpResponse(new ByteArrayOutputStream(0));
        RequestHandlerChainImpl chain = new RequestHandlerChainImpl(Arrays.asList(requestHandlerMock));

        // execute the tested method
        chain.handle(request, response);

        // result and execution verifications
        verify(requestHandlerMock).handle(request, response, chain);
    }

}
