package ass.pools.prochka6;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;

/**
 *
 * @author Kamil Prochazka
 */
public class QueueSimpleObjectPool<T extends CloneableObject<T>> implements ObjectPool<T>, SimpleObjectPool<T> {

    private final ReferenceQueue<T> referenceQueue = new ReferenceQueue<>();

    private Thread referenceCollector;

    private final Lock lock = new ReentrantLock();
    private final Condition emptyQueueCondition = lock.newCondition();
    // Radeji bych rovnou pouzil LinkedBlockingQueue ...
    private final Queue<T> internalQueue = new LinkedList<>();

    public QueueSimpleObjectPool() {
        startReferenceCollector();
    }

    @Nonnull
    @Override
    public T borrowObject() throws InterruptedException {
        T borrowed = poll();

        // add PhantomReference to returned object and attach ReferenceQueue
        new PhantomReference<>(borrowed, referenceQueue);

        // returns borrowed instance
        return borrowed;
    }

    @Override
    public T poll() throws InterruptedException {
        lock.lock();
        try {
            while (internalQueue.isEmpty()) {
                emptyQueueCondition.await();
            }

            return internalQueue.poll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void offer(T object) {
        if (object == null) {
            throw new IllegalArgumentException("Offered object must not be NULL!");
        }

        lock.lock();
        try {
            internalQueue.offer(object);
            emptyQueueCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void startReferenceCollector() {
        referenceCollector = new Thread() {
            @Override
            public void run() {
                try {
                    PhantomReference<? extends T> removedReference = null;
                    while ((removedReference = (PhantomReference<? extends T>) referenceQueue.remove()) != null) {
                        Field referentField = Reference.class.getDeclaredField("referent");
                        referentField.setAccessible(true);
                        T referent = (T) referentField.get(removedReference);
                        offer(referent);
                        System.out.println("Reclaimed: " + referent);
                    }
                } catch (InterruptedException | IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                    return;
                }
            }
        };
        referenceCollector.setDaemon(true);
        referenceCollector.start();
    }

}
