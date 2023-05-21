package midorum.melbone.model.settings.key;

import com.midorum.win32api.facade.IWindow;

import java.util.Objects;

/**
 * to avoid null references
 */
public class WindowHolder {
    public static final WindowHolder EMPTY = new WindowHolder();

    private IWindow window; //effectively final

    private WindowHolder() {
    }

    public WindowHolder(IWindow window) {
        this.window = window;
    }

    public IWindow getWindow() {
        Objects.requireNonNull(window, "Window is not provided");
        return window;
    }
}