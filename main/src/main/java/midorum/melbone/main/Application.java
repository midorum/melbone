package midorum.melbone.main;

import midorum.melbone.executor.ExecutorFactory;
import midorum.melbone.settings.managment.SettingsFactory;
import midorum.melbone.ui.UserInterface;
import midorum.melbone.window.WindowFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Application {

    private static final Logger logger = LogManager.getLogger("main");

    public static void main(String[] args) {
        new Application().mainAction(args.length == 0 ? new Parameters() : new Parameters(args[0]));
    }

    private void mainAction(final Parameters parameters) {
        logger.info("--------------------------------------------------------");
        final SettingsFactory settingsFactory = new SettingsFactory.Builder()
                .propertyFileName(parameters.propertiesFileName)
                .build();
        final WindowFactory windowFactory = new WindowFactory.Builder()
                .settings(settingsFactory.settingsProvider().settings())
                .accountBinding(settingsFactory.settingsProvider().accountBinding())
                .stamps(settingsFactory.settingsProvider().stamps())
                .propertiesProvider(settingsFactory.propertiesProvider())
                .build();
        final ExecutorFactory executorFactory = new ExecutorFactory.Builder()
                .settings(settingsFactory.settingsProvider().settings())
                .windowFactory(windowFactory)
                .build();

        logger.info("--------------------------------------------------------");
        logger.info("application successfully started");
        logger.info("version {}", getClass().getPackage().getImplementationVersion());
        logger.info("--------------------------------------------------------");
        logger.info("sun.arch.data.model: {}", System.getProperty("sun.arch.data.model"));
        logger.info("speed factor: {}", settingsFactory.settingsProvider().settings().application().speedFactor());
        logger.info("screen resolution: {}", windowFactory.screenResolution());
        logger.info("scheduled task period: {}", settingsFactory.settingsProvider().settings().application().scheduledTaskPeriod());
        logger.info("mode: {}", settingsFactory.propertiesProvider().mode());
        logger.info("--------------------------------------------------------");

        new UserInterface.Builder()
                .executorFactory(executorFactory)
                .settings(settingsFactory.settingsProvider().settings())
                .settingStorage(settingsFactory.settingStorage())
                .accountStorage(settingsFactory.accountStorage())
                .taskStorage(settingsFactory.taskStorage())
                .windowFactory(windowFactory)
                .propertiesProvider(settingsFactory.propertiesProvider())
                .build().mainForm().display();
    }

    private record Parameters(String propertiesFileName) {
        public Parameters() {
            this(null);
        }
    }

}
