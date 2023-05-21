package midorum.melbone.ui;

import midorum.melbone.executor.ExecutorFactory;
import midorum.melbone.model.experimental.task.TaskStorage;
import midorum.melbone.model.persistence.AccountStorage;
import midorum.melbone.model.persistence.SettingStorage;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.WindowFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class UserInterfaceTest {

    @Test
    void successfullyBuild() {
        final UserInterface userInterface = new UserInterface.Builder()
                .accountStorage(mock(AccountStorage.class))
                .taskStorage(mock(TaskStorage.class))
                .executorFactory(mock(ExecutorFactory.class))
                .settings(mock(Settings.class))
                .settingStorage(mock(SettingStorage.class))
                .windowFactory(mock(WindowFactory.class))
                .propertiesProvider(mock(PropertiesProvider.class))
                .build();
        assertNotNull(userInterface);
    }

    @Test
    void obtainSameInstanceOfMainForm() {
        final UserInterface userInterface = new UserInterface.Builder()
                .accountStorage(mock(AccountStorage.class))
                .taskStorage(mock(TaskStorage.class))
                .executorFactory(mock(ExecutorFactory.class))
                .settings(mock(Settings.class))
                .settingStorage(mock(SettingStorage.class))
                .windowFactory(mock(WindowFactory.class))
                .propertiesProvider(mock(PropertiesProvider.class))
                .build();
        final Displayable displayable1 = userInterface.mainForm();
        final Displayable displayable2 = userInterface.mainForm();
        assertSame(displayable1, displayable2);
    }

    @Test
    void failedWhenAccountStorageMissed() {
        assertThrows(IllegalArgumentException.class, () -> new UserInterface.Builder()
                .taskStorage(mock(TaskStorage.class))
                .executorFactory(mock(ExecutorFactory.class))
                .settings(mock(Settings.class))
                .settingStorage(mock(SettingStorage.class))
                .windowFactory(mock(WindowFactory.class))
                .propertiesProvider(mock(PropertiesProvider.class))
                .build());
    }

    @Test
    void failedWhenExecutorFactoryMissed() {
        assertThrows(IllegalArgumentException.class, () -> new UserInterface.Builder()
                .accountStorage(mock(AccountStorage.class))
                .taskStorage(mock(TaskStorage.class))
                .settings(mock(Settings.class))
                .settingStorage(mock(SettingStorage.class))
                .windowFactory(mock(WindowFactory.class))
                .propertiesProvider(mock(PropertiesProvider.class))
                .build());
    }

    @Test
    void failedWhenSettingsMissed() {
        assertThrows(IllegalArgumentException.class, () -> new UserInterface.Builder()
                .accountStorage(mock(AccountStorage.class))
                .taskStorage(mock(TaskStorage.class))
                .executorFactory(mock(ExecutorFactory.class))
                .settingStorage(mock(SettingStorage.class))
                .windowFactory(mock(WindowFactory.class))
                .propertiesProvider(mock(PropertiesProvider.class))
                .build());
    }

    @Test
    void failedWhenSettingStorageMissed() {
        assertThrows(IllegalArgumentException.class, () -> new UserInterface.Builder()
                .accountStorage(mock(AccountStorage.class))
                .taskStorage(mock(TaskStorage.class))
                .executorFactory(mock(ExecutorFactory.class))
                .settings(mock(Settings.class))
                .windowFactory(mock(WindowFactory.class))
                .propertiesProvider(mock(PropertiesProvider.class))
                .build());
    }

    @Test
    void failedWhenWindowFactoryMissed() {
        assertThrows(IllegalArgumentException.class, () -> new UserInterface.Builder()
                .accountStorage(mock(AccountStorage.class))
                .taskStorage(mock(TaskStorage.class))
                .executorFactory(mock(ExecutorFactory.class))
                .settings(mock(Settings.class))
                .settingStorage(mock(SettingStorage.class))
                .propertiesProvider(mock(PropertiesProvider.class))
                .build());
    }

    @Test
    void failedWhenPropertiesProviderMissed() {
        assertThrows(IllegalArgumentException.class, () -> new UserInterface.Builder()
                .accountStorage(mock(AccountStorage.class))
                .taskStorage(mock(TaskStorage.class))
                .executorFactory(mock(ExecutorFactory.class))
                .settings(mock(Settings.class))
                .settingStorage(mock(SettingStorage.class))
                .windowFactory(mock(WindowFactory.class))
                .build());
    }

}