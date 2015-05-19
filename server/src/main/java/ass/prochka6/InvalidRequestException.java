package ass.prochka6;

/**
 * Parsing or general invalid request exception, translated by HttpProtocolHandler to HttpResponse with given Status.
 *
 * @author Kamil Prochazka
 */
class InvalidRequestException extends RuntimeException {

    private static final long serialVersionUID = 6569838532917408380L;

    private final Status status;

    public InvalidRequestException(Status status, String message) {
        super(message);
        this.status = status;
    }

    public InvalidRequestException(Status status, String message, Exception e) {
        super(message, e);
        this.status = status;
    }

    public Status getStatus() {
        return this.status;
    }

}
