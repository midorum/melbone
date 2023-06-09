package midorum.melbone.model.settings.key;

/**
 * Represents type of action to manage certain settings
 */
public enum SettingsManagerAction {
    noAction("There is no action defined for this key. Please insert value manually."),
    touchWindow("Switch to target window using Alt + Tab and left click at any point in it."),
    touchWindowElement("Switch to target window using Alt + Tab and left click on necessary element in it."),
    captureWindowElement("Switch to target window using Alt + Tab and select necessary area with rectangle." +
            "\nPress Ctrl + Enter to capture region."),
    touchScreenElement("Left click on necessary element on screen."),
    pressHotkey("Press hotkey it will be captured.");

    private final String description;

    SettingsManagerAction(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
