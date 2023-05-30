package midorum.melbone.model.settings.key;

import java.util.function.Function;

public interface SettingObtainWay {

    SettingsManagerAction action();

    Function<Object, Object> extractor();

}
