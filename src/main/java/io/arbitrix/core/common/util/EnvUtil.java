package io.arbitrix.core.common.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Environment utility class for accessing application properties
 */
@Component
public class EnvUtil implements ApplicationContextAware {

    private static Environment environment;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        environment = applicationContext.getEnvironment();
    }

    /**
     * Get property value by key
     */
    public static String getProperty(String key) {
        if (environment == null) {
            return System.getProperty(key, System.getenv(key));
        }
        return environment.getProperty(key);
    }

    /**
     * Get property value by key with default value
     */
    public static String getProperty(String key, String defaultValue) {
        if (environment == null) {
            String value = System.getProperty(key, System.getenv(key));
            return value != null ? value : defaultValue;
        }
        return environment.getProperty(key, defaultValue);
    }

    /**
     * Get property value as Integer
     */
    public static Integer getIntProperty(String key, Integer defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get property value as Boolean
     */
    public static Boolean getBooleanProperty(String key, Boolean defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
}
