package midorum.melbone.model.settings.account;

import java.util.Optional;

public interface AccountBinding {

    void bindResource(final String accountId, final String resourceId);

    Optional<String> getBoundAccount(final String resourceId);
}
