package midorum.melbone.settings.internal.stamp;

import com.midorum.win32api.facade.Rectangle;
import midorum.melbone.model.persistence.SettingStorage;
import midorum.melbone.model.persistence.StorageKey;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.model.settings.stamp.TargetLauncherStamps;
import midorum.melbone.settings.StampKeys;
import midorum.melbone.settings.internal.management.SettingsFactoryInternal;
import midorum.melbone.settings.internal.storage.KeyValueStorage;
import midorum.melbone.settings.managment.SettingPropertyNaming;
import midorum.melbone.settings.managment.StampBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TargetLauncherStampsImplTest {

    private final SettingsFactoryInternal settingsFactoryInternal = new SettingsFactoryInternal.Builder()
            .settingPropertyNaming(new SettingPropertyNaming.Builder().inMemoryStorage().build()).build();
    private final TargetLauncherStamps targetLauncherStamps = settingsFactoryInternal.settingsProvider().stamps().targetLauncher();
    private final KeyValueStorage internalStorage = settingsFactoryInternal.getKeyValueStorage();
    private final SettingStorage settingStorage = settingsFactoryInternal.settingStorage();

    @AfterEach
    void afterEach() {
        printKeysInStorage();
    }

    private void printKeysInStorage() {
        System.out.println("Keys in storage:");
        internalStorage.getKeySet(StorageKey.targetLauncherStamp)
                .forEach(key -> {
                    final Optional<Object> value = internalStorage.read(StorageKey.targetLauncherStamp, key);
                    System.out.println("key: " + key + "\nvalue: " + value);
                });
        System.out.println();
    }

    @Test
    void clientIsAlreadyRunning() {
        //storing stamp
        settingStorage.write(StampKeys.TargetLauncher.clientIsAlreadyRunning,
                createStamp(StampKeys.TargetLauncher.clientIsAlreadyRunning));
        //reading stamp
        final Stamp stamp = targetLauncherStamps.clientIsAlreadyRunning();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetLauncher.clientIsAlreadyRunning, stamp.key());
    }

    @Test
    void quitConfirmPopup() {
        //storing stamp
        settingStorage.write(StampKeys.TargetLauncher.quitConfirmPopup,
                createStamp(StampKeys.TargetLauncher.quitConfirmPopup));
        //reading stamp
        final Stamp stamp = targetLauncherStamps.quitConfirmPopup();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetLauncher.quitConfirmPopup, stamp.key());
    }

    @Test
    void initializationErrorDialog() {
        //storing stamp
        settingStorage.write(StampKeys.TargetLauncher.initializationErrorDialog,
                createStamp(StampKeys.TargetLauncher.initializationErrorDialog));
        //reading stamp
        final Stamp stamp = targetLauncherStamps.initializationErrorDialog();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetLauncher.initializationErrorDialog, stamp.key());
    }

    @Test
    void loginButtonNoErrorActive() {
        //storing stamp
        settingStorage.write(StampKeys.TargetLauncher.loginButtonNoErrorActive,
                createStamp(StampKeys.TargetLauncher.loginButtonNoErrorActive));
        //reading stamp
        final Stamp stamp = targetLauncherStamps.loginButtonNoErrorActive();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetLauncher.loginButtonNoErrorActive, stamp.key());
    }

    @Test
    void loginButtonNoErrorInactive() {
        //storing stamp
        settingStorage.write(StampKeys.TargetLauncher.loginButtonNoErrorInactive,
                createStamp(StampKeys.TargetLauncher.loginButtonNoErrorInactive));
        //reading stamp
        final Stamp stamp = targetLauncherStamps.loginButtonNoErrorInactive();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetLauncher.loginButtonNoErrorInactive, stamp.key());
    }

    @Test
    void loginButtonWithErrorActive() {
        //storing stamp
        settingStorage.write(StampKeys.TargetLauncher.loginButtonWithErrorActive,
                createStamp(StampKeys.TargetLauncher.loginButtonWithErrorActive));
        //reading stamp
        final Stamp stamp = targetLauncherStamps.loginButtonWithErrorActive();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetLauncher.loginButtonWithErrorActive, stamp.key());
    }

    @Test
    void loginButtonWithErrorInactive() {
        //storing stamp
        settingStorage.write(StampKeys.TargetLauncher.loginButtonWithErrorInactive,
                createStamp(StampKeys.TargetLauncher.loginButtonWithErrorInactive));
        //reading stamp
        final Stamp stamp = targetLauncherStamps.loginButtonWithErrorInactive();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetLauncher.loginButtonWithErrorInactive, stamp.key());
    }

    @Test
    void errorExclamationSign() {
        //storing stamp
        settingStorage.write(StampKeys.TargetLauncher.errorExclamationSign,
                createStamp(StampKeys.TargetLauncher.errorExclamationSign));
        //reading stamp
        final Stamp stamp = targetLauncherStamps.errorExclamationSign();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetLauncher.errorExclamationSign, stamp.key());
    }

    @Test
    void playButtonActive() {
        //storing stamp
        settingStorage.write(StampKeys.TargetLauncher.playButtonActive,
                createStamp(StampKeys.TargetLauncher.playButtonActive));
        //reading stamp
        final Stamp stamp = targetLauncherStamps.startButtonActive();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetLauncher.playButtonActive, stamp.key());
    }

    @Test
    void playButtonInactive() {
        //storing stamp
        settingStorage.write(StampKeys.TargetLauncher.playButtonInactive,
                createStamp(StampKeys.TargetLauncher.playButtonInactive));
        //reading stamp
        final Stamp stamp = targetLauncherStamps.startButtonInactive();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetLauncher.playButtonInactive, stamp.key());
    }

    @Test
    void maintenanceNotice() {
        //storing stamp
        settingStorage.write(StampKeys.TargetLauncher.maintenanceNotice,
                createStamp(StampKeys.TargetLauncher.maintenanceNotice));
        //reading stamp
        final Stamp stamp = targetLauncherStamps.maintenanceNotice();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetLauncher.maintenanceNotice, stamp.key());
    }

    @Test
    void maintenanceInfoSign() {
        //storing stamp
        settingStorage.write(StampKeys.TargetLauncher.maintenanceInfoSign,
                createStamp(StampKeys.TargetLauncher.maintenanceInfoSign));
        //reading stamp
        final Stamp stamp = targetLauncherStamps.maintenanceInfoSign();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetLauncher.maintenanceInfoSign, stamp.key());
    }

    @Test
    void maintenanceInProgressPopup() {
        //storing stamp
        settingStorage.write(StampKeys.TargetLauncher.maintenanceInProgressPopup,
                createStamp(StampKeys.TargetLauncher.maintenanceInProgressPopup));
        //reading stamp
        final Stamp stamp = targetLauncherStamps.maintenanceInProgressPopup();
        assertNotNull(stamp);
        assertEquals(StampKeys.TargetLauncher.maintenanceInProgressPopup, stamp.key());
    }

    private Stamp createStamp(final StampKeys.TargetLauncher key) {
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