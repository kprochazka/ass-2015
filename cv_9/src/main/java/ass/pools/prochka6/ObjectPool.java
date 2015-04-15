package ass.pools.prochka6;


import javax.annotation.Nonnull;

public interface ObjectPool<T extends CloneableObject<T>> {

    @Nonnull
    T borrowObject() throws InterruptedException;

}
