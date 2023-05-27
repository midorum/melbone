package midorum.melbone.model.settings.setting;

import com.midorum.win32api.struct.PointFloat;
import midorum.melbone.model.dto.KeyShortcut;

public interface TargetBaseAppSettings {

    String windowTitle();

    String windowClassName();

    PointFloat menuExitOptionPoint();

    PointFloat manaIndicatorPoint();

    PointFloat windowMinimizeButtonPoint();

    PointFloat disconnectedPopupCloseButtonPoint();

    PointFloat windowCloseButtonPoint();

    PointFloat screenSettingsTabPoint();

    PointFloat uiScaleChooser80Point();

    PointFloat soundSettingsTabPoint();

    PointFloat overallVolumeZeroLevelPoint();

    PointFloat optionsApplyButtonPoint();

    PointFloat needRestartPopupConfirmButtonPoint();

    PointFloat openOptionsButtonPoint();

    PointFloat selectServerButtonPoint();

    PointFloat connectServerButtonPoint();

    PointFloat selectCharacterButtonPoint();

    PointFloat startButtonPoint();

    PointFloat dailyTrackerButtonPoint();

    PointFloat dailyTrackerTabPoint();

    PointFloat trackLoginButtonPoint();

    PointFloat closeDailyTrackerPopupButtonPoint();

    PointFloat actionButtonPoint();

    PointFloat actionSecondButtonPoint();

    int serverPageRenderingTimeout();

    int checkServerPageRenderingDelay();

    int menuRenderingTimeout();

    int checkMenuRenderingDelay();

    int windowAppearingTimeout();

    int windowAppearingDelay();

    int windowAppearingLatency();

    int windowDisappearingTimeout();

    int checkWindowDisappearingDelay();

    int startButtonRenderingTimeout();

    int checkStartButtonRenderingDelay();

    int baseWindowRenderingTimeout();

    int checkBaseWindowRenderingDelay();

    int disconnectedPopupRenderingTimeout();

    int checkDisconnectedPopupRenderingDelay();

    int optionsDialogRenderingTimeout();

    int checkOptionsDialogRenderingDelay();

    int needRestartPopupRenderingTimeout();

    int checkNeedRestartPopupRenderingDelay();

    int serverLineRenderingTimeout();

    int checkServerLineRenderingDelay();

    int dailyTrackerPopupRenderingTimeout();

    int checkDailyTrackerPopupRenderingDelay();

    long afterLaunchAccountDelay();

    String processName();

    KeyShortcut stopAnimationHotkey();

    KeyShortcut cancelCurrentOperationHotkey();

    KeyShortcut openMenuHotkey();

}
