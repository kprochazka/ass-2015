package ass.prochka6.chain;

/**
 * @author Kamil Prochazka
 */
public class Application extends AbstractFilter {

    private final String user;

    public Application(String user) {
        this.user = user;
    }

    @Override
    protected void muxInternal(Message message) {
        String messageText = message.getMessage();

        messageText = "From " + user + ">" + messageText;
        message.setMessage(messageText);

        printMuxHeader(getClass(), messageText);
    }

    @Override
    protected void demuxInternal(Message message) {
        String messageText = message.getMessage();

        int beginIndex = messageText.indexOf(">");
        messageText = messageText.substring(beginIndex + 1, messageText.length());
        message.setMessage(messageText);

        printDemuxHeader(getClass(), messageText);
    }

}
