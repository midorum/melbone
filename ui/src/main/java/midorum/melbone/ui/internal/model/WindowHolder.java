package midorum.melbone.ui.internal.model;

import midorum.melbone.model.window.baseapp.BaseAppWindow;

import java.util.Optional;

public class WindowHolder {
    public static final WindowHolder EMPTY = new WindowHolder();

    private BaseAppWindow window; //effectively final

    private WindowHolder() {
    }

    public WindowHolder(BaseAppWindow window) {
        this.window = window;
    }

    public Optional<BaseAppWindow> getWindow() {
        return Optional.ofNullable(window);
    }
}
