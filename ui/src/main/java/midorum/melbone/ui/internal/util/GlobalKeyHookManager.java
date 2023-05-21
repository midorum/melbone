package midorum.melbone.ui.internal.util;

import com.midorum.win32api.hook.GlobalKeyHook;
import com.sun.jna.platform.win32.Win32VK;
import com.sun.jna.platform.win32.WinUser;
import dma.function.VoidAction;
import dma.validation.Validator;

import java.util.Comparator;
import java.util.function.Function;

public class GlobalKeyHookManager {

    private final static GlobalKeyHookManager INSTANCE = new GlobalKeyHookManager();

    private GlobalKeyHookManager() {
    }

    public static GlobalKeyHookManager getInstance() {
        return INSTANCE;
    }

    public void setHook(final GlobalKeyHook.KeyEvent keyEvent,
                        final KeyEventComparator comparator,
                        final Function<GlobalKeyHook.KeyEvent, Boolean> onCatchAction,
                        final Function<Throwable, Boolean> onErrorAction,
                        final VoidAction onHookReleaseAction) {
        final GlobalKeyHook.KeyEvent keyEventChecked = Validator.checkNotNull(keyEvent).orThrowForSymbol("keyEvent");
        final KeyEventComparator checkedComparator = Validator.checkNotNull(comparator).orThrowForSymbol("event comparator");
        final Function<GlobalKeyHook.KeyEvent, Boolean> onCatchActionChecked = Validator.checkNotNull(onCatchAction).orThrowForSymbol("onCatchAction");
        final Function<Throwable, Boolean> onErrorActionChecked = Validator.checkNotNull(onErrorAction).orThrowForSymbol("onErrorAction");
        final VoidAction onHookReleaseActionChecked = Validator.checkNotNull(onHookReleaseAction).orThrowForSymbol("onHookReleaseAction");
        new GlobalKeyHook((event, unhookRequest) -> {
            if (checkedComparator.compare(keyEventChecked, event) != 0) return false; //default dispatching
            boolean needUnhook = false;
            try {
                needUnhook = onCatchActionChecked.apply(event);
            } catch (Throwable t) {
                needUnhook = onErrorActionChecked.apply(t);
            } finally {
                if (needUnhook) {
                    unhookRequest.unhook();
                    onHookReleaseActionChecked.perform();
                }
            }
            //must catch both KEYUP and KEYDOWN messages to fully prevent dispatching message
            return event.eventType() == WinUser.WM_KEYUP || event.eventType() == WinUser.WM_KEYDOWN;
        });
    }

    public void setHook(final GlobalKeyHook.KeyEvent keyEvent,
                        final KeyEventComparator comparator,
                        final Function<GlobalKeyHook.KeyEvent, Boolean> onCatchAction,
                        final Function<Throwable, Boolean> onErrorAction) {
        setHook(keyEvent, comparator, onCatchAction, onErrorAction, () -> {
        });
    }

    public static class KeyEventBuilder {

        private int vkCode;
        private boolean shift;
        private boolean alt;
        private boolean control;
        private boolean capsLock;

        public KeyEventBuilder virtualKey(final Win32VK virtualKey) {
            this.vkCode = virtualKey.code;
            return this;
        }

        public KeyEventBuilder setShift(final boolean shift) {
            this.shift = shift;
            return this;
        }

        public KeyEventBuilder withShift() {
            return setShift(true);
        }

        public KeyEventBuilder setAlt(final boolean alt) {
            this.alt = alt;
            return this;
        }

        public KeyEventBuilder withAlt() {
            return setAlt(true);
        }

        public KeyEventBuilder setControl(final boolean control) {
            this.control = control;
            return this;
        }

        public KeyEventBuilder withControl() {
            return setControl(true);
        }

        public KeyEventBuilder setCapsLock(final boolean capsLock) {
            this.capsLock = capsLock;
            return this;
        }

        public GlobalKeyHook.KeyEvent build() {
            return new GlobalKeyHook.KeyEvent(-1, vkCode, -1, -1, -1, shift, alt, control, capsLock);
        }
    }

    public enum KeyEventComparator implements Comparator<GlobalKeyHook.KeyEvent> {
        byCode((o1, o2) -> {
            if (o1 == null) return -1;
            if (o2 == null) return -1;
            if (o1.vkCode() != o2.vkCode()) return -1;
            return 0;
        }),
        byAltShiftControlCode((o1, o2) -> {
            if (o1 == null) return -1;
            if (o2 == null) return -1;
            if (o1.vkCode() != o2.vkCode()) return -1;
            if (o1.alt() != o2.alt()) return -1;
            if (o1.control() != o2.control()) return -1;
            if (o1.shift() != o2.shift()) return -1;
            return 0;
        });

        private final Comparator<GlobalKeyHook.KeyEvent> comparator;

        KeyEventComparator(final Comparator<GlobalKeyHook.KeyEvent> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(final GlobalKeyHook.KeyEvent o1, final GlobalKeyHook.KeyEvent o2) {
            return comparator.compare(o1, o2);
        }
    }
}
