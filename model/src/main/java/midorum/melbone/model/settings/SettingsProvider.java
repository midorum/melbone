package midorum.melbone.model.settings;

import midorum.melbone.model.settings.account.AccountBinding;
import midorum.melbone.model.settings.setting.Settings;
import midorum.melbone.model.settings.stamp.Stamps;

public interface SettingsProvider {

    Settings settings();

    Stamps stamps();

    AccountBinding accountBinding();
}
