package midorum.melbone.model.settings.setting;

public interface Settings {

    ApplicationSettings application();

    TargetLauncherSettings targetLauncher();

    TargetCountControlSettings targetCountControl();

    TargetBaseAppSettings targetBaseAppSettings();

    UacSettings uacSettings();
}
