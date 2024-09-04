package midorum.melbone.ui.internal.main;

import midorum.melbone.model.processing.AccountsProcessingRequest;
import midorum.melbone.ui.context.MockedContext;
import midorum.melbone.ui.util.SwingTestUtil;
import midorum.melbone.ui.internal.model.OnCloseNotificator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

class MainFormTest extends MockedContext {

    @Test
    @DisplayName("Caching identify dialog")
    void cachingIdentifyDialog() {
        new MainFormInteraction()
                .scenario("Caching identify dialog")
                .clickIdentifyButton()
                .verifyCreatingIdentifyDialogOnlyOnce()
                .clickIdentifyButton()
                .verifyCreatingIdentifyDialogOnlyOnce();
    }

    @Test
    @DisplayName("Caching settings manager form")
    void cachingSettingsManagerForm() {
        new MainFormInteraction()
                .scenario("Caching settings manager form")
                .clickSettingsButton()
                .verifyCreatingSettingsManagerFormOnlyOnce()
                .clickSettingsButton()
                .verifyCreatingSettingsManagerFormOnlyOnce()
                .clickAddAccountButton()
                .verifyCreatingSettingsManagerFormOnlyOnce()
                .clickAddAccountButton()
                .verifyCreatingSettingsManagerFormOnlyOnce();
    }

    @Test
    @DisplayName("Submit and cancel task")
    void submitAndCancelTask() {
        new MainFormInteraction()
                .scenario("Submit and cancel normal task")
                .clickConfirmButton()
                .verifyTaskSubmittedTimes(1)
                .verifyFrameIsIconified()
                .clickCancelButton()
                .verifyTaskCancelledTimes(1)
                .clickConfirmButton()
                .verifyTaskSubmittedTimes(2)
                .clickCancelButton()
                .verifyTaskCancelledTimes(2)
                .scenario("Submit task with error")
                .clickConfirmButtonWithThrownError()
                .verifyTaskSubmittedTimes(3)
                .verifyFrameIsRestored()
                .scenario("Submit normal task")
                .clickConfirmButton()
                .verifyTaskSubmittedTimes(4)
                .verifyFrameIsIconified()
                .scenario("Submit failed task")
                .clickConfirmButtonWithFailedTask()
                .verifyTaskSubmittedTimes(5)
                .verifyFrameIsRestored();
    }

    private class MainFormInteraction {

        private final MainForm mainForm;
        private final Interaction interaction;
        private final FrameVisibilityCounter frameVisibilityCounter = new FrameVisibilityCounter();

        private MainFormInteraction() {
            this.interaction = new Interaction();
            interaction.whenTryCreateIdentifyDialogThenNewInstance();
            interaction.whenTryCreateSettingsManagerFormThenNewInstance();
            interaction.whenTryCreateFrameVisibilityCounterThenReturn(frameVisibilityCounter);
            clearInvocations(standardDialogsProvider);
            this.mainForm = new MainForm("test", context);
        }

        /* info */

        public MainFormInteraction scenario(final String scenarioName) {
            logger.info("Scenario: {}", scenarioName);
            return this;
        }

        /* obtaining elements */

        private JButton getIdentifyButton() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(mainForm, "identify button", JButton.class);
        }

        private JButton getSettingsButton() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(mainForm, "settings button", JButton.class);
        }

        private JButton getAddAccountButton() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(mainForm, "add account button", JButton.class);
        }

        private JButton getConfirmButton() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(mainForm, "confirm button", JButton.class);
        }

        private JButton getCancelButton() {
            return SwingTestUtil.INSTANCE.getChildNamedOrThrow(mainForm, "cancel button", JButton.class);
        }

        /* actions */

        public MainFormInteraction clickIdentifyButton() {
            getIdentifyButton().doClick();
            return this;
        }

        public MainFormInteraction clickSettingsButton() {
            getSettingsButton().doClick();
            return this;
        }

        public MainFormInteraction clickAddAccountButton() {
            getAddAccountButton().doClick();
            return this;
        }

        public MainFormInteraction clickConfirmButton() {
            interaction.whenTrySendRoutineTask().thenExecuteNormal();
            getConfirmButton().doClick();
            return this;
        }

        public MainFormInteraction clickConfirmButtonWithThrownError() {
            interaction.whenTrySendRoutineTask().thenThrowError(new IllegalStateException("test"));
            getConfirmButton().doClick();
            return this;
        }

        public MainFormInteraction clickConfirmButtonWithFailedTask() {
            interaction.whenTrySendRoutineTask().thenReturnError(new IllegalStateException("test"));
            getConfirmButton().doClick();
            return this;
        }

        public MainFormInteraction clickCancelButton() {
            getCancelButton().doClick();
            return this;
        }

        /* verifying */

        public MainFormInteraction verifyCreatingIdentifyDialogOnlyOnce() {
            verify(standardDialogsProvider).createIdentifyDialog(eq(mainForm), eq(context), any(OnCloseNotificator.class));
            return this;
        }

        public MainFormInteraction verifyCreatingSettingsManagerFormOnlyOnce() {
            verify(standardDialogsProvider).createSettingsManagerForm(eq(context), any(OnCloseNotificator.class));
            return this;
        }

        public MainFormInteraction verifyTaskSubmittedTimes(final int count) {
            verify(executor, times(count)).sendRoutineTask(any(AccountsProcessingRequest.class));
            return this;
        }

        public MainFormInteraction verifyTaskCancelledTimes(final int count) {
            verify(executor, times(count)).cancelCurrentTask();
            return this;
        }

        public MainFormInteraction verifyFrameIsIconified() {
            assertThat(frameVisibilityCounter.isIconified(), is(true));
            return this;
        }

        public MainFormInteraction verifyFrameIsRestored() {
            assertThat(frameVisibilityCounter.isIconified(), is(false));
            return this;
        }
    }
}