package midorum.melbone.settings.internal.obtaining.account;

import dma.validation.Validator;
import midorum.melbone.model.persistence.StorageKey;
import midorum.melbone.model.settings.account.AccountBinding;
import midorum.melbone.settings.internal.storage.KeyValueStorage;

import java.util.Optional;

public class AccountBindingImpl implements AccountBinding {

    private final KeyValueStorage keyValueStorage;

    public AccountBindingImpl(final KeyValueStorage keyValueStorage) {
        this.keyValueStorage = keyValueStorage;
    }

    @Override
    public void bindResource(final String accountId, final String resourceId) {
        Validator.checkNotNull(accountId)
                .andMap(String::trim)
                .andCheckNot(String::isBlank)
                .andCheck(s -> keyValueStorage.containsKey(StorageKey.accounts, s))
                .thanDoWith(Validator.checkNotNull(resourceId)
                                .andMap(String::trim)
                                .andCheckNot(String::isBlank),
                        (acc, res) -> {
                            unbindResources(acc);
                            keyValueStorage.write(StorageKey.registryAccount2, acc, res);
                            keyValueStorage.write(StorageKey.registryResource2, res, acc);
                        })
                .elseThrow("accountId and resourceId can not be empty: accountId=" + accountId + " resourceId=" + resourceId + " and account should be in account storage");
    }

    private void unbindResources(final String accountId) {
        keyValueStorage.read(StorageKey.registryAccount2, accountId)
                .ifPresent(resourceId -> keyValueStorage.removeKey(StorageKey.registryResource2, resourceId));
    }

    @Override
    public Optional<String> getBoundAccount(final String resourceId) {
        return Validator.checkNotNull(resourceId)
                .andMapOptional(id -> keyValueStorage.read(StorageKey.registryResource2, id))
                .cast(String.class)
                .asOptional();
    }

}
