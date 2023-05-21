package midorum.melbone.ui.context;

import com.midorum.win32api.facade.*;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.hook.GlobalMouseKeyHook;
import com.midorum.win32api.struct.PointInt;
import com.midorum.win32api.util.RelativeCoordinates;
import dma.function.ConsumerThrowing;
import dma.function.VoidAction;
import midorum.melbone.executor.ExecutorFactory;
import midorum.melbone.model.experimental.task.TaskStorage;
import midorum.melbone.model.processing.AccountsProcessingRequest;
import midorum.melbone.model.settings.PropertiesProvider;
import midorum.melbone.model.settings.account.AccountBinding;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.model.settings.key.StampKey;
import midorum.melbone.settings.managment.StampBuilder;
import midorum.melbone.ui.internal.Context;
import midorum.melbone.ui.context.experimental.MockedTaskStorage;
import midorum.melbone.ui.internal.util.DataLoader;
import midorum.melbone.ui.internal.util.IdentifyDialog;
import midorum.melbone.ui.internal.model.OnCloseNotificator;
import midorum.melbone.ui.internal.model.FrameVisibilityOperations;
import midorum.melbone.ui.internal.settings.SettingsManagerForm;
import midorum.melbone.ui.internal.util.MouseKeyHookManager;
import midorum.melbone.ui.internal.util.StandardDialogsProvider;
import midorum.melbone.model.dto.Account;
import midorum.melbone.model.persistence.AccountStorage;
import midorum.melbone.model.processing.IExecutor;
import midorum.melbone.model.settings.setting.ApplicationSettings;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.window.baseapp.BaseAppWindow;
import midorum.melbone.model.window.baseapp.RestoredBaseAppWindow;
import midorum.melbone.window.WindowFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MockedContext {

    protected final Context context;
    protected final ExecutorFactory executorFactory = mock(ExecutorFactory.class);
    protected final IExecutor executor = mock(IExecutor.class);
    protected final Settings settings = mock(Settings.class);
    protected final MockedSettingStorage settingStorage = new MockedSettingStorage();
    protected final AccountBinding accountBinding = new MockedAccountBinding();
    protected final AccountStorage accountStorage = new MockedAccountStorage();
    protected final TaskStorage taskStorage = new MockedTaskStorage();
    protected final WindowFactory windowFactory = mock(WindowFactory.class);
    protected final ApplicationSettings applicationSettings = mock(ApplicationSettings.class);
    protected final MouseKeyHookManager mouseKeyHookManager = mock(MouseKeyHookManager.class);
    protected final StandardDialogsProvider standardDialogsProvider = mock(StandardDialogsProvider.class);
    protected final DataLoader dataLoader = mock(DataLoader.class);
    protected final PropertiesProvider propertiesProvider = mock(PropertiesProvider.class);
    protected final Win32System win32System = mock(Win32System.class);
    protected final RelativeCoordinates relativeCoordinates = mock(RelativeCoordinates.class);
    protected final IScreenShotMaker screenShotMaker = mock(IScreenShotMaker.class);
    protected final Logger logger = LogManager.getLogger();

    public MockedContext() {
        this.context = new Context.Builder()
                .executorFactory(executorFactory)
                .settings(settings)
                .settingStorage(settingStorage)
                .accountStorage(accountStorage)
                .taskStorage(taskStorage)
                .windowFactory(windowFactory)
                .mouseKeyHookManager(mouseKeyHookManager)
                .standardDialogsProvider(standardDialogsProvider)
                .dataLoader(dataLoader)
                .propertiesProvider(propertiesProvider)
                .build();
        when(executorFactory.getExecutor()).thenReturn(executor);
        when(settings.application()).thenReturn(applicationSettings);
        when(win32System.getRelativeCoordinates(any(Rectangle.class))).thenReturn(relativeCoordinates);
        when(windowFactory.getScreenShotMaker()).thenReturn(screenShotMaker);
        mockDataLoaderInvocation();
    }

    public MockedContext(final StandardDialogsProvider standardDialogsProvider) {
        this.context = new Context.Builder()
                .executorFactory(executorFactory)
                .settings(settings)
                .settingStorage(settingStorage)
                .accountStorage(accountStorage)
                .taskStorage(taskStorage)
                .windowFactory(windowFactory)
                .mouseKeyHookManager(mouseKeyHookManager)
                .standardDialogsProvider(standardDialogsProvider)
                .dataLoader(dataLoader)
                .propertiesProvider(propertiesProvider)
                .build();
        when(executorFactory.getExecutor()).thenReturn(executor);
        when(settings.application()).thenReturn(applicationSettings);
        when(win32System.getRelativeCoordinates(any(Rectangle.class))).thenReturn(relativeCoordinates);
        when(windowFactory.getScreenShotMaker()).thenReturn(screenShotMaker);
        mockDataLoaderInvocation();
    }

    public final void setLoggerLevel(final Level level) {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        ctx.getConfiguration()
                .getLoggerConfig(LogManager.ROOT_LOGGER_NAME)
                .setLevel(level);
        ctx.updateLoggers();
    }

    @SuppressWarnings("unchecked")
    public final <X extends Throwable> BaseAppWindow createBaseAppWindowMock(
            final ConsumerThrowing<ConsumerThrowing<RestoredBaseAppWindow, InterruptedException>, X> restoreAndDoInvocationConsumer,
            final BiConsumer<String, BaseAppWindow> bindWithAccountInvocationConsumer,
            final Supplier<Optional<String>> boundCharacterNameSupplier
    ) {
        final BaseAppWindow mock = mock(BaseAppWindow.class);
        try {
            doAnswer(invocation -> {
                restoreAndDoInvocationConsumer.accept(invocation.getArgument(0));
                return null;
            }).when(mock).restoreAndDo(any(ConsumerThrowing.class));
            doAnswer(invocation -> {
                bindWithAccountInvocationConsumer.accept(invocation.getArgument(0), mock);
                return null;
            }).when(mock).bindWithAccount(anyString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        when(mock.getCharacterName()).thenReturn(boundCharacterNameSupplier.get());
        return mock;
    }

    public Rectangle createFakeWindowRectangle() {
        return new Rectangle(0, 0, 200, 100);
    }

    public final IWindow createNativeWindowMock(final String windowTitle, final Rectangle rectangle) {
        final IWindow mock = mock(IWindow.class);
        when(mock.getText()).thenReturn(Optional.ofNullable(windowTitle));
        when(mock.getWindowRectangle()).thenReturn(rectangle);
        when(mock.getClientRectangle()).thenReturn(rectangle);
        when(mock.getClientToScreenRectangle()).thenReturn(rectangle);
        return mock;
    }

    public final IWindow createNativeWindowMock() {
        return createNativeWindowMock("Mocked window title", createFakeWindowRectangle());
    }

    public final IWindow createNativeWindowMock(final String windowTitle) {
        return createNativeWindowMock(windowTitle, createFakeWindowRectangle());
    }

    public final IWindow createNativeWindowMock(final Rectangle rectangle) {
        return createNativeWindowMock("Mocked window title", rectangle);
    }

    public IdentifyDialog createIdentifyDialogMock() {
        return mock(IdentifyDialog.class);
    }

    public SettingsManagerForm createSettingsManagerFormMock() {
        return mock(SettingsManagerForm.class);
    }

    public final void doWithMockedWin32System(final VoidAction voidAction) {
        try (MockedStatic<Win32System> mockedStatic = mockStatic(Win32System.class)) {
            mockedStatic.when(Win32System::getInstance).thenReturn(win32System);
            voidAction.perform();
        }
    }

    public RestoredBaseAppWindow createRestoredBaseAppWindowMock() {
        return mock(RestoredBaseAppWindow.class);
    }

    public Account createAccount(final String name) {
        return Account.builder()
                .name(name)
                .login(name + "_login")
                .password(name + "_password")
                .build();
    }

    public Account createAccount(final String name, final String login, final String password) {
        return Account.builder()
                .name(name)
                .login(name + "_login")
                .password(name + "_password")
                .build();
    }

    public GlobalMouseKeyHook.MouseEvent createMouseEvent(final int eventCode, final PointInt eventPoint) {
        return new GlobalMouseKeyHook.MouseEvent(eventCode, eventPoint, 0, 0, 0);
    }

    public GlobalMouseKeyHook.MouseEvent createMouseEvent(final int eventCode) {
        return createMouseEvent(eventCode, new PointInt(-1, -1));
    }

    public Stamp createFakeStamp(final StampKey stampKey, final IWindow capturedNativeWindow, final Rectangle capturedRectangle) {
        return new StampBuilder()
                .key(stampKey)
                .description(stampKey.internal().description())
                .wholeData(new int[capturedRectangle.width() * capturedRectangle.height()])
                .firstLine(new int[capturedRectangle.width()])
                .location(capturedRectangle)
                .windowRect(capturedNativeWindow.getWindowRectangle())
                .windowClientRect(capturedNativeWindow.getClientRectangle())
                .windowClientToScreenRect(capturedNativeWindow.getClientToScreenRectangle())
                .build();
    }

    public void setExecutionMode(final String mode) {
        doReturn(true).when(propertiesProvider).isModeSet(mode);
    }

    @SuppressWarnings("unchecked")
    private void mockDataLoaderInvocation() {
        doAnswer(invocation -> {
            assertEquals(2, invocation.getArguments().length);
            final Supplier supplier = invocation.getArgument(0);
            final Consumer consumer = invocation.getArgument(1);
            consumer.accept(supplier.get());
            return null;
        }).when(dataLoader).loadGuiData(any(Supplier.class), any(Consumer.class));
    }

    public final class Interaction {


        private int accountsLimit = -1;

        private final List<BaseAppWindow> baseAppWindows = new ArrayList<>();

        public Interaction() {
            when(applicationSettings.maxAccountsSimultaneously()).thenAnswer(invocation -> accountsLimit);
            when(windowFactory.getAllBaseAppWindows()).thenReturn(baseAppWindows);
            when(windowFactory.findFirstUnboundBaseAppWindow()).then(invocation ->
                    getFirstUnboundWindow());
        }

        public Interaction setAccountsLimit(final int accountsLimit) {
            if (this.accountsLimit != -1) throw new IllegalStateException("You can set accountBinding limit only once");
            this.accountsLimit = accountsLimit;
            return this;
        }

        public Interaction setTotalAccounts(final String... accountBinding) {
            Arrays.stream(accountBinding).forEach(s -> accountStorage.store(createAccount(s)));
            return this;
        }

        public Interaction setAccountsInUse(final String... accountBinding) {
            final Map<Boolean, List<String>> map = Arrays.stream(accountBinding).collect(Collectors.partitioningBy(accountStorage::isExists));
            if (!map.get(false).isEmpty()) {
                throw new IllegalArgumentException("Accounts " + map.get(false) + " do not exist. Maybe you forgot to add them in total accountBinding list.");
            }
            map.get(true).forEach(accountStorage::addToUsed);
            return this;
        }

        public Interaction generateAccountsInUse(final int count) {
            Stream.generate(UUID::randomUUID).map(UUID::toString).limit(count).forEach(accountId -> {
                accountStorage.store(createAccount(accountId));
                accountStorage.addToUsed(accountId);
            });
            return this;
        }

        public Interaction setBoundAccounts(final String... accounts) {
            final Map<Boolean, List<String>> map = Arrays.stream(accounts).collect(Collectors.partitioningBy(accountStorage::isInUse));
            if (!map.get(false).isEmpty()) {
                throw new IllegalArgumentException("Accounts " + map.get(false) + " do not in use. Maybe you forgot to add them in used accountBinding list.");
            }
            map.get(true).forEach(accountId -> {
                final BaseAppWindow baseAppWindowMock = createBaseAppWindowMock(
                        consumer -> consumer.accept(createRestoredBaseAppWindowMock()),
                        (accountToBind, baseAppWindow) -> {
                            throw new IllegalStateException("Cannot bind window with " + accountToBind + ". Window already bound.");
                        },
                        () -> Optional.of(accountId));
                baseAppWindows.add(baseAppWindowMock);
                accountBinding.bindResource(accountId, baseAppWindowMock.toString());
            });
            return this;
        }

        public Interaction createUnboundWindows(final int count) {
            baseAppWindows.addAll(Stream.generate(() -> createBaseAppWindowMock(
                            consumer -> consumer.accept(createRestoredBaseAppWindowMock()),
                            (accountToBind, baseAppWindow) -> {
                                accountBinding.bindResource(accountToBind, baseAppWindow.toString());
                                when(baseAppWindow.getCharacterName()).thenReturn(Optional.of(accountToBind));
                            },
                            Optional::empty))
                    .limit(count).toList());
            return this;
        }

        public MouseKeyHookInteraction whenTryCatchMouseKeyEvent(final GlobalMouseKeyHook.MouseEvent eventShouldCatch) {
            return new MouseKeyHookInteraction(eventShouldCatch, this);
        }

        public MouseKeyHookInteraction whenTryCatchMouseKeyEvent(final int eventTypeShouldCatch) {
            return whenTryCatchMouseKeyEvent(createMouseEvent(eventTypeShouldCatch));
        }

        public ExecutorInteraction whenTrySendRoutineTask() {
            return new ExecutorInteraction(this);
        }

        public Interaction whenTryGetWindowByPointThenReturn(final IWindow capturedWindow) {
            when(windowFactory.getWindowByPoint(any(PointInt.class))).thenAnswer(invocation -> Optional.ofNullable(capturedWindow));
            return this;
        }

        public GetPointInWindow whenTryGetPointInWindow(final PointInt pointWhereUserClicked) {
            return new GetPointInWindow(pointWhereUserClicked, this);
        }

        public GetPointInWindow whenTryGetAnyPointInWindow() {
            return new GetPointInWindow(this);
        }

        public Interaction whenTryTakeRectangleShotThenReturnStandardImage() {
            when(screenShotMaker.takeRectangle(any(Rectangle.class))).thenAnswer(invocation -> {
                final Rectangle rectangle = invocation.getArgument(0);
                // https://riptutorial.com/java/example/19496/creating-a-simple-image-programmatically-and-displaying-it
                final int width = rectangle.width();
                final int height = rectangle.height();
                BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = img.createGraphics();
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, width, height);
                g2d.setColor(Color.RED);
                g2d.drawLine(0, 0, width, height);
                g2d.drawLine(0, height, width, 0);
                g2d.dispose();
                return img;
            });
            return this;
        }

        private void mockStandardYesNoQuestion(final boolean answer) {
            when(standardDialogsProvider.askYesNoQuestion(any(Component.class), anyString())).thenReturn(answer);
            when(standardDialogsProvider.askYesNoQuestion(any(Component.class), anyString(), anyString())).thenReturn(answer);
        }

        public Interaction whenTryAskYesNoQuestionThenChooseYes() {
            mockStandardYesNoQuestion(true);
            return this;
        }

        public Interaction whenTryAskYesNoQuestionThenChooseNo() {
            mockStandardYesNoQuestion(false);
            return this;
        }

        private void mockStandardOkCancelDialogWithAnswer(final boolean answer) {
            when(standardDialogsProvider.askOkCancelConfirm(any(Component.class), anyString(), anyString())).thenReturn(answer);
        }

        public Interaction whenTryAskOkCancelConfirmationThenChooseOk() {
            mockStandardOkCancelDialogWithAnswer(true);
            return this;
        }

        public Interaction whenTryAskOkCancelConfirmationThenChooseCancel() {
            mockStandardOkCancelDialogWithAnswer(false);
            return this;
        }

        @SuppressWarnings("unchecked")
        public Interaction whenTryCaptureScreenRectangleThenReturn(final Rectangle capturedRectangle) {
            doAnswer(invocation -> {
                assertEquals(1, invocation.getArguments().length);
                final Consumer<Optional<Rectangle>> rectangleConsumer = invocation.getArgument(0);
                rectangleConsumer.accept(Optional.ofNullable(capturedRectangle));
                return null;
            }).when(standardDialogsProvider).captureRectangle(any(Consumer.class));
            return this;
        }

        public Interaction whenTryShowStampPreviewDialogThenVerifyStampIsNotNull() {
            doAnswer(invocation -> {
                assertEquals(1, invocation.getArguments().length);
                assertNotNull(invocation.getArgument(0));
                return null;
            }).when(standardDialogsProvider).showStampPreviewDialog(any(Stamp.class));
            return this;
        }

        public Interaction whenTryCreateIdentifyDialogThenReturn(final IdentifyDialog identifyDialog) {
            when(standardDialogsProvider.createIdentifyDialog(any(Frame.class), eq(context), any(OnCloseNotificator.class)))
                    .thenReturn(identifyDialog);
            return this;
        }

        public Interaction whenTryCreateIdentifyDialogThenNewInstance() {
            when(standardDialogsProvider.createIdentifyDialog(any(Frame.class), eq(context), any(OnCloseNotificator.class)))
                    .thenReturn(createIdentifyDialogMock());
            return this;
        }

        public Interaction whenTryCreateSettingsManagerFormThenNewInstance() {
            when(standardDialogsProvider.createSettingsManagerForm(eq(context), any(OnCloseNotificator.class)))
                    .thenReturn(createSettingsManagerFormMock());
            return this;
        }

        public Interaction whenTryCreateFrameVisibilityCounterThenReturn(final FrameVisibilityOperations frameVisibilityOperations) {
            when(standardDialogsProvider.createFrameVisibilityOperations(any(Frame.class)))
                    .thenReturn(frameVisibilityOperations);
            return this;
        }

        public int getAccountsLimit() {
            return accountsLimit;
        }

        public List<BaseAppWindow> getUnboundWindows() {
            return baseAppWindows.stream().filter(baseAppWindow -> baseAppWindow.getCharacterName().isEmpty()).toList();
        }

        public Optional<BaseAppWindow> getFirstUnboundWindow() {
            return baseAppWindows.stream().filter(baseAppWindow -> baseAppWindow.getCharacterName().isEmpty()).findFirst();
        }

        public Optional<BaseAppWindow> getBoundWindowFor(final String accountId) {
            return baseAppWindows.stream()
                    .filter(baseAppWindow -> {
                        final Optional<String> maybeCharacterName = baseAppWindow.getCharacterName();
                        return maybeCharacterName.isPresent() && maybeCharacterName.get().equals(accountId);
                    })
                    .findFirst();
        }

        public List<String> getBoundAccounts() {
            return baseAppWindows.stream()
                    .map(BaseAppWindow::getCharacterName)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        }

        public List<String> getUnboundAccounts() {
            final List<String> boundAccounts = getBoundAccounts();
            return accountStorage.accountsInUse().stream().filter(s -> !boundAccounts.contains(s)).toList();
        }

        public Interaction printContextInstances() {
            logger.info("""
                            Mocked context instances:
                            \tcontext: {}""",
                    context);
            return this;
        }

        public Interaction printState(final String additionalInfo) {
            logger.info("""
                            Mocked context state:
                            \taccounts limit: {}
                            \ttotal accounts: {}
                            \taccounts in use: {}
                            \tunbound accountBinding: {}
                            \tbound accountBinding: {}
                            \ttotal windows: {}
                            \tunbound windows: {}
                            \t{}""",
                    accountsLimit,
                    accountStorage.accounts(),
                    accountStorage.accountsInUse(),
                    getUnboundAccounts(),
                    getBoundAccounts(),
                    baseAppWindows,
                    getUnboundWindows(),
                    additionalInfo);
            return this;
        }

        public Interaction printState() {
            printState("\b");
            return this;
        }

        public Interaction printStateBriefly(final String additionalInfo) {
            logger.info("""
                            Mocked context state:
                            \taccounts limit: {}
                            \ttotal accounts count: {}
                            \taccounts in use count: {}
                            \tunbound accountBinding count: {}
                            \tbound accountBinding: {}
                            \ttotal windows count: {}
                            \tunbound windows count: {}
                            \t{}""",
                    accountsLimit,
                    accountStorage.accounts().size(),
                    accountStorage.accountsInUse().size(),
                    getUnboundAccounts().size(),
                    getBoundAccounts(),
                    baseAppWindows.size(),
                    getUnboundWindows().size(),
                    additionalInfo);
            return this;
        }

        public Interaction printStateBriefly() {
            printStateBriefly("\b");
            return this;
        }

    }

    public class MouseKeyHookInteraction {

        private final GlobalMouseKeyHook.MouseEvent eventShouldCatch;
        private final Interaction interactionInstance;

        private MouseKeyHookInteraction(final GlobalMouseKeyHook.MouseEvent eventShouldCatch, final Interaction interactionInstance) {
            this.eventShouldCatch = eventShouldCatch;
            this.interactionInstance = interactionInstance;
        }

        private void mockSetHookInvocation(final BiConsumer<GlobalMouseKeyHook.MouseEvent, Function<GlobalMouseKeyHook.MouseEvent, Boolean>> onCatchActionConsumer,
                                           final Consumer<Function<Throwable, Boolean>> onErrorActionConsumer,
                                           final Consumer<VoidAction> onHookReleaseActionConsumer) {
            final Answer<Void> answer = invocation -> {
                final int eventType = invocation.getArgument(0);
                assertEquals(eventShouldCatch.eventType(), eventType, "Hook should catch " + eventShouldCatch.eventType() + " event type but passed " + eventType);
                onCatchActionConsumer.accept(eventShouldCatch, invocation.getArgument(1));
                onErrorActionConsumer.accept(invocation.getArgument(2));
                if (invocation.getArguments().length == 4)
                    onHookReleaseActionConsumer.accept(invocation.getArgument(3));
                return null;
            };
            doAnswer(answer).when(mouseKeyHookManager).setHook(anyInt(), any(), any());
            doAnswer(answer).when(mouseKeyHookManager).setHook(anyInt(), any(), any(), any());
        }

        public Interaction thenCatchWithSuccessAndDoAfter(final VoidAction afterCatchAction) {
            mockSetHookInvocation(
                    (mouseEvent, onCatchAction) -> {
                        assertTrue(onCatchAction.apply(mouseEvent), "The on-catch action must return true to release the hook");
                        afterCatchAction.perform();
                    },
                    onErrorAction -> {
                    },
                    VoidAction::perform);
            return interactionInstance;
        }

        public Interaction thenCatchWithSuccess() {
            return thenCatchWithSuccessAndDoAfter(() -> {
            });
        }

        public Interaction thenCatchWithErrorAndDoAfter(final Throwable throwable, final Consumer<Throwable> afterErrorAction) {
            mockSetHookInvocation(
                    (mouseEvent, onCatchAction) -> {
                    },
                    onErrorAction -> {
                        assertTrue(onErrorAction.apply(throwable), "The on-error action must return true to release the hook");
                        afterErrorAction.accept(throwable);
                    },
                    VoidAction::perform);
            return interactionInstance;
        }

        public Interaction thenCatchWithError(final Throwable throwable) {
            return thenCatchWithErrorAndDoAfter(throwable, t -> {
            });
        }
    }

    public class ExecutorInteraction {

        private final Interaction interactionInstance;

        public ExecutorInteraction(final Interaction interactionInstance) {
            this.interactionInstance = interactionInstance;
        }

        public Interaction thenExecuteNormal() {
            doAnswer(invocation -> {
                assertEquals(1, invocation.getArguments().length);
                final AccountsProcessingRequest request = invocation.getArgument(0);
                System.out.println("accountBinding in work: " + Arrays.toString(request.getAccounts()));
                return null;
            }).when(executor).sendRoutineTask(any(AccountsProcessingRequest.class));
            return interactionInstance;
        }

        public Interaction thenThrowError(final Throwable throwable) {
            doThrow(throwable).when(executor).sendRoutineTask(any(AccountsProcessingRequest.class));
            return interactionInstance;
        }

        public Interaction thenReturnError(final Throwable throwable) {
            doAnswer(invocation -> {
                assertEquals(1, invocation.getArguments().length);
                final AccountsProcessingRequest request = invocation.getArgument(0);
                request.getErrorHandler().accept(throwable);
                return null;
            }).when(executor).sendRoutineTask(any(AccountsProcessingRequest.class));
            return interactionInstance;
        }

    }

    public static class FrameVisibilityCounter implements FrameVisibilityOperations {

        private Boolean isIconified = null;
        private Boolean isVisible = null;

        @Override
        public void iconify() {
            isIconified = true;
        }

        @Override
        public void restore() {
            isIconified = false;
        }

        @Override
        public boolean isIconified() {
            return Optional.ofNullable(isIconified).orElseThrow(() -> new IllegalStateException("Frame state has not set yet"));
        }

        @Override
        public void show() {
            isVisible = true;
        }

        @Override
        public void hide() {
            isVisible = false;
        }

        @Override
        public boolean isVisible() {
            return Optional.ofNullable(isVisible).orElseThrow(() -> new IllegalStateException("Frame visibility has not set yet"));
        }
    }

    public class GetPointInWindow {

        final PointInt pointWhereUserClicked;
        private final Interaction interactionInstance;

        public GetPointInWindow(final PointInt pointWhereUserClicked, final Interaction interactionInstance) {
            this.pointWhereUserClicked = pointWhereUserClicked;
            this.interactionInstance = interactionInstance;
        }

        public GetPointInWindow(final Interaction interactionInstance) {
            this(new PointInt(-1, -1), interactionInstance);
        }

        public Interaction thenReturn(final WindowPoint windowPoint) {
            when(windowFactory.getPointInWindow(pointWhereUserClicked)).thenReturn(Optional.ofNullable(windowPoint));
            return this.interactionInstance;
        }

        public Interaction thenReturnAnyWindowPoint() {
            final IWindow foundNativeWindow = createNativeWindowMock();
            final Rectangle clientRectangle = foundNativeWindow.getClientRectangle();
            final PointInt somePointInFoundWindow = new PointInt(clientRectangle.width() / 2, clientRectangle.height() / 2);
            return thenReturn(new WindowPoint(foundNativeWindow, somePointInFoundWindow));
        }

    }
}
