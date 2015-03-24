package ass.prochka6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

public class TCPEchoClientExtended implements Runnable {

    private String hostname;
    private String word;
    private Integer port;

    private String response;

    public TCPEchoClientExtended(String hostname, String word, Integer port) {
        this.hostname = hostname;
        if (word == null) {
            throw new IllegalArgumentException("Word must not be null");
        }
        this.word = word;
        if (port == null || port < 8080) {
            port = 8080;
        } else {
            this.port = port;
        }
    }

    private String readResponse(InputStream in) throws IOException {
        try (BufferedReader bf = new BufferedReader(new InputStreamReader(in, Charset.forName("utf-8")))) {
            return bf.lines().collect(Collectors.joining("\n"));
        }
    }

    String getResponse() {
        return this.response;
    }

    @Override
    public void run() {
        try {
            // Convert argument String to bytes using the default character encoding
            byte[] data = word.getBytes(Charset.forName("utf-8"));

            // Create socket that is connected to server on specified port
            Socket socket = new Socket(hostname, port);

            System.out.println("Connected to server...sending echo string");

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            out.write(data);  // Send the encoded string to the server

            // Receive the same string back from the server
            String response = readResponse(in);
            this.response = response;

            System.out.println("Received: " + response);

            socket.close();  // Close the socket and its streams
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
