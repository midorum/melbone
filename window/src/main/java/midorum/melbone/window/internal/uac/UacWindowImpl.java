package midorum.melbone.window.internal.uac;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Rectangle;
import midorum.melbone.model.window.uac.UacWindow;
import midorum.melbone.model.settings.setting.Settings;

@SuppressWarnings("ClassCanBeRecord")
public class UacWindowImpl implements UacWindow {

    private final IWindow window;
    private final Settings settings;

    public UacWindowImpl(IWindow window, final Settings settings) {
        this.window = window;
        this.settings = settings;
    }

    @Override
    public boolean isValid() {
        return this.window.getClassName().filter(s -> settings.uacSettings().windowClassName().equals(s)).isPresent()
                && checkDimensions(settings.uacSettings().windowDimensions(), this.window.getWindowRectangle());
    }

    private boolean checkDimensions(final Rectangle requiredDimensions, final Rectangle windowRectangle) {
        return requiredDimensions.width() == windowRectangle.width()
                && requiredDimensions.height() == windowRectangle.height();
    }
}
