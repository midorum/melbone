package midorum.melbone.executor.internal.processor;

import dma.util.Delay;
import dma.util.DurationFormatter;
import midorum.melbone.executor.internal.StaticResources;
import midorum.melbone.model.exception.ControlledInterruptedException;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.WindowFactory;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class RoutineAccountProcessor implements Runnable {

    private final Logger logger = StaticResources.LOGGER;
    private final LaunchAccountAction launchAccountAction;
    private final OnRunningAccountAction onRunningAccountAction;
    private final Settings settings;
    private final WindowFactory windowFactory;

    RoutineAccountProcessor(final LaunchAccountAction launchAccountAction,
                            final OnRunningAccountAction onRunningAccountAction,
                            final WindowFactory windowFactory,
                            final Settings settings) {
        this.launchAccountAction = launchAccountAction;
        this.onRunningAccountAction = onRunningAccountAction;
        this.windowFactory = windowFactory;
        this.settings = settings;
    }

    @Override
    public void run() {
        try {
            logger.info("routine task started");
            launchAccountAction.perform();
            doRandomDelay();
            if (thereIsStartedAccount()) onRunningAccountAction.perform();
            logger.info("routine task done");
        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
            throw new ControlledInterruptedException(e);
        }
    }

    private void doRandomDelay() throws InterruptedException {
        new Delay(settings.application().speedFactor())
                .randomSleep(0, settings.application().randomRoutineDelayMax(), TimeUnit.MINUTES,
                        duration -> logger.info("routine task delayed on {}",
                                new DurationFormatter(duration).toStringWithoutZeroParts()));
        logger.info("routine task continue execution");
    }

    private boolean thereIsStartedAccount() {
        return windowFactory.getAllBaseAppWindows().stream()
                .anyMatch(w -> w.getCharacterName().isPresent());
    }
}
