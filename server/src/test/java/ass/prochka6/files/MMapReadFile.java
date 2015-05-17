package ass.prochka6.files;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 *
 * @author Kamil Prochazka (Kamil.Prochazka@airbank.cz)
 */
public class MMapReadFile {
    public static void main(String[] args) throws IOException {

        MappedByteBuffer buff = getBufferFor(new File("server/src/test/resources/test"));
        CharBuffer charBuffer = buff.asCharBuffer();
        System.out.println(charBuffer.toString());

        String results = String.valueOf(buff.asCharBuffer());
//        String s = new String( buff.asCharBuffer(), Charset.defaultCharset());
//        s=new String()
//        System.out.println(results);
    }

    public static MappedByteBuffer getBufferFor(File f) throws IOException {
        RandomAccessFile file = new RandomAccessFile(f, "r");

        MappedByteBuffer buffer = file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, f.length());
        file.close();
        return buffer;
    }

}
