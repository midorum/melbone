package midorum.melbone.main;

import midorum.melbone.executor.ExecutorFactory;
import midorum.melbone.model.experimental.task.TaskStorage;
import midorum.melbone.model.persistence.AccountStorage;
import midorum.melbone.model.persistence.SettingStorage;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.ui.Displayable;
import midorum.melbone.ui.UserInterface;
import midorum.melbone.window.WindowFactory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

class ApplicationTest {

    @Test
    void testAccessibility() {
        final Displayable displayable = new UserInterface.Builder()
                .executorFactory(mock(ExecutorFactory.class))
                .settings(mock(Settings.class))
                .settingStorage(mock(SettingStorage.class))
                .accountStorage(mock(AccountStorage.class))
                .taskStorage(mock(TaskStorage.class))
                .windowFactory(mock(WindowFactory.class))
                .propertiesProvider(mock(PropertiesProvider.class))
                .build().mainForm();
        final Class<? extends Displayable> displayableClass = displayable.getClass();
        System.out.println(displayableClass);
        for (Field field : displayableClass.getDeclaredFields()) {
            final int modifiers = field.getModifiers();
            System.out.println("field: " + Modifier.toString(modifiers) + " " + field.getName() + ":" + field.getType());
            if (Modifier.isPublic(modifiers)) continue;
            assertFalse(field.trySetAccessible());
        }
        for (Method method : displayableClass.getDeclaredMethods()) {
            final int modifiers = method.getModifiers();
            System.out.println("method: " + Modifier.toString(modifiers) + " " + method.getName() + "(" + getParametersString(method) + "):" + method.getReturnType());
            if (Modifier.isPublic(modifiers)) continue;
            assertFalse(method.trySetAccessible());
        }
    }

    private String getParametersString(final Method method) {
        return Arrays.stream(method.getParameters()).map(Parameter::getType).map(Objects::toString).reduce((s, s2) -> s + "," + s2).orElse("");
    }

}