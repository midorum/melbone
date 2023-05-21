package midorum.melbone.settings.internal.obtaining.stamp;

import midorum.melbone.model.settings.stamp.TargetBaseAppStamps;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.settings.StampKeys;
import midorum.melbone.settings.internal.storage.KeyValueStorage;

public class TargetBaseAppStampsImpl extends StampValueExtractor implements TargetBaseAppStamps {

    public TargetBaseAppStampsImpl(final KeyValueStorage keyValueStorage) {
        super(keyValueStorage);
    }

    @Override
    public Stamp menuExitOption() {
        return getStamp(StampKeys.TargetBaseApp.menuExitOption);
    }

    @Override
    public Stamp disconnectedPopup() {
        return getStamp(StampKeys.TargetBaseApp.disconnectedPopup);
    }

    @Override
    public Stamp optionsButtonBaseScale() {
        return getStamp(StampKeys.TargetBaseApp.optionsButtonBaseScale);
    }

    @Override
    public Stamp optionsButtonDefaultScale() {
        return getStamp(StampKeys.TargetBaseApp.optionsButtonDefaultScale);
    }

    @Override
    public Stamp optionsPopupCaption() {
        return getStamp(StampKeys.TargetBaseApp.optionsPopupCaption);
    }

    @Override
    public Stamp needRestartPopup() {
        return getStamp(StampKeys.TargetBaseApp.needRestartPopup);
    }

    @Override
    public Stamp serverLineUnselected() {
        return getStamp(StampKeys.TargetBaseApp.serverLineUnselected);
    }

    @Override
    public Stamp serverLineSelected() {
        return getStamp(StampKeys.TargetBaseApp.serverLineSelected);
    }

    @Override
    public Stamp startButton() {
        return getStamp(StampKeys.TargetBaseApp.startButton);
    }

    @Override
    public Stamp dailyTrackerPopupCaption() {
        return getStamp(StampKeys.TargetBaseApp.dailyTrackerPopupCaption);
    }
}
