package midorum.melbone.model.exception;

public class CriticalErrorException extends RuntimeException {
    public CriticalErrorException(final String message) {
        super(message);
    }

    public CriticalErrorException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CriticalErrorException(final Throwable cause) {
        super(cause);
    }
}
