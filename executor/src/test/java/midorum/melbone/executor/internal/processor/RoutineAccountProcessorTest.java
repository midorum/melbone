package midorum.melbone.executor.internal.processor;

import midorum.melbone.model.window.baseapp.BaseAppWindow;
import midorum.melbone.model.settings.setting.ApplicationSettings;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.WindowFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import java.util.*;

class RoutineAccountProcessorTest {

    private final Settings settingsMock = Mockito.mock(Settings.class);
    private final OnRunningAccountAction onRunningAccountActionMock = Mockito.mock(OnRunningAccountAction.class);
    private final LaunchAccountAction launchAccountActionMock = Mockito.mock(LaunchAccountAction.class);
    private final ApplicationSettings applicationSettingsMock = Mockito.mock(ApplicationSettings.class);
    private final WindowFactory windowFactory = Mockito.mock(WindowFactory.class);

    @BeforeAll
    public static void beforeAll() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @BeforeEach
    void beforeEach() {
        Mockito.when(settingsMock.application()).thenReturn(applicationSettingsMock);
        Mockito.when(applicationSettingsMock.randomRoutineDelayMax()).thenReturn(1L);
        Mockito.when(applicationSettingsMock.speedFactor()).thenReturn(0.01F);
    }

    @AfterEach
    public void afterEach() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @Test
    void runLaunchAccountActionOnce_inAnyCase() throws InterruptedException {
        System.out.println("runLaunchAccountActionOnce_inAnyCase");
        final VerificationMode countOfRunAction = Mockito.only();
        final RoutineAccountProcessor instance = new RoutineAccountProcessor(launchAccountActionMock, onRunningAccountActionMock, windowFactory, settingsMock);
        instance.run();
        Mockito.verify(launchAccountActionMock, countOfRunAction).perform();
    }

    @Test
    void runOnRunningAccountActionOnce_whenExistsBoundWindow() throws InterruptedException {
        System.out.println("runOnRunningAccountActionOnce_whenExistsBoundWindow");
        final List<BaseAppWindow> testBaseWindows = getNonEmptyTestBaseWindows();
        final VerificationMode countOfRunAction = Mockito.only();
        Mockito.when(windowFactory.getAllBaseAppWindows()).thenReturn(testBaseWindows);
        final RoutineAccountProcessor instance = new RoutineAccountProcessor(launchAccountActionMock, onRunningAccountActionMock, windowFactory, settingsMock);
        instance.run();
        Mockito.verify(onRunningAccountActionMock, countOfRunAction).perform();
    }

    @Test
    void neverRunOnRunningAccountAction_whenNotFoundBoundWindows() throws InterruptedException {
        System.out.println("neverRunOnRunningAccountAction_whenNotFoundBoundWindows");
        final List<BaseAppWindow> testBaseWindows = getNotBoundTestBaseWindows();
        final VerificationMode countOfRunAction = Mockito.never();
        Mockito.when(windowFactory.getAllBaseAppWindows()).thenReturn(testBaseWindows);
        final RoutineAccountProcessor instance = new RoutineAccountProcessor(launchAccountActionMock, onRunningAccountActionMock, windowFactory, settingsMock);
        instance.run();
        Mockito.verify(onRunningAccountActionMock, countOfRunAction).perform();
    }

    @Test
    void neverRunOnRunningAccountAction_whenNotFoundWindows() throws InterruptedException {
        System.out.println("neverRunOnRunningAccountAction_whenNotFoundWindows");
        final List<BaseAppWindow> testBaseWindows = getEmptyTestBaseWindows();
        final VerificationMode countOfRunAction = Mockito.never();
        Mockito.when(windowFactory.getAllBaseAppWindows()).thenReturn(testBaseWindows);
        final RoutineAccountProcessor instance = new RoutineAccountProcessor(launchAccountActionMock, onRunningAccountActionMock, windowFactory, settingsMock);
        instance.run();
        Mockito.verify(onRunningAccountActionMock, countOfRunAction).perform();
    }

    private List<BaseAppWindow> getNonEmptyTestBaseWindows() {
        return Arrays.asList(getBaseAppWindow("test"), getBaseAppWindow(null));
    }

    private List<BaseAppWindow> getNotBoundTestBaseWindows() {
        return Arrays.asList(getBaseAppWindow(null), getBaseAppWindow(null));
    }

    @SuppressWarnings("unchecked")
    private List<BaseAppWindow> getEmptyTestBaseWindows() {
        return Collections.EMPTY_LIST;
    }

    private BaseAppWindow getBaseAppWindow(final String characterName) {
        final BaseAppWindow mock = Mockito.mock(BaseAppWindow.class);
        Mockito.when(mock.getCharacterName()).thenReturn(Optional.ofNullable(characterName));
        return mock;
    }

}