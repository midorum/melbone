module midorum.melbone.settings {
    requires com.midorum.win32api;
    requires midorum.melbone.model;
    requires dma.util;
    requires com.h2database;
    requires org.apache.logging.log4j;

    exports midorum.melbone.settings;
    exports midorum.melbone.settings.managment to midorum.melbone.main, midorum.melbone.ui;
}