package midorum.melbone.settings;

import midorum.melbone.model.settings.key.*;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.model.persistence.StorageKey;
import midorum.melbone.settings.internal.defining.SettingDataImpl;
import midorum.melbone.settings.internal.defining.SettingObtainWays;

import java.util.stream.Stream;

/**
 * Represents key under which stamps are stored
 */
public interface StampKeys {

    static StampKey[] values() {
        return Stream.of(TargetLauncher.values(), TargetBaseApp.values())
                .flatMap(Stream::of)
                .filter(e -> e.internal().isEnabled())
                .map(StampKey.class::cast)
                .toArray(StampKey[]::new);
    }

    enum TargetLauncher implements StampKey {
        clientIsAlreadyRunning("\"The client is already running\" window"),
        quitConfirmPopup("Quit confirm popup"),
        networkErrorDialog("network error dialog"),
        loginButtonNoErrorActive("Login button; No error; Active"),
        loginButtonNoErrorInactive("Login button; No error; Inactive"),
        loginButtonWithErrorActive("Login button; With error; Active"),
        loginButtonWithErrorInactive("Login button; With error; Inactive"),
        errorExclamationSign("Error exclamation sign"),
        playButtonActive("Play button; Active"),
        playButtonInactive("Play button; Inactive"),
        maintenanceNotice("Maintenance notice"),
        maintenanceInfoSign("Maintenance Info sign"),
        maintenanceInProgressPopup("Maintenance in progress popup");

        private final SettingData settingData;

        TargetLauncher(final String description) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(Stamp.class)
                    .description(description)
                    .storageKey(StorageKey.targetLauncherStamp)
                    .obtainWay(SettingObtainWays.captureWindowElementAndGetShot)
                    .build();
        }

        @Override
        public SettingData internal() {
            return settingData;
        }
    }

    enum TargetBaseApp implements StampKey {
        menuExitOption("Base window - in-game menu - Exit game option; Base scale"),
        optionsPopupCaption("Base window - Select server page - Options popup caption; Default scale"),
        optionsButtonDefaultScale("Base window - Select server page - Options button; Default scale"),
        optionsButtonBaseScale("Base window - Select server page - Options button; Base scale"),
        startButton("Base window - Select character page - Start game button; Base scale"),
        serverLineSelected("Base window - Select server page - Server line; Selected; Base scale"),
        serverLineUnselected("Base window - Select server page - Server line; Unselected; Base scale"),
        needRestartPopup("Need restart popup; Base scale"),
        disconnectedPopup("Disconnected popup; Base scale"),
        dailyTrackerPopupCaption("In-game daily tracker popup caption; Base scale"),
        accountInfoPopupCaption("Account info popup caption; Base scale");

        private final SettingData settingData;

        TargetBaseApp(final String description) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(Stamp.class)
                    .description(description)
                    .storageKey(StorageKey.targetBaseAppStamp)
                    .obtainWay(SettingObtainWays.captureWindowElementAndGetShot)
                    .build();
        }

        @Override
        public SettingData internal() {
            return settingData;
        }

    }

    enum Noop implements StampKey, NoopKey {
        noop("No operation. No stamp. No faith.");

        private final SettingData settingData;

        Noop(final String description) {
            this.settingData = new SettingDataImpl.Builder()
                    .type(Stamp.class)
                    .description(description)
                    .storageKey(StorageKey.noopStamp)
                    .build();
        }

        @Override
        public SettingData internal() {
            return settingData;
        }
    }

}
