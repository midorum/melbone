package midorum.melbone.model.exception;

public class ControlledInterruptedException extends RuntimeException {

    public ControlledInterruptedException(Throwable cause) {
        super(cause);
    }

    public ControlledInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
