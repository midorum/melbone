package midorum.melbone.settings.internal.management;

import dma.validation.Validator;
import midorum.melbone.model.dto.Account;
import midorum.melbone.model.persistence.AccountStorage;
import midorum.melbone.model.persistence.StorageKey;
import midorum.melbone.settings.internal.storage.KeyValueStorage;

import java.util.*;

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
        final String accountId = checkedAccount.name();
        keyValueStorage.write(StorageKey.accounts, accountId, checkedAccount);
        updateCommentaryIndex(accountId);
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
        final Optional<Account> maybeAccount = Optional.ofNullable(keyValueStorage.removeKey(StorageKey.accounts, key));
        updateCommentaryIndex(key);
        return maybeAccount;
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

    @Override
    public Collection<String> commentaries() {
        return keyValueStorage.getKeySet(StorageKey.accountCommentaries);
    }

    private List<String> getAccountCommentaryTokens(final Account account) {
        final Optional<String> maybeCommentary = account.commentary();
        if (maybeCommentary.isEmpty()) return List.of();
        return Arrays.stream(maybeCommentary.get().split(";")).map(String::trim).toList();
    }

    private void updateCommentaryIndex(final String accountId) {
        clearCommentaryTokens(accountId);
        keyValueStorage.read(StorageKey.accounts, accountId)
                .map(Account.class::cast)
                .ifPresent(account -> getAccountCommentaryTokens(account).forEach(token -> storeCommentaryToken(token, accountId)));
    }

    @SuppressWarnings("unchecked")
    private void clearCommentaryTokens(final String accountId) {
        final Set<String> tokensSet = keyValueStorage.getKeySet(StorageKey.accountCommentaries);
        for (String token : tokensSet) {
            keyValueStorage.read(StorageKey.accountCommentaries, token)
                    .map(o -> (Set<String>) o)
                    .ifPresent(accounts -> {
                        if (accounts.remove(accountId) && accounts.isEmpty())
                            keyValueStorage.removeKey(StorageKey.accountCommentaries, token);
                        else keyValueStorage.write(StorageKey.accountCommentaries, token, accounts);
                    });
        }
    }

    @SuppressWarnings("unchecked")
    private void storeCommentaryToken(final String token, final String accountId) {
        final Set<String> accountsSet = keyValueStorage.read(StorageKey.accountCommentaries, token)
                .map(o -> (Set<String>) o)
                .orElse(new HashSet<>());
        accountsSet.add(accountId);
        keyValueStorage.write(StorageKey.accountCommentaries, token, accountsSet);
    }
}
