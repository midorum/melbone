package midorum.melbone.model.exception;

public class NeedRetryException extends RuntimeException {
    public NeedRetryException(final String message) {
        super(message);
    }

    public NeedRetryException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
