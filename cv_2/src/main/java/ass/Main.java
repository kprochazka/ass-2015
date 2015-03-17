package ass;

import java.util.Arrays;

/**
 * @author Kamil Prochazka
 */
public class Main {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Some arguments have to be passed");
            System.exit(-1);
        }

        // parse to Integers
        Integer[] numbers = new Integer[args.length];
        for (int i = 0; i < args.length; i++) {
            numbers[i] = Integer.parseInt(args[i]);
        }

//        List<Integer> numList = Arrays.asList(args).stream().map((arg) -> Integer.parseInt(arg)).collect(Collectors.toList());
//        Integer[] integers = numList.toArray(new Integer[numList.size()]);

        QuickSort<Integer> quickSort = new QuickSort<>();
        quickSort.sort(numbers, 0, args.length - 1);
        System.out.println(Arrays.toString(numbers));
    }

}
