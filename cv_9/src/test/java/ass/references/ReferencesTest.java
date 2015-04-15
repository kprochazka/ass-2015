package ass.references;

import org.junit.Test;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import static org.junit.Assert.*;

/**
 * User: jajusko
 * Date: 07/04/14
 * Time: 22:06
 */
@SuppressWarnings({"RedundantStringConstructorCall", "CallToSystemGC", "AssignmentToNull", "ReuseOfLocalVariable"})
public class ReferencesTest {

    @Test
    public void testWeakReference() {
        String strongReference = new String("Wow, much strong.");
        WeakReference<String> weakReference = new WeakReference<String>(strongReference);
        assertSame(strongReference, weakReference.get());
        System.gc();
        assertSame(strongReference, weakReference.get());
        strongReference = null;
        System.gc();
        assertNull(weakReference.get());
    }

    @Test
    public void testSoftReference() {
        String strongReference = new String("Wow, much strong. AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        SoftReference<String> softReference = new SoftReference<String>(strongReference);
        assertSame(strongReference, softReference.get());
        System.gc();
        assertSame(strongReference, softReference.get());
        strongReference = null;
        System.gc();
        assertNotNull(softReference.get());
        byte[] someData = new byte[88420000]; // !!! size depends on -Xmx settings
        System.gc();
        assertNull(softReference.get());
    }

    @Test
    public void testPhantomReference() throws InterruptedException {
        String strongReference = new String("Wow, much strong.");
        final ReferenceQueue<String> referenceQueue = new ReferenceQueue<String>();
        final PhantomReference<String> phantomReference = new PhantomReference<String>(strongReference, referenceQueue);
        final BooleanHolder same = new BooleanHolder();
        Thread referenceQueueChecker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    PhantomReference<String> polledPhantomReference = (PhantomReference<String>) referenceQueue.remove(60 * 1000);
                    same.setValue(polledPhantomReference == phantomReference);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        referenceQueueChecker.start();

        strongReference = null;
        System.gc();

        referenceQueueChecker.join();

        assertTrue(same.getValue());
    }

    private static class BooleanHolder {

        private  boolean value = false;

        public boolean getValue() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }
    }

}
