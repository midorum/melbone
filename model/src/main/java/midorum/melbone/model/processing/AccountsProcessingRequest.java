package midorum.melbone.model.processing;

import midorum.melbone.model.dto.Account;

import java.util.function.Consumer;

public interface AccountsProcessingRequest {

    Account[] getAccounts();

    Consumer<Throwable> getErrorHandler();
}
