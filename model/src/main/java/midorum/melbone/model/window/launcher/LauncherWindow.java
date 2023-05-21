package midorum.melbone.model.window.launcher;

import dma.function.ConsumerThrowing;

public interface LauncherWindow {

    void restoreAndDo(ConsumerThrowing<RestoredLauncherWindow, InterruptedException> consumer) throws InterruptedException;
}
