package midorum.melbone.model.persistence;

import midorum.melbone.model.dto.Account;

import java.util.Collection;
import java.util.Optional;

public interface AccountStorage {

    Collection<Account> accounts();

    Collection<String> accountsNames();

    boolean isExists(final String accountId);

    void store(final Account account);

    Account get(final String accountId);

    Optional<Account> remove(final String accountId);

    Collection<String> accountsInUse();

    boolean isInUse(final String accountId);

    void addToUsed(final String accountId);

    Optional<String> removeFromUsed(final String accountId);

    Collection<String> commentaries();
}
