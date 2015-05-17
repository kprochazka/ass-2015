package ass.prochka6.cache2;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Kamil Prochazka
 */
public interface Cache2<K, V> {

    @Nullable
    V get(K key);

    void put(@Nonnull K key, @Nonnull V value);

}
