package ass.prochka6;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * @author Kamil Prochazka
 */
public class Main {

    public static void main(String[] args) throws InterruptedException {
        String a = "AAAAA"; // strong reference

        SoftReference<String> soft = new SoftReference<String>(a); // strong
        soft.get();

        a = null;

        String b = "BBBBB";
        WeakReference<String> weak = new WeakReference<String>(b);
        weak.get();

        System.gc();
        System.gc();
        System.gc();
        Thread.sleep(1000);
        for (int i = 0; i < 1000; i++) {
            System.out.print("");
        }
        Thread.sleep(1000);
        System.gc();
        System.gc();
        System.gc();

        System.out.println(soft.get());
        System.out.println(weak.get());

    }

}
