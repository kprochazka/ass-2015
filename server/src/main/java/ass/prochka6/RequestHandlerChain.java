package ass.prochka6;

import java.util.List;

/**
 * RequestHandler chain responsible for handling HttpRequest.
 *
 * @author Kamil Prochazka
 */
public interface RequestHandlerChain {

    List<RequestHandler> getRequestHandlers();

    void handle(HttpRequest request, HttpResponse response) throws Exception;

}
