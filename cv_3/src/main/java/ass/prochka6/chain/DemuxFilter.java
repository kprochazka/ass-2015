package ass.prochka6.chain;

/**
 * @author Kamil Prochazka
 */
public interface DemuxFilter {

    void demux(Message message);

    /**
     * Predecessor in chain used during demux()
     */
    void setPredecessor(DemuxFilter predecessor);

}
