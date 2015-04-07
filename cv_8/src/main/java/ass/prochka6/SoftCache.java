package ass.prochka6;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Kamil Prochazka
 */
public class SoftCache<K, V> implements Cache<K, V> {

    private final Map<K, SoftReference<V>> internalMap = new ConcurrentHashMap<>();

    private final Integer hardCacheSize;
    private final Object lock = new Object();
    private final LinkedList<V> hardCache = new LinkedList<>();

    private final ReferenceQueue<SoftReference<V>> referenceQueue = new ReferenceQueue<>();

    public SoftCache() {
        this(null);
    }

    public SoftCache(Integer hardSize) {
        this.hardCacheSize = hardSize;
    }

    @Nullable
    @Override
    public V get(K key) {
        V result = null;

        SoftReference<V> softRef = internalMap.get(key);
        if (softRef != null) {
            result = softRef.get();
            if (result == null) {
                internalMap.remove(key);
            } else if (hardCacheSize != null) {
                synchronized (lock) {
                    hardCache.addFirst(result);
                    if (hardCache.size() > hardCacheSize) {
                        hardCache.removeLast();
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void put(@Nonnull K key, @Nonnull V value) {
        processQueue();
        internalMap.put(key, new SoftHolder<K, V>(key, value, referenceQueue));
    }

    public void clear() {
        if (hardCacheSize != null) {
            synchronized (lock) {
                hardCache.clear();
            }
        }
        processQueue();
        internalMap.clear();
    }

    public int size() {
        processQueue();
        return internalMap.size();
    }

    private void processQueue() {
        SoftHolder holder;
        while ((holder = (SoftHolder) referenceQueue.poll()) != null) {
            internalMap.remove(holder.key);
        }
    }

    private static class SoftHolder<K, V> extends SoftReference<V> {

        private final K key;

        public SoftHolder(K key, V referent, ReferenceQueue queue) {
            super(referent, queue);
            this.key = key;
        }

    }

}
