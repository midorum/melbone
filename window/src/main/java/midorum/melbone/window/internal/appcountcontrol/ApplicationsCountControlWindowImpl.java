package midorum.melbone.window.internal.appcountcontrol;

import com.midorum.win32api.facade.IMouse;
import com.midorum.win32api.facade.IWindow;
import midorum.melbone.model.window.appcountcontrol.ApplicationsCountControlWindow;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.internal.common.CommonWindowService;
import midorum.melbone.window.internal.util.Log;
import midorum.melbone.window.internal.util.StaticResources;

public class ApplicationsCountControlWindowImpl implements ApplicationsCountControlWindow {

    private final IWindow window;
    private final Log log;
    private final Settings settings;
    private final CommonWindowService commonWindowService;

    public ApplicationsCountControlWindowImpl(IWindow window, final CommonWindowService commonWindowService, final Settings settings) {
        this.window = window;
        this.log = new Log(StaticResources.LOGGER, "app count control [" + window.getSystemId() + "]");
        this.settings = settings;
        this.commonWindowService = commonWindowService;
    }

    @Override
    public void clickConfirmButton() throws InterruptedException {
        log.info("confirm dialog");
        if (commonWindowService.bringWindowForeground(window)) {
            getMouse().move(settings.targetCountControl().confirmButtonPoint()).leftClick();
            log.info("dialog confirmed");
        } else {
            log.warn("cannot bring window foreground");
        }
    }

    private IMouse getMouse() {
        return window.getWindowMouse(settings.application().speedFactor());
    }
}
