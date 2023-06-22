package midorum.melbone.executor.internal;

import dma.validation.Validator;
import midorum.melbone.executor.internal.processor.AccountProcessorFactory;
import midorum.melbone.model.dto.Account;
import midorum.melbone.model.processing.AccountsProcessingRequest;
import midorum.melbone.model.processing.IExecutor;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.WindowFactory;

import java.util.concurrent.*;
import java.util.function.Consumer;

@SuppressWarnings("ClassCanBeRecord")
public class ExecutorImpl implements IExecutor {

    private final InternalScheduledExecutor internalExecutor;
    private final Settings settings;
    private final AccountProcessorFactory accountProcessorFactory;
    private final WindowFactory windowFactory; //FIXME get rid transitive dependency

    public ExecutorImpl(final AccountProcessorFactory accountProcessorFactory,
                        final InternalScheduledExecutor internalExecutor,
                        final Settings settings,
                        final WindowFactory windowFactory) {
        this.accountProcessorFactory = accountProcessorFactory;
        this.internalExecutor = internalExecutor;
        this.settings = settings;
        this.windowFactory = windowFactory;
    }

    @Override
    public void sendRoutineTask(final AccountsProcessingRequest request) {
        final AccountsProcessingRequest checkedRequest = Validator.checkNotNull(request).orThrowForSymbol("request");
        final int maxAccountsSimultaneously = settings.application().maxAccountsSimultaneously();
        final Account[] accounts = Validator.checkNotNull(checkedRequest.getAccounts())
                .andCheck(arr -> maxAccountsSimultaneously <= 0 || arr.length <= maxAccountsSimultaneously)
                .orThrow("maximum accounts limit (" + maxAccountsSimultaneously + ") exceeded");
        final Consumer<Throwable> errorHandler = Validator.checkNotNull(checkedRequest.getErrorHandler()).orThrowForSymbol("task error handler");
        internalExecutor.scheduleWithFixedDelay(
                new CatchingRunnable(accountProcessorFactory.routineProcessor(accounts, windowFactory, settings), errorHandler),
                settings.application().taskPerformingDelay(),
                settings.application().scheduledTaskPeriod(),
                TimeUnit.SECONDS);
    }

    @Override
    public void cancelCurrentTask() {
        internalExecutor.cancelCurrentTask();
    }

}
