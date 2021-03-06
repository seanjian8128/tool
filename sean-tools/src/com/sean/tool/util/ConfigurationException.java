package com.sean.tool.util;

import org.apache.shiro.ShiroException;

public class ConfigurationException extends ShiroException
{

    /**
     * Creates a new ConfigurationException.
     */
    public ConfigurationException() {
        super();
    }

    /**
     * Constructs a new ConfigurationException.
     *
     * @param message the reason for the exception
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ConfigurationException.
     *
     * @param cause the underlying Throwable that caused this exception to be thrown.
     */
    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new ConfigurationException.
     *
     * @param message the reason for the exception
     * @param cause   the underlying Throwable that caused this exception to be thrown.
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
