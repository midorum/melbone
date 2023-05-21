package midorum.melbone.ui.internal.model;

import midorum.melbone.model.dto.Account;

/**
 * Defines operations available with accounts holder
 */
public interface AccountsHolder {

    int getAccountsCount();

    Account[] getSelectedAccounts();
}
