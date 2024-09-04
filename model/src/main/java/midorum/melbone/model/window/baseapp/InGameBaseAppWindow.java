package midorum.melbone.model.window.baseapp;

import midorum.melbone.model.exception.CannotGetUserInputException;

public interface InGameBaseAppWindow {

    void checkInLoginTracker() throws InterruptedException, CannotGetUserInputException;

    void checkInAction() throws InterruptedException, CannotGetUserInputException;
}
