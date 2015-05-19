package ass.prochka6;

import java.io.IOException;

/**
 * HttpServer Main.
 *
 * @author Kamil Prochazka
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

}
