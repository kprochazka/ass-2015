package ass.prochka6.chain;

/**
 * @author Kamil Prochazka
 */
public abstract class AbstractFilter implements MuxFilter, DemuxFilter {

    /**
     * Successor in chain used during mux()
     */
    protected MuxFilter successor;

    /**
     * Predecessor in chain used during demux()
     */
    protected DemuxFilter predecessor;

    @Override
    public void setSuccessor(MuxFilter successor) {
        this.successor = successor;
    }

    @Override
    public void setPredecessor(DemuxFilter predecessor) {
        this.predecessor = predecessor;
    }

    @Override
    public void mux(Message message) {
        muxInternal(message);

        if (successor != null) {
            successor.mux(message);
        }
    }

    protected abstract void muxInternal(Message message);

    @Override
    public void demux(Message message) {
        demuxInternal(message);

        if (predecessor != null) {
            predecessor.demux(message);
        }
    }

    protected abstract void demuxInternal(Message message);

    protected void printMuxHeader(Class<? extends AbstractFilter> filterClass, String messageText) {
        System.out.println(filterClass.getSimpleName() + ": mux(\"" + messageText + "\")");
    }

    protected void printDemuxHeader(Class<? extends AbstractFilter> filterClass, String messageText) {
        System.out.println(filterClass.getSimpleName() + ": demux(\"" + messageText + "\")");
    }

}
