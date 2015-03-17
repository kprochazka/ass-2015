package ass.prochka6.chain;

/**
 * @author Kamil Prochazka
 */
public class ProtocolFilter extends AbstractFilter {

    protected final String protocolIdentification;

    public ProtocolFilter(String protocolIdentification) {
        this.protocolIdentification = protocolIdentification;
    }

    @Override
    protected void muxInternal(Message message) {
        String messageText = message.getMessage();

        messageText = protocolIdentification + ":" + messageText;
        message.setMessage(messageText);

        printMuxHeader(getClass(), messageText);
    }

    @Override
    protected void demuxInternal(Message message) {
        String messageText = message.getMessage();

        int beginIndex = messageText.indexOf(":");
        messageText = messageText.substring(beginIndex + 1, messageText.length());
        message.setMessage(messageText);

        printDemuxHeader(getClass(), messageText);
    }

}
