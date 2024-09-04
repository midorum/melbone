package midorum.melbone.window.internal.uac;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.facade.exception.Win32ApiException;
import midorum.melbone.model.window.uac.UacWindow;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.internal.util.Log;
import midorum.melbone.window.internal.util.StaticResources;

import java.util.function.Supplier;

public class UacWindowImpl implements UacWindow {

    private final IWindow window;
    private final Settings settings;
    private final Log log;

    public UacWindowImpl(IWindow window, final Settings settings) {
        this.window = window;
        this.settings = settings;
        this.log = new Log(StaticResources.LOGGER, "UAC [" + window.getSystemId() + "]");
    }

    @Override
    public boolean isValid() {
        try {
            return this.window.getClassName().filter(s -> settings.uacSettings().windowClassName().equals(s)).getOrThrow().isPresent()
                    && window.getWindowRectangle().map(r -> checkDimensions(settings.uacSettings().windowDimensions(), r)).getOrThrow();
        } catch (Win32ApiException e) {
            log.error("cannot check UAC attributes", e);
            return false;
        }
    }

    private boolean checkDimensions(final Rectangle requiredDimensions, final Rectangle windowRectangle) {
        return requiredDimensions.width() == windowRectangle.width()
                && requiredDimensions.height() == windowRectangle.height();
    }
}
