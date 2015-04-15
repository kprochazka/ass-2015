package ass.pools.prochka6;

import javax.annotation.Nonnull;

public interface CloneableObject<T> extends Cloneable {

    @Nonnull
    T clone();

}
