package midorum.melbone.ui.dev;

import com.midorum.win32api.win32.IWinUser;
import midorum.melbone.ui.context.MockedContext;
import midorum.melbone.ui.internal.main.MainForm;
import midorum.melbone.ui.internal.util.StandardDialogsProvider;
import org.apache.logging.log4j.Level;

import static org.mockito.Mockito.when;

class MainFormVisualDev extends MockedContext {

    private enum IWantToSee {
        formWhenNotAccountsInUse,
        formWhenNotWindowsAnymore,
        formWhenNotUnboundWindows,
        formWhenExistsUnboundWindow,
        formWhenExistThreeUnboundWindows,
        formWhenExperimentalIsOn
    }

    public MainFormVisualDev() {
        super(StandardDialogsProvider.getInstance());
    }

    public static void main(String[] args) {
        new MainFormVisualDev().show(IWantToSee.formWhenExperimentalIsOn);
    }

    private void show(final IWantToSee wantToSee) {
        switch (wantToSee) {
            case formWhenNotAccountsInUse -> formWhenNotAccountsInUse();
            case formWhenNotWindowsAnymore -> formWhenNotWindowsAnymore();
            case formWhenNotUnboundWindows -> formWhenNotUnboundWindows();
            case formWhenExistsUnboundWindow -> formWhenExistsUnboundWindow();
            case formWhenExistThreeUnboundWindows -> formWhenExistThreeUnboundWindows();
            case formWhenExperimentalIsOn -> formWhenExperimentalIsOn();
        }
    }

    private void formWhenNotAccountsInUse() {
        final int accountsLimit = 3;
        when(applicationSettings.maxAccountsSimultaneously()).thenReturn(accountsLimit);

        new MainForm("formWhenNotAccountsInUse", context).display();
    }

    private void formWhenNotWindowsAnymore() {
        final int accountsLimit = 3;
        final int accountsInUseCount = 20;
        new Interaction()
                .setAccountsLimit(accountsLimit)
                .generateAccountsInUse(accountsInUseCount);

        new MainForm("formWhenNotWindowsAnymore", context).display();
    }

    private void formWhenNotUnboundWindows() {
        final int accountsLimit = 3;
        final int accountsInUseCount = 20;
        new Interaction()
                .setAccountsLimit(accountsLimit)
                .generateAccountsInUse(accountsInUseCount)
                .setTotalAccounts("acc1", "acc2", "acc3")
                .setAccountsInUse("acc1", "acc2", "acc3")
                .setBoundAccounts("acc1", "acc2", "acc3")
                .printStateBriefly();

        new MainForm("formWhenNotUnboundWindows", context).display();
    }

    private void formWhenExistsUnboundWindow() {
        final int accountsLimit = 3;
        final int accountsInUseCount = 20;
        final int unboundWindowsCount = 1;
        new Interaction()
                .setAccountsLimit(accountsLimit)
                .generateAccountsInUse(accountsInUseCount)
                .createUnboundWindows(unboundWindowsCount)
                .whenTryCatchMouseKeyEvent(IWinUser.WM_LBUTTONDOWN).thenCatchWithSuccess()
                .whenTryGetWindowByPointThenReturn(createNativeWindowMock())
                .whenTryTakeRectangleShotThenReturnStandardImage();

        new MainForm("formWhenExistsUnboundWindow", context).display();
    }

    private void formWhenExistThreeUnboundWindows() {
        final int accountsLimit = 3;
        final int accountsInUseCount = 20;
        final int unboundWindowsCount = 3;
        new Interaction()
                .setAccountsLimit(accountsLimit)
                .generateAccountsInUse(accountsInUseCount)
                .createUnboundWindows(unboundWindowsCount)
                .whenTryCatchMouseKeyEvent(IWinUser.WM_LBUTTONDOWN).thenCatchWithSuccess();

        new MainForm("formWhenExistsUnboundWindow", context).display();
    }

    private void formWhenExperimentalIsOn() {
        setExecutionMode("experimental");
        setLoggerLevel(Level.TRACE);
        final int accountsLimit = 1;
        final int accountsInUseCount = 3;
        new Interaction()
                .setAccountsLimit(accountsLimit)
                .generateAccountsInUse(accountsInUseCount)
                .whenTryGetAnyPointInWindow().thenReturnAnyWindowPoint() //mocking settings pane
                .whenTryCatchMouseKeyEvent(IWinUser.WM_LBUTTONDOWN).thenCatchWithSuccess()  //mocking settings pane and stamp pane
                .whenTryGetWindowByPointThenReturn(createNativeWindowMock()) //mocking stamp pane
                .whenTryTakeRectangleShotThenReturnStandardImage(); //mocking stamp pane

        new MainForm("experimental mode", context).display();
    }

}