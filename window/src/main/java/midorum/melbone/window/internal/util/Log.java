package midorum.melbone.window.internal.util;

import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public record Log(Logger logger, Supplier<String> markerSupplier) {

    public Log(final Logger logger, final String marker) {
        this(logger, () -> marker);
    }

    public void error(final String message, Object... params) {
        if (!logger.isErrorEnabled()) return;
        logger.error("(" + markerSupplier.get() + ") " + message, params);
    }

    public void error(final String message, Throwable t) {
        if (!logger.isErrorEnabled()) return;
        logger.error("(" + markerSupplier.get() + ") " + message, t);
    }

    public void warn(final String message, Object... params) {
        if (!logger.isWarnEnabled()) return;
        logger.warn("(" + markerSupplier.get() + ") " + message, params);
    }

    public void info(final String message, Object... params) {
        if (!logger.isInfoEnabled()) return;
        logger.info("(" + markerSupplier.get() + ") " + message, params);
    }

    public void debug(final String message, Object... params) {
        if (!logger.isDebugEnabled()) return;
        logger.debug("(" + markerSupplier.get() + ") " + message, params);
    }

}
