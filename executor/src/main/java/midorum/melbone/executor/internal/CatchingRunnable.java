package midorum.melbone.executor.internal;

import midorum.melbone.model.exception.NeedRetryException;
import org.apache.logging.log4j.Logger;


import java.util.function.Consumer;

public class CatchingRunnable implements Runnable {

    private final Logger logger = StaticResources.LOGGER;
    private final Runnable delegate;
    private final Consumer<Throwable> errorHandler;

    public CatchingRunnable(final Runnable delegate, final Consumer<Throwable> errorHandler) {
        this.delegate = delegate;
        this.errorHandler = errorHandler;
    }

    @Override
    public void run() {
        try {
            delegate.run();
        } catch (NeedRetryException t) {
            logger.warn("caught need retry exception: ", t);
            // continue scheduled task execution
        } catch (Throwable t) {
            this.errorHandler.accept(t); // handling exception
            throw t; // break task execution
        }

    }
}
