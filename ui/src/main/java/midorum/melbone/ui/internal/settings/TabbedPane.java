package midorum.melbone.ui.internal.settings;

import midorum.melbone.model.settings.PropertiesProvider;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Optional;

public class TabbedPane extends JTabbedPane {

    private final TabComponents tabComponents;

    public TabbedPane(final TabComponents tabComponents, final PropertiesProvider propertiesProvider) {
        this.tabComponents = tabComponents;
        Arrays.stream(Tab.values())
                .filter(tab -> propertiesProvider.isModeSet("experimental") || tab.inUse())
                .forEachOrdered(tab -> {
                    final Component component = tabComponents.getComponent(tab);
                    addTab(tab.caption(), component);
                    setMnemonicAt(tab.ordinal(), tab.keyEvent());
                });
        //The following line enables to use scrolling tabs.
        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    public void setSelectedTab(final Tab selectedTab) {
        setSelectedComponent(tabComponents.getComponent(selectedTab));
    }

}
