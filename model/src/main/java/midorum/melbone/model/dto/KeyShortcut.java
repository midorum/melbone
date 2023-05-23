package midorum.melbone.model.dto;

import com.midorum.win32api.win32.Win32VirtualKey;

import java.io.Serial;
import java.io.Serializable;

public class KeyShortcut implements Serializable {

    @Serial
    private static final long serialVersionUID=1L;

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
