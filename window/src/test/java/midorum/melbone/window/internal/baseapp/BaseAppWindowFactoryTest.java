package midorum.melbone.window.internal.baseapp;

import com.midorum.win32api.facade.Either;
import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Win32System;
import com.midorum.win32api.facade.exception.Win32ApiException;
import midorum.melbone.model.settings.account.AccountBinding;
import midorum.melbone.model.settings.stamp.Stamps;
import midorum.melbone.model.window.baseapp.BaseAppWindow;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.setting.TargetBaseAppSettings;
import midorum.melbone.window.internal.common.CommonWindowService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BaseAppWindowFactoryTest {

    public static final String BASE_APP_WINDOW_TITLE = "BaseAppWindowTitle";
    public static final String BASE_APP_WINDOW_CLASSNAME = "BaseAppWindowClassname";
    public static final String ACCOUNT_TO_BIND = "test_account";
    public static final int PROCESS_ID_1 = 12345;
    public static final int PROCESS_ID_2 = 54321;
    public static final long PROCESS_CREATION_TIME = 123456L;
    public static final String BOUND_ACCOUNT_1 = "bound_account_1";
    public static final String BOUND_ACCOUNT_2 = "bound_account_2";
    private final CommonWindowService commonWindowService = mock(CommonWindowService.class);
    private final Win32System win32System = mock(Win32System.class);
    private final Settings settings = mock(Settings.class);
    private final TargetBaseAppSettings targetBaseAppSettings = mock(TargetBaseAppSettings.class);
    private final AccountBinding accountBinding = mock(AccountBinding.class);
    private final Stamps stamps = mock(Stamps.class);

    @BeforeAll
    public static void beforeAll() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @BeforeEach
    public void beforeEach() {
        // system
        when(commonWindowService.getWin32System()).thenReturn(win32System);
        when(settings.targetBaseAppSettings()).thenReturn(targetBaseAppSettings);
        when(targetBaseAppSettings.windowTitle()).thenReturn(BASE_APP_WINDOW_TITLE);
        when(targetBaseAppSettings.windowClassName()).thenReturn(BASE_APP_WINDOW_CLASSNAME);
        when(commonWindowService.getUID(any(IWindow.class))).thenAnswer(invocationOnMock -> Either.resultOf(invocationOnMock.getArgument(0, IWindow.class)::toString));
    }

    @AfterEach
    public void afterEach() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @Test
    void targetWindowsNotFound() {
        System.out.println("targetWindowsNotFound");
        when(win32System.findAllWindows(BASE_APP_WINDOW_TITLE, BASE_APP_WINDOW_CLASSNAME, false)).thenReturn(List.of());
        final BaseAppWindowFactory instance = new BaseAppWindowFactory(commonWindowService, settings, accountBinding, stamps);
        final List<BaseAppWindow> allWindows = instance.getAllWindows();
        assertTrue(allWindows.isEmpty());
    }

    @Test
    void getAllWindows() {
        System.out.println("getAllWindows");
        final List<IWindow> baseAppWindows = getAnyBaseAppWindows();
        when(win32System.findAllWindows(BASE_APP_WINDOW_TITLE, BASE_APP_WINDOW_CLASSNAME, false)).thenReturn(baseAppWindows);
        final BaseAppWindowFactory instance = new BaseAppWindowFactory(commonWindowService, settings, accountBinding, stamps);
        final List<BaseAppWindow> foundWindows = instance.getAllWindows();
        assertEquals(2, foundWindows.size());
    }

    @Test
    void findUnboundWindowAndBindWithAccount() throws Win32ApiException {
        System.out.println("findUnboundWindowAndBindWithAccount");
        final List<IWindow> baseAppWindows = getBaseAppWindowsWithUnbound();
        when(win32System.findAllWindows(BASE_APP_WINDOW_TITLE, BASE_APP_WINDOW_CLASSNAME, false)).thenReturn(baseAppWindows);
        final BaseAppWindowFactory instance = new BaseAppWindowFactory(commonWindowService, settings, accountBinding, stamps);
        final Optional<BaseAppWindow> boundWindow = instance.findUnboundWindowAndBindWithAccount(ACCOUNT_TO_BIND);
        assertTrue(boundWindow.isPresent());
        verify(accountBinding).bindResource(ACCOUNT_TO_BIND, PROCESS_ID_2 + "_" + PROCESS_CREATION_TIME);
    }

    @Test
    void unboundWindowNotFound() throws Win32ApiException {
        System.out.println("unboundWindowNotFound");
        final List<IWindow> baseAppWindows = getBaseAppWindowsWithoutUnbound();
        when(win32System.findAllWindows(BASE_APP_WINDOW_TITLE, BASE_APP_WINDOW_CLASSNAME, false)).thenReturn(baseAppWindows);
        final BaseAppWindowFactory instance = new BaseAppWindowFactory(commonWindowService, settings, accountBinding, stamps);
        final Optional<BaseAppWindow> boundWindow = instance.findUnboundWindowAndBindWithAccount(ACCOUNT_TO_BIND);
        assertTrue(boundWindow.isEmpty());
        verify(accountBinding, never()).bindResource(eq(ACCOUNT_TO_BIND), anyString());
    }

    private List<IWindow> getAnyBaseAppWindows() {
        return List.of(getAnyBaseAppWindowMock("0x7f34"), getAnyBaseAppWindowMock("0xacf5"));
    }

    private IWindow getAnyBaseAppWindowMock(final String id) {
        final IWindow mock = mock(IWindow.class);
        when(mock.getSystemId()).thenReturn(id);
        return mock;
    }

    private List<IWindow> getBaseAppWindowsWithUnbound() {
        return List.of(getBoundBaseAppWindowMock("0x7f34", PROCESS_ID_1, BOUND_ACCOUNT_1), getUnboundBaseAppWindowMock("0xacf5", PROCESS_ID_2));
    }

    private List<IWindow> getBaseAppWindowsWithoutUnbound() {
        return List.of(getBoundBaseAppWindowMock("0x7f34", PROCESS_ID_1, BOUND_ACCOUNT_1), getBoundBaseAppWindowMock("0xacf5", PROCESS_ID_2, BOUND_ACCOUNT_2));
    }

    private IWindow getBoundBaseAppWindowMock(final String id, final int processId, final String accountId) {
        final String resourceUid = processId + "_" + PROCESS_CREATION_TIME;
        when(accountBinding.getBoundAccount(resourceUid)).thenReturn(Optional.of(accountId));
        final IWindow mock = mock(IWindow.class);
        when(mock.getSystemId()).thenReturn(id);
        when(commonWindowService.getUID(mock)).thenReturn(Either.resultOf(() -> processId + "_" + PROCESS_CREATION_TIME));
        when(commonWindowService.checkIfWindowRendered(mock)).thenReturn(getWindowRenderedState(true));
        return mock;
    }

    private IWindow getUnboundBaseAppWindowMock(final String id, final int processId) {
        final String resourceUid = processId + "_" + PROCESS_CREATION_TIME;
        when(accountBinding.getBoundAccount(resourceUid)).thenReturn(Optional.empty());
        final IWindow mock = mock(IWindow.class);
        when(mock.getSystemId()).thenReturn(id);
        when(commonWindowService.getUID(mock)).thenReturn(Either.resultOf(() -> resourceUid));
        when(commonWindowService.checkIfWindowRendered(mock)).thenReturn(getWindowRenderedState(true));
        return mock;
    }

    private Either<Boolean> getWindowRenderedState(final boolean state) {
        return Either.value(() -> state).whenReturnsTrue(true);
    }
}