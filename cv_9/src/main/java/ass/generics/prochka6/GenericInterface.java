package ass.generics.prochka6;

import java.util.Map;
import java.util.Set;

public interface GenericInterface<K, V extends Comparable<V>> {

    void foo(K key, V value);

    Map<K, Set<V>> getStuff();

}
