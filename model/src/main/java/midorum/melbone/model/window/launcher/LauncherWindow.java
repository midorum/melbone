package midorum.melbone.model.window.launcher;

import com.midorum.win32api.facade.exception.Win32ApiException;
import midorum.melbone.model.window.WindowConsumer;

public interface LauncherWindow {

    void restoreAndDo(WindowConsumer<RestoredLauncherWindow> consumer) throws InterruptedException, Win32ApiException;
}
