package org.apache.shindig.logging;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * Very simple delegate class which allows to use {@link ResourceBundle} to form log entries.
 */
public class I18nLogger implements Logger {

    private final Logger delegate;
    private final ResourceBundle bundle;

    public I18nLogger(Logger delegate, ResourceBundle bundle) {
        this.delegate = delegate;
        this.bundle = bundle;
    }

    public String getName() {
        return delegate.getName();
    }

    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    public void trace(String msg) {
        delegate.trace(format(msg));
    }

    public void trace(String format, Object arg) {
        delegate.trace(format(format, arg), arg);
    }

    public void trace(String format, Object arg1, Object arg2) {
        delegate.trace(format(format, arg1, arg2), arg1, arg2);
    }

    public void trace(String format, Object[] argArray) {
        delegate.trace(format(format, argArray), argArray);
    }

    public void trace(String msg, Throwable t) {
        delegate.trace(format(msg), t);
    }

    public boolean isTraceEnabled(Marker marker) {
        return delegate.isTraceEnabled(marker);
    }

    public void trace(Marker marker, String msg) {
        delegate.trace(marker, format(msg));
    }

    public void trace(Marker marker, String format, Object arg) {
        delegate.trace(marker, format(format, arg), arg);
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        delegate.trace(marker, format(format, arg1, arg2), arg1, arg2);
    }

    public void trace(Marker marker, String format, Object[] argArray) {
        delegate.trace(marker, format(format, argArray), argArray);
    }

    public void trace(Marker marker, String msg, Throwable t) {
        delegate.trace(marker, format(msg), t);
    }

    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    public void debug(String msg) {
        delegate.debug(format(msg));
    }

    public void debug(String format, Object arg) {
        delegate.debug(format(format, arg), arg);
    }

    public void debug(String format, Object arg1, Object arg2) {
        delegate.debug(format(format, arg1, arg2), arg1, arg2);
    }

    public void debug(String format, Object[] argArray) {
        delegate.debug(format(format, argArray), argArray);
    }

    public void debug(String msg, Throwable t) {
        delegate.debug(format(msg), t);
    }

    public boolean isDebugEnabled(Marker marker) {
        return delegate.isDebugEnabled(marker);
    }

    public void debug(Marker marker, String msg) {
        delegate.debug(marker, format(msg));
    }

    public void debug(Marker marker, String format, Object arg) {
        delegate.debug(marker, format(format, arg), arg);
    }

    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        delegate.debug(marker, format(format, arg1, arg2), arg1, arg2);
    }

    public void debug(Marker marker, String format, Object[] argArray) {
        delegate.debug(marker, format(format, argArray), argArray);
    }

    public void debug(Marker marker, String msg, Throwable t) {
        delegate.debug(marker, format(msg), t);
    }

    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    public void info(String msg) {
        delegate.info(format(msg));
    }

    public void info(String format, Object arg) {
        delegate.info(format(format, arg), arg);
    }

    public void info(String format, Object arg1, Object arg2) {
        delegate.info(format(format, arg1, arg2), arg1, arg2);
    }

    public void info(String format, Object[] argArray) {
        delegate.info(format(format, argArray), argArray);
    }

    public void info(String msg, Throwable t) {
        delegate.info(format(msg), t);
    }

    public boolean isInfoEnabled(Marker marker) {
        return delegate.isInfoEnabled(marker);
    }

    public void info(Marker marker, String msg) {
        delegate.info(marker, format(msg));
    }

    public void info(Marker marker, String format, Object arg) {
        delegate.info(marker, format(format, arg), arg);
    }

    public void info(Marker marker, String format, Object arg1, Object arg2) {
        delegate.info(marker, format(format, arg1, arg2), arg1, arg2);
    }

    public void info(Marker marker, String format, Object[] argArray) {
        delegate.info(marker, format(format, argArray), argArray);
    }

    public void info(Marker marker, String msg, Throwable t) {
        delegate.info(marker, format(msg), t);
    }

    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    public void warn(String msg) {
        delegate.warn(format(msg));
    }

    public void warn(String format, Object arg) {
        delegate.warn(format(format, arg), arg);
    }

    public void warn(String format, Object[] argArray) {
        delegate.warn(format(format, argArray), argArray);
    }

    public void warn(String format, Object arg1, Object arg2) {
        delegate.warn(format(format, arg1, arg2), arg1, arg2);
    }

    public void warn(String msg, Throwable t) {
        delegate.warn(format(msg), t);
    }

    public boolean isWarnEnabled(Marker marker) {
        return delegate.isWarnEnabled(marker);
    }

    public void warn(Marker marker, String msg) {
        delegate.warn(marker, format(msg));
    }

    public void warn(Marker marker, String format, Object arg) {
        delegate.warn(marker, format(format, arg), arg);
    }

    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        delegate.warn(marker, format(format, arg1, arg2), arg1, arg2);
    }

    public void warn(Marker marker, String format, Object[] argArray) {
        delegate.warn(marker, format(format, argArray), argArray);
    }

    public void warn(Marker marker, String msg, Throwable t) {
        delegate.warn(marker, format(msg), t);
    }

    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    public void error(String msg) {
        delegate.error(format(msg));
    }

    public void error(String format, Object arg) {
        delegate.error(format(format, arg), arg);
    }

    public void error(String format, Object arg1, Object arg2) {
        delegate.error(format(format, arg1, arg2), arg1, arg2);
    }

    public void error(String format, Object[] argArray) {
        delegate.error(format(format, argArray), argArray);
    }

    public void error(String msg, Throwable t) {
        delegate.error(format(msg), t);
    }

    public boolean isErrorEnabled(Marker marker) {
        return delegate.isErrorEnabled(marker);
    }

    public void error(Marker marker, String msg) {
        delegate.error(marker, format(msg));
    }

    public void error(Marker marker, String format, Object arg) {
        delegate.error(marker, format(format, arg), arg);
    }

    public void error(Marker marker, String format, Object arg1, Object arg2) {
        delegate.error(marker, format(format, arg1, arg2), arg1, arg2);
    }

    public void error(Marker marker, String format, Object[] argArray) {
        delegate.error(marker, format(format, argArray), argArray);
    }

    public void error(Marker marker, String msg, Throwable t) {
        delegate.error(marker, format(msg), t);
    }

    // -- helper method to format messages

    private String format(String key) {
        return bundle.getString(key);
    }

    private String format(String key, Object ... args) {
        return MessageFormat.format(bundle.getString(key), args);
    }

    public static Logger getLogger(Class<?> clazz, ResourceBundle bundle) {
        return new I18nLogger(LoggerFactory.getLogger(clazz), bundle);
    }

}
