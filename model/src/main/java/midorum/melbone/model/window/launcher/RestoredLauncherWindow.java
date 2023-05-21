package midorum.melbone.model.window.launcher;

import midorum.melbone.model.dto.Account;

public interface RestoredLauncherWindow {

    boolean checkClientIsAlreadyRunningWindowRendered() throws InterruptedException;

    void login(Account account) throws InterruptedException;

    void startGameWhenGetReady() throws InterruptedException;
}
