module midorum.melbone.ui {
    requires com.midorum.win32api;
    requires dma.util;
    requires midorum.melbone.executor;
    requires midorum.melbone.model;
    requires midorum.melbone.window;
    requires org.apache.logging.log4j;
    requires java.desktop;
    requires midorum.melbone.settings;
    requires com.sun.jna.platform;

    exports midorum.melbone.ui;
}