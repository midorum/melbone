package midorum.melbone.model.settings.setting;

import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;

public interface TargetLauncherSettings {

    String windowTitle();

    Rectangle windowDimensions();

    String confirmQuitDialogTitle();

    Rectangle confirmQuitDialogDimensions();

    PointInt desktopShortcutLocationPoint();

    String initializationErrorDialogTitle();

    Rectangle initializationErrorDialogDimensions();

    PointFloat closeInitializationErrorDialogButtonPoint();

    int attemptToFindStartButton();

    int searchStartButtonDelay();

    PointFloat windowCloseButtonPoint();

    PointFloat closeQuitConfirmPopupButtonPoint();

    PointFloat accountDropListPoint();

    PointFloat accountLogoutPoint();

    PointFloat loginInputPoint();

    PointFloat passwordInputPoint();

    PointFloat loginButtonPoint();

    PointFloat startButtonPoint();

    int attemptsToWindowRendering();

    int windowRenderingDelay();

    int closingWindowDelay();

    int windowAppearingTimeout();

    int windowAppearingDelay();

    int windowAppearingLatency();

    String processName();

    long brokenProcessTimeout();

}
