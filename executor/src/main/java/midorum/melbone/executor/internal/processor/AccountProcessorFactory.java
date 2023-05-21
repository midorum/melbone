package midorum.melbone.executor.internal.processor;

import midorum.melbone.model.dto.Account;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.WindowFactory;

public class AccountProcessorFactory {

    public static final AccountProcessorFactory INSTANCE = new AccountProcessorFactory();

    private AccountProcessorFactory() {
    }

    public Runnable routineProcessor(final Account[] accounts,
                                     final WindowFactory windowFactory,
                                     final Settings settings) {
        return new RoutineAccountProcessor(
                new LaunchAccountAction(accounts, windowFactory, settings),
                new OnRunningAccountAction(windowFactory),
                windowFactory,
                settings);
    }

}
