package ass.prochka6;

import java.util.List;

/**
 * Chain of {@link RequestHandler RequestHandlers} responsible for response to client.
 *
 * @author Kamil Prochazka
 */
public class RequestHandlerChainImpl implements RequestHandlerChain {

    private int invokedHandler = 0;

    private final List<RequestHandler> requestHandlers;

    public RequestHandlerChainImpl(List<RequestHandler> requestHandlers) {
        if (requestHandlers == null || requestHandlers.isEmpty()) {
            throw new IllegalArgumentException("Request Handlers could not be empty list!");
        }
        this.requestHandlers = requestHandlers;
    }

    @Override
    public List<RequestHandler> getRequestHandlers() {
        return requestHandlers;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws Exception {
        if (invokedHandler < requestHandlers.size()) {
            RequestHandler requestHandler = requestHandlers.get(invokedHandler++);
            requestHandler.handle(request, response, this);
        }
    }

}
