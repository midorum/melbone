package midorum.melbone.settings.internal.management;

import dma.validation.Validator;
import midorum.melbone.model.dto.Account;
import midorum.melbone.model.persistence.AccountStorage;
import midorum.melbone.model.persistence.StorageKey;
import midorum.melbone.settings.internal.storage.KeyValueStorage;

import java.util.Collection;
import java.util.Optional;

public class AccountStorageImpl implements AccountStorage {

    private final KeyValueStorage keyValueStorage;

    public AccountStorageImpl(final KeyValueStorage keyValueStorage) {
        this.keyValueStorage = keyValueStorage;
    }

    @Override
    public Collection<Account> accounts() {
        return keyValueStorage.getValues(StorageKey.accounts);
    }

    @Override
    public Collection<String> accountsNames() {
        return keyValueStorage.getKeySet(StorageKey.accounts);
    }

    @Override
    public boolean isExists(final String accountId) {
        return keyValueStorage.containsKey(StorageKey.accounts, Validator.checkNotNull(accountId).orThrowForSymbol("accountId"));
    }

    @Override
    public void store(final Account account) {
        final Account checkedAccount = Validator.checkNotNull(account).orThrowForSymbol("account");
        keyValueStorage.write(StorageKey.accounts, checkedAccount.name(), checkedAccount);
    }

    @Override
    public Account get(final String accountId) {
        return Validator.checkNotNull(accountId)
                .andMapOptional(s -> keyValueStorage.read(StorageKey.accounts, s))
                .cast(Account.class)
                .orThrow("account with id=" + accountId + " not found");
    }

    @Override
    public Optional<Account> remove(final String accountId) {
        final String key = Validator.checkNotNull(accountId).orThrowForSymbol("accountId");
        removeFromUsed(key);
        return Optional.ofNullable(keyValueStorage.removeKey(StorageKey.accounts, key));
    }

    @Override
    public Collection<String> accountsInUse() {
        return keyValueStorage.getKeySet(StorageKey.inUse);
    }

    @Override
    public boolean isInUse(final String accountId) {
        return keyValueStorage.containsKey(StorageKey.inUse, Validator.checkNotNull(accountId).orThrowForSymbol("accountId"));
    }

    @Override
    public void addToUsed(final String accountId) {
        final String key = Validator.checkNotNull(accountId)
                .andMap(String::trim)
                .andCheckNot(String::isBlank)
                .andCheck(s -> keyValueStorage.containsKey(StorageKey.accounts, s))
                .orThrow("accountId (" + accountId + ") cannot be empty and account should be in account storage");
        keyValueStorage.write(StorageKey.inUse, key, key);
    }

    @Override
    public Optional<String> removeFromUsed(final String accountId) {
        return Optional.ofNullable(keyValueStorage.removeKey(StorageKey.inUse, Validator.checkNotNull(accountId).orThrowForSymbol("accountId")));
    }
}
