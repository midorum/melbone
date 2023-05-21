package midorum.melbone.settings.internal.setting;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.struct.PointInt;
import midorum.melbone.model.settings.key.WindowHolder;
import midorum.melbone.model.settings.setting.UacSettings;
import midorum.melbone.settings.SettingKeys;
import midorum.melbone.settings.internal.management.SettingsFactoryInternal;
import midorum.melbone.settings.managment.SettingPropertyNaming;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UacSettingsImplTest {

    private final SettingsFactoryInternal settingsFactoryInternal = new SettingsFactoryInternal.Builder()
            .settingPropertyNaming(new SettingPropertyNaming.Builder().inMemoryStorage().build()).build();
    private final UacSettings uacSettings = settingsFactoryInternal.settingsProvider().settings().uacSettings();

    private final IWindow window = mock(IWindow.class);

    @Test
    void windowClassName() {
        when(window.getClassName()).thenReturn(Optional.of("window class name"));
        new SettingTester(settingsFactoryInternal, SettingKeys.Uac.windowClassName)
                .settingGetter(uacSettings::windowClassName)
                .normalValues("string", "")
                .invalidValues()
                .wrongTypeValues()
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(-1, -1)))
                .test();
    }

    @Test
    void windowDimensions() {
        when(window.getWindowRectangle()).thenReturn(new Rectangle(1, 2, 3, 4));
        new SettingTester(settingsFactoryInternal, SettingKeys.Uac.windowDimensions)
                .settingGetter(uacSettings::windowDimensions)
                .normalValues(new Rectangle(0, 0, 0, 0), new Rectangle(0, 0, 100, 50))
                .invalidValues(new Rectangle(-1, -1, -1, -1), new Rectangle(0, 0, -1, -1))
                .wrongTypeValues("string")
                .extractors(new SettingTester.ExtractorParameter(new WindowHolder(window), new PointInt(-1, -1)))
                .test();
    }
}