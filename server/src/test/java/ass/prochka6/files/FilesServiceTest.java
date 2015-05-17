package ass.prochka6.files;

import org.junit.Test;

import java.lang.ref.SoftReference;

/**
 * Service for accessing Files.
 *
 * @author Kamil Prochazka
 * @see java.nio.file.Path
 */
public class FilesServiceTest {

    public void test() {
        SoftReference<Holder<byte[]>> ref = new SoftReference<Holder<byte[]>>(new Holder<>(new byte[]{(byte) 128, (byte) 156}));

        Holder<byte[]> holder = ref.get();
        holder.getValue();
    }

    @Test
    public void test2() {
        byte[] arr = new byte[]{(byte) 128, (byte) 512};
    }

    static class Holder<V> {

        V value;

        public Holder(V value) {
            this.value = value;
        }

        public V getValue() {
            return value;
        }
    }

}
