package midorum.melbone.ui.internal.settings;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.facade.exception.Win32ApiException;
import com.midorum.win32api.win32.IWinUser;
import dma.validation.Validator;
import midorum.melbone.model.settings.key.SettingsManagerAction;
import midorum.melbone.model.settings.key.StampKey;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.settings.StampKeys;
import midorum.melbone.settings.managment.StampBuilder;
import midorum.melbone.ui.internal.Context;
import midorum.melbone.ui.internal.common.NoticePane;
import midorum.melbone.ui.internal.model.FrameStateOperations;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

public class StampsPane extends JPanel {

    private final Context context;
    private final FrameStateOperations ownerFrame;
    private final JComboBox<StampKey> comboBox;
    private final JLabel keyDescriptionLabel;
    private final JLabel stampKeyLabel;
    private final JLabel stampDescriptionLabel;
    private final JLabel stampWholeDataLabel;
    private final JLabel stampFirstLineLabel;
    private final JLabel stampLocationLabel;
    private final JLabel stampWindowRectLabel;
    private final JLabel stampWindowClientRectLabel;
    private final JLabel stampWindowClientToScreenRectLabel;
    private final NoticePane noticePane;

    public StampsPane(final FrameStateOperations ownerFrame, final Context context) {
        super(false);
        this.ownerFrame = ownerFrame;
        this.context = context;

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(500, 200));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        this.comboBox = createComboBox();
        this.keyDescriptionLabel = createKeyDescriptionLabel();
        this.stampKeyLabel = createStampKeyLabel();
        this.stampDescriptionLabel = createStampDescriptionLabel();
        this.stampWholeDataLabel = createStampWholeDataLabel();
        this.stampFirstLineLabel = createStampFirstLineLabel();
        this.stampLocationLabel = createStampLocationLabel();
        this.stampWindowRectLabel = createStampWindowRectLabel();
        this.stampWindowClientRectLabel = createStampWindowClientRectLabel();
        this.stampWindowClientToScreenRectLabel = createStampWindowClientToScreenRectLabel();
        this.noticePane = new NoticePane(context.logger());

        add(comboBox, BorderLayout.NORTH);
        add(Box.createVerticalStrut(5));
        add(createDescriptionPane(), BorderLayout.WEST);
        add(Box.createVerticalStrut(5));
        add(createBottomPane(), BorderLayout.SOUTH);
    }

    private JComboBox<StampKey> createComboBox() {
        JComboBox<StampKey> comboBox = new JComboBox<>(StampKeys.values());
        comboBox.setName("select setting combo box");
        comboBox.setRenderer(new StampKeyListCellRenderer());
        comboBox.setSelectedIndex(-1);
        comboBox.addActionListener(e -> getSelectedStampKey().ifPresent(this::displayStampKeyInfo));
        return comboBox;
    }

    private JLabel createKeyDescriptionLabel() {
        final JLabel label = new JLabel();
        label.setName("key description label");
        return label;
    }

    private JLabel createStampKeyLabel() {
        final JLabel label = new JLabel();
        label.setName("stamp key label");
        return label;
    }

    private JLabel createStampDescriptionLabel() {
        final JLabel label = new JLabel();
        label.setName("stamp description label");
        return label;
    }

    private JLabel createStampWholeDataLabel() {
        final JLabel label = new JLabel();
        label.setName("stamp whole data label");
        return label;
    }

    private JLabel createStampFirstLineLabel() {
        final JLabel label = new JLabel();
        label.setName("stamp first line label");
        return label;
    }

    private JLabel createStampLocationLabel() {
        final JLabel label = new JLabel();
        label.setName("stamp location label");
        return label;
    }

    private JLabel createStampWindowRectLabel() {
        final JLabel label = new JLabel();
        label.setName("stamp window rect label");
        return label;
    }

    private JLabel createStampWindowClientRectLabel() {
        final JLabel label = new JLabel();
        label.setName("stamp window client rect label");
        return label;
    }

    private JLabel createStampWindowClientToScreenRectLabel() {
        final JLabel label = new JLabel();
        label.setName("stamp window client-to-screen rect label");
        return label;
    }

    private JButton createShowButton() {
        final JButton button = new JButton("Show");
        button.setName("show stamp button");
        button.addActionListener(e -> getSelectedStampKey().flatMap(this::loadKeyFromStorage).ifPresent(stamp ->
                context.standardDialogsProvider().showStampPreviewDialog(stamp)));
        return button;
    }

    private JButton createCaptureButton() {
        final JButton button = new JButton("Capture");
        button.setName("capture stamp button");
        button.addActionListener(e -> getSelectedStampKey().ifPresentOrElse(key -> {
            final SettingsManagerAction action = key.internal().obtainWay().action();
            if (action.equals(SettingsManagerAction.noAction)) {
                noticePane.showError(action.description());
                return;
            }
            if (!context.standardDialogsProvider().askOkCancelConfirm(this, action.description(), "Capturing object"))
                return;
            if (action == SettingsManagerAction.captureWindowElement) {
                captureWindowRegion(key);
            } else {
                throw new UnsupportedOperationException();
            }
        }, () -> noticePane.showError("You should select stamp to capture")));
        return button;
    }

    private JPanel createDescriptionPane() {
        JPanel descriptionPane = new JPanel();
        descriptionPane.setLayout(new BoxLayout(descriptionPane, BoxLayout.Y_AXIS));
        descriptionPane.add(keyDescriptionLabel);
        descriptionPane.add(stampKeyLabel);
        descriptionPane.add(stampDescriptionLabel);
        descriptionPane.add(stampWholeDataLabel);
        descriptionPane.add(stampFirstLineLabel);
        descriptionPane.add(stampLocationLabel);
        descriptionPane.add(stampWindowRectLabel);
        descriptionPane.add(stampWindowClientRectLabel);
        descriptionPane.add(stampWindowClientToScreenRectLabel);
        return descriptionPane;
    }

    private JPanel createBottomPane() {
        JPanel bottomPane = new JPanel();
        bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.LINE_AXIS));
        bottomPane.add(Box.createVerticalStrut(5));
        bottomPane.add(noticePane);
        bottomPane.add(Box.createHorizontalStrut(5));
        bottomPane.add(createShowButton());
        bottomPane.add(Box.createHorizontalStrut(5));
        bottomPane.add(createCaptureButton());
        return bottomPane;
    }

    private Optional<StampKey> getSelectedStampKey() {
        return Validator.checkNotNull(comboBox.getSelectedItem())
                .andIsInstance(StampKey.class)
                .cast(StampKey.class)
                .asOptional();
    }

    private Optional<Stamp> loadKeyFromStorage(final StampKey key) {
        return context.settingStorage().read(key);
    }

    private void captureWindowRegion(final StampKey key) {
        context.mouseHookHelper().setGlobalHookForKey(IWinUser.WM_LBUTTONDOWN,
                mouseEvent -> {
                    context.targetWindowOperations().getWindowByPoint(mouseEvent.point()).ifPresentOrElse(windowPoint -> {
                        final IWindow window = windowPoint.window();
                        try {
                            window.bringForeground();
                            window.moveWindow(0, 0); //FIXME убрать, когда научимся брать метрики без позиционирования окна
                            context.standardDialogsProvider().captureRectangle(maybeRectangle -> maybeRectangle.ifPresentOrElse(capturedRectangle -> {
                                try {
                                    window.getWindowRectangle()
                                            .flatMap(wr -> window.getClientRectangle()
                                                    .flatMap(cr -> window.getClientToScreenRectangle()
                                                            .map(csr -> new WindowRectangles(wr, cr, csr))))
                                            .map(windowRectangles -> captureStamp(key, windowRectangles, capturedRectangle))
                                            .consumeOrThrow(stamp -> {
                                                displayStampInfo(stamp);
                                                noticePane.showInfo("Captured");
                                                //FIXME need preview before save
                                                saveStampToStorage(key, stamp);
                                                noticePane.showSuccess("Stamp saved successfully");
                                                ownerFrame.restore();
                                            });
                                } catch (Win32ApiException e) {
                                    context.logger().error("cannot get window attributes (" + window.getSystemId() + ")", e);
                                    noticePane.showError("Cannot get window attributes");
                                }
                            }, () -> noticePane.showError("No rectangle was captured")));
                        } catch (Win32ApiException e) {
                            context.logger().error("cannot adjust target window (" + window.getSystemId() + ")", e);
                            noticePane.showError("Cannot adjust target window");
                        }
                    }, () -> noticePane.showError("No foreground window was found"));
                    return true;
                },
                throwable -> {
                    noticePane.showError(throwable);
                    return true;
                });
    }

    private Stamp captureStamp(StampKey key, WindowRectangles windowRectangles, Rectangle rectangle) {
        final BufferedImage image = takeRectangleShot(rectangle);
        final int[] wholeData = image.getRGB(
                image.getMinX(), image.getMinY(),
                image.getWidth(), image.getHeight(),
                null, 0,
                image.getWidth());
        final int[] firstLine = image.getRGB(
                image.getMinX(), image.getMinY(),
                image.getWidth(), 1,
                null, 0,
                image.getWidth());
        return new StampBuilder()
                .key(key)
                .description(key.internal().description())
                .wholeData(wholeData)
                .firstLine(firstLine)
                .location(rectangle)
                .windowRect(windowRectangles.windowRectangle)
                .windowClientRect(windowRectangles.clientRectangle)
                .windowClientToScreenRect(windowRectangles.clientToScreenRectangle)
                .build();
    }

    private BufferedImage takeRectangleShot(final Rectangle rectangle) {
        return context.getScreenShotMaker().takeRectangle(Validator.checkNotNull(rectangle).orThrow());
    }

    private void saveStampToStorage(final StampKey key, final Stamp stamp) {
        context.settingStorage().write(key, stamp);
    }

    private void displayStampKeyInfo(final StampKey key) {
        keyDescriptionLabel.setText(key.internal().description());
        loadKeyFromStorage(key).ifPresentOrElse(stamp -> {
            displayStampInfo(stamp);
            noticePane.showInfo("Loaded from storage");
        }, () -> {
            clearStampInfo();
            noticePane.showInfo("Not provided yet");
        });
    }

    private void displayStampInfo(Stamp stamp) {
        stampKeyLabel.setText("key: " + stamp.key().internal().groupName() + "." + stamp.key().name());
        stampDescriptionLabel.setText("description: " + stamp.description());
        stampWholeDataLabel.setText("whole data size: " + stamp.wholeData().length);
        stampFirstLineLabel.setText("first line size: " + stamp.firstLine().length);
        stampLocationLabel.setText("location: " + rectangleToString(stamp.location()));
        stampWindowRectLabel.setText("windowRect: " + rectangleToString(stamp.windowRect()));
        stampWindowClientRectLabel.setText("windowClientRect: " + rectangleToString(stamp.windowClientRect()));
        stampWindowClientToScreenRectLabel.setText("windowClientToScreenRect: " + rectangleToString(stamp.windowClientToScreenRect()));
    }

    private void clearStampInfo() {
        stampKeyLabel.setText(null);
        stampDescriptionLabel.setText(null);
        stampWholeDataLabel.setText(null);
        stampFirstLineLabel.setText(null);
        stampLocationLabel.setText(null);
        stampWindowRectLabel.setText(null);
        stampWindowClientRectLabel.setText(null);
        stampWindowClientToScreenRectLabel.setText(null);
    }

    private String rectangleToString(Rectangle rectangle) {
        return String.format("(%d, %d)(%d, %d)", rectangle.left(), rectangle.top(), rectangle.width(), rectangle.height());
    }

    private static class StampKeyListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Object formattedString;
            if (value instanceof StampKey key) {
                formattedString = key.internal().groupName() + "." + key.name();
            } else {
                formattedString = value;
            }
            return super.getListCellRendererComponent(list, formattedString, index, isSelected, cellHasFocus);
        }

    }

    private record WindowRectangles(Rectangle windowRectangle,
                                    Rectangle clientRectangle,
                                    Rectangle clientToScreenRectangle) {
    }
}
