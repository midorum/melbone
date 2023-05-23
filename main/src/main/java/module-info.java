module midorum.melbone.main {
    requires java.desktop;
    requires java.sql;
    requires rxjava;
    requires com.h2database;
    requires midorum.melbone.model;
    requires midorum.melbone.settings;
    requires midorum.melbone.executor;
    requires dma.util;
    requires org.apache.logging.log4j;
    requires com.midorum.win32api;
    requires midorum.melbone.window;
    requires midorum.melbone.ui;
}