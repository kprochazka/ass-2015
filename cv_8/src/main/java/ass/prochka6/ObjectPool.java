package ass.prochka6;


import javax.annotation.Nonnull;

public interface ObjectPool<T extends CloneableObject<T>> {

    @Nonnull
    T borrowObject();

}
