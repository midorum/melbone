package midorum.melbone.settings.internal.obtaining.setting;

import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;
import midorum.melbone.model.settings.setting.TargetLauncherSettings;
import midorum.melbone.settings.SettingKeys;
import midorum.melbone.settings.internal.storage.KeyValueStorage;

public class TargetLauncherSettingsImpl extends SettingValueExtractor implements TargetLauncherSettings {

    public TargetLauncherSettingsImpl(final KeyValueStorage keyValueStorage) {
        super(keyValueStorage);
    }

    @Override
    public PointInt desktopShortcutLocationPoint() {
        return (PointInt) getValue(SettingKeys.TargetLauncher.desktopShortcutLocationPoint);
    }

    @Override
    public String networkErrorDialogTitle() {
        return (String) getValue(SettingKeys.TargetLauncher.networkErrorDialogTitle);
    }

    @Override
    public Rectangle networkErrorDialogDimensions() {
        return (Rectangle) getValue(SettingKeys.TargetLauncher.networkErrorDialogDimensions);
    }

    @Override
    public PointFloat closeNetworkErrorDialogButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetLauncher.closeNetworkErrorDialogButtonPoint);
    }

    @Override
    public Rectangle windowDimensions() {
        return (Rectangle) getValue(SettingKeys.TargetLauncher.windowDimensions);
    }

    @Override
    public String windowTitle() {
        return (String) getValue(SettingKeys.TargetLauncher.windowTitle);
    }

    @Override
    public PointFloat windowCloseButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetLauncher.windowCloseButtonPoint);
    }

    @Override
    public PointFloat accountDropListPoint() {
        return (PointFloat) getValue(SettingKeys.TargetLauncher.accountDropListPoint);
    }

    @Override
    public PointFloat accountLogoutPoint() {
        return (PointFloat) getValue(SettingKeys.TargetLauncher.accountLogoutPoint);
    }

    @Override
    public PointFloat loginInputPoint() {
        return (PointFloat) getValue(SettingKeys.TargetLauncher.loginInputPoint);
    }

    @Override
    public PointFloat passwordInputPoint() {
        return (PointFloat) getValue(SettingKeys.TargetLauncher.passwordInputPoint);
    }

    @Override
    public PointFloat loginButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetLauncher.loginButtonPoint);
    }

    @Override
    public PointFloat startButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetLauncher.startButtonPoint);
    }

    @Override
    public int searchStartButtonTimeout() {
        //FIXME такая большая задержка нужна для запуска после обнов, когда ждем скачивания и установки патча;
        // а еще бывает не дожидается кнопки когда система тупит и мало свободной памяти
        return 600_000;
    }

    @Override
    public int searchStartButtonDelay() {
        return 1_000;
    }

    @Override
    public int windowRenderingTimeout() {
        return 120_000;
    }

    @Override
    public int windowRenderingDelay() {
        return 1_000;
    }

    @Override
    public int closingWindowTimeout() {
        return 120_000;
    }

    @Override
    public int closingWindowDelay() {
        return 1_000;
    }

    @Override
    public int windowAppearingTimeout() {
        return 60_000;
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
    public String processName() {
        return (String) getValue(SettingKeys.TargetLauncher.processName);
    }

    @Override
    public long brokenProcessTimeout() {
        return 600_000;
    }

    @Override
    public int loginTimeout() {
        return 5_000;
    }

    @Override
    public int checkLoginDelay() {
        return 1_000;
    }

    @Override
    public Rectangle confirmQuitDialogDimensions() {
        return (Rectangle) getValue(SettingKeys.TargetLauncher.confirmQuitDialogDimensions);
    }

    @Override
    public String confirmQuitDialogTitle() {
        return (String) getValue(SettingKeys.TargetLauncher.confirmQuitDialogTitle);
    }

    @Override
    public int confirmQuitDialogRenderingTimeout() {
        return 3_000;
    }

    @Override
    public int confirmQuitDialogRenderingDelay() {
        return 500;
    }

    @Override
    public PointFloat closeQuitConfirmPopupButtonPoint() {
        return (PointFloat) getValue(SettingKeys.TargetLauncher.closeQuitConfirmPopupButtonPoint);
    }

    @Override
    public int networkErrorDialogTimeout() {
        return 5_000;
    }

    @Override
    public int networkErrorDialogDelay() {
        return 500;
    }
}
