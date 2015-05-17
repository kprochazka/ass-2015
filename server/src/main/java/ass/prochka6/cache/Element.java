package ass.prochka6.cache;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cached element.
 *
 * @author Kamil Prochazka
 */
public class Element implements Serializable {

    private final Object key;

    private final Object value;

    private AtomicLong hitCount = new AtomicLong(0);

    // Time to live in seconds. 0s means unlimited
    /**
     * Time the Element live in cache.
     */
    private volatile int timeToLive = Integer.MIN_VALUE;

    /**
     * Time the Element idle in Cache in seconds. 0 means unlimited.
     */
    private volatile int timeToIdle = Integer.MIN_VALUE;

    /**
     * The creation time.
     */
    private transient long creationTime = System.currentTimeMillis();

    /**
     * The last access time.
     */
    private transient long lastAccessTime;

    /**
     * The size in (bytes) of this element)
     */
    private final long size;

    public Element( Object key,  Object value) {
        this.key = key;
        this.value = value;
        this.size = 0;
    }

    public Element(final Object key, final Object value, final int size) {
        this.key = key;
        this.value = value;
        this.size = size;
    }

    public Element(final Object key, final Object value, final int size,
                   int timeToLive, int timeToIdle) {
        this(key, value, size);
        this.timeToLive = timeToLive;
        this.timeToIdle = timeToIdle;
    }

    public Object getKey() {
        return this.key;
    }

    public Object getValue() {
        return this.value;
    }

    public long getSize() {
        return size;
    }

    public void setTimeToLive(int timeToLiveSeconds) {
        if (timeToLiveSeconds < 0) {
            throw new IllegalArgumentException("Time to live must be >= 0!");
        }
        this.timeToLive = timeToLiveSeconds;
    }

    public void setTimeToIdle(int timeToIdleSeconds) {
        if (timeToIdleSeconds < 0) {
            throw new IllegalArgumentException("Time to idle must be >= 0!");
        }
        this.timeToIdle = timeToIdleSeconds;
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public long getHitCount() {
        return hitCount.get();
    }

    /**
     * Sets the last access time to now and increase the hit count.
     */
    public final void updateAccessStatistics() {
        lastAccessTime = System.currentTimeMillis();
        hitCount.incrementAndGet();
    }


    /**
     * Resets the hit count to 0 and the last access time to now. Used when an Element is put into a cache.
     */
    public final void resetAccessStatistics() {
        lastAccessTime = System.currentTimeMillis();
        hitCount.set(0);
    }

    /**
     * An element is expired if the expiration time as given by {@link #getExpirationTime()} is in the past.
     *
     * @return true if the Element is expired, otherwise false. If no lifespan has been set for the Element it is
     *         considered not able to expire.
     * @see #getExpirationTime()
     */
    public boolean isExpired() {
        if (!isLifespanSet()) {
            return false;
        }

        long now = System.currentTimeMillis();
        long expirationTime = getExpirationTime();

        return now > expirationTime;
    }

    /**
     * Returns the expiration time based on time to live. If this element also has a time to idle setting, the expiry
     * time will vary depending on whether the element is accessed.
     *
     * @return the time to expiration
     */
    public long getExpirationTime() {
        if (!isLifespanSet()) {
            return Long.MAX_VALUE;
        }

        long expirationTime = 0;
        long ttlExpiry = creationTime + toMillis(getTimeToLive());

        long mostRecentTime = Math.max(creationTime, lastAccessTime);
        long ttiExpiry = mostRecentTime + toMillis(getTimeToIdle());

        if (getTimeToLive() != 0 && (getTimeToIdle() == 0 || lastAccessTime == 0)) {
            expirationTime = ttlExpiry;
        } else if (getTimeToLive() == 0) {
            expirationTime = ttiExpiry;
        } else {
            expirationTime = Math.min(ttlExpiry, ttiExpiry);
        }
        return expirationTime;
    }

    /**
     * Whether any combination of TTL or TTI has been set.
     *
     * @return true if set.
     */
    public boolean isLifespanSet() {
        return this.timeToIdle != Integer.MIN_VALUE || this.timeToLive != Integer.MIN_VALUE;
    }

    /**
     * @return the time to live, in seconds
     */
    public int getTimeToLive() {
        if (Integer.MIN_VALUE == timeToLive) {
            return 0;
        } else {
            return timeToLive;
        }
    }

    /**
     * @return the time to idle, in seconds
     */
    public int getTimeToIdle() {
        if (Integer.MIN_VALUE == timeToIdle) {
            return 0;
        } else {
            return timeToIdle;
        }
    }

    /**
     * Set the default parameters of this element - those from its enclosing cache.
     *
     * @param tti TTI in seconds
     * @param ttl TTL in seconds
     */
    protected void setLifespanDefaults(int tti, int ttl) {
        timeToIdle = tti;
        timeToLive = ttl;
    }

    private long toMillis(int seconds) {
        return 1000 * seconds;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Element{");
        sb.append("key=").append(key);
        sb.append(", value=").append(value);
        sb.append(", hitCount=").append(hitCount);
        sb.append(", timeToLive=").append(timeToLive);
        sb.append(", timeToIdle=").append(timeToIdle);
        sb.append(", creationTime=").append(creationTime);
        sb.append(", lastAccessTime=").append(lastAccessTime);
        sb.append('}');
        return sb.toString();
    }
}
