package midorum.melbone.model.exception;

public class CannotGetUserInputException extends Exception {

    public CannotGetUserInputException() {
    }

    public CannotGetUserInputException(final String message) {
        super(message);
    }

    public CannotGetUserInputException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
