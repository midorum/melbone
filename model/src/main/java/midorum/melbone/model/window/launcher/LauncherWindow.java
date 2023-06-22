package midorum.melbone.model.window.launcher;

import midorum.melbone.model.window.WindowConsumer;

public interface LauncherWindow {

    void restoreAndDo(WindowConsumer<RestoredLauncherWindow> consumer) throws InterruptedException;
}
