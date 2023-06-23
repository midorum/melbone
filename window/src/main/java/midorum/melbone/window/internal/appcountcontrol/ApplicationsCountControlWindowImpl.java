package midorum.melbone.window.internal.appcountcontrol;

import com.midorum.win32api.facade.IWindow;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.window.appcountcontrol.ApplicationsCountControlWindow;
import midorum.melbone.model.exception.CannotGetUserInputException;
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
        try {
            commonWindowService.bringForeground(window).andDo(foregroundWindow -> {
                foregroundWindow.getMouse().clickAtPoint(settings.targetCountControl().confirmButtonPoint());
                log.info("dialog confirmed");
            });
        } catch (CannotGetUserInputException e) {
            final String marker = Long.toString(System.currentTimeMillis());
            log.error("Cannot get user input in target window (marker=" + marker + "): ", e);
            commonWindowService.takeAndSaveWholeScreenShot("cannot get user input in app count control window", marker);
        }
    }
}
