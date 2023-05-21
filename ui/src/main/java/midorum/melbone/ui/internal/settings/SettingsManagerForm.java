package midorum.melbone.ui.internal.settings;

import midorum.melbone.ui.internal.Context;
import midorum.melbone.ui.internal.model.OnCloseNotificator;
import midorum.melbone.ui.internal.model.FrameVisibilityOperations;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SettingsManagerForm extends JFrame {

    private final TabbedPane tabbedPane;

    public SettingsManagerForm(final Context context, final OnCloseNotificator onCloseNotificator) {
        super("Settings Manager");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final FrameVisibilityOperations visibilityOperations = context.standardDialogsProvider().createFrameVisibilityOperations(this);
        this.tabbedPane = new TabbedPane(new TabComponents(visibilityOperations, context), context.propertiesProvider());

        add(tabbedPane);
        pack();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(final WindowEvent e) {
                onCloseNotificator.doOnClose();
            }
        });
    }

    public void display() {
        javax.swing.SwingUtilities.invokeLater(() -> setVisible(true));
    }

    public void display(final Tab selectedTab) {
        tabbedPane.setSelectedTab(selectedTab);
        display();
    }
}
