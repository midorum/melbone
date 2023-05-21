package midorum.melbone.model.window.baseapp;

import dma.function.ConsumerThrowing;

import java.util.Optional;

public interface BaseAppWindow {

    Optional<String> getCharacterName();

    void bindWithAccount(final String accountId);

    void restoreAndDo(ConsumerThrowing<RestoredBaseAppWindow, InterruptedException> consumer) throws InterruptedException;

    void doInGameWindow(ConsumerThrowing<InGameBaseAppWindow, InterruptedException> consumer) throws InterruptedException;
}
