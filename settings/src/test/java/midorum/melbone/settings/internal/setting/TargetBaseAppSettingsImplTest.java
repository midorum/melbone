package midorum.melbone.settings.internal.setting;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.facade.WindowPoint;
import com.midorum.win32api.struct.PointFloat;
import midorum.melbone.model.settings.setting.TargetBaseAppSettings;
import midorum.melbone.settings.SettingKeys;
import midorum.melbone.settings.internal.management.SettingsFactoryInternal;
import midorum.melbone.settings.managment.SettingPropertyNaming;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TargetBaseAppSettingsImplTest {

    private final SettingsFactoryInternal settingsFactoryInternal = new SettingsFactoryInternal.Builder()
            .settingPropertyNaming(new SettingPropertyNaming.Builder().inMemoryStorage().build()).build();
    private final TargetBaseAppSettings targetBaseAppSettings = settingsFactoryInternal.settingsProvider().settings().targetBaseAppSettings();

    private final IWindow window = mock(IWindow.class);

    @Test
    void windowTitle() {
        when(window.getText()).thenReturn(Optional.of("window title"));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.windowTitle)
                .settingGetter(targetBaseAppSettings::windowTitle)
                .normalValues("string", "")
                .invalidValues()
                .wrongTypeValues()
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("test", "test"))
                .test();
    }

    @Test
    void windowClassName() {
        when(window.getClassName()).thenReturn(Optional.of("window class name"));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.windowClassName)
                .settingGetter(targetBaseAppSettings::windowClassName)
                .normalValues("string", "")
                .invalidValues()
                .wrongTypeValues()
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("test", "test"))
                .test();
    }

    @Test
    void menuExitOptionPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.menuExitOptionPoint)
                .settingGetter(targetBaseAppSettings::menuExitOptionPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void manaIndicatorPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.manaIndicatorPoint)
                .settingGetter(targetBaseAppSettings::manaIndicatorPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void windowMinimizeButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.windowMinimizeButtonPoint)
                .settingGetter(targetBaseAppSettings::windowMinimizeButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void disconnectedPopupCloseButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.disconnectedPopupCloseButtonPoint)
                .settingGetter(targetBaseAppSettings::disconnectedPopupCloseButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void windowCloseButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.windowCloseButtonPoint)
                .settingGetter(targetBaseAppSettings::windowCloseButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void screenSettingsTabPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.screenSettingsTabPoint)
                .settingGetter(targetBaseAppSettings::screenSettingsTabPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void uiScaleChooser80Point() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.uiScaleChooser80Point)
                .settingGetter(targetBaseAppSettings::uiScaleChooser80Point)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void soundSettingsTabPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.soundSettingsTabPoint)
                .settingGetter(targetBaseAppSettings::soundSettingsTabPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void overallVolumeZeroLevelPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.overallVolumeZeroLevelPoint)
                .settingGetter(targetBaseAppSettings::overallVolumeZeroLevelPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void optionsApplyButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.optionsApplyButtonPoint)
                .settingGetter(targetBaseAppSettings::optionsApplyButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void needRestartPopupConfirmButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.needRestartPopupConfirmButtonPoint)
                .settingGetter(targetBaseAppSettings::needRestartPopupConfirmButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void openOptionsButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.openOptionsButtonPoint)
                .settingGetter(targetBaseAppSettings::openOptionsButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void selectServerButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.selectServerButtonPoint)
                .settingGetter(targetBaseAppSettings::selectServerButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void connectServerButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.connectServerButtonPoint)
                .settingGetter(targetBaseAppSettings::connectServerButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void selectCharacterButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.selectCharacterButtonPoint)
                .settingGetter(targetBaseAppSettings::selectCharacterButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void startButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.startButtonPoint)
                .settingGetter(targetBaseAppSettings::startButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void dailyTrackerButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.dailyTrackerButtonPoint)
                .settingGetter(targetBaseAppSettings::dailyTrackerButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void dailyTrackerTabPointer() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.dailyTrackerTabPoint)
                .settingGetter(targetBaseAppSettings::dailyTrackerTabPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void trackLoginButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.trackLoginButtonPoint)
                .settingGetter(targetBaseAppSettings::trackLoginButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void closeDailyTrackerPopupButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.closeDailyTrackerPopupButtonPoint)
                .settingGetter(targetBaseAppSettings::closeDailyTrackerPopupButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void actionButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.actionButtonPoint)
                .settingGetter(targetBaseAppSettings::actionButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

    @Test
    void actionSecondButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetBaseApp.actionSecondButtonPoint)
                .settingGetter(targetBaseAppSettings::actionSecondButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[x=0.5, y=0.75]", new PointFloat(.5f, .75f)))
                .test();
    }

}