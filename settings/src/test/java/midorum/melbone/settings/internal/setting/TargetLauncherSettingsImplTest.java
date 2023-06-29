package midorum.melbone.settings.internal.setting;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.facade.WindowPoint;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;
import midorum.melbone.model.exception.CriticalErrorException;
import midorum.melbone.model.settings.setting.TargetLauncherSettings;
import midorum.melbone.settings.SettingKeys;
import midorum.melbone.settings.internal.management.SettingsFactoryInternal;
import midorum.melbone.settings.managment.SettingPropertyNaming;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TargetLauncherSettingsImplTest {

    private final SettingsFactoryInternal settingsFactoryInternal = new SettingsFactoryInternal.Builder()
            .settingPropertyNaming(new SettingPropertyNaming.Builder().inMemoryStorage().build()).build();
    private final TargetLauncherSettings targetLauncherSettings = settingsFactoryInternal.settingsProvider().settings().targetLauncher();

    private final IWindow window = mock(IWindow.class);

    @Test
    void windowTitle() {
        when(window.getText()).thenReturn(Optional.of("window title"));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.windowTitle)
                .settingGetter(targetLauncherSettings::windowTitle)
                .normalValues("string", "")
                .invalidValues()
                .wrongTypeValues()
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("test", "test"))
                .test();
    }

    @Test
    void windowTitleMissed() {
        //FIXME на данный момент в Windows окно всегда имеет заголовок, хотя бы пустой, но гарантий нет
        // что делать если заголовка у окна нет в принципе (даже пустого) пока непонятно, потому что сразу ломается поиск окон по заголовку
        // на данный момент программа падает с ошибкой в таком случае
        when(window.getText()).thenReturn(Optional.empty());
        assertThrows(CriticalErrorException.class, () -> new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.windowTitle)
                .settingGetter(targetLauncherSettings::windowTitle)
                .normalValues("string", "")
                .invalidValues()
                .wrongTypeValues()
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("test", "test"))
                .test());
    }

    @Test
    void windowDimensions() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.windowDimensions)
                .settingGetter(targetLauncherSettings::windowDimensions)
                .normalValues(new Rectangle(0, 0, 0, 0), new Rectangle(0, 0, 100, 50))
                .invalidValues(new Rectangle(-1, -1, -1, -1), new Rectangle(0, 0, -1, -1))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[(0,1)(1919,1080)-(1919:1079)]", new Rectangle(0, 1, 1919, 1080)))
                .test();
    }

    @Test
    void confirmQuitDialogTitle() {
        when(window.getText()).thenReturn(Optional.of("window title"));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.confirmQuitDialogTitle)
                .settingGetter(targetLauncherSettings::confirmQuitDialogTitle)
                .normalValues("string", "")
                .invalidValues()
                .wrongTypeValues()
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("test", "test"))
                .test();
    }

    @Test
    void confirmQuitDialogDimensions() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.confirmQuitDialogDimensions)
                .settingGetter(targetLauncherSettings::confirmQuitDialogDimensions)
                .normalValues(new Rectangle(0, 0, 0, 0), new Rectangle(0, 0, 100, 50))
                .invalidValues(new Rectangle(-1, -1, -1, -1), new Rectangle(0, 0, -1, -1))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[(0,1)(1919,1080)-(1919:1079)]", new Rectangle(0, 1, 1919, 1080)))
                .test();
    }

    @Test
    void networkErrorDialogTitle() {
        when(window.getText()).thenReturn(Optional.of("window title"));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.networkErrorDialogTitle)
                .settingGetter(targetLauncherSettings::networkErrorDialogTitle)
                .normalValues("string", "")
                .invalidValues()
                .wrongTypeValues()
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("test", "test"))
                .test();
    }

    @Test
    void networkErrorDialogDimensions() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.networkErrorDialogDimensions)
                .settingGetter(targetLauncherSettings::networkErrorDialogDimensions)
                .normalValues(new Rectangle(0, 0, 0, 0), new Rectangle(0, 0, 100, 50))
                .invalidValues(new Rectangle(-1, -1, -1, -1), new Rectangle(0, 0, -1, -1))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[(0,1)(1919,1080)-(1919:1079)]", new Rectangle(0, 1, 1919, 1080)))
                .test();
    }

    @Test
    void windowCloseButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.windowCloseButtonPoint)
                .settingGetter(targetLauncherSettings::windowCloseButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void desktopShortcutLocationPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 4, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.desktopShortcutLocationPoint)
                .settingGetter(targetLauncherSettings::desktopShortcutLocationPoint)
                .normalValues(new PointInt(0, 0), new PointInt(100, 50))
                .invalidValues(new PointInt(-1, 0), new PointInt(0, -1))
                .wrongTypeValues("string")
                .extractFrom(new PointInt(100, 50))
                .parseFrom(new SettingTester.ParsePair("[x=100, y=50]", new PointInt(100, 50)))
                .test();
    }

    @Test
    void closeQuitConfirmPopupButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 4, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.closeQuitConfirmPopupButtonPoint)
                .settingGetter(targetLauncherSettings::closeQuitConfirmPopupButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.53f, .35f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.7f, -.3f), new PointFloat(1.1f, 1.7f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void closeNetworkErrorDialogButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 4, 5));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.closeNetworkErrorDialogButtonPoint)
                .settingGetter(targetLauncherSettings::closeNetworkErrorDialogButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.55f, .35f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.7f, -.3f), new PointFloat(1.1f, 1.7f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void startButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 5, 5));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.startButtonPoint)
                .settingGetter(targetLauncherSettings::startButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.55f, .35f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.7f, -.3f), new PointFloat(1.1f, 1.7f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void accountDropListPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 6, 5));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.accountDropListPoint)
                .settingGetter(targetLauncherSettings::accountDropListPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.55f, .35f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.7f, -.3f), new PointFloat(1.1f, 1.7f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void accountLogoutPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 6, 10));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.accountLogoutPoint)
                .settingGetter(targetLauncherSettings::accountLogoutPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.55f, .35f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.7f, -.3f), new PointFloat(1.1f, 1.7f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void loginInputPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 6, 10));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.loginInputPoint)
                .settingGetter(targetLauncherSettings::loginInputPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.55f, .35f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.7f, -.3f), new PointFloat(1.1f, 1.7f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void passwordInputPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 15, 10));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.passwordInputPoint)
                .settingGetter(targetLauncherSettings::passwordInputPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.55f, .35f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.7f, -.3f), new PointFloat(1.1f, 1.7f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void loginButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 15, 10));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.loginButtonPoint)
                .settingGetter(targetLauncherSettings::loginButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.55f, .35f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.7f, -.3f), new PointFloat(1.1f, 1.7f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

}