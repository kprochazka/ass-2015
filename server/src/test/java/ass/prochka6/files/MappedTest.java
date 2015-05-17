package ass.prochka6.files;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author Kamil Prochazka (Kamil.Prochazka@airbank.cz)
 */
public class MappedTest {

    public static void main(String[] args) throws Exception {
        File file = new File("server/src/test/resources/test");
        System.out.println(file.getAbsolutePath());

        for (int i = 0; i < 50; i++) {
//            test2();
        }
    }

    static void test2() throws Exception {
        File file = new File("server/src/test/resources/test");

        try (RandomAccessFile r = new RandomAccessFile(file, "r")) {
            long start = System.currentTimeMillis();

            FileChannel channel = r.getChannel();

            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0, r.length());

            StringBuilder sb = new StringBuilder();
            while (map.hasRemaining()) {
                char aChar = map.getChar();
                sb.append(aChar);
            }

//            System.out.println(sb);

            System.out.println(("Time:" + (System.currentTimeMillis() - start)));
        }
    }

    static void test() throws Exception {
        File file = new File("server/src/test/resources/test");

        try (RandomAccessFile r = new RandomAccessFile(file, "r")) {
            long start = System.currentTimeMillis();

            String line = null;
            while ((line = r.readLine()) != null) {
//                System.out.println(line);
            }

            System.out.println(("Time:" + (System.currentTimeMillis() - start)));
        }
    }


}
