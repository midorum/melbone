package midorum.melbone.window.internal.common;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Win32System;
import com.midorum.win32api.facade.exception.Win32ApiException;
import com.midorum.win32api.struct.PointFloat;
import midorum.melbone.model.exception.ControlledInterruptedException;
import midorum.melbone.model.settings.setting.ApplicationSettings;

public class AdjustingMouse extends Mouse {

    private final PointFloat mousePosition;

    AdjustingMouse(final ApplicationSettings applicationSettings, final Win32System win32System, final IWindow window, final PointFloat mousePosition) {
        super(applicationSettings, mousePosition != null ? window.getWindowMouse(applicationSettings.speedFactor()) : win32System.getScreenMouse(applicationSettings.speedFactor()));
        this.mousePosition = mousePosition;
    }

    public void adjust() throws Win32ApiException {
        try {
            if (mousePosition != null) mouse.move(mousePosition);
            else mouse.move(0, 0);
        } catch (InterruptedException e) {
            throw new ControlledInterruptedException(e);
        }
    }

}
