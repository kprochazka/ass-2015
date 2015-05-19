package ass.prochka6;

/**
 * Request handler is responsible for handling HttpRequest by generating HttpResponse or pass it to another
 * handler in chain.
 *
 * @author Kamil Prochazka
 */
interface RequestHandler {

    void handle(HttpRequest request, HttpResponse response, RequestHandlerChain chain) throws Exception;

}
