package midorum.melbone.executor.internal.processor;

import com.midorum.win32api.facade.exception.Win32ApiException;
import com.midorum.win32api.util.Win32AccessDeniedException;
import com.midorum.win32api.util.Win32RuntimeException;
import dma.flow.Waiting;
import dma.function.VoidActionThrowing;
import dma.util.Delay;
import dma.util.DurationFormatter;
import midorum.melbone.executor.internal.StaticResources;
import midorum.melbone.model.dto.Account;
import midorum.melbone.model.exception.ControlledInterruptedException;
import midorum.melbone.model.exception.CriticalErrorException;
import midorum.melbone.model.exception.NeedRetryException;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.window.baseapp.BaseAppWindow;
import midorum.melbone.model.window.baseapp.RestoredBaseAppWindow;
import midorum.melbone.model.window.launcher.LauncherWindow;
import midorum.melbone.model.window.launcher.RestoredLauncherWindow;
import midorum.melbone.window.WindowFactory;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LaunchAccountAction implements VoidActionThrowing<InterruptedException> {

    private final Logger logger = StaticResources.LOGGER;
    private final Account[] accountsToLaunch;
    private final Settings settings;
    private final WindowFactory windowFactory;

    LaunchAccountAction(final Account[] accountsToLaunch, final WindowFactory windowFactory, final Settings settings) {
        this.accountsToLaunch = accountsToLaunch;
        this.settings = settings;
        this.windowFactory = windowFactory;
    }

    @Override
    public void perform() {
        logger.info("launch account task started");

        try {
            closeAllUnnecessaryWindowsAndCheckHealthForRest();
        } catch (Win32ApiException e) {
            throw new NeedRetryException("caught exception during launch", e);
        }
        final Set<String> alreadyLaunchedAccounts = getAlreadyLaunchedAccounts();
        logger.info("already launched accounts: {}", alreadyLaunchedAccounts);
        if (alreadyLaunchedAccounts.size() == accountsToLaunch.length) {
            logger.info("no new accounts to launch");
            return;
        }
        if (isTargetProcessesLimitReached()) {
            logger.warn("target processes limit is reached - cannot launch any new account");
            return;
        }
        Arrays.stream(accountsToLaunch).filter(account -> {
            final boolean needLaunch = !alreadyLaunchedAccounts.contains(account.name());
            logger.info("account {} ({}) {}", account.name(), account.login(),
                    needLaunch ? "need launch - processing" : "already launched - skip");
            return needLaunch;
        }).forEach(account -> {
            try {
                checkAndThrowIfIsInterrupted();//FIXME do we really need this check here?
                final Delay delay = new Delay(settings.application().speedFactor());
                launchAccount(account, delay);
                delay.sleep(10, TimeUnit.SECONDS);
            } catch (Win32AccessDeniedException | com.midorum.win32api.facade.exception.Win32AccessDeniedException e) {
                throw new CriticalErrorException("The program must run with administrator privileges", e);
            } catch (IllegalStateException | Win32RuntimeException | Win32ApiException e) {
                throw new NeedRetryException("caught exception during launch " + account.login() + ": " + e.getMessage(), e);
            } catch (InterruptedException e) {
                throw new ControlledInterruptedException(e);
            }
        });

        logger.info("launch account task done");
    }

    private boolean isTargetProcessesLimitReached() {
        return windowFactory.countAllTargetProcesses()
                .map(count -> count >= settings.application().maxAccountsSimultaneously())
                .getOrHandleError(e -> {
                    logger.error("cannot obtain target processes count", e);
                    return true;
                });
    }

    private void closeAllUnnecessaryWindowsAndCheckHealthForRest() throws Win32ApiException {
        logger.info("close all unbound and unnecessary windows and check health for rest");
        try {
            final Set<String> accountToLaunchSet = Arrays.stream(accountsToLaunch).map(Account::name).collect(Collectors.toSet());
            final Map<Boolean, List<BaseAppWindow>> windowsMap = windowFactory.getAllBaseAppWindows().stream()
                    .collect(Collectors.partitioningBy(w -> {
                        final Optional<String> characterName = w.getCharacterName();
                        return characterName.isEmpty() || !accountToLaunchSet.contains(characterName.get());
                    }));
            windowsMap.get(true).forEach(w -> {
                try {
                    logger.info("closing unnecessary window ({})", w.getCharacterName().orElse("unbound"));
                    w.restoreAndDo(RestoredBaseAppWindow::close);
                } catch (InterruptedException e) {
                    throw new ControlledInterruptedException(e);
                } catch (Win32ApiException e) {
                    throw new ControlledWin32ApiException(e);
                }
            });
            if (!settings.application().checkHealthBeforeLaunch()) return;
            windowsMap.get(false).forEach(w -> {
                try {
                    final String characterName = w.getCharacterName().orElse("unbound");
                    logger.info("check window health ({})", characterName);
                    w.doInGameWindow(inGameBaseAppWindow -> logger.info("({}) health checked", characterName));
                } catch (InterruptedException e) {
                    throw new ControlledInterruptedException(e);
                } catch (Win32ApiException e) {
                    throw new ControlledWin32ApiException(e);
                }
            });
        } catch (ControlledWin32ApiException e) {
            throw (Win32ApiException) e.getCause();
        }
    }

    private Set<String> getAlreadyLaunchedAccounts() {
        return windowFactory.getAllBaseAppWindows().stream()
                .map(BaseAppWindow::getCharacterName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private void checkAndThrowIfIsInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }

    private void launchAccount(final Account account, final Delay delay) throws InterruptedException, Win32ApiException {
        logger.info("launch account {} ({})", account.name(), account.login());
        findOrStartLauncherAndLoginAccount(account);
        waitForBaseAppWindowAndBindItWithAccount(account, delay)
                .orElseThrow(() -> new NeedRetryException("base window for login " + account.login() + " not found - need retry"))
                .restoreAndDo(openedBaseAppWindow -> {
                    logger.info("found base window for login {}", account.login());
                    openedBaseAppWindow.selectServer();
                    openedBaseAppWindow.chooseCharacter();
                    logger.info("account {} ({}) has been launched successfully", account.name(), account.login());
                    openedBaseAppWindow.checkInGameWindowRendered();
                });
    }

    private void findOrStartLauncherAndLoginAccount(final Account account) throws InterruptedException, Win32ApiException {
        windowFactory.findOrTryStartLauncherWindow()
                .orElseThrow(() -> new NeedRetryException("launcher not found - need retry"))
                .restoreAndDo(restoredLauncherWindow -> {
                    if (restoredLauncherWindow.checkClientIsAlreadyRunningWindowRendered()) {
                        //FIXME close launcher in this case instead of throwing exception
                        throw new CriticalErrorException("Maximum clients are already running");
                    }
                    restoredLauncherWindow.login(account);
                });
    }

    private Optional<BaseAppWindow> waitForBaseAppWindowAndBindItWithAccount(final Account account, final Delay delay) throws InterruptedException, Win32ApiException {
        //FIXME после логина лаунчер может долго обновляться, поэтому нужна настраиваемая задержка.
        // Это повторение сделано для обхода случая с обновлением после профилактики.
        //TODO сделать по человечески, когда научимся различать обновление и запуск игры.
        // Запуск игры (до появления окна) можно попробовать контролировать по запуску процесса.
        try {
            final Waiting.EmptyConsumer startAction = () -> {
                try {
                    checkLauncherIsReadyAndStartGame();
                } catch (Win32ApiException e) {
                    throw new ControlledWin32ApiException(e);
                }
                waitForApplicationCountControlWindowAndCloseIt(delay);
            };
            return new Waiting()
                    .latency(settings.targetBaseAppSettings().windowAppearingLatency(), TimeUnit.MILLISECONDS)
                    .timeout(settings.targetBaseAppSettings().windowAppearingTimeout(), TimeUnit.MILLISECONDS)
                    .withDelay(settings.targetBaseAppSettings().windowAppearingDelay(), TimeUnit.MILLISECONDS)
                    .startFrom(startAction)
                    .doOnEveryFailedIteration(i -> {
                        logger.debug("{}: base window not found yet", new DurationFormatter(i.fromStart()).toStringWithoutZeroParts());
                        startAction.accept();
                    })
                    .waitFor(() -> {
                        logger.info("check base window is appeared for login {}", account.login());
                        return windowFactory.findUnboundBaseAppWindowAndBindWithAccount(account.name());
                    });
        } catch (ControlledWin32ApiException e) {
            throw (Win32ApiException) e.getCause();
        }
    }

    private void checkLauncherIsReadyAndStartGame() throws InterruptedException, Win32ApiException {
        logger.info("check launcher is ready and start game");
        final Optional<LauncherWindow> maybeLauncherWindow = windowFactory.findLauncherWindow();
        if (maybeLauncherWindow.isPresent())
            maybeLauncherWindow.get().restoreAndDo(RestoredLauncherWindow::startGameWhenGetReady);
        else logger.info("launcher window not found");
    }

    //TODO replace with Waiting idiom
    private void waitForApplicationCountControlWindowAndCloseIt(final Delay delay) throws InterruptedException {
        delay.sleep(settings.targetCountControl().windowTimeout(), TimeUnit.SECONDS);
        checkApplicationsCountControlWindowAndCloseIt();
    }

    private void checkApplicationsCountControlWindowAndCloseIt() {
        logger.info("check applications count control window is appeared and close it");
        windowFactory.findApplicationsCountControlWindow()
                .ifPresentOrElse(applicationsCountControlWindow -> {
                            try {
                                applicationsCountControlWindow.clickConfirmButton();
                            } catch (InterruptedException e) {
                                throw new ControlledInterruptedException(e);
                            }
                        },
                        () -> logger.info("applications count control window not found"));
    }

    private static class ControlledWin32ApiException extends RuntimeException {
        public ControlledWin32ApiException(final Win32ApiException cause) {
            super(cause);
        }
    }
}
