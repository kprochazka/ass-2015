package ass.prochka6;

/**
 * @author Kamil Prochazka (Kamil.Prochazka@airbank.cz)
 */
public class Test {

    public static void main(String[] args) {
        SoftCache<String, String> cache = new SoftCache<>(10);

        for (int i = 0; i < 100_000; i++) {
            if (i % 5 == 0) {
                cache.get("test1");
            }
            cache.put("test" + i, "value" + i);
        }

        String value1 = cache.get("test1");
        System.out.println(value1);
        System.out.println(cache.size());
    }

}
