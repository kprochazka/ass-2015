package ass.prochka6;

import javax.annotation.Nonnull;

/**
 * @author Kamil Prochazka
 */
public class AdvancedObjectPool<T extends CloneableObject<T>> implements ObjectPool<T> {

    @Nonnull
    @Override
    public T borrowObject() {
        return null;
    }

}
