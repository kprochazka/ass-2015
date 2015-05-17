package ass.prochka6;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Map like Server wide context.
 *
 * @author Kamil Prochazka
 */
public class ServerContext {

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();


    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public Object setAttribute(String name, Object value) {
        return attributes.put(name, value);
    }

    public Object removeAttribute(String name) {
        return attributes.remove(name);
    }

}
