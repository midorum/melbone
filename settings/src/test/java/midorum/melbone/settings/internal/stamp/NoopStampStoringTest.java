package midorum.melbone.settings.internal.stamp;

import com.midorum.win32api.facade.Rectangle;
import midorum.melbone.model.persistence.SettingStorage;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.settings.StampKeys;
import midorum.melbone.settings.internal.management.SettingsFactoryInternal;
import midorum.melbone.settings.managment.SettingPropertyNaming;
import midorum.melbone.settings.managment.StampBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class NoopStampStoringTest {

    private final SettingsFactoryInternal settingsFactoryInternal = new SettingsFactoryInternal.Builder()
            .settingPropertyNaming(new SettingPropertyNaming.Builder().inMemoryStorage().build()).build();
    private final SettingStorage settingStorage = settingsFactoryInternal.settingStorage();

    @Test
    void storingNoopStamp() {
        //storing Noop stamp
        assertThrows(IllegalArgumentException.class,
                () -> settingStorage.write(StampKeys.Noop.noop, createStamp(StampKeys.Noop.noop)));
    }

    private Stamp createStamp(final StampKeys.Noop key) {
        return new StampBuilder()
                .key(key)
                .description(key.internal().description())
                .wholeData(new int[]{0, 1})
                .firstLine(new int[]{0, 1})
                .location(new Rectangle(0, 0, 1, 1))
                .windowRect(new Rectangle(0, 0, 1, 1))
                .windowClientRect(new Rectangle(0, 0, 1, 1))
                .windowClientToScreenRect(new Rectangle(0, 0, 1, 1))
                .build();
    }
}