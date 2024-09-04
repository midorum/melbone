package midorum.melbone.model.settings.setting;

import java.util.Set;

public interface ApplicationSettings {

    /**
     * Maximum of simultaneously accounts can run
     *
     * @return count of maximum accounts
     */
    int maxAccountsSimultaneously();

    /**
     * Delay before start scheduled task in seconds
     *
     * @return value in seconds
     */
    int taskPerformingDelay();

    /**
     * Delay between scheduled task runs in seconds
     *
     * @return value in seconds
     */
    int scheduledTaskPeriod();

    /**
     * Factor to reduce or increase delay duration
     *
     * @return factor
     */
    float speedFactor();

    /**
     * Minimal delay between mouse positioning and click in milliseconds. Use when target window does not accept mouse clicks.
     *
     * @return delay
     */
    long mouseClickDelay();

    /**
     * Max duration the routine delay lasts in minutes
     *
     * @return value in minutes
     */
    long randomRoutineDelayMax();

    int stampDeviation();

    int actionsCount();

    boolean checkHealthBeforeLaunch();

    boolean closeOverlappingWindows();

    boolean shotOverlappingWindows();

    int bringWindowForegroundTimeout();

    int bringWindowForegroundDelay();

    String[] overlappingWindowsToSkip();

    String[] overlappingWindowsToClose();
}
