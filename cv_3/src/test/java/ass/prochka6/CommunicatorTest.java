package ass.prochka6;

import org.junit.Before;
import org.junit.Test;

import ass.prochka6.chain.Application;
import ass.prochka6.chain.Communicator;
import ass.prochka6.chain.Internet;
import ass.prochka6.chain.Link;
import ass.prochka6.chain.Message;
import ass.prochka6.chain.Transport;

import static org.junit.Assert.assertEquals;

/**
 * @author Kamil Prochazka
 */
public class CommunicatorTest {

    private Communicator communicatorAlice;
    private Communicator communicatorBob;

    @Before
    public void setUp() {
        {
            Application application = new Application("Alice");
            Transport transport = new Transport("Port1");
            Internet internet = new Internet("IP1");
            Link link = new Link("MAC1");

            application.setSuccessor(transport);
            transport.setPredecessor(application);

            transport.setSuccessor(internet);
            internet.setPredecessor(transport);

            internet.setSuccessor(link);
            link.setPredecessor(internet);

            communicatorAlice = new Communicator(application, link);
        }
        {
            Application application = new Application("Bob");
            Transport transport = new Transport("Port2");
            Internet internet = new Internet("IP2");
            Link link = new Link("MAC2");

            application.setSuccessor(transport);
            transport.setPredecessor(application);

            transport.setSuccessor(internet);
            internet.setPredecessor(transport);

            internet.setSuccessor(link);
            link.setPredecessor(internet);

            communicatorBob = new Communicator(application, link);
        }
    }

    @Test
    public void testCommunication() {
        Message message = new Message("Hi Bob!");
        communicatorAlice.send(message);
        assertEquals("MAC1:IP1:Port1:From Alice>Hi Bob!", message.getMessage());

        communicatorBob.receive(message);
        assertEquals("Hi Bob!", message.getMessage());

        message.setMessage("Hi Alice!");
        communicatorBob.send(message);
        assertEquals("MAC2:IP2:Port2:From Bob>Hi Alice!", message.getMessage());

        communicatorAlice.receive(message);
        assertEquals("Hi Alice!", message.getMessage());
    }

}
