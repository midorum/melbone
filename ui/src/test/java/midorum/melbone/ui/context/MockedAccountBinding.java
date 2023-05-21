package midorum.melbone.ui.context;

import midorum.melbone.model.settings.account.AccountBinding;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class MockedAccountBinding implements AccountBinding {

    private final Logger logger = LogManager.getLogger();
    private final Map<String, String> boundAccounts = new ConcurrentHashMap<>();

    @Override
    public void bindResource(final String accountId, final String resourceId) {
        logger.trace("bind account \"{}\" with resource \"{}\"", accountId, resourceId);
        boundAccounts.put(resourceId, accountId);
    }

    @Override
    public Optional<String> getBoundAccount(final String resourceId) {
        final Optional<String> result = Optional.ofNullable(boundAccounts.get(resourceId));
        logger.trace("get bound account with resource \"{}\": {}", resourceId, result);
        return result;
    }
}
