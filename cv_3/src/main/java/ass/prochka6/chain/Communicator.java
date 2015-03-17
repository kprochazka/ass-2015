package ass.prochka6.chain;

/**
 * @author Kamil Prochazka
 */
public class Communicator {

    private final Application sender;
    private final Link receiver;

    public Communicator(Application sender, Link receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public void send(Message message) {
        System.out.println();
        System.out.println("Message = \"" + message.getMessage() + "\"");

        sender.mux(message);
    }

    public void receive(Message message) {
        System.out.println();
        System.out.println("Recv(\"" + message.getMessage() + "\")");

        receiver.demux(message);
    }

}
