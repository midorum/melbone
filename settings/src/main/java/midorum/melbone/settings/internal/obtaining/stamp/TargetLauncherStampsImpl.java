package midorum.melbone.settings.internal.obtaining.stamp;

import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.model.settings.stamp.TargetLauncherStamps;
import midorum.melbone.settings.StampKeys;
import midorum.melbone.settings.internal.storage.KeyValueStorage;

public class TargetLauncherStampsImpl extends StampValueExtractor implements TargetLauncherStamps {

    public TargetLauncherStampsImpl(final KeyValueStorage keyValueStorage) {
        super(keyValueStorage);
    }

    @Override
    public Stamp clientIsAlreadyRunning() {
        return getStamp(StampKeys.TargetLauncher.clientIsAlreadyRunning);
    }

    @Override
    public Stamp quitConfirmPopup() {
        return getStamp(StampKeys.TargetLauncher.quitConfirmPopup);
    }

    @Override
    public Stamp loginButtonNoErrorInactive() {
        return getStamp(StampKeys.TargetLauncher.loginButtonNoErrorInactive);
    }

    @Override
    public Stamp loginButtonWithErrorInactive() {
        return getStamp(StampKeys.TargetLauncher.loginButtonWithErrorInactive);
    }

    @Override
    public Stamp loginButtonNoErrorActive() {
        return getStamp(StampKeys.TargetLauncher.loginButtonNoErrorActive);
    }

    @Override
    public Stamp loginButtonWithErrorActive() {
        return getStamp(StampKeys.TargetLauncher.loginButtonWithErrorActive);
    }

    @Override
    public Stamp startButtonInactive() {
        return getStamp(StampKeys.TargetLauncher.playButtonInactive);
    }

    @Override
    public Stamp startButtonActive() {
        return getStamp(StampKeys.TargetLauncher.playButtonActive);
    }

    @Override
    public Stamp errorExclamationSign() {
        return getStamp(StampKeys.TargetLauncher.errorExclamationSign);
    }

    @Override
    public Stamp networkErrorDialog() {
        return getStamp(StampKeys.TargetLauncher.networkErrorDialog);
    }

    @Override
    public Stamp maintenanceNotice() {
        return getStamp(StampKeys.TargetLauncher.maintenanceNotice);
    }

    @Override
    public Stamp maintenanceInfoSign() {
        return getStamp(StampKeys.TargetLauncher.maintenanceInfoSign);
    }

    @Override
    public Stamp maintenanceInProgressPopup() {
        return getStamp(StampKeys.TargetLauncher.maintenanceInProgressPopup);
    }
}
