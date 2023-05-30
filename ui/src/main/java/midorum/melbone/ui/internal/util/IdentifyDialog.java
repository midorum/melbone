package midorum.melbone.ui.internal.util;

import com.midorum.win32api.win32.IWinUser;
import dma.validation.Validator;
import midorum.melbone.model.window.baseapp.BaseAppWindow;
import midorum.melbone.ui.internal.Context;
import midorum.melbone.ui.internal.common.NoticePane;
import midorum.melbone.ui.internal.model.OnCloseNotificator;
import midorum.melbone.ui.internal.model.TargetWindowOperations;
import midorum.melbone.ui.internal.model.WindowHolder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.NoSuchElementException;
import java.util.Optional;

public class IdentifyDialog extends JDialog {

    private final Context context;
    private final TargetWindowOperations targetWindowOperations;
    private final JComboBox<String> comboBox;
    private final NoticePane noticePane;
    private final JButton checkWindowButton;
    private final JButton bindButton;
    private WindowHolder currentUnboundWindow;

    public IdentifyDialog(final Frame ownerFrame, final Context context, final OnCloseNotificator onCloseNotificator) {
        super(ownerFrame, "Identify windows", true);
        this.context = context;
        setSize(new Dimension(500, 300));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(getParent());
        setModal(true);

        this.targetWindowOperations = context.targetWindowOperations();

        this.comboBox = createComboBox();
        this.checkWindowButton = createCheckWindowButton();
        this.bindButton = createBindButton();
        this.noticePane = new NoticePane(context.logger());

        packFrame();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(final WindowEvent e) {
                onCloseNotificator.doOnClose();
            }
        });

        repaintForm();
    }

    public void display() {
        setVisible(true);
    }

    private void packFrame() {
        final JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(checkWindowButton);
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(bindButton);
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(createCloseButton());

        final JPanel actionPane = new JPanel();
        actionPane.setLayout(new BorderLayout(0, 5));
        actionPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        actionPane.add(comboBox, BorderLayout.CENTER);
        actionPane.add(buttonPane, BorderLayout.SOUTH);

        final JPanel mainPane = new JPanel();
        mainPane.setLayout(new BorderLayout());
        mainPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPane.add(createTipPane(), BorderLayout.NORTH);
        mainPane.add(actionPane, BorderLayout.CENTER);
        mainPane.add(noticePane, BorderLayout.SOUTH);
        add(mainPane);
    }

    private JPanel createTipPane() {
        final JLabel tipLabel = new JLabel("<html>" +
                "<body>" +
                "1 - press Check window button to select unbound window" +
                "<br>" +
                "2 - check window account and left click in it" +
                "<br>" +
                "3 - select appropriate account from list" +
                "<br>" +
                "4 - click Bind button" +
                "</body>" +
                "</html>");
        final JPanel tipPane = new JPanel();
        tipPane.add(tipLabel);
        return tipPane;
    }

    private JComboBox<String> createComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setName("select account combo box");
        comboBox.setSelectedIndex(-1);
        comboBox.addActionListener(e -> {
            bindButton.setEnabled(comboBox.getSelectedItem() != null);
        });
        return comboBox;
    }

    private JButton createButton(final String name, ActionListener actionListener) {
        JButton button = new JButton(name);
        button.addActionListener(actionListener);
        return button;
    }

    private JButton createCheckWindowButton() {
        final JButton button = createButton("Check window", e -> {
            try {
                currentUnboundWindow = WindowHolder.EMPTY;
                final BaseAppWindow baseAppWindow = targetWindowOperations.getFirstNotBoundWindow().orElseThrow();
                baseAppWindow.restoreAndDo(restoredBaseAppWindow -> context.mouseHookHelper().setGlobalHookForKey(
                        IWinUser.WM_LBUTTONDOWN,
                        (mouseEvent) -> {
                            updateComboBoxModel();
                            repaintFormForChoosingAccount(baseAppWindow);
                            return true;
                        }, throwable -> {
                            this.noticePane.showError(throwable);
                            return true;
                        }));
            } catch (NoSuchElementException ex) {
                this.noticePane.showError("Unbound window not found");
            } catch (InterruptedException ex) {
                this.noticePane.showError("caught unexpected interruption - try repeat");
            }
        });
        button.setName("check window button");
        return button;
    }

    private JButton createBindButton() {
        final JButton button = createButton("Bind", e -> getSelectedAccount().ifPresentOrElse(
                account -> this.currentUnboundWindow.getWindow().ifPresentOrElse(
                        baseAppWindow -> {
                            baseAppWindow.bindWithAccount(account);
                            updateComboBoxModel();
                            repaintForm();
                        },
                        () -> this.noticePane.showError("Window to bind is not selected")),
                () -> this.noticePane.showError("Account to bind is not selected")));
        button.setName("bind button");
        return button;
    }

    private JButton createCloseButton() {
        final JButton button = createButton("Close", e -> this.dispose());
        button.setName("close button");
        return button;
    }

    private void repaintForm() {
        if (targetWindowOperations.getNotBoundAccounts().isEmpty()) {
            noticePane.showInfo("There is no more unbound accounts");
            repaintFormForExit();
        } else if (targetWindowOperations.getFirstNotBoundWindow().isEmpty()) {
            noticePane.showInfo("There is no more unbound windows");
            repaintFormForExit();
        } else {
            noticePane.showInfo("Select unbound window");
            repaintFormForChoosingWindow();
        }
    }

    private Optional<String> getSelectedAccount() {
        return Validator.checkNotNull(comboBox.getSelectedItem())
                .andIsInstance(String.class)
                .cast(String.class)
                .asOptional();
    }

    private void repaintFormForChoosingWindow() {
        currentUnboundWindow = WindowHolder.EMPTY;
        checkWindowButton.setEnabled(true);
        bindButton.setEnabled(false);
        comboBox.setEnabled(false);
    }

    private void repaintFormForChoosingAccount(final BaseAppWindow selectedWindow) {
        currentUnboundWindow = new WindowHolder(selectedWindow);
        checkWindowButton.setEnabled(false);
        bindButton.setEnabled(false);
        comboBox.setEnabled(true);
        noticePane.showInfo("Window has been selected; choose appropriate account and click Bind button");
    }

    private void repaintFormForExit() {
        currentUnboundWindow = WindowHolder.EMPTY;
        checkWindowButton.setEnabled(false);
        bindButton.setEnabled(false);
        comboBox.setEnabled(false);
    }

    private void updateComboBoxModel() {
        final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addAll(targetWindowOperations.getNotBoundAccounts());
        this.comboBox.setModel(model);
    }
}
