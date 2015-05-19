package ass.prochka6.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for eviction of registered Caches.
 *
 * @author Kamil Prochazka
 */
public class CacheManager {

    private static final Logger LOG = LoggerFactory.getLogger(CacheManager.class);

    private final ScheduledExecutorService scheduledExecutorService;

    public CacheManager() {
        this(1);
    }

    public CacheManager(int threads) {
        if (threads < 1) {
            throw new IllegalArgumentException("Thread count > 0!");
        }

        scheduledExecutorService = Executors.newScheduledThreadPool(threads);
    }

    public void register(Cache cache, int evictionIntervalSeconds) {
        scheduledExecutorService.scheduleWithFixedDelay(new CacheEvictRunnable(cache), 0, evictionIntervalSeconds, TimeUnit.SECONDS);
    }

    public void shutdown() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
        }
    }

    private static class CacheEvictRunnable implements Runnable {
        private final Cache cache;

        private CacheEvictRunnable(Cache cache) {
            this.cache = cache;
        }

        @Override
        public void run() {
            LOG.debug("Eviction started!");

            Cache.EvictionCandidate candidate = null;
            while ((candidate = cache.evictionQueue.poll()) != null) {
                Element element = cache.getInternal(candidate.key);
                if (element != null) {
                    boolean offer = cache.evictionQueue.offer(new Cache.EvictionCandidate(element.getKey(), element.getExpirationTime()));
                    if (!offer) {
                        LOG.debug("Element ({}) was refused by evictionQueue!", element.getKey());
                    }
                    return;
                } else {
                    LOG.debug("Evicted Element ({})", candidate.key);
                }
            }
        }

    }

}
