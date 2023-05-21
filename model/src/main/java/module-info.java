module midorum.melbone.model {
    requires java.desktop; //FIXME get rid
    requires dma.util;
    requires com.midorum.win32api;

    exports midorum.melbone.model.dto;
    exports midorum.melbone.model.persistence;
    exports midorum.melbone.model.processing;
    exports midorum.melbone.model.exception;
    exports midorum.melbone.model.window.appcountcontrol;
    exports midorum.melbone.model.window.baseapp;
    exports midorum.melbone.model.window.launcher;
    exports midorum.melbone.model.window.uac;
    exports midorum.melbone.model.settings;
    exports midorum.melbone.model.settings.key;
    exports midorum.melbone.model.settings.stamp;
    exports midorum.melbone.model.settings.setting;
    exports midorum.melbone.model.settings.account;
    exports midorum.melbone.model.experimental.task;
}