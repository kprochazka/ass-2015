package ass.prochka6.http;

import java.net.SocketException;

/**
 *
 * @author Kamil Prochazka
 */
public class ProtocolSocketException extends SocketException {

    public ProtocolSocketException(String msg) {
        super(msg);
    }

    public ProtocolSocketException(String msg, Throwable cause) {
        super(msg);
        initCause(cause);
    }

}
