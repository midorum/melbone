package midorum.melbone.settings.internal.setting;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.facade.WindowPoint;
import com.midorum.win32api.struct.PointFloat;
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
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("test", "test"))
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
                .extractFrom(new WindowPoint(window, new PointFloat(0.5f, 0.5f)))
                .parseFrom(new SettingTester.ParsePair("[(0,1)(1919,1080)-(1919:1079)]", new Rectangle(0, 1, 1919, 1080)))
                .test();
    }
}