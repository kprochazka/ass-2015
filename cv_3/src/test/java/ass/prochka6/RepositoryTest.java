package ass.prochka6;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Test for {@link ass.prochka6.Repository}.
 *
 * @author Kamil Prochazka
 */
public class RepositoryTest {

    private final Repository<Double> repository = new Repository<>(1.0);

    /**
     * <strong>Task:</strong>
     *
     * <p> synchronizace (Readers-writers, 1 repository allows simultaneous read and single write at the time = 10ms, 10 peers are randomly trying to access
     * repository, prevent write collisions)
     */
    @Test
    public void test() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            executorService.submit(new Reader());
            if (i % 2 == 0) {
                executorService.submit(new Writer());
            }
        }

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdownNow();
        }

    }

    class Reader implements Runnable {

        @Override
        public void run() {
            while (true) {
                repository.readValue();

                try {
                    Thread.sleep(TimeUnit.MILLISECONDS.toMillis(5));
                } catch (InterruptedException e) {
                    System.out.println("Interrupted reader: " + Thread.currentThread().getName());
                    return;
                }
            }
        }
    }

    class Writer implements Runnable {

        @Override
        public void run() {
            while (true) {
                repository.storeValue(generate());

                try {
                    Thread.sleep(TimeUnit.MILLISECONDS.toMillis(5));
                } catch (InterruptedException e) {
                    System.out.println("Interrupted writer: " + Thread.currentThread().getName());
                    return;
                }
            }
        }

        private double generate() {
            return Math.random() * 1000;
        }
    }

}
