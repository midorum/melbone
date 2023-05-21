package midorum.melbone.ui.internal.settings.experimental;

import midorum.melbone.ui.internal.Context;

import javax.swing.*;
import java.awt.*;

public class RepeatableTasksPane extends JTabbedPane {

    public RepeatableTasksPane(final Context context) {

        final JComponent tasksPane = new TasksPane(context);
        final JComponent actionsPane = new ActionsPane(context);

        addTab("Tasks", tasksPane);
        addTab("Actions", actionsPane);

        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        setBackground(Color.red);//FIXME experimental marker

        setSelectedComponent(tasksPane);
    }
}
