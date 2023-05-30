package midorum.melbone.ui.internal.main;

import com.midorum.win32api.hook.KeyHookHelper;
import com.midorum.win32api.win32.Win32VirtualKey;
import midorum.melbone.model.dto.Account;
import midorum.melbone.model.exception.OptionHasNoValue;
import midorum.melbone.model.processing.AccountsProcessingRequest;
import midorum.melbone.ui.internal.Context;
import midorum.melbone.ui.internal.common.NoticePane;
import midorum.melbone.ui.internal.model.*;
import midorum.melbone.ui.internal.settings.SettingsManagerForm;
import midorum.melbone.ui.internal.settings.Tab;
import midorum.melbone.ui.internal.util.IdentifyDialog;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MainForm extends JFrame {

    private final Context context;
    private final NoticePane noticePane;
    private final AccountsHolder accountsHolder;
    private final OnCloseNotificator onCloseNotificator;
    private final ArrayList<Updatable> updatableComponents = new ArrayList<>(2);
    private final FrameVisibilityOperations visibilityOperations;
    private final TaskExecutorOperationsImpl taskExecutorOperations;
    private final AccountsPane accountsPane;
    private final ActionPane actionPane;
    private Supplier<IdentifyDialog> identifyDialogSupplier = this::obtainAndCacheIdentifyDialog;
    private Supplier<SettingsManagerForm> settingsManagerFormSupplier = this::obtainAndCacheSettingsManagerForm;

    public MainForm(final String title, final Context context) {
        super(title);
        this.context = context;
        this.visibilityOperations = context.standardDialogsProvider().createFrameVisibilityOperations(this);
        this.taskExecutorOperations = new TaskExecutorOperationsImpl(visibilityOperations);
        this.onCloseNotificator = () -> {
            updateFrame();
            if (!visibilityOperations.isVisible()) visibilityOperations.show();
            if (visibilityOperations.isIconified()) visibilityOperations.restore();
        };
        this.noticePane = new NoticePane(this.context.logger());
        this.accountsPane = new AccountsPane(context.accountStorage(),
                context.targetWindowOperations(),
                context.settings().application(),
                context.dataLoader(),
                context.logger());
        this.accountsHolder = accountsPane;
        this.actionPane = new ActionPane(accountsHolder,
                taskExecutorOperations,
                new SettingsManagerOperationsImpl(visibilityOperations),
                this.context.targetWindowOperations(),
                () -> {
                    noticePane.clearNotice();
                    identifyDialogSupplier.get().display();
                },
                context.logger());
        this.updatableComponents.add(accountsPane);
        this.updatableComponents.add(actionPane);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(500, 500));//TODO restore last user preferred dimension
        setComponentsSize();
        //highlightComponents();//for debug purposes

        final JPanel containerPanel = new JPanel();
        containerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.PAGE_AXIS));
        containerPanel.add(accountsPane);
        containerPanel.add(noticePane);
        containerPanel.add(actionPane);
        add(containerPanel);
        pack();
    }

    public void display() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            updateFrame();
            setCancelTaskHook();
            visibilityOperations.show();
        });
    }

    private void setCancelTaskHook() {
        context.keyHookHelper().setGlobalHook(
                new KeyHookHelper.KeyEventBuilder().virtualKey(Win32VirtualKey.VK_S).withControl().withShift().build(),
                KeyHookHelper.KeyEventComparator.byAltControlShiftCode,
                keyEvent -> {
                    taskExecutorOperations.cancelCurrentTask();
                    return false; // keep hook
                }, throwable -> {
                    noticePane.showError(throwable);
                    return false; // keep hook
                });
    }

    private void updateFrame() {
        updatableComponents.forEach(updatable -> {
            try {
                updatable.update();
            } catch (OptionHasNoValue e) {
                context.logger().warn("Option has not found in storage:", e);
                noticePane.showError("Option has not found in storage: " + e.getMessage());
            }
        });
    }

    private void setComponentsSize() {
        //accountsPane is resizable
        setPreferredHeight(noticePane, 30);
        setMaximumHeight(noticePane, 30);
        setPreferredHeight(actionPane, 60);
        setMaximumHeight(actionPane, 60);
    }

    private void highlightComponents() {
        setBorderColor(accountsPane, Color.red);
        setBorderColor(noticePane, Color.red);
        setBorderColor(actionPane, Color.red);
    }

    private void setPreferredHeight(final JComponent component, final int preferredHeight) {
        final Dimension preferredSize = component.getPreferredSize();
        preferredSize.height = preferredHeight;
        component.setPreferredSize(preferredSize);
    }

    private void setMaximumHeight(final JComponent component, final int maximumHeight) {
        final Dimension maximumSize = component.getMaximumSize();
        maximumSize.height = maximumHeight;
        component.setMaximumSize(maximumSize);
    }

    private void setBorderColor(final JComponent component, final Color color) {
        component.setBorder(new LineBorder(color));
    }

    private Frame self() {
        return this;
    }

    private synchronized IdentifyDialog obtainAndCacheIdentifyDialog() {
        class Factory implements Supplier<IdentifyDialog> {
            private final IdentifyDialog identifyDialog;

            Factory() {
                this.identifyDialog = context.standardDialogsProvider().createIdentifyDialog(self(), context, onCloseNotificator);
            }

            @Override
            public IdentifyDialog get() {
                return this.identifyDialog;
            }
        }

        if (!(this.identifyDialogSupplier instanceof Factory)) {
            this.identifyDialogSupplier = new Factory();
        }
        return this.identifyDialogSupplier.get();
    }

    private synchronized SettingsManagerForm obtainAndCacheSettingsManagerForm() {
        class Factory implements Supplier<SettingsManagerForm> {
            private final SettingsManagerForm settingsManagerForm;

            Factory() {
                this.settingsManagerForm = context.standardDialogsProvider().createSettingsManagerForm(context, onCloseNotificator);
            }

            @Override
            public SettingsManagerForm get() {
                return this.settingsManagerForm;
            }
        }

        if (!(this.settingsManagerFormSupplier instanceof Factory)) {
            this.settingsManagerFormSupplier = new Factory();
        }
        return this.settingsManagerFormSupplier.get();
    }

    private class TaskExecutorOperationsImpl implements TaskExecutorOperations {


        private final FrameStateOperations frameStateOperations;

        private TaskExecutorOperationsImpl(final FrameStateOperations frameStateOperations) {
            this.frameStateOperations = frameStateOperations;
        }

        @Override
        public void sendRoutineTask() {
            noticePane.clearNotice();
            frameStateOperations.iconify();
            try {
                context.taskExecutor().sendRoutineTask(getAccountsProcessingRequest());
            } catch (Throwable t) {
                noticePane.showError(t);
                frameStateOperations.restore();
            }
        }

        @Override
        public void cancelCurrentTask() {
            noticePane.clearNotice();
            try {
                context.taskExecutor().cancelCurrentTask();
                noticePane.showInfo("Current task cancelled");
                updateFrame();
            } catch (Throwable t) {
                noticePane.showError(t);
            } finally {
                frameStateOperations.restore();
            }
        }

        private AccountsProcessingRequest getAccountsProcessingRequest() {
            return new AccountsProcessingRequest() {
                @Override
                public Account[] getAccounts() {
                    return accountsHolder.getSelectedAccounts();
                }

                @Override
                public Consumer<Throwable> getErrorHandler() {
                    return throwable -> {
                        noticePane.showError(throwable);
                        frameStateOperations.restore();
                    };
                }
            };
        }

    }

    private class SettingsManagerOperationsImpl implements SettingsManagerOperations {

        private final FrameVisibilityOperations frameVisibilityOperations;

        private SettingsManagerOperationsImpl(final FrameVisibilityOperations frameVisibilityOperations) {
            this.frameVisibilityOperations = frameVisibilityOperations;
        }

        @Override
        public void showSettingsTab() {
            noticePane.clearNotice();
            frameVisibilityOperations.hide();
            settingsManagerFormSupplier.get().display();//FIXME check releasing resources after dialog closed
        }

        @Override
        public void showAccountsTab() {
            noticePane.clearNotice();
            frameVisibilityOperations.hide();
            settingsManagerFormSupplier.get().display(Tab.accounts);//FIXME check releasing resources after dialog closed
        }

    }
}
