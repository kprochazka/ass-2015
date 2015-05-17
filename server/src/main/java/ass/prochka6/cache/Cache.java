package ass.prochka6.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Cache.
 *
 * @author Kamil Prochazka
 */
public class Cache {

    private static final Logger LOG = LoggerFactory.getLogger(Cache.class);

    final BlockingQueue<EvictionCandidate> evictionQueue = new PriorityBlockingQueue<>();
    private final ConcurrentMap<Object, Element> store = new ConcurrentHashMap<>();

    private final int timeToIdle;
    private final int timeToLive;

    private final long maxElements;
    private final long maxSize;

    private long elementsSize = 0;
    private final Object elementsSizeLock = new Object();

    public Cache(int timeToLive, int timeToIdle) {
        this(timeToLive, timeToIdle, Long.MAX_VALUE, Long.MAX_VALUE);
    }

    public Cache(int timeToLive, int timeToIdle, long maxElements, long maxSize) {
        this.timeToLive = timeToLive;
        this.timeToIdle = timeToIdle;
        this.maxElements = maxElements;
        this.maxSize = maxSize;
    }

    public Element get(Object key) {
        Element element = getInternal(key);

        element.updateAccessStatistics();
        evictionQueue.offer(new EvictionCandidate(element.getKey(), element.getExpirationTime()));

        return element;
    }

    protected Element getInternal(Object key) {
        Element element = store.get(key);

        if (element == null) {
            return null;
        }

        if (element.isExpired()) {
            remove(element.getKey());
            return null;
        }

        return element;
    }

    public void put(Element element) {
        if (element == null || element.isExpired()) {
            return;
        }

        if (element.getKey() == null || element.getValue() == null) {
            LOG.info("Element ({}) ignored.", element);
            return;
        }

        if (maxElements != Long.MAX_VALUE && store.size() >= maxElements) {
            LOG.info("Cache is full. Element is ignored.");
            return;
        }

        if (!checkSize(element)) {
            return;
        }

        element.resetAccessStatistics();
        applyDefaultsToElementWithoutLifespanSet(element);
        evictionQueue.offer(new EvictionCandidate(element.getKey(), element.getExpirationTime()));

        store.put(element.getKey(), element);
    }

    public void putIfAbsent(Element element) {
        if (element == null || element.isExpired()) {
            return;
        }

        if (element.getKey() == null || element.getValue() == null) {
            LOG.info("Element ({}) ignored.", element);
            return;
        }

        if (maxElements != Long.MAX_VALUE && store.size() >= maxElements) {
            LOG.info("Cache is full. Element is ignored.");
            return;
        }

        if (!checkSize(element)) {
            return;
        }

        element.resetAccessStatistics();
        applyDefaultsToElementWithoutLifespanSet(element);
        evictionQueue.offer(new EvictionCandidate(element.getKey(), element.getExpirationTime()));

        store.putIfAbsent(element.getKey(), element);
    }

    public boolean remove(Object key) {
        if (key == null) {
            return false;
        }

        Element element = store.remove(key);
        if (element != null) {
            if (maxSize != Long.MAX_VALUE) {
                synchronized (elementsSizeLock) {
                    elementsSize -= element.getSize();
                }
            }
        }

        return element != null;
    }

    public int size() {
        return store.size();
    }

    private void applyDefaultsToElementWithoutLifespanSet(Element element) {
        if (!element.isLifespanSet()) {
            element.setLifespanDefaults(timeToIdle, timeToLive);
        }
    }

    private boolean checkSize(Element element) {
        if (maxSize != Long.MAX_VALUE) {
            synchronized (elementsSizeLock) {
                if (elementsSize + element.getSize() > maxSize) {
                    return false;
                }

                elementsSize += element.getSize();
            }
        }
        return true;
    }

    static class EvictionCandidate implements Comparable<EvictionCandidate> {
        final Object key;
        final long expirationTime;

        public EvictionCandidate(Object key, long expirationTime) {
            this.key = key;
            this.expirationTime = expirationTime;
        }

        @Override
        public int compareTo(EvictionCandidate o) {
            if (expirationTime > o.expirationTime) {
                return 1;
            }
            if (expirationTime < o.expirationTime) {
                return -1;
            }
            return 0;
        }
    }
}
