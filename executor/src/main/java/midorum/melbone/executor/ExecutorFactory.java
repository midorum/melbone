package midorum.melbone.executor;

import dma.validation.Validator;
import midorum.melbone.executor.internal.ExecutorImpl;
import midorum.melbone.executor.internal.InternalScheduledExecutor;
import midorum.melbone.executor.internal.processor.AccountProcessorFactory;
import midorum.melbone.model.processing.IExecutor;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.WindowFactory;

public class ExecutorFactory {

    private final ExecutorImpl executor;

    private ExecutorFactory(final Settings settings, final WindowFactory windowFactory) {
        this.executor = new ExecutorImpl(AccountProcessorFactory.INSTANCE, InternalScheduledExecutor.INSTANCE, settings, windowFactory);
    }

    public IExecutor getExecutor() {
        return executor;
    }

    public static class Builder {

        private Settings settings;
        private WindowFactory windowFactory;

        public Builder settings(Settings settings) {
            this.settings = settings;
            return this;
        }

        public Builder windowFactory(final WindowFactory windowFactory) {
            this.windowFactory = windowFactory;
            return this;
        }

        public ExecutorFactory build() {
            return new ExecutorFactory(Validator.checkNotNull(settings).orThrowForSymbol("settings"),
                    Validator.checkNotNull(windowFactory).orThrowForSymbol("window factory"));
        }
    }
}
