package midorum.melbone.executor.internal.processor;

import midorum.melbone.model.dto.Account;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.WindowFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotSame;

class AccountProcessorFactoryTest {

    private final Settings settingsMock = Mockito.mock(Settings.class);
    private final WindowFactory windowFactory = Mockito.mock(WindowFactory.class);

    @BeforeAll
    public static void beforeAll() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @AfterEach
    public void afterEach() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @Test
    void newInstanceRoutineProcessorEachTime() {
        System.out.println("newInstanceRoutineProcessorEachTime");
        final Account[] accounts = {};
        final Runnable first = AccountProcessorFactory.INSTANCE.routineProcessor(accounts, windowFactory, settingsMock);
        final Runnable second = AccountProcessorFactory.INSTANCE.routineProcessor(accounts, windowFactory, settingsMock);
        assertNotSame(first, second);
    }
}