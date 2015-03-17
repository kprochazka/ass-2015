package ass.prochka6;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Simple object repository. Allowing multiple parallel reads and only single write operation at a time.
 *
 * @author Kamil Prochazka
 */
public class Repository<T> {

    private static final Logger LOG = Logger.getLogger(Repository.class.getName());

    private T value;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public Repository(T value) {
        this.value = value;
    }

    public T readValue() {
        readLock.lock();
        try {
            LOG.info("Reading value: " + value);
            return value;
        } finally {
            readLock.unlock();
        }
    }

    public void storeValue(T value) {
        writeLock.lock();
        try {
            try {
                // processing storing value (DB, etc, ...)
                Thread.sleep(TimeUnit.MILLISECONDS.toMillis(10));
            } catch (InterruptedException e) {
                // ignore
            }

            LOG.info("Storing value: " + value + ", oldValue: " + this.value);
            this.value = value;
        } finally {
            writeLock.unlock();
        }
    }


}
