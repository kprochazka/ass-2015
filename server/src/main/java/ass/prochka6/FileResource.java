package ass.prochka6;

import java.io.Serializable;

/**
 * FileResource represent filesystem resource.
 *
 * @author Kamil Prochazka
 */
public class FileResource implements Serializable {

    private final String path;

    private final String name;
    private final long contentSize;
    private final String contentType;

    private final byte[] data;

    public FileResource(String path, String name, long contentSize, String contentType, byte[] data) {
        this.path = path;
        this.name = name;
        this.contentSize = contentSize;
        this.contentType = contentType;
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public long getContentSize() {
        return contentSize;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getData() {
        return data;
    }
}
