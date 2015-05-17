package ass.prochka6.cache2;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ass.prochka6.util.Validate;

/**
 *
 * TODO upper bound
 *
 * @author Kamil Prochazka
 */
public class SoftCache<K, V> implements Cache2<K, V> {

    private final ReferenceQueue referenceQueue = new ReferenceQueue<>();
    private final Map<K, SoftReference<V>> internalCacheMap = new ConcurrentHashMap<>();

    public SoftCache() {
        Thread cleanerThread = new Thread(new SoftCacheReferenceCleaner());
        cleanerThread.setDaemon(true);
        cleanerThread.setName(getClass().getSimpleName() + ":" + SoftCacheReferenceCleaner.class.getSimpleName());
        cleanerThread.start();
    }

    @Nullable
    @Override
    public V get(K key) {
        V result = null;

        SoftReference<V> softRef = internalCacheMap.get(key);
        if (softRef != null) {
            result = softRef.get();
            if (result == null) {
                internalCacheMap.remove(key);
            }
        }

        return result;
    }

    @Override
    public void put(@Nonnull K key, @Nonnull V value) {
        Validate.notNull(key, "Key must not be NULL!");
        Validate.notNull(value, "Value must not be NULL!");

        internalCacheMap.put(key, new SoftReferenceHolder<K, V>(key, value, referenceQueue));
    }

    public void clear() {
        internalCacheMap.clear();
    }

    public int size() {
        return internalCacheMap.size();
    }

    private class SoftReferenceHolder<K, V> extends SoftReference<V> {
        final K key;

        public SoftReferenceHolder(K key, V referent, ReferenceQueue queue) {
            super(referent, queue);
            this.key = key;
        }
    }

    private class SoftCacheReferenceCleaner implements Runnable {
        @Override
        public void run() {
            try {
                SoftReferenceHolder<K, V> holder = null;
                while ((holder = (SoftReferenceHolder<K, V>) referenceQueue.remove()) != null) {
                    internalCacheMap.remove(holder.key);
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }

}
