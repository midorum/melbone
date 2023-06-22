package midorum.melbone.model.settings.setting;

import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;

public interface TargetLauncherSettings {

    String windowTitle();

    Rectangle windowDimensions();

    PointInt desktopShortcutLocationPoint();

    String initializationErrorDialogTitle();

    Rectangle initializationErrorDialogDimensions();

    PointFloat closeInitializationErrorDialogButtonPoint();

    @Deprecated
    int attemptToFindStartButton();

    int searchStartButtonTimeout();

    int searchStartButtonDelay();

    PointFloat windowCloseButtonPoint();

    PointFloat accountDropListPoint();

    PointFloat accountLogoutPoint();

    PointFloat loginInputPoint();

    PointFloat passwordInputPoint();

    PointFloat loginButtonPoint();

    PointFloat startButtonPoint();

    @Deprecated
    //FIXME >>> remove
    int attemptsToWindowRendering();

    int windowRenderingTimeout();

    int windowRenderingDelay();

    int closingWindowTimeout();

    int closingWindowDelay();

    int windowAppearingTimeout();

    int windowAppearingDelay();

    int windowAppearingLatency();

    String processName();

    long brokenProcessTimeout();

    int loginTimeout();

    int checkLoginDelay();

    String confirmQuitDialogTitle();

    Rectangle confirmQuitDialogDimensions();

    int confirmQuitDialogRenderingTimeout();

    int confirmQuitDialogRenderingDelay();

    PointFloat closeQuitConfirmPopupButtonPoint();
}
