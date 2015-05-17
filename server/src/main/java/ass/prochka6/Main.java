package ass.prochka6;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import javax.activation.MimetypesFileTypeMap;

import ass.prochka6.http.HttpServer;

/**
 *
 * @author Kamil Prochazka (Kamil.Prochazka@airbank.cz)
 */
public class Main {

    public static void main(String[] args) throws IOException {
        HttpServer httpServer = new HttpServer(8080);
        httpServer.start();

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            httpServer.stop();
        }
    }

    static void read() throws IOException {
        File x = new File("ads");

        MimetypesFileTypeMap mime = new MimetypesFileTypeMap();

        Path path = Paths.get("C:\\cvut\\workspace\\ass-parent", "E:\\Films\\Rebelove-2001.avi");

        boolean exists = Files.exists(path);
        System.out.println(exists);

        BasicFileAttributes att = Files.readAttributes(path, BasicFileAttributes.class);
        System.out.println(att.fileKey());
        System.out.println(att.size());
    }

}
