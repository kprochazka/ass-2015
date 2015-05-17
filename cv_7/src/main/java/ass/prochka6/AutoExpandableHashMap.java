package ass.prochka6;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Kamil Prochazka
 */
public class AutoExpandableHashMap extends HashMap<String, Object> implements Map<String, Object> {

    private boolean expand = true;

    @Override
    public Object get(Object key) {
        Object value = super.get(key);
        if (expand && value == null && key != null) {
            put(key.toString(), new AutoExpandableHashMap());
        }

        return super.get(key);
    }

    public void disableExpand() {
        this.expand = false;
    }

    public void enableExpand() {
        this.expand = true;
    }

}
