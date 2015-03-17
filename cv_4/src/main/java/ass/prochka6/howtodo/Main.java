package ass.prochka6.howtodo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ass.prochka6.Timing;

/**
 * @author Kamil Prochazka
 */
public class Main {

    private static final String FILE_PATH_0 = Main.class.getResource("/big.txt").getFile();

    public static void main(String[] args) throws Exception {
        Timing oldSchool = new Timing("OldSchool", true);
        for (int i = 0; i < 100; i++) {
            List<String> strings = readLinesUsingFileReader(FILE_PATH_0);
            oldSchool.print(i + 1);
        }
        oldSchool.print(100);

        Timing streamTimer = new Timing("StreamTimer", true);
        for (int i = 0; i < 100; i++) {
            List<String> strings = readStreamOfLinesUsingFiles(FILE_PATH_0);
            streamTimer.print(i + 1);
        }
        streamTimer.print(100);
    }


    private static List<String> readLinesUsingFileReader(String filePath) throws IOException {
        List<String> result = new LinkedList<>();

        File file = new File(filePath);

        try (BufferedReader br = new BufferedReader(new FileReader(file));) {
            String line;
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
        }

        return result;
    }

    private static List<String> readStreamOfLinesUsingFiles(String filePath) throws IOException {
        List<String> result = new LinkedList<>();

        try (Stream<String> lines = Files.lines(Paths.get(new File(filePath).getPath()));) {
            return lines.collect(Collectors.toCollection(LinkedList<String>::new));
        }
        //Close the stream and it's underlying file as well
    }

}
