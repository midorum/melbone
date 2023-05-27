package midorum.melbone.model.dto;

import com.midorum.win32api.facade.HotKey;
import com.midorum.win32api.hook.GlobalKeyHook;
import com.midorum.win32api.win32.Win32VirtualKey;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class KeyShortcut implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;
    private final boolean shift;
    private final boolean alt;
    private final boolean control;

    private KeyShortcut(final int code, final boolean shift, final boolean alt, final boolean control) {
        this.code = code;
        this.shift = shift;
        this.alt = alt;
        this.control = control;
    }

    public int getCode() {
        return code;
    }

    public boolean isShift() {
        return shift;
    }

    public boolean isAlt() {
        return alt;
    }

    public boolean isControl() {
        return control;
    }

    @Override
    public String toString() {
        return "KeyShortcut{" +
                "code=" + code +
                ", shift=" + shift +
                ", alt=" + alt +
                ", control=" + control +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final KeyShortcut that = (KeyShortcut) o;
        return code == that.code && shift == that.shift && alt == that.alt && control == that.control;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, shift, alt, control);
    }

    public String toPrettyString() {
        return this.stringifyAltModifier() + this.stringifyControlModifier() + this.stringifyShiftModifier() + this.stringifySignificantKey();
    }

    private String stringifyAltModifier() {
        return this.alt ? "Alt+" : "";
    }

    private String stringifyControlModifier() {
        return this.control ? "Ctrl+" : "";
    }

    private String stringifyShiftModifier() {
        return this.shift ? "Shift+" : "";
    }

    private String stringifySignificantKey() {
        return Win32VirtualKey.fromValue(this.code).name().substring(3);
    }

    public static KeyShortcut fromKeyEvent(final GlobalKeyHook.KeyEvent keyEvent) {
        return new KeyShortcut(keyEvent.vkCode(), keyEvent.shift(), keyEvent.alt(), keyEvent.control());
    }

    public static KeyShortcut valueOf(final String s) {
        final String[] split = Objects.requireNonNull(s).split("\\+");
        final Builder builder = new Builder();
        for (String part : split) {
            if ("Alt".equals(part)) builder.withAlt();
            else if ("Ctrl".equals(part)) builder.withControl();
            else if ("Shift".equals(part)) builder.withShift();
            else builder.code(Win32VirtualKey.valueOf("VK_" + part.toUpperCase(Locale.ROOT)).code);
        }
        return builder.build();
    }

    public HotKey toHotKey() {
        return new HotKey.Builder().setShift(shift).setControl(control).setAlt(alt).code(code).build();
    }

    public static class Builder {

        private int code;
        private boolean shift;
        private boolean alt;
        private boolean control;

        public Builder code(final int code) {
            this.code = code;
            return this;
        }

        public Builder setShift(final boolean shift) {
            this.shift = shift;
            return this;
        }

        public Builder withShift() {
            return setShift(true);
        }

        public Builder setAlt(final boolean alt) {
            this.alt = alt;
            return this;
        }

        public Builder withAlt() {
            return setAlt(true);
        }

        public Builder setControl(final boolean control) {
            this.control = control;
            return this;
        }

        public Builder withControl() {
            return setControl(true);
        }

        public KeyShortcut build() {
            return new KeyShortcut(code, shift, alt, control);
        }
    }
}
