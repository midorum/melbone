package midorum.melbone.settings.internal.setting;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;
import midorum.melbone.model.settings.key.WindowHolder;
import midorum.melbone.model.settings.setting.TargetCountControlSettings;
import midorum.melbone.settings.SettingKeys;
import midorum.melbone.settings.internal.management.SettingsFactoryInternal;
import midorum.melbone.settings.managment.SettingPropertyNaming;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TargetCountControlSettingsImplTest {

    private final SettingsFactoryInternal settingsFactoryInternal = new SettingsFactoryInternal.Builder()
            .settingPropertyNaming(new SettingPropertyNaming.Builder().inMemoryStorage().build()).build();
    private final TargetCountControlSettings targetCountControlSettings = settingsFactoryInternal.settingsProvider().settings().targetCountControl();

    private final IWindow window = mock(IWindow.class);

    @Test
    void windowTimeout() {
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetCountControl.windowTimeout)
                .settingGetter(targetCountControlSettings::windowTimeout)
                .normalValues(0L, 1L, 5L)
                .invalidValues(-1L)
                .wrongTypeValues("string")
                .extractors()
                .test();
    }

    @Test
    void windowTitle() {
        when(window.getText()).thenReturn(Optional.of("window title"));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetCountControl.windowTitle)
                .settingGetter(targetCountControlSettings::windowTitle)
                .normalValues("string", "")
                .invalidValues()
                .wrongTypeValues()
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(-1, -1)))
                .test();
    }

    @Test
    void confirmButtonPoint() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.TargetCountControl.confirmButtonPoint)
                .settingGetter(targetCountControlSettings::confirmButtonPoint)
                .normalValues(new PointFloat(0f, 0f), new PointFloat(.5f, .3f), new PointFloat(1f, 1f))
                .invalidValues(new PointFloat(-.5f, -.3f), new PointFloat(1.1f, 1.6f))
                .wrongTypeValues("string")
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(2, 3)))
                .test();
    }
}