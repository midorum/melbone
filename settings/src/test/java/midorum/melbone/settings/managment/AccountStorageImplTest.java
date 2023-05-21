package midorum.melbone.settings.managment;

import midorum.melbone.model.dto.Account;
import midorum.melbone.model.persistence.AccountStorage;
import midorum.melbone.model.persistence.StorageKey;
import midorum.melbone.settings.internal.management.SettingsFactoryInternal;
import midorum.melbone.settings.internal.storage.KeyValueStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AccountStorageImplTest {

    private final SettingsFactoryInternal settingsFactoryInternal = new SettingsFactoryInternal.Builder()
            .settingPropertyNaming(new SettingPropertyNaming.Builder().inMemoryStorage().build()).build();
    private final AccountStorage accountStorage = settingsFactoryInternal.accountStorage();
    private final KeyValueStorage internalStorage = settingsFactoryInternal.getKeyValueStorage();

    @BeforeEach
    void beforeEach() {
        internalStorage.removeMap(StorageKey.accounts);
        internalStorage.removeMap(StorageKey.inUse);
        internalStorage.removeMap(StorageKey.registryAccount2);
        internalStorage.removeMap(StorageKey.registryResource2);
    }

    private Account createAccount(final String name) {
        return Account.builder()
                .name(name)
                .login(name + "_login")
                .password(name + "_password")
                .build();
    }

    @Nested
    class TestStoreAndRemove {

        @Test
        void storingGettingAndRemovingAccounts() {
            assertTrue(accountStorage.accountsNames().isEmpty());
            assertThrows(IllegalArgumentException.class, () -> accountStorage.store(null));

            //storing accounts
            final String acc1 = "acc1";
            final String acc2 = "acc2";
            accountStorage.store(createAccount(acc1));
            accountStorage.store(createAccount(acc2));
            assertEquals(2, accountStorage.accountsNames().size());
            assertEquals(2, accountStorage.accounts().size());
            assertTrue(accountStorage.isExists(acc1));

            //removing accounts
            final Optional<Account> removed = accountStorage.remove(acc1);
            assertTrue(removed.isPresent());
            assertEquals(1, accountStorage.accountsNames().size());
            assertFalse(accountStorage.isExists(acc1));
            assertTrue(accountStorage.isExists(acc2));
        }

    }

    @Nested
    class TestAccountsInUse {

        @Test
        void addingInUseNormalAccount() {
            final String acc1 = "acc1";
            final String acc2 = "acc2";
            assertTrue(accountStorage.accountsInUse().isEmpty());
            assertThrows(IllegalArgumentException.class, () -> accountStorage.addToUsed(null));

            //adding in use normal account
            accountStorage.store(createAccount(acc1));
            accountStorage.store(createAccount(acc2));
            accountStorage.addToUsed(acc1);
            accountStorage.addToUsed(acc2);
            assertTrue(accountStorage.isInUse(acc1));
            assertTrue(accountStorage.isInUse(acc2));
            assertEquals(2, accountStorage.accountsInUse().size());
        }

        @Test
        void accountHaveNotStoredBefore() {
            final String acc1 = "acc1";
            assertTrue(accountStorage.accountsInUse().isEmpty());

            //account haven't stored before
            assertThrows(IllegalArgumentException.class, () -> accountStorage.addToUsed(acc1));
            assertFalse(accountStorage.isInUse(acc1));
            assertEquals(0, accountStorage.accountsInUse().size());
        }

        @Test
        void removingFromUsed() {
            final String acc1 = "acc1";
            assertTrue(accountStorage.accountsInUse().isEmpty());

            //adding in use
            accountStorage.store(createAccount(acc1));
            accountStorage.addToUsed(acc1);
            assertTrue(accountStorage.isInUse(acc1));
            assertEquals(1, accountStorage.accountsInUse().size());

            //removing from used
            final Optional<String> removed = accountStorage.removeFromUsed(acc1);
            assertTrue(removed.isPresent());
            assertFalse(accountStorage.isInUse(acc1));
            assertEquals(0, accountStorage.accountsInUse().size());
        }

        @Test
        void removingAccountAtAll() {
            final String acc1 = "acc1";
            assertTrue(accountStorage.accountsInUse().isEmpty());

            //adding in use
            accountStorage.store(createAccount(acc1));
            accountStorage.addToUsed(acc1);
            assertTrue(accountStorage.isInUse(acc1));
            assertEquals(1, accountStorage.accountsInUse().size());

            //removing account at all
            accountStorage.remove(acc1);
            assertFalse(accountStorage.isInUse(acc1));
            assertEquals(0, accountStorage.accountsInUse().size());
        }

    }

}