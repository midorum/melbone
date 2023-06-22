package midorum.melbone.model.window.launcher;

import midorum.melbone.model.dto.Account;
import midorum.melbone.model.exception.CannotGetUserInputException;

public interface RestoredLauncherWindow {

    boolean checkClientIsAlreadyRunningWindowRendered() throws InterruptedException, CannotGetUserInputException;

    void login(Account account) throws InterruptedException, CannotGetUserInputException;

    void startGameWhenGetReady() throws InterruptedException, CannotGetUserInputException;
}
