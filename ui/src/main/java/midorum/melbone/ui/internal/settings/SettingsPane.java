package midorum.melbone.ui.internal.settings;

import com.midorum.win32api.hook.GlobalKeyHook;
import com.midorum.win32api.hook.KeyHookHelper;
import com.midorum.win32api.win32.IWinUser;
import com.midorum.win32api.win32.Win32VirtualKey;
import dma.validation.Validator;
import midorum.melbone.model.dto.KeyShortcut;
import midorum.melbone.model.settings.key.SettingKey;
import midorum.melbone.model.settings.key.SettingsManagerAction;
import midorum.melbone.settings.SettingKeys;
import midorum.melbone.ui.internal.Context;
import midorum.melbone.ui.internal.common.NoticePane;
import midorum.melbone.ui.internal.model.FrameStateOperations;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SettingsPane extends JPanel {

    private final FrameStateOperations ownerFrame;
    private final Context context;
    private final JComboBox<SettingKey> comboBox;
    private final JTextField textField;
    private final JLabel defaultValueLabel;
    private final JLabel descriptionLabel;
    private final NoticePane noticePane;

    public SettingsPane(final FrameStateOperations ownerFrame, final Context context) {
        super(false);
        this.ownerFrame = ownerFrame;
        this.context = context;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(500, 200));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        this.comboBox = createComboBox();
        this.descriptionLabel = createDescriptionLabel();
        this.defaultValueLabel = getDefaultValueLabel();
        this.textField = createValueTextField();
        this.noticePane = new NoticePane(context.logger());

        add(comboBox);
        add(Box.createVerticalStrut(5));
        add(createDescriptionPane());
        add(Box.createVerticalStrut(5));
        add(createDefaultValuePane());
        add(Box.createVerticalStrut(5));
        add(textField);
        add(noticePane);
        add(createActionPane());
    }

    private JComboBox<SettingKey> createComboBox() {
        final JComboBox<SettingKey> comboBox = new JComboBox<>(SettingKeys.values());
        comboBox.setName("select setting combo box");
        comboBox.setRenderer(new SettingKeyListCellRenderer());
        comboBox.setSelectedIndex(-1);
        comboBox.addActionListener(e -> getSelectedSettingsKey().ifPresent(this::displaySettingKeyInfo));
        return comboBox;
    }

    private JLabel createDescriptionLabel() {
        final JLabel label = new JLabel();
        label.setName("description label");
        return label;
    }

    private JLabel getDefaultValueLabel() {
        final JLabel label = new JLabel();
        label.setName("default value label");
        return label;
    }

    private JPanel createDescriptionPane() {
        final JPanel descriptionPane = new JPanel();
        descriptionPane.setLayout(new BoxLayout(descriptionPane, BoxLayout.LINE_AXIS));
        descriptionPane.add(descriptionLabel);
        return descriptionPane;
    }

    private JPanel createDefaultValuePane() {
        final JPanel defaultValuePane = new JPanel();
        defaultValuePane.setLayout(new BoxLayout(defaultValuePane, BoxLayout.LINE_AXIS));
        defaultValuePane.add(new JLabel("Default value: "));
        defaultValuePane.add(defaultValueLabel);
        return defaultValuePane;
    }

    private JTextField createValueTextField() {
        final JTextField textField = new JTextField();
        textField.setName("setting value field");
        textField.setHorizontalAlignment(SwingConstants.CENTER);
        return textField;
    }

    private JButton createSetDefaultButton() {
        JButton button = new JButton("Set default");
        button.setName("set default value button");
        button.addActionListener(e -> getSelectedSettingsKey().ifPresent(key ->
                key.internal().defaultValue().ifPresentOrElse(defaultValue -> {
                    textField.setText(stringifyValue(defaultValue));
                    context.settingStorage().write(key, defaultValue);
                    noticePane.showSuccess("Default value saved successfully");
                }, () -> noticePane.showError("This setting hasn't default value"))));
        return button;
    }

    private JButton createCaptureButton() {
        final JButton button = new JButton("Capture");
        button.setName("capture button");
        button.addActionListener(e -> getSelectedSettingsKey().ifPresent(key -> {
            final SettingsManagerAction action = key.internal().obtainWay().action();
            if (action.equals(SettingsManagerAction.noAction)) {
                noticePane.showError(action.description());
                return;
            }
            if (action.equals(SettingsManagerAction.pressHotkey)) {
                captureHotkey(key);
                return;
            }
            if (!context.standardDialogsProvider().askOkCancelConfirm(this, action.description(), "Capturing object"))
                return;
            switch (action) {
                case touchWindow -> captureWindow(key);
                case touchWindowElement -> captureWindowElement(key);
                case touchScreenElement -> captureScreen(key);
                default -> throw new UnsupportedOperationException("Unsupported action: " + action);
            }
        }));
        return button;
    }

    private JButton createSaveButton() {
        JButton button = new JButton("Save");
        button.setName("save button");
        button.addActionListener(e -> getSelectedSettingsKey().ifPresent(key -> {
            try {
                Validator.checkNotNull(textField.getText())
                        .andMap(String::trim)
                        .andCheckNot(String::isBlank)
                        .andMap(s -> key.internal().parser().apply(s))
                        .andCheck(v -> key.internal().checkValueType(v))
                        .andCheck(v -> key.internal().validator().test(v))
                        .thanDo(v -> {
                            context.settingStorage().write(key, v);
                            noticePane.showSuccess("Value saved successfully");
                        })
                        .elseDo(s -> noticePane.showError(stringifyValue(s) + " is not valid value"));
            } catch (Throwable t) {
                noticePane.showError("Wrong type (must be " + key.internal().type().getSimpleName() + ")", t);
            }
        }));
        return button;
    }

    private JPanel createActionPane() {
        final JPanel bottomPane = new JPanel();
        bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.LINE_AXIS));
        bottomPane.add(Box.createVerticalStrut(5));
        bottomPane.add(createSetDefaultButton());
        bottomPane.add(Box.createHorizontalStrut(5));
        bottomPane.add(createCaptureButton());
        bottomPane.add(Box.createHorizontalStrut(5));
        bottomPane.add(createSaveButton());
        return bottomPane;
    }

    private Optional<SettingKey> getSelectedSettingsKey() {
        return Validator.checkNotNull(comboBox.getSelectedItem())
                .andIsInstance(SettingKey.class)
                .cast(SettingKey.class)
                .asOptional();
    }

    private void displaySettingKeyInfo(SettingKey key) {
        descriptionLabel.setText(key.internal().description());
        defaultValueLabel.setText(key.internal().defaultValue().map(this::stringifyValue).orElse("[empty]"));
        textField.setText(loadKeyFromStorage(key).orElse(null));
    }

    private String stringifyValue(Object value) {
        if (value instanceof final int[] ints) {
            return Arrays.toString(ints);
        }
        if (value instanceof final String[] strings) {
            return Arrays.stream(strings)
                    .map(s -> "\"" + s + "\"")
                    .collect(Collectors.joining(",", "[", "]"));
        }
        if (value instanceof final Optional<?> o) {
            return o.isPresent() ? Objects.toString(o.get()) : "[empty]";
        }
        if (value instanceof final KeyShortcut o) {
            return o.toPrettyString();
        }
        return Objects.toString(value);
    }

    private Optional<String> loadKeyFromStorage(SettingKey key) {
        final Optional<String> maybeValue = context.settingStorage().read(key).map(this::stringifyValue);
        maybeValue.ifPresentOrElse(s -> noticePane.showInfo("Loaded from storage"),
                () -> noticePane.showInfo("Using default value"));
        return maybeValue;
    }

    private void captureHotkey(final SettingKey key) {
        final GlobalKeyHook.KeyEvent eventToBreakCapturing = new KeyHookHelper.KeyEventBuilder().virtualKey(Win32VirtualKey.VK_ESCAPE).withControl().build();
        noticePane.showInfo(key.internal().obtainWay().action().description() + " Press " + eventToBreakCapturing.toPrettyString() + " to cancel capturing.");
        context.keyHookHelper().capture(eventToBreakCapturing, KeyHookHelper.KeyEventComparator.byAltControlShiftCode,
                keyEvent -> {
                    textField.setText(KeyShortcut.fromKeyEvent(keyEvent).toPrettyString());
                    noticePane.showInfo("Captured. Please check and save value.");
                },
                keyEvent -> noticePane.showInfo("Hotkey was not captured."));
    }

    private void captureWindow(final SettingKey key) {
        ownerFrame.iconify();
        context.mouseHookHelper().setGlobalHookForKey(IWinUser.WM_LBUTTONDOWN,
                (mouseEvent) -> {
                    context.targetWindowOperations().getWindowByPoint(mouseEvent.point()).ifPresentOrElse(windowPoint -> {
                                textField.setText(stringifyValue(key.internal().obtainWay().extractor().apply(windowPoint)));
                                noticePane.showInfo("Captured. Please check and save value.");
                            },
                            () -> noticePane.showError("No foreground window was found"));
                    return true;
                },
                throwable -> {
                    noticePane.showError(throwable);
                    return true;
                },
                ownerFrame::restore);
    }

    private void captureWindowElement(final SettingKey key) {
        ownerFrame.iconify();
        context.mouseHookHelper().setGlobalHookForKey(IWinUser.WM_LBUTTONDOWN,
                (mouseEvent) -> {
                    context.targetWindowOperations().getWindowByPoint(mouseEvent.point()).ifPresentOrElse(windowPoint -> {
                                textField.setText(stringifyValue(key.internal().obtainWay().extractor().apply(windowPoint)));
                                noticePane.showInfo("Captured. Please check and save value.");
                            },
                            () -> noticePane.showError("No foreground window was found"));
                    return true;
                },
                throwable -> {
                    noticePane.showError(throwable);
                    return true;
                },
                ownerFrame::restore);
    }

    private void captureScreen(final SettingKey key) {
        ownerFrame.iconify();
        context.targetWindowOperations().minimizeAllWindows();
        context.mouseHookHelper().setGlobalHookForKey(IWinUser.WM_LBUTTONDOWN,
                (mouseEvent) -> {
                    textField.setText(stringifyValue(key.internal().obtainWay().extractor().apply(mouseEvent.point())));
                    noticePane.showInfo("Captured. Please check and save value.");
                    return true;
                },
                throwable -> {
                    noticePane.showError(throwable);
                    return true;
                },
                ownerFrame::restore);
    }

    private static class SettingKeyListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Object formattedString;
            if (value instanceof SettingKey key) {
                formattedString = key.internal().groupName() + "." + key.name();
            } else {
                formattedString = value;
            }
            return super.getListCellRendererComponent(list, formattedString, index, isSelected, cellHasFocus);
        }

    }
}
