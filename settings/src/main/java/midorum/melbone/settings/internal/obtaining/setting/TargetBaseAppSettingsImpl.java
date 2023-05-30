package midorum.melbone.settings.internal.obtaining.setting;

import com.midorum.win32api.struct.PointFloat;
import midorum.melbone.model.dto.KeyShortcut;
import midorum.melbone.model.settings.setting.TargetBaseAppSettings;
import midorum.melbone.settings.SettingKeys;
import midorum.melbone.settings.internal.storage.KeyValueStorage;

public class TargetBaseAppSettingsImpl extends SettingValueExtractor implements TargetBaseAppSettings {

    public TargetBaseAppSettingsImpl(final KeyValueStorage keyValueStorage) {
        super(keyValueStorage);
    }

    @Override
    public String windowTitle() {
        return (String) getValue(SettingKeys.TargetBaseApp.windowTitle);
    }

    @Override
    public String windowClassName() {
        return (String) getValue(SettingKeys.TargetBaseApp.windowClassName);
    }

    @Override
    public PointFloat menuExitOptionPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.menuExitOptionPoint);
    }

    @Override
    public PointFloat manaIndicatorPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.manaIndicatorPoint);
    }

    @Override
    public PointFloat windowMinimizeButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.windowMinimizeButtonPoint);
    }

    @Override
    public PointFloat disconnectedPopupCloseButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.disconnectedPopupCloseButtonPoint);
    }

    @Override
    public PointFloat windowCloseButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.windowCloseButtonPoint);
    }

    @Override
    public PointFloat screenSettingsTabPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.screenSettingsTabPoint);
    }

    @Override
    public PointFloat uiScaleChooser80Point() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.uiScaleChooser80Point);
    }

    @Override
    public PointFloat soundSettingsTabPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.soundSettingsTabPoint);
    }

    @Override
    public PointFloat overallVolumeZeroLevelPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.overallVolumeZeroLevelPoint);
    }

    @Override
    public PointFloat optionsApplyButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.optionsApplyButtonPoint);
    }

    @Override
    public PointFloat needRestartPopupConfirmButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.needRestartPopupConfirmButtonPoint);
    }

    @Override
    public PointFloat openOptionsButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.openOptionsButtonPoint);
    }

    @Override
    public PointFloat selectServerButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.selectServerButtonPoint);
    }

    @Override
    public PointFloat connectServerButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.connectServerButtonPoint);
    }

    @Override
    public PointFloat selectCharacterButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.selectCharacterButtonPoint);
    }

    @Override
    public PointFloat startButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.startButtonPoint);
    }

    @Override
    public PointFloat dailyTrackerButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.dailyTrackerButtonPoint);
    }

    @Override
    public PointFloat dailyTrackerTabPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.dailyTrackerTabPoint);
    }

    @Override
    public PointFloat trackLoginButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.trackLoginButtonPoint);
    }

    @Override
    public PointFloat closeDailyTrackerPopupButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.closeDailyTrackerPopupButtonPoint);
    }

    @Override
    public PointFloat actionButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.actionButtonPoint);
    }

    @Override
    public PointFloat actionSecondButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetBaseApp.actionSecondButtonPoint);
    }

    @Override
    public int menuRenderingTimeout() {
        return 5_000;
    }

    @Override
    public int checkMenuRenderingDelay() {
        return 500;
    }

    @Override
    public int windowAppearingTimeout() {
        return 600_000;
    }

    @Override
    public int windowAppearingDelay() {
        return 10_000;
    }

    @Override
    public int windowAppearingLatency() {
        return 5_000;
    }

    @Override
    public int windowDisappearingTimeout() {
        return 30_000;
    }

    @Override
    public int checkWindowDisappearingDelay() {
        return 1_000;
    }

    @Override
    public int serverPageRenderingTimeout() {
        return 60_000;
    }

    @Override
    public int checkServerPageRenderingDelay() {
        return 5_000;
    }

    @Override
    public int startButtonRenderingTimeout() {
        return 30_000;
    }

    @Override
    public int checkStartButtonRenderingDelay() {
        return 3_000;
    }

    @Override
    public int baseWindowRenderingTimeout() {
        return 300_000;
    }

    @Override
    public int checkBaseWindowRenderingDelay() {
        return 5_000;
    }

    @Override
    public int disconnectedPopupRenderingTimeout() {
        return 3_000;
    }

    @Override
    public int checkDisconnectedPopupRenderingDelay() {
        return 500;
    }

    @Override
    public int optionsDialogRenderingTimeout() {
        return 5_000;
    }

    @Override
    public int checkOptionsDialogRenderingDelay() {
        return 500;
    }

    @Override
    public int needRestartPopupRenderingTimeout() {
        return 5_000;
    }

    @Override
    public int checkNeedRestartPopupRenderingDelay() {
        return 500;
    }

    @Override
    public int serverLineRenderingTimeout() {
        return 5_000;
    }

    @Override
    public int checkServerLineRenderingDelay() {
        return 500;
    }

    @Override
    public int dailyTrackerPopupRenderingTimeout() {
        return 3_000;
    }

    @Override
    public int checkDailyTrackerPopupRenderingDelay() {
        return 500;
    }

    @Override
    public int accountInfoPopupRenderingTimeout() {
        return 2_000;
    }

    @Override
    public int accountInfoPopupRenderingDelay() {
        return 500;
    }

    @Override
    public long afterLaunchAccountDelay() {
        return (long) getValue(SettingKeys.TargetBaseApp.afterLaunchAccountDelay);
    }

    @Override
    public String processName() {
        return (String) getValue(SettingKeys.TargetBaseApp.processName);
    }

    @Override
    public KeyShortcut stopAnimationHotkey() {
        return (KeyShortcut) getValue(SettingKeys.TargetBaseApp.stopAnimationHotkey);
    }

    @Override
    public KeyShortcut cancelCurrentOperationHotkey() {
        return (KeyShortcut) getValue(SettingKeys.TargetBaseApp.cancelCurrentOperationHotkey);
    }

    @Override
    public KeyShortcut openMenuHotkey() {
        return (KeyShortcut) getValue(SettingKeys.TargetBaseApp.openMenuHotkey);
    }

    @Override
    public KeyShortcut openAccountInfoHotkey() {
        return (KeyShortcut) getValue(SettingKeys.TargetBaseApp.openAccountInfoHotkey);
    }
}
