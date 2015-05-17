package ass.prochka6.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (req.getRequestURI().contains("favicon")) {
            return;
        }

        read1(resp);
        read2(resp);
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 50; i++) {
            b();
            a();
        }
    }

    static void a() throws Exception {
        FileChannel fc = new RandomAccessFile(new File("C:\\cvut\\workspace\\ass-parent\\server\\src\\test\\resources\\test"), "r").getChannel();

        long bufferSize = 5 * 1000 * 1000;
        long end = fc.size() > bufferSize ? bufferSize : fc.size();
        MappedByteBuffer mem = fc.map(FileChannel.MapMode.READ_ONLY, 0, end);
        long oldSize = fc.size();

        long currentPos = 0;
        long xx = currentPos;

        long startTime = System.currentTimeMillis();
        long lastValue = -1;
        for (; ; ) {
            while (mem.hasRemaining()) {
                lastValue = mem.get();
                currentPos++;
            }
            if (currentPos < oldSize) {
                xx = xx + mem.position();
                end = xx + bufferSize > fc.size() ? fc.size() - xx : bufferSize;
                mem = fc.map(FileChannel.MapMode.READ_ONLY, xx, end);
                continue;
            } else {
                end = System.currentTimeMillis();
                long tot = end - startTime;
                System.out.println(String.format("Last Value Read %s , Time(ms) %s ", lastValue, tot));
                break;
            }
        }
    }

    static void b() throws IOException {
        File file = new File("C:\\cvut\\workspace\\ass-parent\\server\\src\\test\\resources\\test3");

        long start = System.currentTimeMillis();

//        try (InputStream i = new BufferedInputStream(new FileInputStream(file))) {
//            int v;
//            while ((v = i.read()) != -1) {
//            }
//        }

        byte[] bytes = Files.readAllBytes(file.toPath());

        System.out.println("Time: " + (System.currentTimeMillis() - start) + "ms");
    }

    static void read1(HttpServletResponse resp) throws IOException {
        File file = new File("C:\\cvut\\workspace\\ass-parent\\server\\src\\test\\resources\\test");

        resp.addHeader("Content-Type", "text/plain");

        long start = System.currentTimeMillis();

        try (RandomAccessFile r = new RandomAccessFile(file, "r"); FileChannel channel = r.getChannel()) {
            if (r.length() == 0) {
                return;
            }

            ServletOutputStream outputStream = resp.getOutputStream();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0, r.length());

            while (map.hasRemaining()) {
                outputStream.write(map.get());
            }
        }

        System.out.println("Time[m]: " + (System.currentTimeMillis() - start) + "ms");
    }

    static void read(HttpServletResponse resp) throws IOException {
        File file = new File("C:\\cvut\\workspace\\ass-parent\\server\\src\\test\\resources\\test");

        resp.addHeader("Content-Type", "text/plain");

        long start = System.currentTimeMillis();

        try (RandomAccessFile r = new RandomAccessFile(file, "r")) {
            FileChannel channel = r.getChannel();
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0, r.length());

            ServletOutputStream outputStream = resp.getOutputStream();

            while (map.hasRemaining()) {
                byte b = map.get();
                outputStream.write(b);
            }
        }

        System.out.println("Time[m]: " + (System.currentTimeMillis() - start) + "ms");
    }

    static void read2(HttpServletResponse resp) throws IOException {
        File file = new File("C:\\cvut\\workspace\\ass-parent\\server\\src\\test\\resources\\test");

        resp.addHeader("Content-Type", "text/plain");

        ServletOutputStream out = resp.getOutputStream();

        long start = System.currentTimeMillis();

        try (BufferedInputStream i = new BufferedInputStream(new FileInputStream(file))) {
            int v;
            while ((v = i.read()) != -1) {
                out.write(v);
            }
        }

        System.out.println("Time: " + (System.currentTimeMillis() - start) + "ms");
    }

}
