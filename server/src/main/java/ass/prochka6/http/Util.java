package ass.prochka6.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 *
 * @author Kamil Prochazka (Kamil.Prochazka@airbank.cz)
 */
public class Util {

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                LOG.error("Could not close", e);
            }
        }
    }

    /**
     * Decode percent encoded <code>String</code> values.
     *
     * @param str
     *            the percent encoded <code>String</code>
     * @return expanded form of the input, for example "foo%20bar" becomes
     *         "foo bar"
     */
    static String decodePercent(String str) {
        String decoded = null;
        try {
            decoded = URLDecoder.decode(str, "UTF8");
        } catch (UnsupportedEncodingException ignored) {
            LOG.warn("Encoding not supported, ignored", ignored);
        }
        return decoded;
    }

}
