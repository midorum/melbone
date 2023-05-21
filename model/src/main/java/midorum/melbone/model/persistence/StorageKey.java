package midorum.melbone.model.persistence;

/**
 * Represents storage collections
 */
public enum StorageKey {
    accounts,
    inUse,
    @Deprecated
    registryAccount,
    @Deprecated
    registryResource,
    registryAccount2,
    registryResource2,
    application,
    targetLauncher,
    targetCountControl,
    targetBaseApp,
    targetLauncherStamp,
    targetCountControlStamp,
    targetBaseAppStamp,
    noopStamp,
    uac,
    data,
    internal
}
