package ass.prochka6.cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertTrue;

/**
 * Test for {@link Cache}.
 *
 * @author Kamil Prochazka
 */
public class CacheTest {

    private CacheManager cacheManager;

    @Before
    public void setUp() {
        cacheManager = new CacheManager();
    }

    @After
    public void cleanup() {
        cacheManager.shutdown();
    }

    @Test
    public void testCache() throws InterruptedException {
        // test data preparation
        Cache cache = new Cache(1, 10, 10, 50);
        cacheManager.register(cache, 1);

        // execute the tested method
        for (int i = 0; i < 20; i++) {
            Element element = new Element(i, i, ((int) Math.random() * 10));
            cache.put(element);
        }

        // result and execution verifications
        assertTrue(cache.size() == 10);

        // sleep to let cache clean after 1s
        Thread.sleep(TimeUnit.SECONDS.toMillis(3));

        assertTrue(cache.size() == 0);
    }

    @Test
    public void testPutIfAbsent() throws InterruptedException {
        // test data preparation
        Cache cache = new Cache(1, 2);

        Element firstElement = new Element(1, 1);
        Element secondElement = new Element(1, 1);

        // execute the tested method
        cache.putIfAbsent(firstElement);
        long inserted = System.currentTimeMillis();

        // sleep
        Thread.sleep(100);

        cache.putIfAbsent(secondElement);

        // result and execution verifications
        assertTrue(firstElement.getLastAccessTime() <= inserted);
    }

}
