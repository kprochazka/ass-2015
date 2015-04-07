package ass.prochka6;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: Jan Jusko, jajusko@cisco.com
 * Date: 4/17/12
 * Time: 3:14 PM
 */
public interface Cache<K, V> {

    @Nullable
    V get(K key);

    void put(@Nonnull K key, @Nonnull V value);

}
