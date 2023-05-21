package midorum.melbone.settings.internal.setting;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.facade.Win32System;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;
import com.midorum.win32api.struct.PointLong;
import midorum.melbone.model.exception.CriticalErrorException;
import midorum.melbone.model.settings.key.WindowHolder;
import midorum.melbone.model.settings.setting.TargetLauncherSettings;
import midorum.melbone.settings.SettingKeys;
import midorum.melbone.settings.internal.management.SettingsFactoryInternal;
import midorum.melbone.settings.managment.SettingPropertyNaming;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(-1, -1)))
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
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(-1, -1)))
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
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(-1, -1)))
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
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(-1, -1)))
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
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(-1, -1)))
                .test();
    }

    @Test
    void initializationErrorDialogTitle() {
        when(window.getText()).thenReturn(Optional.of("window title"));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.initializationErrorDialogTitle)
                .settingGetter(targetLauncherSettings::initializationErrorDialogTitle)
                .normalValues("string", "")
                .invalidValues()
                .wrongTypeValues()
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(-1, -1)))
                .test();
    }

    @Test
    void initializationErrorDialogDimensions() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.initializationErrorDialogDimensions)
                .settingGetter(targetLauncherSettings::initializationErrorDialogDimensions)
                .normalValues(new Rectangle(0, 0, 0, 0), new Rectangle(0, 0, 100, 50))
                .invalidValues(new Rectangle(-1, -1, -1, -1), new Rectangle(0, 0, -1, -1))
                .wrongTypeValues("string")
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(-1, -1)))
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
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(2, 3)))
                .test();
    }

    @Test
    void desktopShortcutLocationAbsolutePoint() {
        final Win32System win32System = mock(Win32System.class);
        when(win32System.getAbsoluteScreenPoint(any(PointInt.class))).thenAnswer(invocation -> {
            assertEquals(1, invocation.getArguments().length);
            return invocation.getArgument(0, PointInt.class); //simply pass through
        });
        try (MockedStatic<Win32System> mockedStatic = mockStatic(Win32System.class)) {
            mockedStatic.when(Win32System::getInstance).thenReturn(win32System);
            new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.desktopShortcutLocationAbsolutePoint)
                    .settingGetter(targetLauncherSettings::desktopShortcutLocationAbsolutePoint)
                    .normalValues(new PointLong(0L, 0L), new PointLong(1000L, 2000L))
                    .invalidValues(new PointFloat(-1L, -1L))
                    .wrongTypeValues("string")
                    .extractors(new SettingTester.ExtractorParameter(WindowHolder.EMPTY, new PointInt(1024, 768)))
                    .test();
        }
    }

    @Test
    void closeQuitConfirmPopupButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 4, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.closeQuitConfirmPopupButtonPoint)
                .settingGetter(targetLauncherSettings::closeQuitConfirmPopupButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.53f, .35f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.7f, -.3f), new PointFloat(1.1f, 1.7f))
                .wrongTypeValues("string")
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(3, 3)))
                .test();
    }

    @Test
    void closeInitializationErrorDialogButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 4, 5));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetLauncher.closeInitializationErrorDialogButtonPoint)
                .settingGetter(targetLauncherSettings::closeInitializationErrorDialogButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.55f, .35f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.7f, -.3f), new PointFloat(1.1f, 1.7f))
                .wrongTypeValues("string")
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(3, 4)))
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
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(4, 4)))
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
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(5, 4)))
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
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(5, 3)))
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
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(5, 6)))
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
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(7, 6)))
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
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(8, 6)))
                .test();
    }

}