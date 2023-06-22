package midorum.melbone.settings.internal.stamp;

import com.midorum.win32api.facade.Rectangle;
import midorum.melbone.model.persistence.SettingStorage;
import midorum.melbone.model.persistence.StorageKey;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.model.settings.stamp.TargetBaseAppStamps;
import midorum.melbone.settings.StampKeys;
import midorum.melbone.settings.internal.management.SettingsFactoryInternal;
import midorum.melbone.settings.internal.storage.KeyValueStorage;
import midorum.melbone.settings.managment.SettingPropertyNaming;
import midorum.melbone.settings.managment.StampBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TargetBaseAppStampsImplTest {

    private final SettingsFactoryInternal settingsFactoryInternal = new SettingsFactoryInternal.Builder()
            .settingPropertyNaming(new SettingPropertyNaming.Builder().inMemoryStorage().build()).build();
    private final TargetBaseAppStamps targetBaseAppStamps = settingsFactoryInternal.settingsProvider().stamps().targetBaseApp();
    private final KeyValueStorage internalStorage = settingsFactoryInternal.getKeyValueStorage();
    private final SettingStorage settingStorage = settingsFactoryInternal.settingStorage();

    @AfterEach
    void afterEach() {
        printKeysInStorage();
    }

    private void printKeysInStorage() {
        System.out.println("Keys in storage:");
        internalStorage.getKeySet(StorageKey.targetBaseAppStamp)
                .forEach(key -> {
                    final Optional<Object> value = internalStorage.read(StorageKey.targetBaseAppStamp, key);
                    System.out.println("key: " + key + "\nvalue: " + value);
                });
        System.out.println();
    }

    @Disabled
    @Test
    void testStampSerialization() throws IOException, ClassNotFoundException {
        final String fileName = "stamp.ser";

        ////try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
        //    final StampImpl stampToSerialize = createStamp(StampKeys.TargetBaseApp.manaIndicator);
        //    System.out.println("stampToSerialize: " + stampToSerialize);
        //    out.writeObject(stampToSerialize);
        //}

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            final Stamp deserializedStamp = (Stamp) in.readObject();
            System.out.println("deserializedStamp: " + deserializedStamp);
        }
    }

    @Test
    void menuExitOption() {
        //storing stamp
        settingStorage.write(StampKeys.TargetBaseApp.menuExitOption,
                createStamp(StampKeys.TargetBaseApp.menuExitOption));
        //reading stamp
        final Stamp stamp = targetBaseAppStamps.menuExitOption();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetBaseApp.menuExitOption, stamp.key());
    }

    @Test
    void optionsPopupCaption() {
        //storing stamp
        settingStorage.write(StampKeys.TargetBaseApp.optionsPopupCaption,
                createStamp(StampKeys.TargetBaseApp.optionsPopupCaption));
        //reading stamp
        final Stamp stamp = targetBaseAppStamps.optionsPopupCaption();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetBaseApp.optionsPopupCaption, stamp.key());
    }

    @Test
    void optionsButtonDefaultScale() {
        //storing stamp
        settingStorage.write(StampKeys.TargetBaseApp.optionsButtonDefaultScale,
                createStamp(StampKeys.TargetBaseApp.optionsButtonDefaultScale));
        //reading stamp
        final Stamp stamp = targetBaseAppStamps.optionsButtonDefaultScale();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetBaseApp.optionsButtonDefaultScale, stamp.key());
    }

    @Test
    void optionsButtonBaseScale() {
        //storing stamp
        settingStorage.write(StampKeys.TargetBaseApp.optionsButtonBaseScale,
                createStamp(StampKeys.TargetBaseApp.optionsButtonBaseScale));
        //reading stamp
        final Stamp stamp = targetBaseAppStamps.optionsButtonBaseScale();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetBaseApp.optionsButtonBaseScale, stamp.key());
    }

    @Test
    void startButton() {
        //storing stamp
        settingStorage.write(StampKeys.TargetBaseApp.startButton,
                createStamp(StampKeys.TargetBaseApp.startButton));
        //reading stamp
        final Stamp stamp = targetBaseAppStamps.startButton();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetBaseApp.startButton, stamp.key());
    }

    @Test
    void serverLineSelected() {
        //storing stamp
        settingStorage.write(StampKeys.TargetBaseApp.serverLineSelected,
                createStamp(StampKeys.TargetBaseApp.serverLineSelected));
        //reading stamp
        final Stamp stamp = targetBaseAppStamps.serverLineSelected();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetBaseApp.serverLineSelected, stamp.key());
    }

    @Test
    void serverLineUnselected() {
        //storing stamp
        settingStorage.write(StampKeys.TargetBaseApp.serverLineUnselected,
                createStamp(StampKeys.TargetBaseApp.serverLineUnselected));
        //reading stamp
        final Stamp stamp = targetBaseAppStamps.serverLineUnselected();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetBaseApp.serverLineUnselected, stamp.key());
    }

    @Test
    void needRestartPopup() {
        //storing stamp
        settingStorage.write(StampKeys.TargetBaseApp.needRestartPopup,
                createStamp(StampKeys.TargetBaseApp.needRestartPopup));
        //reading stamp
        final Stamp stamp = targetBaseAppStamps.needRestartPopup();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetBaseApp.needRestartPopup, stamp.key());
    }

    @Test
    void disconnectedPopup() {
        //storing stamp
        settingStorage.write(StampKeys.TargetBaseApp.disconnectedPopup,
                createStamp(StampKeys.TargetBaseApp.disconnectedPopup));
        //reading stamp
        final Stamp stamp = targetBaseAppStamps.disconnectedPopup();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetBaseApp.disconnectedPopup, stamp.key());
    }

    @Test
    void dailyTrackerPopupCaption() {
        //storing stamp
        settingStorage.write(StampKeys.TargetBaseApp.dailyTrackerPopupCaption,
                createStamp(StampKeys.TargetBaseApp.dailyTrackerPopupCaption));
        //reading stamp
        final Stamp stamp = targetBaseAppStamps.dailyTrackerPopupCaption();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetBaseApp.dailyTrackerPopupCaption, stamp.key());
    }

    private Stamp createStamp(final StampKeys.TargetBaseApp key) {
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