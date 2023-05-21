package midorum.melbone.ui.internal.model;

/**
 * This interface provided to have able mock this operations
 */
public interface FrameStateOperations {

    void iconify();

    void restore();

    boolean isIconified();
}
