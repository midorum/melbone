package midorum.melbone.window.internal.uac;

import com.midorum.win32api.facade.Win32System;
import midorum.melbone.model.window.uac.UacWindow;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.internal.common.CommonWindowService;

import java.util.Optional;

public class UacWindowFactory {

    private final Win32System win32System;
    private final Settings settings;

    public UacWindowFactory(final CommonWindowService commonWindowService, final Settings settings) {
        this.win32System = commonWindowService.getWin32System();
        this.settings = settings;
    }

    public Optional<UacWindow> findUacOverlayWindow() {
        return win32System.getForegroundWindow()
                .map(window -> new UacWindowImpl(window, settings))
                .map(UacWindow.class::cast)
                .filter(UacWindow::isValid);
    }
}