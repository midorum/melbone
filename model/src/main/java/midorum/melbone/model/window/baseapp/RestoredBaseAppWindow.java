package midorum.melbone.model.window.baseapp;

import midorum.melbone.model.exception.CannotGetUserInputException;

public interface RestoredBaseAppWindow {

    void close() throws InterruptedException, CannotGetUserInputException;

    void selectServer() throws InterruptedException, CannotGetUserInputException;

    void chooseCharacter() throws InterruptedException, CannotGetUserInputException;

    void checkInGameWindowRendered() throws InterruptedException;
}
