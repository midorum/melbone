package midorum.melbone.executor.internal;

import midorum.melbone.executor.internal.processor.AccountProcessorFactory;
import midorum.melbone.model.dto.Account;
import midorum.melbone.model.exception.NeedRetryException;
import midorum.melbone.model.processing.AccountsProcessingRequest;
import midorum.melbone.model.processing.IExecutor;
import midorum.melbone.model.settings.setting.ApplicationSettings;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.WindowFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

class ExecutorImplTest {

    private final Settings settingsMock = Mockito.mock(Settings.class);
    private final ApplicationSettings applicationSettingsMock = Mockito.mock(ApplicationSettings.class);
    private final AccountsProcessingRequest accountsProcessingRequestMock = Mockito.mock(AccountsProcessingRequest.class);
    private final InternalScheduledExecutor internalScheduledExecutorMock = Mockito.mock(InternalScheduledExecutor.class);
    private final AccountProcessorFactory accountProcessorFactoryMock = Mockito.mock(AccountProcessorFactory.class);
    private final WindowFactory windowFactory = Mockito.mock(WindowFactory.class);


    public ExecutorImplTest() {
        Mockito.when(settingsMock.application()).thenReturn(applicationSettingsMock);
    }

    @BeforeAll
    public static void beforeAll() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @AfterEach
    public void afterEach() {
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    @Test
    public void failOnCorruptedTask() {
        System.out.println("failOnCorruptedTask");
        final IExecutor executor = new ExecutorImpl(accountProcessorFactoryMock, internalScheduledExecutorMock, settingsMock, windowFactory);
        Mockito.when(accountProcessorFactoryMock.routineProcessor(Mockito.any(Account[].class), Mockito.any(WindowFactory.class), Mockito.any(Settings.class))).thenReturn(getNormalAccountProcessor());
        Assertions.assertThrows(IllegalArgumentException.class, () -> executor.sendRoutineTask(null));
        Mockito.when(accountsProcessingRequestMock.getAccounts()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> executor.sendRoutineTask(accountsProcessingRequestMock));
        Mockito.when(accountsProcessingRequestMock.getAccounts()).thenReturn(getTestAccounts());
        Mockito.when(accountsProcessingRequestMock.getErrorHandler()).thenReturn(null);
        Assertions.assertThrows(IllegalArgumentException.class, () -> executor.sendRoutineTask(accountsProcessingRequestMock));
    }

    @Test
    public void failOnAccountsLimitExceeded() {
        System.out.println("failOnAccountsLimitExceeded");
        final int maxAccounts = 1;
        final IExecutor executor = new ExecutorImpl(accountProcessorFactoryMock, internalScheduledExecutorMock, settingsMock, windowFactory);
        Mockito.when(accountProcessorFactoryMock.routineProcessor(Mockito.any(Account[].class), Mockito.any(WindowFactory.class), Mockito.any(Settings.class))).thenReturn(getNormalAccountProcessor());
        Mockito.when(applicationSettingsMock.maxAccountsSimultaneously()).thenReturn(maxAccounts);
        Mockito.when(accountsProcessingRequestMock.getAccounts()).thenReturn(getTestAccounts());
        Mockito.when(accountsProcessingRequestMock.getErrorHandler()).thenReturn(getTestErrorHandler());
        final Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () -> executor.sendRoutineTask(accountsProcessingRequestMock));
        Assertions.assertEquals("maximum accounts limit (" + maxAccounts + ") exceeded", exception.getMessage());
    }

    @Test
    public void passOnAccountsLimitDisabled() {
        System.out.println("passOnAccountsLimitDisabled");
        final int maxAccounts = 0;
        final int initialDelay = 3;
        final int taskPeriod = 1;
        shouldPass(maxAccounts, initialDelay, taskPeriod);
    }

    @Test
    public void passOnAccountsLimitSatisfied() {
        System.out.println("passOnAccountsLimitSatisfied");
        final int maxAccounts = 2;
        final int initialDelay = 0;
        final int taskPeriod = 2;
        shouldPass(maxAccounts, initialDelay, taskPeriod);
    }

    private void shouldPass(final int maxAccounts, final int initialDelay, final int taskPeriod) {
        getExecutorForTest(
                internalScheduledExecutorMock,
                getNormalAccountProcessor(),
                maxAccounts, initialDelay, taskPeriod,
                getTestAccounts(), getTestErrorHandler())
                .sendRoutineTask(accountsProcessingRequestMock);
        Mockito.verify(internalScheduledExecutorMock, Mockito.only())
                .scheduleWithFixedDelay(Mockito.any(Runnable.class),
                        Mockito.eq(Long.valueOf(initialDelay)),
                        Mockito.eq(Long.valueOf(taskPeriod)),
                        Mockito.eq(TimeUnit.SECONDS));
    }

    @Test
    public void cancelCurrentTask() {
        System.out.println("cancelCurrentTask");
        final IExecutor executor = new ExecutorImpl(accountProcessorFactoryMock, internalScheduledExecutorMock, settingsMock, windowFactory);
        executor.cancelCurrentTask();
        Mockito.verify(internalScheduledExecutorMock, Mockito.only())
                .cancelCurrentTask();
    }

    @Test
    public void normalTaskProcessingOnRealPool() throws InterruptedException {
        System.out.println("normalTaskProcessingOnRealPool");
        final int maxAccounts = 2;
        final int initialDelay = 1;
        final int taskPeriod = 1;
        final int wait = initialDelay + taskPeriod;
        final Account[] testAccounts = getTestAccounts();
        final Consumer<Throwable> testErrorHandler = getTestErrorHandler();
        final RunnableCounter task = getNormalAccountProcessor();
        final IExecutor executor = getExecutorForTest(
                InternalScheduledExecutor.INSTANCE,
                task,
                maxAccounts, initialDelay, taskPeriod,
                testAccounts, testErrorHandler);
        executor.sendRoutineTask(accountsProcessingRequestMock);
        Thread.sleep(wait * 1000);
        executor.cancelCurrentTask();
        Assertions.assertTrue(task.getCounter() > 0);
        Mockito.verify(accountProcessorFactoryMock, Mockito.only()).routineProcessor(testAccounts, windowFactory, settingsMock);
    }

    @Test
    public void sequencedTaskProcessingOnRealPool() throws InterruptedException {
        System.out.println("sequencedTaskProcessingOnRealPool");
        final int maxAccounts = 2;
        final int initialDelay = 1;
        final int taskPeriod = 1;
        final int wait = initialDelay + taskPeriod;
        final Account[] testAccounts = getTestAccounts();
        final Consumer<Throwable> testErrorHandler = getTestErrorHandler();
        final RunnableCounter task = getNormalAccountProcessor();
        final IExecutor executor = getExecutorForTest(
                InternalScheduledExecutor.INSTANCE,
                task,
                maxAccounts, initialDelay, taskPeriod,
                testAccounts, testErrorHandler);
        executor.sendRoutineTask(accountsProcessingRequestMock);
        executor.sendRoutineTask(accountsProcessingRequestMock);
        Thread.sleep(wait * 1000);
        executor.cancelCurrentTask();
        Assertions.assertTrue(task.getCounter() > 0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void catchTaskExceptionOnRealPool() throws InterruptedException {
        System.out.println("catchTaskExceptionOnRealPool");
        final int maxAccounts = 2;
        final int initialDelay = 1;
        final int taskPeriod = 1;
        final int wait = initialDelay + taskPeriod;
        final Account[] testAccounts = getTestAccounts();
        final RunnableCounter task = getBrokenAccountProcessor();
        @SuppressWarnings("rawtypes") final Consumer errorHandlerMock = Mockito.mock(Consumer.class);
        final IExecutor executor = getExecutorForTest(
                InternalScheduledExecutor.INSTANCE,
                task,
                maxAccounts, initialDelay, taskPeriod,
                testAccounts, errorHandlerMock);
        executor.sendRoutineTask(accountsProcessingRequestMock);
        Thread.sleep(wait * 1000);
        Mockito.verify(errorHandlerMock, Mockito.only()).accept(Mockito.any(Throwable.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void needRetryExceptionProcessingOnRealPool() throws InterruptedException {
        System.out.println("needRetryExceptionProcessingOnRealPool");
        final int maxAccounts = 2;
        final int initialDelay = 1;
        final int taskPeriod = 1;
        final int wait = initialDelay + taskPeriod;
        final Account[] testAccounts = getTestAccounts();
        final RunnableCounter task = getNeedRetryAccountProcessor();
        @SuppressWarnings("rawtypes") final Consumer errorHandlerMock = Mockito.mock(Consumer.class);
        final IExecutor executor = getExecutorForTest(
                InternalScheduledExecutor.INSTANCE,
                task,
                maxAccounts, initialDelay, taskPeriod,
                testAccounts, errorHandlerMock);
        executor.sendRoutineTask(accountsProcessingRequestMock);
        Thread.sleep(wait * 1000 * 2);
        executor.cancelCurrentTask();
        Mockito.verify(errorHandlerMock, Mockito.never()).accept(Mockito.any(Throwable.class));
    }

    private IExecutor getExecutorForTest(
            final InternalScheduledExecutor internalExecutor,
            final RunnableCounter task,
            final int maxAccounts, final int initialDelay, final int taskPeriod,
            final Account[] testAccounts, final Consumer<Throwable> testErrorHandler
    ) {
        Mockito.when(accountProcessorFactoryMock.routineProcessor(Mockito.any(Account[].class), Mockito.any(WindowFactory.class), Mockito.any(Settings.class))).thenReturn(task);
        Mockito.when(applicationSettingsMock.maxAccountsSimultaneously()).thenReturn(maxAccounts);
        Mockito.when(applicationSettingsMock.taskPerformingDelay()).thenReturn(initialDelay);
        Mockito.when(applicationSettingsMock.scheduledTaskPeriod()).thenReturn(taskPeriod);
        Mockito.when(accountsProcessingRequestMock.getAccounts()).thenReturn(testAccounts);
        Mockito.when(accountsProcessingRequestMock.getErrorHandler()).thenReturn(testErrorHandler);
        return new ExecutorImpl(accountProcessorFactoryMock, internalExecutor, settingsMock, windowFactory);
    }

    private Account[] getTestAccounts() {
        return new Account[]{buildAccount("test-account-1"), buildAccount("test-account-2")};
    }

    private Account buildAccount(final String name) {
        return Account.builder()
                .name(name)
                .login("login")
                .password("password")
                .build();
    }

    private Consumer<Throwable> getTestErrorHandler() {
        return Throwable::printStackTrace;
    }

    private static class RunnableCounter implements Runnable {

        private final Runnable delegate;
        private int counter = 0;

        private RunnableCounter(final Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            counter++;
            delegate.run();
        }

        public int getCounter() {
            return counter;
        }
    }

    private RunnableCounter getNormalAccountProcessor() {
        return new RunnableCounter(() -> System.out.println("processing accounts: isInterrupted=" + Thread.currentThread().isInterrupted()));
    }

    private RunnableCounter getBrokenAccountProcessor() {
        return new RunnableCounter(() -> {
            System.out.println("processing accounts: isInterrupted=" + Thread.currentThread().isInterrupted());
            System.out.println("throwing exception");
            throw new UnsupportedOperationException();
        });
    }

    private RunnableCounter getNeedRetryAccountProcessor() {
        return new RunnableCounter(() -> {
            System.out.println("processing accounts: isInterrupted=" + Thread.currentThread().isInterrupted());
            System.out.println("throwing need retry exception");
            throw new NeedRetryException("need retry");
        });
    }

}