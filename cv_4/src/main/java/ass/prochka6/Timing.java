package ass.prochka6;

import java.text.MessageFormat;

/**
 * @author Kamil Prochazka
 */
public class Timing {

    private final String name;
    private final boolean nanos;

    private final long start;
    private Long end;

    public Timing(String name) {
        this(name, false);
    }

    public Timing(String name, boolean nanos) {
        this.name = name;
        this.nanos = nanos;
        if (nanos) {
            start = System.nanoTime();
        } else {
            start = System.currentTimeMillis();
        }
    }

    public void stop() {
        this.end = currentTime();
    }


    private long currentTime() {
        if (nanos) {
            return System.nanoTime();
        }

        return System.currentTimeMillis();
    }

    public void print(int iterations) {
        long currentEnd = end != null ? end : currentTime();

        String unit = " ms";
        if (nanos) {
            unit = " ns";
        }

        System.out.println(MessageFormat.format("Timing ({0}) takes {1}{2}", name, (currentEnd - start) / iterations, unit));
    }

}
