package midorum.melbone.settings.internal.account;

import midorum.melbone.model.dto.Account;
import midorum.melbone.model.persistence.AccountStorage;
import midorum.melbone.model.persistence.StorageKey;
import midorum.melbone.model.settings.account.AccountBinding;
import midorum.melbone.settings.internal.management.SettingsFactoryInternal;
import midorum.melbone.settings.internal.storage.KeyValueStorage;
import midorum.melbone.settings.managment.SettingPropertyNaming;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountBindingImplTest {

    private final SettingsFactoryInternal settingsFactoryInternal = new SettingsFactoryInternal.Builder()
            .settingPropertyNaming(new SettingPropertyNaming.Builder().inMemoryStorage().build()).build();
    private final AccountBinding accountBinding = settingsFactoryInternal.settingsProvider().accountBinding();
    private final AccountStorage accountStorage = settingsFactoryInternal.accountStorage();;

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

    @Test
    void bindingNormalAccounts() {
        final String acc1 = "acc1";
        final String resource1 = "resource1";
        assertTrue(accountBinding.getBoundAccount(resource1).isEmpty());

        //binding normal account
        accountStorage.store(createAccount(acc1));
        accountBinding.bindResource(acc1, resource1);
        assertTrue(accountBinding.getBoundAccount(resource1).isPresent());
        assertEquals(acc1, accountBinding.getBoundAccount(resource1).get());

        assertEquals(1, internalStorage.getKeySet(StorageKey.registryAccount2).size());
        assertEquals(1, internalStorage.getKeySet(StorageKey.registryResource2).size());
    }

    @Test
    void accountHaveNotStoredBefore() {
        final String acc1 = "acc1";
        final String resource1 = "resource1";

        //account haven't stored before
        assertThrows(IllegalArgumentException.class, () -> accountBinding.bindResource(acc1, resource1));
        assertTrue(accountBinding.getBoundAccount(resource1).isEmpty());

        assertEquals(0, internalStorage.getKeySet(StorageKey.registryAccount2).size());
        assertEquals(0, internalStorage.getKeySet(StorageKey.registryResource2).size());
    }

    @Test
    void clearingOldBinding() {
        final String acc1 = "acc1";
        final String resource1 = "resource1";
        final String resource2 = "resource2";

        //first binding
        accountStorage.store(createAccount(acc1));
        accountBinding.bindResource(acc1, resource1);
        assertTrue(accountBinding.getBoundAccount(resource1).isPresent());
        assertEquals(acc1, accountBinding.getBoundAccount(resource1).get());

        assertEquals(1, internalStorage.getKeySet(StorageKey.registryAccount2).size());
        assertEquals(1, internalStorage.getKeySet(StorageKey.registryResource2).size());

        //next binding
        accountBinding.bindResource(acc1, resource2);
        assertTrue(accountBinding.getBoundAccount(resource2).isPresent());
        assertEquals(acc1, accountBinding.getBoundAccount(resource2).get());
        assertTrue(accountBinding.getBoundAccount(resource1).isEmpty());

        assertEquals(1, internalStorage.getKeySet(StorageKey.registryAccount2).size());
        assertEquals(1, internalStorage.getKeySet(StorageKey.registryResource2).size());
    }

}