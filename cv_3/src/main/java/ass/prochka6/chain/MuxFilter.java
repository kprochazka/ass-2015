package ass.prochka6.chain;

/**
 * @author Kamil Prochazka
 */
public interface MuxFilter {

    void mux(Message message);

    /**
     * Successor in chain used during mux()
     */
    void setSuccessor(MuxFilter successor);

}
