package midorum.melbone.model.window.baseapp;

import com.midorum.win32api.facade.exception.Win32ApiException;
import midorum.melbone.model.window.WindowConsumer;

import java.util.Optional;

public interface BaseAppWindow {

    Optional<String> getCharacterName();

    void bindWithAccount(final String accountId);

    void restoreAndDo(WindowConsumer<RestoredBaseAppWindow> consumer) throws InterruptedException, Win32ApiException;

    void doInGameWindow(WindowConsumer<InGameBaseAppWindow> consumer) throws InterruptedException, Win32ApiException;
}
