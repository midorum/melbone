package midorum.melbone.model.window;

import midorum.melbone.model.exception.CannotGetUserInputException;

@FunctionalInterface
public interface WindowConsumer<W> {

    void accept(W window) throws InterruptedException, CannotGetUserInputException;
}
