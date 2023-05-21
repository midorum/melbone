package midorum.melbone.ui.util;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SwingTestUtil {

    public static final SwingTestUtil INSTANCE = new SwingTestUtil();

    private SwingTestUtil() {
    }

    public Optional<Component> getChildNamed(final Component parent, final String name) {
        if (name.equals(parent.getName())) return Optional.of(parent);
        if (!(parent instanceof Container)) return Optional.empty();
        final Component[] components = parent instanceof JMenu ? ((JMenu) parent).getMenuComponents() : ((Container) parent).getComponents();
        for (Component c : components) {
            final Optional<Component> childNamed = getChildNamed(c, name);
            if (childNamed.isPresent()) return childNamed;
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T getChildNamedOrThrow(final Component parent, final String name, final Class<T> type) {
        final Optional<Component> maybeComponent = getChildNamed(parent, name);
        if (maybeComponent.isEmpty())
            throw new IllegalStateException("Parent component \""
                    + Optional.ofNullable(parent.getName()).orElse(parent.getClass().getSimpleName())
                    + "\" should have child component \"" + name + "\" but it wasn't found");
        final Component found = maybeComponent.get();
        if (!type.isInstance(found))
            throw new IllegalStateException("Component with name \"" + name + "\" is not instance of " + type);
        return (T) found;
    }

    public <T extends Component> List<T> getChildrenOfType(final Component parent, final Class<T> type) {
        final ArrayList<T> accumulator = new ArrayList<>();
        getChildrenOfType(parent, type, accumulator);
        return accumulator;
    }

    @SuppressWarnings("unchecked")
    private <T extends Component> void getChildrenOfType(final Component parent, final Class<T> type, final ArrayList<T> accumulator) {
        if (type.isInstance(parent)) accumulator.add((T) parent);
        if (!(parent instanceof Container)) return;
        final Component[] components = parent instanceof JMenu ? ((JMenu) parent).getMenuComponents() : ((Container) parent).getComponents();
        for (Component c : components) {
            getChildrenOfType(c, type, accumulator);
        }
    }
}
