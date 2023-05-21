package midorum.melbone.ui.internal.settings;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.facade.WindowPoint;
import com.midorum.win32api.struct.PointFloat;
import com.midorum.win32api.struct.PointInt;
import com.midorum.win32api.struct.PointLong;
import com.midorum.win32api.win32.IWinUser;
import midorum.melbone.model.settings.key.SettingKey;
import midorum.melbone.model.settings.key.SettingsManagerAction;
import midorum.melbone.settings.SettingKeys;
import midorum.melbone.ui.context.MockedContext;
import midorum.melbone.ui.util.SwingTestUtil;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class SettingsPaneTest extends MockedContext {

    @Test
    @DisplayName("Empty pane shouldn't work")
    void emptyPaneShouldNotWork() {
        new SettingsPaneInteraction()
                .scenario("Empty pane shouldn't work")
                .listItemsInComboBox()
                .printStateBriefly()
                .verifySelectSettingComboBoxHaveNotSelectedValue()
                .verifyDescriptionSettingLabelIsEmpty()
                .verifySettingDefaultValueLabelIsEmpty()
                .verifySettingValueFieldIsEmpty()
                .clickSetDefaultValueButton()
                .verifySettingStorageIsEmpty()
                .clickCaptureButton()
                .verifyMouseKeyHookNotSet()
                .clickSaveButton()
                .verifySettingStorageIsEmpty();
    }

    @Test
    @DisplayName("Display and change setting value with no management action")
    void displayAndChangeDefaultSettingValueWithNoManagementAction() {
        final SettingKey settingKey = SettingKeys.Application.maxAccountsSimultaneously;
        final int newValue = 10;
        assertThat(settingKey.internal().settingsManagerAction(), equalTo(SettingsManagerAction.noAction));
        final SettingsPaneInteraction interaction = new SettingsPaneInteraction();
        interaction.scenario("Select setting in combo box")
                .selectSettingInComboBox(settingKey)
                .verifyDescriptionSettingLabelIs(settingKey)
                .verifySettingDefaultValueLabelContains(settingKey)
                .verifySettingValueFieldIsEmpty()
                .printStateBriefly();
        interaction.scenario("Store new value for setting")
                .typeSettingValue(String.valueOf(newValue))
                .clickSaveButton()
                .verifySettingValueInStorage(settingKey, newValue);
        interaction.scenario("Verifying stored setting value on form")
                .selectSettingInComboBox(settingKey)
                .verifyDescriptionSettingLabelIs(settingKey)
                .verifySettingDefaultValueLabelContains(settingKey)
                .verifySettingValueField(String.valueOf(newValue))
                .printStateBriefly();
        interaction.scenario("Try store wrong type value for setting")
                .typeSettingValue("wrong")
                .clickSaveButton()
                .verifySettingValueInStorage(settingKey, newValue);
        interaction.scenario("Restore default value for setting")
                .clickSetDefaultValueButton()
                .verifySettingValueInStorage(settingKey, settingKey.internal().defaultValue().orElseThrow());
        interaction.scenario("Try capturing value")
                .clickCaptureButton()
                .verifySettingValueInStorage(settingKey, settingKey.internal().defaultValue().orElseThrow());
    }

    @Test
    @DisplayName("Display and change setting value without default value")
    void displayAndChangeDefaultSettingValueWithoutDefaultValue() {
        final SettingKey settingKey = SettingKeys.TargetLauncher.windowTitle;
        final String newValue = "some text";
        final String capturedWindowTitle = "captured window title";
        assertThat(settingKey.internal().settingsManagerAction(), equalTo(SettingsManagerAction.touchWindow));
        final SettingsPaneInteraction interaction = new SettingsPaneInteraction();
        interaction.scenario("Select setting in combo box")
                .selectSettingInComboBox(settingKey)
                .verifyDescriptionSettingLabelIs(settingKey)
                .verifySettingDefaultValueLabelIsEmpty()
                .verifySettingValueFieldIsEmpty()
                .printStateBriefly();
        interaction.scenario("Store new value for setting")
                .typeSettingValue(newValue)
                .clickSaveButton()
                .verifySettingValueInStorage(settingKey, newValue);
        interaction.scenario("Verifying stored setting value on form")
                .selectSettingInComboBox(settingKey)
                .verifyDescriptionSettingLabelIs(settingKey)
                .verifySettingDefaultValueLabelContains(settingKey)
                .verifySettingValueField(newValue)
                .printStateBriefly();
        interaction.scenario("Restore default value for setting")
                .clickSetDefaultValueButton()
                .verifySettingValueInStorage(settingKey, newValue);
        interaction.scenario("Capturing value")
                .clickCaptureButtonWithConfirm(capturedWindowTitle)
                .verifySettingValueField(capturedWindowTitle)
                .clickSaveButton()
                .verifySettingValueInStorage(settingKey, capturedWindowTitle)
                .printStateBriefly();
    }

    @Test
    @DisplayName("Display and change setting value with touch window element action")
    void displayAndChangeDefaultSettingValueWithTouchWindowElementAction() {
        final SettingKey settingKey = SettingKeys.TargetLauncher.windowCloseButtonPoint;
        final String newValue = "[x=0.1, y=0.2]";
        final PointFloat newValueToCheck = new PointFloat(0.1f, 0.2f);
        final PointFloat capturedPoint = new PointFloat(0.5f, 0.7f);
        assertThat(settingKey.internal().settingsManagerAction(), equalTo(SettingsManagerAction.touchWindowElement));
        final SettingsPaneInteraction interaction = new SettingsPaneInteraction();
        interaction.scenario("Select setting in combo box")
                .selectSettingInComboBox(settingKey)
                .verifyDescriptionSettingLabelIs(settingKey)
                .verifySettingDefaultValueLabelIsEmpty()
                .verifySettingValueFieldIsEmpty()
                .printStateBriefly();
        interaction.scenario("Store new value for setting")
                .typeSettingValue(newValue)
                .clickSaveButton()
                .verifySettingValueInStorage(settingKey, newValueToCheck);
        interaction.scenario("Verifying stored setting value on form")
                .selectSettingInComboBox(settingKey)
                .verifyDescriptionSettingLabelIs(settingKey)
                .verifySettingDefaultValueLabelContains(settingKey)
                .verifySettingValueField(newValueToCheck.toString())
                .printStateBriefly();
        interaction.scenario("Restore default value for setting")
                .clickSetDefaultValueButton()
                .verifySettingValueInStorage(settingKey, newValueToCheck);
        interaction.scenario("Capturing value")
                .clickCaptureButtonWithConfirm(capturedPoint)
                .verifySettingValueField(capturedPoint.toString())
                .clickSaveButton()
                .verifySettingValueInStorage(settingKey, capturedPoint)
                .printStateBriefly();
    }

    @Test
    @DisplayName("Display and change setting value with touch screen element action")
    void displayAndChangeDefaultSettingValueWithTouchScreenElementAction() {
        final SettingKey settingKey = SettingKeys.TargetLauncher.desktopShortcutLocationAbsolutePoint;
        final String newValue = "[x=1, y=2]";
        final PointLong newValueToCheck = new PointLong(1L, 2L);
        final PointLong capturedPoint = new PointLong(1000L, 555L);
        assertThat(settingKey.internal().settingsManagerAction(), equalTo(SettingsManagerAction.touchScreenElement));
        final SettingsPaneInteraction interaction = new SettingsPaneInteraction();
        interaction.scenario("Select setting in combo box")
                .selectSettingInComboBox(settingKey)
                .verifyDescriptionSettingLabelIs(settingKey)
                .verifySettingDefaultValueLabelIsEmpty()
                .verifySettingValueFieldIsEmpty()
                .printStateBriefly();
        interaction.scenario("Store new value for setting")
                .typeSettingValue(newValue)
                .clickSaveButton()
                .verifySettingValueInStorage(settingKey, newValueToCheck);
        interaction.scenario("Verifying stored setting value on form")
                .selectSettingInComboBox(settingKey)
                .verifyDescriptionSettingLabelIs(settingKey)
                .verifySettingDefaultValueLabelContains(settingKey)
                .verifySettingValueField(newValueToCheck.toString())
                .printStateBriefly();
        interaction.scenario("Restore default value for setting")
                .clickSetDefaultValueButton()
                .verifySettingValueInStorage(settingKey, newValueToCheck);
        interaction.scenario("Capturing value")
                .clickCaptureButtonWithConfirm(capturedPoint)
                .verifySettingValueField(capturedPoint.toString())
                .clickSaveButton()
                .verifySettingValueInStorage(settingKey, capturedPoint)
                .printStateBriefly();
    }

    private class SettingsPaneInteraction {

        private final SettingsPane settingsPane;
        private final Interaction interaction;

        private SettingsPaneInteraction() {
            this.interaction = new Interaction();
            this.settingsPane = new SettingsPane(new FrameVisibilityCounter(), context);
        }

        /* info */

        public SettingsPaneInteraction scenario(final String scenarioName) {
            logger.info("Scenario: {}", scenarioName);
            return this;
        }

        public SettingsPaneInteraction printStateBriefly() {
            logger.info("""
                            SettingsPane current state:\s
                            \tcombo box selected item: {}
                            \tdescription label content: {}
                            \tdefault value label content: {}
                            \tcurrent value field content: {}""",
                    getSelectSettingComboBox().getSelectedItem(),
                    getSettingDescriptionLabel().getText(),
                    getSettingDefaultValueLabel().getText(),
                    getSettingValueField().getText());
            return this;
        }

        public SettingsPaneInteraction listItemsInComboBox() {
            final ComboBoxModel<?> comboBoxModel = getSelectSettingComboBox().getModel();
            final int size = comboBoxModel.getSize();
            logger.info("combo box model size: {}", size);
            for (int i = 0; i < size; i++) {
                logger.info("item {}: {}", i, comboBoxModel.getElementAt(i));
            }
            logger.info("selected item is: {}", comboBoxModel.getSelectedItem());
            return this;
        }

        /* obtaining components */

        private JComboBox<?> getSelectSettingComboBox() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(settingsPane, "select setting combo box", JComboBox.class);
        }

        private JLabel getSettingDescriptionLabel() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(settingsPane, "description label", JLabel.class);
        }

        private JLabel getSettingDefaultValueLabel() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(settingsPane, "default value label", JLabel.class);
        }

        private JTextField getSettingValueField() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(settingsPane, "setting value field", JTextField.class);
        }

        private JButton getSetDefaultValueButton() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(settingsPane, "set default value button", JButton.class);
        }

        private JButton getCaptureButton() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(settingsPane, "capture button", JButton.class);
        }

        private JButton getSaveButton() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(settingsPane, "save button", JButton.class);
        }

        /* actions */

        public SettingsPaneInteraction clickSetDefaultValueButton() {
            getSetDefaultValueButton().doClick();
            return this;
        }

        public SettingsPaneInteraction clickCaptureButton() {
            getCaptureButton().doClick();
            return this;
        }

        public SettingsPaneInteraction clickCaptureButtonWithConfirm(final String capturedWindowTitle) {
            final PointInt pointWhereUserClicked = new PointInt(-1, -1);
            final PointInt samePointInFoundWindow = new PointInt(2, 3);
            final IWindow foundNativeWindow = createNativeWindowMock(capturedWindowTitle);
            final WindowPoint windowPoint = new WindowPoint(foundNativeWindow, samePointInFoundWindow);
            interaction.whenTryGetPointInWindow(pointWhereUserClicked).thenReturn(windowPoint);
            interaction.whenTryAskOkCancelConfirmationThenChooseOk();
            interaction.whenTryCatchMouseKeyEvent(createMouseEvent(IWinUser.WM_LBUTTONDOWN, pointWhereUserClicked)).thenCatchWithSuccess();
            getCaptureButton().doClick();
            return this;
        }

        public SettingsPaneInteraction clickCaptureButtonWithConfirm(final PointFloat capturedPoint) {
            doWithMockedWin32System(() -> {
                final PointInt pointWhereUserClicked = new PointInt(-1, -1);
                final Rectangle windowRectangle = new Rectangle(0, 0, 200, 100);
                final PointInt samePointInFoundWindow = new PointInt(2, 3);
                final IWindow foundNativeWindow = createNativeWindowMock(windowRectangle);
                final WindowPoint windowPoint = new WindowPoint(foundNativeWindow, samePointInFoundWindow);
                interaction.whenTryGetPointInWindow(pointWhereUserClicked).thenReturn(windowPoint);
                when(relativeCoordinates.windowRelativeX(samePointInFoundWindow.x())).thenReturn(capturedPoint.x());
                when(relativeCoordinates.windowRelativeY(samePointInFoundWindow.y())).thenReturn(capturedPoint.y());
                interaction.whenTryAskOkCancelConfirmationThenChooseOk();
                interaction.whenTryCatchMouseKeyEvent(createMouseEvent(IWinUser.WM_LBUTTONDOWN, pointWhereUserClicked)).thenCatchWithSuccess();
                getCaptureButton().doClick();
            });
            return this;
        }

        public SettingsPaneInteraction clickCaptureButtonWithConfirm(final PointLong capturedPoint) {
            doWithMockedWin32System(() -> {
                final PointInt pointWhereUserClicked = new PointInt(-1, -1);
                final PointInt samePointInFoundWindow = new PointInt(2, 3);
                final PointInt absoluteScreenPoint = new PointInt((int) capturedPoint.x(), (int) capturedPoint.y());
                final IWindow someNativeWindow = createNativeWindowMock();
                final WindowPoint windowPoint = new WindowPoint(someNativeWindow, samePointInFoundWindow);
                when(windowFactory.getPointInWindow(pointWhereUserClicked)).thenReturn(Optional.of(windowPoint));
                when(win32System.getAbsoluteScreenPoint(samePointInFoundWindow)).thenReturn(absoluteScreenPoint);
                interaction.whenTryAskOkCancelConfirmationThenChooseOk();
                interaction.whenTryCatchMouseKeyEvent(createMouseEvent(IWinUser.WM_LBUTTONDOWN, pointWhereUserClicked)).thenCatchWithSuccess();
                getCaptureButton().doClick();
            });
            return this;
        }

        public SettingsPaneInteraction clickSaveButton() {
            getSaveButton().doClick();
            return this;
        }

        public SettingsPaneInteraction selectSettingInComboBox(final SettingKey settingKey) {
            getSelectSettingComboBox().setSelectedItem(settingKey);
            return this;
        }

        /* verifying */

        public SettingsPaneInteraction verifySelectSettingComboBoxHaveNotSelectedValue() {
            assertThat(getSelectSettingComboBox().getSelectedItem(), nullValue());
            return this;
        }

        public SettingsPaneInteraction verifyDescriptionSettingLabelIsEmpty() {
            assertThat(getSettingDescriptionLabel().getText(), emptyOrNullString());
            return this;
        }

        public SettingsPaneInteraction verifySettingDefaultValueLabelIsEmpty() {
            assertThat(getSettingDefaultValueLabel().getText(), anyOf(emptyOrNullString(), equalTo("[empty]")));
            return this;
        }

        public SettingsPaneInteraction verifySettingValueFieldIsEmpty() {
            assertThat(getSettingValueField().getText(), anyOf(emptyOrNullString(), equalTo("[empty]")));
            return this;
        }

        public SettingsPaneInteraction verifySettingStorageIsEmpty() {
            MatcherAssert.assertThat(settingStorage.mapsInStorage(), is(0));
            return this;
        }

        public SettingsPaneInteraction verifyMouseKeyHookNotSet() {
            verify(mouseKeyHookManager, never()).setHook(anyInt(), any(), any());
            verify(mouseKeyHookManager, never()).setHook(anyInt(), any(), any(), any());
            return this;
        }

        public SettingsPaneInteraction verifyDescriptionSettingLabelIs(final SettingKey settingKey) {
            assertThat(getSettingDescriptionLabel().getText(), equalTo(settingKey.internal().description()));
            return this;
        }

        public SettingsPaneInteraction verifySettingDefaultValueLabelContains(final SettingKey settingKey) {
            assertThat(getSettingDefaultValueLabel().getText(), equalTo(settingKey.internal().defaultValue().map(Object::toString).orElse("[empty]")));
            return this;
        }

        public SettingsPaneInteraction typeSettingValue(final String value) {
            getSettingValueField().setText(value);
            return this;
        }

        public SettingsPaneInteraction verifySettingValueInStorage(final SettingKey settingKey, final Object value) {
            assertThat(settingStorage.containsKey(settingKey), is(true));
            assertThat(settingStorage.read(settingKey).orElse(null), equalTo(value));
            return this;
        }

        public SettingsPaneInteraction verifySettingValueField(final String value) {
            assertThat(getSettingValueField().getText(), equalTo(value));
            return this;
        }
    }
}