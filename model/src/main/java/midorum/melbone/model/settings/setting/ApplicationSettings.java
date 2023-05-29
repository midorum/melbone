package midorum.melbone.model.settings.setting;

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
     * Max duration the routine delay lasts in minutes
     *
     * @return value in minutes
     */
    long randomRoutineDelayMax();

    int stampDeviation();

    int actionsCount();

    boolean checkHealthBeforeLaunch();
}
