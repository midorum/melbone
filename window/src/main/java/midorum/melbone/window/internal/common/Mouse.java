package midorum.melbone.window.internal.common;

import com.midorum.win32api.facade.IMouse;
import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.exception.Win32ApiException;
import com.midorum.win32api.struct.PointFloat;
import dma.util.Delay;
import midorum.melbone.model.settings.setting.ApplicationSettings;

import java.util.concurrent.TimeUnit;

public class Mouse {

    protected final IMouse mouse;
    protected final Delay delay;
    protected final long mouseClickDelay;

    Mouse(final ApplicationSettings applicationSettings, final IWindow window) {
        this.mouse = window.getWindowMouse(applicationSettings.speedFactor());
        this.delay = new Delay(applicationSettings.speedFactor());
        this.mouseClickDelay = applicationSettings.mouseClickDelay();
    }

    Mouse(final ApplicationSettings applicationSettings, final IMouse mouse) {
        this.mouse = mouse;
        this.delay = new Delay(applicationSettings.speedFactor());
        this.mouseClickDelay = applicationSettings.mouseClickDelay();
    }

    public void clickAtPoint(final PointFloat point) throws InterruptedException, Win32ApiException {
        mouse.move(point);
        delay.sleep(mouseClickDelay, TimeUnit.MILLISECONDS);
        mouse.move(point).leftClick();
    }

    public void clickAtPoint(final float x, final float y) throws InterruptedException, Win32ApiException {
        mouse.move(x, y);
        delay.sleep(mouseClickDelay, TimeUnit.MILLISECONDS);
        mouse.move(x, y).leftClick();
    }
}
