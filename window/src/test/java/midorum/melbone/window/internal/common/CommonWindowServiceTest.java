package midorum.melbone.window.internal.common;

import com.midorum.win32api.facade.Win32System;
import dma.function.VoidActionThrowing;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.model.settings.setting.Settings;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class CommonWindowServiceTest {

    private final Win32System win32System = mock(Win32System.class);
    private final Settings settings = mock(Settings.class);
    private final PropertiesProvider propertiesProvider = mock(PropertiesProvider.class);

    private void doWithMockedWin32System(final VoidActionThrowing<InterruptedException> voidAction) throws InterruptedException {
        try (MockedStatic<Win32System> mockedStatic = mockStatic(Win32System.class)) {
            mockedStatic.when(Win32System::getInstance).thenReturn(win32System);
            voidAction.perform();
        }
    }

    @Test
    void testInstance() throws InterruptedException {
        doWithMockedWin32System(() -> assertNotNull(new CommonWindowService(settings, propertiesProvider).getWin32System()));
    }

}