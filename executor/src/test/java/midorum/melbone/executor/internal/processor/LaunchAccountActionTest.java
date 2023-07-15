package midorum.melbone.executor.internal.processor;

import com.midorum.win32api.facade.Either;
import com.midorum.win32api.facade.exception.Win32ApiException;
import midorum.melbone.model.dto.Account;
import midorum.melbone.model.exception.CriticalErrorException;
import midorum.melbone.model.exception.NeedRetryException;
import midorum.melbone.model.settings.setting.TargetBaseAppSettings;
import midorum.melbone.model.window.WindowConsumer;
import midorum.melbone.model.window.baseapp.BaseAppWindow;
import midorum.melbone.model.window.launcher.LauncherWindow;
import midorum.melbone.model.window.baseapp.RestoredBaseAppWindow;
import midorum.melbone.model.window.launcher.RestoredLauncherWindow;
import midorum.melbone.model.settings.setting.ApplicationSettings;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.setting.TargetCountControlSettings;
import midorum.melbone.window.WindowFactory;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class LaunchAccountActionTest {

    public static final String TEST_ACCOUNT_FOR_CLOSE = "test_account_for_close";
    public static final String TEST_ACCOUNT_TO_LAUNCH_1 = "test_account_1";
    public static final String TEST_ACCOUNT_TO_LAUNCH_2 = "test_account_2";
    public static final String TEST_ACCOUNT_TO_LAUNCH_3 = "test_account_3";
    public static final String TEST_ACCOUNT_TO_LAUNCH_4 = "test_account_4";
    public static final String TEST_ACCOUNT_NOT_BOUND_BEFORE = null;

    private final Settings settings = Mockito.mock(Settings.class);
    private final ApplicationSettings applicationSettings = Mockito.mock(ApplicationSettings.class);
    private final TargetCountControlSettings targetCountControlSettings = Mockito.mock(TargetCountControlSettings.class);
    private final TargetBaseAppSettings targetBaseAppSettings = Mockito.mock(TargetBaseAppSettings.class);
    private final WindowFactory windowFactory = Mockito.mock(WindowFactory.class);
    private final MockitoUtil mockitoUtil = MockitoUtil.INSTANCE;

    @BeforeAll
    public static void beforeAll() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @BeforeEach
    void beforeEach() {
        when(settings.application()).thenReturn(applicationSettings);
        when(settings.targetCountControl()).thenReturn(targetCountControlSettings);
        when(settings.targetBaseAppSettings()).thenReturn(targetBaseAppSettings);
        when(targetBaseAppSettings.windowAppearingLatency()).thenReturn(100);
        when(targetBaseAppSettings.windowAppearingDelay()).thenReturn(100);
        when(targetBaseAppSettings.windowAppearingTimeout()).thenReturn(1_000);
    }

    @AfterEach
    public void afterEach() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @SuppressWarnings("unchecked")
    @Test
    void allAccountsAlreadyLaunched_checkHealthIsOn() throws InterruptedException, Win32ApiException {
        System.out.println("allAccountsAlreadyLaunched_checkHealthIsOn");
        when(applicationSettings.checkHealthBeforeLaunch()).thenReturn(true);
        final Account[] testAccountsToLaunch = getTestAccountsToLaunch();
        final List<BaseAppWindow> testWindowsForCheck = mockTestBaseAppWindows(getTestBoundWindowsForCheck(TEST_ACCOUNT_TO_LAUNCH_1, TEST_ACCOUNT_TO_LAUNCH_2));
        final LaunchAccountAction instance = new LaunchAccountAction(testAccountsToLaunch, windowFactory, settings);
        instance.perform();
        testWindowsForCheck.forEach(baseAppWindow -> {
            try {
                Mockito.verify(baseAppWindow, Mockito.times(1)).doInGameWindow(Mockito.any(WindowConsumer.class));
            } catch (InterruptedException | Win32ApiException e) {
                throw new IllegalStateException(e);
            }
        });
        Mockito.verify(windowFactory, Mockito.never()).findOrTryStartLauncherWindow();
    }

    @SuppressWarnings("unchecked")
    @Test
    void allAccountsAlreadyLaunched_checkHealthIsOff() throws InterruptedException, Win32ApiException {
        System.out.println("allAccountsAlreadyLaunched_checkHealthIsOn");
        when(applicationSettings.checkHealthBeforeLaunch()).thenReturn(false);
        final Account[] testAccountsToLaunch = getTestAccountsToLaunch();
        final List<BaseAppWindow> testWindowsForCheck = mockTestBaseAppWindows(getTestBoundWindowsForCheck(TEST_ACCOUNT_TO_LAUNCH_1, TEST_ACCOUNT_TO_LAUNCH_2));
        final LaunchAccountAction instance = new LaunchAccountAction(testAccountsToLaunch, windowFactory, settings);
        instance.perform();
        testWindowsForCheck.forEach(baseAppWindow -> {
            try {
                Mockito.verify(baseAppWindow, Mockito.never()).doInGameWindow(Mockito.any(WindowConsumer.class));
            } catch (InterruptedException | Win32ApiException e) {
                throw new IllegalStateException(e);
            }
        });
        Mockito.verify(windowFactory, Mockito.never()).findOrTryStartLauncherWindow();
    }

    @Test
    @SuppressWarnings("unchecked")
    void closeUnboundWindowsAndLaunchNewAccounts_launcherNotFound() throws InterruptedException, Win32ApiException {
        System.out.println("closeUnboundWindowsAndLaunchNewAccounts_launcherNotFound");
        final List<BaseAppWindow> testBoundWindowsForCheck = getTestBoundWindowsForCheck(TEST_ACCOUNT_TO_LAUNCH_1);
        final List<BaseAppWindow> testUnboundWindowsForClose = getTestUnboundWindowsForClose(TEST_ACCOUNT_FOR_CLOSE, TEST_ACCOUNT_NOT_BOUND_BEFORE);
        final List<BaseAppWindow> existWindows = mergeLists(testBoundWindowsForCheck, testUnboundWindowsForClose);
        when(windowFactory.getAllBaseAppWindows()).thenReturn(existWindows).thenReturn(testBoundWindowsForCheck);
        mockLauncherWindowForNotFoundCase();
        mockTargetProcessesLimitNotReached();
        final LaunchAccountAction instance = new LaunchAccountAction(getTestAccountsToLaunch(), windowFactory, settings);
        assertThrows(NeedRetryException.class, instance::perform);
        existWindows.forEach(baseAppWindow -> {
            try {
                if (baseAppWindow.getCharacterName().isEmpty()) {
                    Mockito.verify(baseAppWindow, Mockito.times(1)).restoreAndDo(Mockito.any(WindowConsumer.class));
                } else if (TEST_ACCOUNT_FOR_CLOSE.equals(baseAppWindow.getCharacterName().get())) {
                    Mockito.verify(baseAppWindow, Mockito.times(1)).restoreAndDo(Mockito.any(WindowConsumer.class));
                } else {
                    Mockito.verify(baseAppWindow, Mockito.never()).restoreAndDo(Mockito.any());
                }
            } catch (InterruptedException | Win32ApiException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private <T> List<T> mergeLists(final List<T> first, final List<T> second) {
        final List<T> result = new ArrayList<>(first);
        result.addAll(second);
        return result;
    }

    @Test
    void closeUnboundWindowsAndLaunchNewAccounts() throws InterruptedException, Win32ApiException {
        System.out.println("closeUnboundWindowsAndLaunchNewAccounts");
        final Account[] testAccountsToLaunch = getTestAccountsToLaunch();
        mockTestBaseAppWindows(getEmptyTestWindows());
        mockTargetProcessesLimitNotReached();
        mockLauncherWindowForLoginAccounts(testAccountsToLaunch);
        final BaseAppWindow newBaseAppWindow = getBaseAppWindow(null);
        mockFindUnboundBaseAppWindowAndBindWithAccount(newBaseAppWindow);
        verifyBaseAppWindowChoosingCharacter(newBaseAppWindow);
        mockTargetProcessesLimitNotReached();
        final LaunchAccountAction instance = new LaunchAccountAction(testAccountsToLaunch, windowFactory, settings);
        instance.perform();
        verify(newBaseAppWindow, atLeastOnce()).restoreAndDo(any(WindowConsumer.class));
    }

    @Test
    void closeUnnecessaryWindowsAndLaunchNewAccounts() throws InterruptedException, Win32ApiException {
        System.out.println("closeUnnecessaryWindowsAndLaunchNewAccounts");
        final Account[] testAccountsToLaunch = getTestAccountsToLaunch();
        final List<BaseAppWindow> testUnboundWindowsForClose = getTestUnboundWindowsForClose(TEST_ACCOUNT_TO_LAUNCH_3, TEST_ACCOUNT_TO_LAUNCH_4);
        when(windowFactory.getAllBaseAppWindows()).thenReturn(testUnboundWindowsForClose).thenReturn(List.of());
        mockLauncherWindowForLoginAccounts(testAccountsToLaunch);
        mockTargetProcessesLimitNotReached();
        final BaseAppWindow newBaseAppWindow = getBaseAppWindow(null);
        mockFindUnboundBaseAppWindowAndBindWithAccount(newBaseAppWindow);
        verifyBaseAppWindowChoosingCharacter(newBaseAppWindow);
        final LaunchAccountAction instance = new LaunchAccountAction(testAccountsToLaunch, windowFactory, settings);
        instance.perform();
        verify(newBaseAppWindow, atLeastOnce()).restoreAndDo(any(WindowConsumer.class));
    }

    @Test
    void haveBrokenTargetProcess() throws InterruptedException, Win32ApiException {
        System.out.println("haveBrokenTargetProcess");
        final Account[] testAccountsToLaunch = getTestAccountsToLaunch();
        mockTestBaseAppWindows(getEmptyTestWindows());
        mockTargetProcessesLimitReached();
        final LaunchAccountAction instance = new LaunchAccountAction(testAccountsToLaunch, windowFactory, settings);
        instance.perform();
        Mockito.verify(windowFactory, Mockito.never()).findOrTryStartLauncherWindow();
    }

    private Account[] getTestAccountsToLaunch() {
        return new Account[]{
                buildAccount(TEST_ACCOUNT_TO_LAUNCH_1),
                buildAccount(TEST_ACCOUNT_TO_LAUNCH_2)
        };
    }

    private List<BaseAppWindow> getTestBoundWindowsForCheck(final String account, final String... accounts) {
        return Stream.concat(Stream.of(account), Stream.of(accounts)).map(this::getBaseAppWindow).toList();
    }

    private List<BaseAppWindow> getTestUnboundWindowsForClose(final String account, final String... accounts) {
        return Stream.concat(Stream.of(account), Stream.of(accounts)).map(s -> {
            try {
                return getBaseAppWindowThatMustBeClosed(s);
            } catch (InterruptedException | Win32ApiException e) {
                throw new IllegalStateException(e);
            }
        }).toList();
    }

    @SuppressWarnings("unchecked")
    private List<BaseAppWindow> getEmptyTestWindows() {
        return Collections.EMPTY_LIST;
    }

    private BaseAppWindow getBaseAppWindow(final String characterName) {
        final BaseAppWindow mock = Mockito.mock(BaseAppWindow.class);
        when(mock.getCharacterName()).thenReturn(Optional.ofNullable(characterName));
        return mock;
    }

    @SuppressWarnings("unchecked")
    private BaseAppWindow getBaseAppWindowThatMustBeClosed(final String characterName) throws InterruptedException, Win32ApiException {
        final BaseAppWindow mock = getBaseAppWindow(characterName);
        doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            Assertions.assertEquals(1, args.length);
            final WindowConsumer<RestoredBaseAppWindow> consumer = (WindowConsumer<RestoredBaseAppWindow>) args[0];
            final RestoredBaseAppWindow restoredBaseAppWindowMock = Mockito.mock(RestoredBaseAppWindow.class);
            consumer.accept(restoredBaseAppWindowMock);
            Mockito.verify(restoredBaseAppWindowMock, Mockito.times(1)).close();
            return null;
        }).when(mock).restoreAndDo(Mockito.any(WindowConsumer.class));
        return mock;
    }

    private List<BaseAppWindow> mockTestBaseAppWindows(final List<BaseAppWindow> testWindows) {
        when(windowFactory.getAllBaseAppWindows()).thenReturn(testWindows);
        return testWindows;
    }

    private void mockFindUnboundBaseAppWindowAndBindWithAccount(final BaseAppWindow window) {
        when(windowFactory.findUnboundBaseAppWindowAndBindWithAccount(Mockito.anyString())).thenReturn(Optional.of(window));
    }

    @SuppressWarnings("unchecked")
    private void verifyBaseAppWindowChoosingCharacter(final BaseAppWindow window) throws InterruptedException, Win32ApiException {
        doAnswer(invocation -> {
            Assertions.assertEquals(1, invocation.getArguments().length);
            final WindowConsumer<RestoredBaseAppWindow> consumer = invocation.getArgument(0);
            final RestoredBaseAppWindow restoredBaseAppWindowMock = Mockito.mock(RestoredBaseAppWindow.class);
            consumer.accept(restoredBaseAppWindowMock);
            Mockito.verify(restoredBaseAppWindowMock, Mockito.times(1)).selectServer();
            Mockito.verify(restoredBaseAppWindowMock, Mockito.times(1)).chooseCharacter();
            return null;
        }).when(window).restoreAndDo(Mockito.any(WindowConsumer.class));
    }

    private void mockLauncherWindowForNotFoundCase() throws InterruptedException, Win32ApiException {
        when(windowFactory.findOrTryStartLauncherWindow()).thenReturn(Optional.empty());
    }

    private void mockLauncherWindowForLoginAccounts(final Account[] testAccountsToLaunch) throws InterruptedException, Win32ApiException {
        final List<Optional<LauncherWindow>> mocks = new ArrayList<>(testAccountsToLaunch.length);
        for (Account account : testAccountsToLaunch) {
            mocks.add(Optional.of(getLauncherMockForLoginAccount(account)));
        }
        mockitoUtil.mockReturnVararg(windowFactory.findOrTryStartLauncherWindow(), mocks);
    }

    @SuppressWarnings("unchecked")
    private LauncherWindow getLauncherMockForLoginAccount(final Account account) throws InterruptedException, Win32ApiException {
        final LauncherWindow mock = Mockito.mock(LauncherWindow.class);
        doAnswer(invocation -> {
            Assertions.assertEquals(1, invocation.getArguments().length);
            final RestoredLauncherWindow restoredLauncherWindowMock = Mockito.mock(RestoredLauncherWindow.class);
            invocation.<WindowConsumer<RestoredLauncherWindow>>getArgument(0).accept(restoredLauncherWindowMock);
            Mockito.verify(restoredLauncherWindowMock, Mockito.times(1)).checkClientIsAlreadyRunningWindowRendered();
            Mockito.verify(restoredLauncherWindowMock, Mockito.times(1)).login(account);
            return null;
        }).when(mock).restoreAndDo(Mockito.any(WindowConsumer.class));
        return mock;
    }

    private void mockTargetProcessesLimitNotReached() {
        when(applicationSettings.maxAccountsSimultaneously()).thenReturn(2);
        when(windowFactory.countAllTargetProcesses()).thenReturn(Either.value(() -> 1).whenReturnsTrue(true));
    }

    private void mockTargetProcessesLimitReached() {
        when(applicationSettings.maxAccountsSimultaneously()).thenReturn(2);
        when(windowFactory.countAllTargetProcesses()).thenReturn(Either.value(() -> 2).whenReturnsTrue(true));
    }

    private Account buildAccount(final String name) {
        return new Account.Builder()
                .name(name)
                .login(name + "_login")
                .password(name + "_password")
                .build();
    }

}