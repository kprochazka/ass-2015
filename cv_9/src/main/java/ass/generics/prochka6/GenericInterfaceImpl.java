package ass.generics.prochka6;

import java.util.Map;
import java.util.Set;

/**
 * Generic interface impl.
 *
 * @author Kamil Prochazka
 */
public class GenericInterfaceImpl<K, V extends Comparable<V>> implements GenericInterface<K, V> {

    public <Z> Z find(K key) {
        return null;
    }

    @Override
    public void foo(K key, V value) {

    }

    @Override
    public Map<K, Set<V>> getStuff() {
        return null;
    }
}
