package midorum.melbone.executor.internal.processor;

import com.midorum.win32api.facade.exception.Win32ApiException;
import dma.function.VoidActionThrowing;
import midorum.melbone.executor.internal.StaticResources;
import midorum.melbone.model.exception.ControlledInterruptedException;
import midorum.melbone.model.window.baseapp.RestoredBaseAppWindow;
import midorum.melbone.window.WindowFactory;
import org.apache.logging.log4j.Logger;

public class OnRunningAccountAction implements VoidActionThrowing<InterruptedException> {

    private final Logger logger = StaticResources.LOGGER;
    private final WindowFactory windowFactory;

    OnRunningAccountAction(final WindowFactory windowFactory) {
        this.windowFactory = windowFactory;
    }

    @Override
    public void perform() {
        logger.info("on-running task started");
        windowFactory.getAllBaseAppWindows().forEach(baseAppWindow -> {
            try {
                final String characterName = baseAppWindow.getCharacterName().getOrThrow().orElse("unbound");
                logger.info("processing account {}", characterName);
                baseAppWindow.doInGameWindow(inGameBaseAppWindow -> {
                    //TODO привязать к расписанию по логину
                    logger.info("check in login tracker for {}", characterName);
                    inGameBaseAppWindow.checkInLoginTracker();
                    logger.info("check in login tracker for {} done", characterName);

                    logger.info("check in periodic action for {}", characterName);
                    inGameBaseAppWindow.checkInAction();
                    logger.info("check in periodic action for {} done", characterName);
                });
            } catch (InterruptedException e) {
                throw new ControlledInterruptedException(e);
            } catch (Win32ApiException e) {
                logger.error("got error while processing account - try close window", e);
                try {
                    baseAppWindow.restoreAndDo(RestoredBaseAppWindow::close);
                } catch (InterruptedException ex) {
                    throw new ControlledInterruptedException(e);
                } catch (Win32ApiException ex) {
                    logger.error("got error while closing window - skip", e);
                }
            }
        });
        logger.info("on-running task done");
    }
}
