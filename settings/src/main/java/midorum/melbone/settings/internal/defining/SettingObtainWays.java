package midorum.melbone.settings.internal.defining;

import com.midorum.win32api.facade.WindowPoint;
import midorum.melbone.model.exception.CriticalErrorException;
import midorum.melbone.model.settings.key.SettingObtainWay;
import midorum.melbone.model.settings.key.SettingsManagerAction;

import java.util.function.Function;

public enum SettingObtainWays implements SettingObtainWay {
    insertManually(SettingsManagerAction.noAction, Function.identity()),
    touchWindowAndGetTitle(SettingsManagerAction.touchWindow, o -> ((WindowPoint) o).window().getText().orElseThrow(() -> new CriticalErrorException("Window hasn't title"))),
    touchWindowAndGetClassName(SettingsManagerAction.touchWindow, o -> ((WindowPoint) o).window().getClassName().orElseThrow(() -> new CriticalErrorException("Window hasn't class name"))),
    touchWindowAndGetProcessName(SettingsManagerAction.touchWindow, o -> ((WindowPoint) o).window().getProcess().name().orElseThrow(() -> new CriticalErrorException("Window process hasn't name"))),
    touchWindowAndGetDimensions(SettingsManagerAction.touchWindow, o -> ((WindowPoint) o).window().getWindowRectangle()),
    touchWindowAndGetRelativePoint(SettingsManagerAction.touchWindowElement, o -> ((WindowPoint) o).point()),
    touchScreenElementAndGetPoint(SettingsManagerAction.touchScreenElement, Function.identity()),
    captureWindowElementAndGetShot(SettingsManagerAction.captureWindowElement, Function.identity()),
    pressHotkey(SettingsManagerAction.pressHotkey, Function.identity());

    private final SettingsManagerAction action;
    private final Function<Object, Object> extractor;

    SettingObtainWays(final SettingsManagerAction action, final Function<Object, Object> extractor) {
        this.action = action;
        this.extractor = extractor;
    }

    @Override
    public SettingsManagerAction action() {
        return action;
    }

    @Override
    public Function<Object, Object> extractor() {
        return extractor;
    }
}
