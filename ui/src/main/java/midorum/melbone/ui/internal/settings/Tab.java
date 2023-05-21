package midorum.melbone.ui.internal.settings;

import java.awt.event.KeyEvent;

public enum Tab {
    settings("Settings", KeyEvent.VK_1),
    accounts("Accounts", KeyEvent.VK_2),
    stamps("Stamps", KeyEvent.VK_3),
    repeatableTasks("Repeatable tasks", KeyEvent.VK_4, false);

    private final String caption;
    private final int keyEvent;
    private final boolean inUse;

    Tab(final String caption, final int keyEvent, final boolean inUse) {
        this.caption = caption;
        this.keyEvent = keyEvent;
        this.inUse = inUse;
    }

    Tab(final String caption, final int keyEvent) {
        this.caption = caption;
        this.keyEvent = keyEvent;
        this.inUse = true;
    }

    public String caption() {
        return this.caption;
    }

    public int keyEvent() {
        return this.keyEvent;
    }

    public boolean inUse() {
        return this.inUse;
    }
}
