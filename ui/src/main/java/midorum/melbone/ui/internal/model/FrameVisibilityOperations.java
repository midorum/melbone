package midorum.melbone.ui.internal.model;

/**
 * This interface provided to have able mock this operations
 */
public interface FrameVisibilityOperations extends FrameStateOperations {

    void show();

    void hide();

    boolean isVisible();
}
