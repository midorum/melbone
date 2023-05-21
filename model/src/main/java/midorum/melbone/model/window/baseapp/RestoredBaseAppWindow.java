package midorum.melbone.model.window.baseapp;

public interface RestoredBaseAppWindow {

    void close() throws InterruptedException;

    void selectServer() throws InterruptedException;

    void chooseCharacter() throws InterruptedException;
}
