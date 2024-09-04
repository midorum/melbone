package midorum.melbone.window.internal.baseapp;

import com.midorum.win32api.facade.Either;
import com.midorum.win32api.facade.IProcess;
import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Win32System;
import com.midorum.win32api.facade.exception.Win32ApiException;
import midorum.melbone.model.settings.account.AccountBinding;
import midorum.melbone.model.settings.stamp.Stamps;
import midorum.melbone.model.window.baseapp.BaseAppWindow;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.window.internal.common.CommonWindowService;
import midorum.melbone.window.internal.util.StaticResources;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BaseAppWindowFactory {

    private final Logger logger = StaticResources.LOGGER;
    private final CommonWindowService commonWindowService;
    private final Win32System win32System;
    private final Settings settings;
    private final AccountBinding accountBinding;
    private final Stamps stamps;

    public BaseAppWindowFactory(final CommonWindowService commonWindowService,
                                final Settings settings,
                                final AccountBinding accountBinding,
                                final Stamps stamps) {
        this.commonWindowService = commonWindowService;
        this.win32System = commonWindowService.getWin32System();
        this.settings = settings;
        this.accountBinding = accountBinding;
        this.stamps = stamps;
    }

    public List<BaseAppWindow> getAllWindows() {
        final String windowTitle = settings.targetBaseAppSettings().windowTitle();
        final String windowClassName = settings.targetBaseAppSettings().windowClassName();
        logger.debug("get all windows with title - [{}] and class name - [{}]", windowTitle, windowClassName);
        return win32System.findAllWindows(windowTitle, windowClassName, false).stream()
                .map(window -> {
                    logger.debug("found window {} ({})", window.getText(), window.getSystemId());
                    return new BaseAppWindowImpl(window, commonWindowService, settings, accountBinding, stamps);
                })
                .collect(Collectors.toList());
    }

    public Optional<BaseAppWindow> findFirstUnboundWindow() {
        return findUnboundWindow().map(window -> new BaseAppWindowImpl(window, commonWindowService, settings, accountBinding, stamps));
    }

    public Optional<BaseAppWindow> findUnboundWindowAndBindWithAccount(final String characterName) throws Win32ApiException {
        try {
            final Optional<IWindow> unboundWindow = findUnboundWindow();
            unboundWindow.ifPresentOrElse(w -> {
                logger.info("found unbound base window ({}): bind with {}", w.getSystemId(), characterName);
                accountBinding.bindResource(characterName, commonWindowService.getUID(w).getOrThrow(ControlledWin32ApiException::new));
            }, () -> logger.info("unbound base window not found"));
            return unboundWindow.map(window -> new BaseAppWindowImpl(window, commonWindowService, settings, accountBinding, stamps));
        } catch (ControlledWin32ApiException e) {
            throw (Win32ApiException) e.getCause();
        }
    }

    public Either<List<IProcess>> listAllTargetProcesses() {
        return win32System.listProcessesWithName(settings.targetBaseAppSettings().processName());
    }

    private Optional<IWindow> findUnboundWindow() {
        final String windowTitle = settings.targetBaseAppSettings().windowTitle();
        final String windowClassName = settings.targetBaseAppSettings().windowClassName();
        logger.info("searching base window with title - [{}] and class name - [{}]", windowTitle, windowClassName);
        return win32System.findAllWindows(windowTitle, windowClassName, false).stream()
                .filter(window -> commonWindowService.getUID(window).map(uid -> accountBinding.getBoundAccount(uid).isEmpty()).getOrHandleError(e -> {
                    logger.warn("cannot check window attributes (" + window.getSystemId() + ") - skip", e);
                    return false;
                }))
                .filter(window -> commonWindowService.checkIfWindowRendered(window).getOrHandleError(e -> {
                    logger.warn("cannot check window attributes (" + window.getSystemId() + ") - skip", e);
                    return false;
                }))
                .findFirst();
    }

    private static class ControlledWin32ApiException extends RuntimeException {
        public ControlledWin32ApiException(final Win32ApiException cause) {
            super(cause);
        }
    }
}
