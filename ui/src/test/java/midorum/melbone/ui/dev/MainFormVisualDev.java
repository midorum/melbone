package midorum.melbone.ui.dev;

import com.midorum.win32api.facade.exception.Win32ApiException;
import com.midorum.win32api.win32.IWinUser;
import midorum.melbone.model.dto.Account;
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
        formWhenExperimentalIsOn,
        formWhenAccountsHaveCommentary
    }

    public MainFormVisualDev() {
        super(StandardDialogsProvider.getInstance());
    }

    public static void main(String[] args) throws Win32ApiException {
        new MainFormVisualDev().show(IWantToSee.formWhenAccountsHaveCommentary);
    }

    private void show(final IWantToSee wantToSee) throws Win32ApiException {
        switch (wantToSee) {
            case formWhenNotAccountsInUse -> formWhenNotAccountsInUse();
            case formWhenNotWindowsAnymore -> formWhenNotWindowsAnymore();
            case formWhenNotUnboundWindows -> formWhenNotUnboundWindows();
            case formWhenExistsUnboundWindow -> formWhenExistsUnboundWindow();
            case formWhenExistThreeUnboundWindows -> formWhenExistThreeUnboundWindows();
            case formWhenExperimentalIsOn -> formWhenExperimentalIsOn();
            case formWhenAccountsHaveCommentary -> formWhenAccountsHaveCommentary();
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

    private void formWhenExistsUnboundWindow() throws Win32ApiException {
        final int accountsLimit = 3;
        final int accountsInUseCount = 20;
        final int unboundWindowsCount = 1;
        new Interaction()
                .setAccountsLimit(accountsLimit)
                .generateAccountsInUse(accountsInUseCount)
                .createUnboundWindows(unboundWindowsCount)
                .whenTryCatchMouseKeyEvent(IWinUser.WM_LBUTTONDOWN).thenCatchWithSuccess()
                .whenTryGetWindowByAnyPoint().thenReturnAnyWindowPoint() //mocking settings pane
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

    private void formWhenExperimentalIsOn() throws Win32ApiException {
        setExecutionMode("experimental");
        setLoggerLevel(Level.TRACE);
        final int accountsLimit = 1;
        final int accountsInUseCount = 3;
        new Interaction()
                .setAccountsLimit(accountsLimit)
                .generateAccountsInUse(accountsInUseCount)
                .whenTryGetWindowByAnyPoint().thenReturnAnyWindowPoint() //mocking settings pane
                .whenTryCatchMouseKeyEvent(IWinUser.WM_LBUTTONDOWN).thenCatchWithSuccess()  //mocking settings pane and stamp pane
                .whenTryTakeRectangleShotThenReturnStandardImage(); //mocking stamp pane

        new MainForm("experimental mode", context).display();
    }

    private void formWhenAccountsHaveCommentary() throws Win32ApiException {
        final int accountsLimit = 3;
        final String commentaryGroup1 = "group1";
        final String commentaryGroup2 = "group2";
        final String commentaryGroup3 = "group3";
        final Account account1 = createAccount("John");
        final Account account2 = createAccountWithCommentary("Bob", commentaryGroup1);
        final Account account3 = createAccountWithCommentary("Robert", commentaryGroup2);
        final Account account4 = createAccount("David");
        final Account account5 = createAccountWithCommentary("Anthony", commentaryGroup2);
        final Account account6 = createAccountWithCommentary("Paul", commentaryGroup1);
        final Account account7 = createAccountWithCommentary("Kevin", commentaryGroup1 + "; " + commentaryGroup3);
        final Account account8 = createAccountWithCommentary("1_Robert", commentaryGroup3 + ";" + commentaryGroup1);
        final Account account9 = createAccountWithCommentary("2_Bob", commentaryGroup1 + "; " + commentaryGroup2);
        new Interaction()
                .setAccountsLimit(accountsLimit)
                .setTotalAccounts(account4, account2, account8, account3, account1, account6, account7, account9, account5)
                .setAccountsInUse(account3, account1, account4, account2, account9, account6, account7, account5, account8)
                .setBoundAccounts(account1.name(), account2.name(), account3.name())
                .whenTryCatchMouseKeyEvent(IWinUser.WM_LBUTTONDOWN).thenCatchWithSuccess()
                .whenTryGetWindowByAnyPoint().thenReturnAnyWindowPoint() //mocking settings pane
                .whenTryTakeRectangleShotThenReturnStandardImage();

        new MainForm("formWhenAccountsHaveCommentary", context).display();
    }

}