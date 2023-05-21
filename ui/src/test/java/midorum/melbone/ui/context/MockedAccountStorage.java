package midorum.melbone.ui.context;

import midorum.melbone.model.dto.Account;
import midorum.melbone.model.persistence.AccountStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class MockedAccountStorage implements AccountStorage {

    private final Logger logger = LogManager.getLogger();

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final Set<String> accountsInUse = new ConcurrentSkipListSet<>();

    @Override
    public Collection<Account> accounts() {
        final Collection<Account> result = List.copyOf(accounts.values());
        logger.trace("get all accounts: {}", result);
        return result;
    }

    @Override
    public Collection<String> accountsNames() {
        final Set<String> result = Set.copyOf(accounts.keySet());
        logger.trace("get accounts names: {}", result);
        return result;
    }

    @Override
    public boolean isExists(final String accountId) {
        final boolean result = accounts.containsKey(accountId);
        logger.trace("is exist account \"{}\": {}", accountId, result);
        return result;
    }

    @Override
    public void store(final Account account) {
        logger.trace("store account: \"{}\" (login: \"{}\", password: \"{}\")", account, account.login(), account.password());
        accounts.put(account.name(), account);
    }

    @Override
    public Account get(final String accountId) {
        final Account result = accounts.get(accountId);
        logger.trace("get account \"{}\": {}", accountId, result);
        return result;
    }

    @Override
    public Optional<Account> remove(final String accountId) {
        final Optional<Account> result = Optional.ofNullable(accounts.remove(accountId));
        logger.trace("remove account \"{}\": {}", accountId, result);
        return result;
    }

    @Override
    public Collection<String> accountsInUse() {
        final List<String> result = accountsInUse.stream().toList();
        logger.trace("get accounts in use: {}", result);
        return result;
    }

    @Override
    public boolean isInUse(final String accountId) {
        final boolean result = accountsInUse.contains(accountId);
        logger.trace("is account \"{}\" is in use: {}", accountId, result);
        return result;
    }

    @Override
    public void addToUsed(final String accountId) {
        logger.trace("add account \"{}\" in use", accountId);
        accountsInUse.add(accountId);
    }

    @Override
    public Optional<String> removeFromUsed(final String accountId) {
        logger.trace("remove account \"{}\" from used", accountId);
        return accountsInUse.remove(accountId) ? Optional.ofNullable(accountId) : Optional.empty();
    }
}
