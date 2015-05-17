package ass.prochka6;

import java.util.Arrays;

/**
 * @author Kamil Prochazka (Kamil.Prochazka@airbank.cz)
 */
public class Test {

    @org.junit.Test
    public void test() {
        String[] chars = {"</flip>"};

        int i = Arrays.binarySearch(chars, "</flip>");
        System.out.println(i);
        int a = '/';
        System.out.println(a);
    }

}
