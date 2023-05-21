package midorum.melbone.ui.internal.util;

import com.midorum.win32api.hook.GlobalMouseKeyHook;
import dma.function.VoidAction;
import dma.validation.Validator;

import java.util.function.Function;

public class MouseKeyHookManager {

    private static final MouseKeyHookManager INSTANCE = new MouseKeyHookManager();

    private MouseKeyHookManager() {
    }

    public static MouseKeyHookManager getInstance() {
        return INSTANCE;
    }

    /**
     * Set global mouse hook
     *
     * @param eventType           event type to catch
     * @param onCatchAction       action performed when appropriate event caught; must return true to release the hook after processing or false to keep the hook active
     * @param onErrorAction       action performed when catch action throws exception; must return true to release the hook after processing or false to keep the hook active
     * @param onHookReleaseAction action performed when hook released any way regardless what happened
     */
    public void setHook(final int eventType,
                        final Function<GlobalMouseKeyHook.MouseEvent, Boolean> onCatchAction,
                        final Function<Throwable, Boolean> onErrorAction,
                        final VoidAction onHookReleaseAction) {
        final Function<GlobalMouseKeyHook.MouseEvent, Boolean> onCatchActionChecked = Validator.checkNotNull(onCatchAction).orThrowForSymbol("onCatchAction");
        final Function<Throwable, Boolean> onErrorActionChecked = Validator.checkNotNull(onErrorAction).orThrowForSymbol("onErrorAction");
        final VoidAction onHookReleaseActionChecked = Validator.checkNotNull(onHookReleaseAction).orThrowForSymbol("onHookReleaseAction");
        new GlobalMouseKeyHook((mouseEvent, unhookRequest) -> {
            if (mouseEvent.eventType() != eventType) return false; //default dispatching
            boolean needUnhook = false;
            try {
                needUnhook = onCatchActionChecked.apply(mouseEvent);
            } catch (Throwable t) {
                needUnhook = onErrorActionChecked.apply(t);
            } finally {
                if (needUnhook) {
                    unhookRequest.unhook();
                    onHookReleaseActionChecked.perform();
                }
            }
            return true; //prevent dispatching
        });
    }

    /**
     * Set global mouse hook
     *
     * @param eventType     event type to catch
     * @param onCatchAction action performed when appropriate event caught; must return true to release the hook after processing or false to keep the hook active
     * @param onErrorAction action performed when catch action throws exception; must return true to release the hook after processing or false to keep the hook active
     */
    public void setHook(final int eventType,
                        final Function<GlobalMouseKeyHook.MouseEvent, Boolean> onCatchAction,
                        final Function<Throwable, Boolean> onErrorAction) {
        setHook(eventType, onCatchAction, onErrorAction, () -> {
        });
    }
}
