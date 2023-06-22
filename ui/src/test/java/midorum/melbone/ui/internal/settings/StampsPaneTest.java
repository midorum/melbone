package midorum.melbone.ui.internal.settings;

import com.midorum.win32api.facade.IWindow;
import com.midorum.win32api.facade.Rectangle;
import com.midorum.win32api.win32.IWinUser;
import midorum.melbone.model.settings.stamp.Stamp;
import midorum.melbone.model.settings.key.StampKey;
import midorum.melbone.settings.StampKeys;
import midorum.melbone.ui.context.MockedContext;
import midorum.melbone.ui.util.SwingTestUtil;
import midorum.melbone.ui.internal.model.FrameStateOperations;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import javax.swing.*;
import java.awt.*;

import static midorum.melbone.ui.util.StampMatcher.equalToStamp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

class StampsPaneTest extends MockedContext {

    @Test
    @DisplayName("Empty pane shouldn't work")
    void emptyPaneShouldNotWork() {
        new StampsPaneInteraction()
                .scenario("Empty pane shouldn't work")
                .listItemsInComboBox()
                .printFormStateBriefly()
                .verifySelectSettingComboBoxHaveNotSelectedValue()
                .verifyKeyDescriptionLabelIsEmpty()
                .verifyInfoAboutCapturedStampIsEmpty()
                .verifyShowButtonClickDoesNotEffect()
                .verifyCaptureButtonClickDoesNotEffect()
                .verifySettingStorageIsEmpty();
    }

    @Test
    @DisplayName("Select stamp and capture it")
    void selectStampAndCaptureIt() {
        final StampKey stampKey = StampKeys.TargetLauncher.playButtonActive;
        final StampKey stampKey2 = StampKeys.TargetLauncher.playButtonInactive;
        final IWindow capturedNativeWindow = createNativeWindowMock(new Rectangle(0, 0, 1000, 500));
        final Rectangle capturedRectangle = new Rectangle(0, 0, 100, 50);
        final StampsPaneInteraction interaction = new StampsPaneInteraction();
        interaction.scenario("Select first non captured stamp (" + stampKey + ") and verify its info")
                .selectSettingInComboBox(stampKey)
                .verifyKeyDescriptionLabelHasInfoFor(stampKey)
                .verifyInfoAboutCapturedStampIsEmpty()
                .verifyShowButtonClickDoesNotEffect();
        interaction.scenario("Capture shot for " + stampKey)
                .makeFakeStampToCapture(stampKey, capturedNativeWindow, capturedRectangle)
                .clickCaptureButtonWithConfirmAndCapturingStamp()
                .verifyKeyDescriptionLabelHasInfoFor(stampKey)
                .verifyInfoAboutCapturedStamp()
                .verifySettingValueInStorage(stampKey)
                .printFormStateBriefly()
                .verifyShowButtonClickHasShownPreviewDialog();
        interaction.scenario("Select second non captured stamp (" + stampKey2 + ") and verify its info")
                .selectSettingInComboBox(stampKey2)
                .verifyKeyDescriptionLabelHasInfoFor(stampKey2)
                .verifyInfoAboutCapturedStampIsEmpty()
                .verifyShowButtonClickDoesNotEffect();
        interaction.scenario("Capture shot for " + stampKey2)
                .makeFakeStampToCapture(stampKey2, capturedNativeWindow, capturedRectangle)
                .clickCaptureButtonWithConfirmAndCapturingStamp()
                .verifyKeyDescriptionLabelHasInfoFor(stampKey2)
                .verifyInfoAboutCapturedStamp()
                .verifySettingValueInStorage(stampKey2)
                .printFormStateBriefly()
                .verifyShowButtonClickHasShownPreviewDialog();
        interaction.scenario("Select first captured stamp (" + stampKey + ") and verify its info")
                .selectSettingInComboBox(stampKey)
                .verifyKeyDescriptionLabelHasInfoFor(stampKey)
                .verifyInfoAboutCapturedStamp()
                .verifyShowButtonClickHasShownPreviewDialog();
    }

    private class StampsPaneInteraction {

        private final Interaction interaction;
        private final StampsPane stampsPane;
        private Stamp stampShouldBeDisplayed;

        public StampsPaneInteraction() {
            this.interaction = new Interaction();
            final FrameStateOperations ownerFrame = mock(FrameStateOperations.class);
            this.stampsPane = new StampsPane(ownerFrame, context);
        }

        /* info */

        public StampsPaneInteraction scenario(final String scenarioName) {
            logger.info("Scenario: {}", scenarioName);
            return this;
        }

        public StampsPaneInteraction printFormStateBriefly() {
            logger.info("""
                            StampsPane current state:
                            \tcombo box selected item ····························→ {}
                            \tkey description label content ······················→ {}
                            \tstamp key label content ····························→ {}
                            \tstamp description label content ····················→ {}
                            \tstamp whole data label content ·····················→ {}
                            \tstamp first line label content ·····················→ {}
                            \tstamp location label content ·······················→ {}
                            \tstamp window rect label content ····················→ {}
                            \tstamp window client rect label content ·············→ {}
                            \tstamp window client-to-screen rect label content ···→ {}""",
                    getSelectSettingComboBox().getSelectedItem(),
                    getKeyDescriptionLabel().getText(),
                    getStampKeyLabel().getText(),
                    getStampDescriptionLabel().getText(),
                    getStampWholeDataLabel().getText(),
                    getStampFirstLineLabel().getText(),
                    getStampLocationLabel().getText(),
                    getStampWindowRectLabel().getText(),
                    getStampWindowClientRectLabel().getText(),
                    getStampWindowClientToScreenRectLabel().getText());
            return this;
        }

        public StampsPaneInteraction listItemsInComboBox() {
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
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(stampsPane, "select setting combo box", JComboBox.class);
        }

        private JLabel getKeyDescriptionLabel() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(stampsPane, "key description label", JLabel.class);
        }

        private JLabel getStampKeyLabel() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(stampsPane, "stamp key label", JLabel.class);
        }

        private JLabel getStampDescriptionLabel() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(stampsPane, "stamp description label", JLabel.class);
        }

        private JLabel getStampWholeDataLabel() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(stampsPane, "stamp whole data label", JLabel.class);
        }

        private JLabel getStampFirstLineLabel() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(stampsPane, "stamp first line label", JLabel.class);
        }

        private JLabel getStampLocationLabel() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(stampsPane, "stamp location label", JLabel.class);
        }

        private JLabel getStampWindowRectLabel() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(stampsPane, "stamp window rect label", JLabel.class);
        }

        private JLabel getStampWindowClientRectLabel() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(stampsPane, "stamp window client rect label", JLabel.class);
        }

        private JLabel getStampWindowClientToScreenRectLabel() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(stampsPane, "stamp window client-to-screen rect label", JLabel.class);
        }

        public JButton getShowButton() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(stampsPane, "show stamp button", JButton.class);
        }

        private JButton getCaptureButton() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(stampsPane, "capture stamp button", JButton.class);
        }

        /* actions */

        public StampsPaneInteraction makeFakeStampToCapture(final StampKey stampKey, final IWindow capturedNativeWindow, final Rectangle capturedRectangle) {
            interaction.whenTryGetWindowByAnyPoint().thenReturnPointForWindow(capturedNativeWindow)
                    .whenTryCaptureScreenRectangleThenReturn(capturedRectangle)
                    .whenTryTakeRectangleShotThenReturnStandardImage();
            this.stampShouldBeDisplayed = StampsPaneTest.super.createFakeStamp(stampKey, capturedNativeWindow, capturedRectangle);
            return this;
        }

        public StampsPaneInteraction clickCaptureButtonWithConfirmAndCapturingStamp() {
            interaction.whenTryAskOkCancelConfirmationThenChooseOk()
                    .whenTryCatchMouseKeyEvent(IWinUser.WM_LBUTTONDOWN).thenCatchWithSuccess();
            getCaptureButton().doClick();
            return this;
        }

        public StampsPaneInteraction selectSettingInComboBox(final StampKey stampKey) {
            getSelectSettingComboBox().setSelectedItem(stampKey);
            this.stampShouldBeDisplayed = (Stamp) settingStorage.read(stampKey).orElse(null);
            return this;
        }


        /* verifying */
        public StampsPaneInteraction verifySelectSettingComboBoxHaveNotSelectedValue() {
            assertThat(getSelectSettingComboBox().getSelectedItem(), nullValue());
            return this;
        }

        public StampsPaneInteraction verifyKeyDescriptionLabelIsEmpty() {
            assertThat(getKeyDescriptionLabel().getText(), emptyOrNullString());
            return this;
        }

        private StampsPaneInteraction verifyKeyDescriptionLabelHasInfoFor(final StampKey stampKey) {
            assertThat(getKeyDescriptionLabel().getText(), equalTo(stampKey.internal().description()));
            return this;
        }

        public StampsPaneInteraction verifyInfoAboutCapturedStampIsEmpty() {
            assertThat(getStampKeyLabel().getText(), emptyOrNullString());
            assertThat(getStampDescriptionLabel().getText(), emptyOrNullString());
            assertThat(getStampWholeDataLabel().getText(), emptyOrNullString());
            assertThat(getStampFirstLineLabel().getText(), emptyOrNullString());
            assertThat(getStampLocationLabel().getText(), emptyOrNullString());
            assertThat(getStampWindowRectLabel().getText(), emptyOrNullString());
            assertThat(getStampWindowClientRectLabel().getText(), emptyOrNullString());
            assertThat(getStampWindowClientToScreenRectLabel().getText(), emptyOrNullString());
            return this;
        }

        public StampsPaneInteraction verifyInfoAboutCapturedStamp() {
            assertThat(stampShouldBeDisplayed, notNullValue());
            assertThat(getStampKeyLabel().getText(), containsString(stampShouldBeDisplayed.key().internal().groupName() + "." + stampShouldBeDisplayed.key().name()));
            assertThat(getStampDescriptionLabel().getText(), containsString(stampShouldBeDisplayed.key().internal().description()));
            assertThat(getStampWholeDataLabel().getText(), containsString(Integer.toString(stampShouldBeDisplayed.wholeData().length)));
            assertThat(getStampFirstLineLabel().getText(), containsString(Integer.toString(stampShouldBeDisplayed.firstLine().length)));
            assertThat(getStampFirstLineLabel().getText(), containsString(Integer.toString(stampShouldBeDisplayed.firstLine().length)));
            assertThat(getStampLocationLabel().getText(), containsString(rectangleToString(stampShouldBeDisplayed.location())));
            assertThat(getStampWindowRectLabel().getText(), containsString(rectangleToString(stampShouldBeDisplayed.windowRect())));
            assertThat(getStampWindowClientRectLabel().getText(), containsString(rectangleToString(stampShouldBeDisplayed.windowClientRect())));
            assertThat(getStampWindowClientToScreenRectLabel().getText(), containsString(rectangleToString(stampShouldBeDisplayed.windowClientToScreenRect())));
            return this;
        }

        public StampsPaneInteraction verifySettingStorageIsEmpty() {
            MatcherAssert.assertThat(settingStorage.mapsInStorage(), is(0));
            return this;
        }

        public StampsPaneInteraction verifySettingValueInStorage(final StampKey stampKey) {
            assertThat(settingStorage.containsKey(stampKey), is(true));
            assertThat((Stamp) settingStorage.read(stampKey).orElse(null), equalToStamp(stampShouldBeDisplayed));
            return this;
        }

        public StampsPaneInteraction verifyShowButtonClickDoesNotEffect() {
            interaction.whenTryShowStampPreviewDialogThenVerifyStampIsNotNull();
            clearInvocations(standardDialogsProvider);
            getShowButton().doClick();
            verify(standardDialogsProvider, never()).showStampPreviewDialog(Mockito.any(Stamp.class));
            return this;
        }

        public StampsPaneInteraction verifyShowButtonClickHasShownPreviewDialog() {
            interaction.whenTryShowStampPreviewDialogThenVerifyStampIsNotNull();
            clearInvocations(standardDialogsProvider);
            getShowButton().doClick();
            verify(standardDialogsProvider).showStampPreviewDialog(Mockito.any(Stamp.class));
            return this;
        }

        public StampsPaneInteraction verifyCaptureButtonClickDoesNotEffect() {
            clearInvocations(standardDialogsProvider);
            getCaptureButton().doClick();
            verify(standardDialogsProvider, never()).askOkCancelConfirm(Mockito.any(Component.class), Mockito.anyString(), Mockito.anyString());
            return this;
        }

        private String rectangleToString(Rectangle rectangle) {
            return "(" + rectangle.left() + ", " + rectangle.top() + ")(" + rectangle.width() + ", " + rectangle.height() + ")";
        }
    }
}

