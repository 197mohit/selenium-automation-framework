package com.paytm.framework.utils;

import java.io.*;
import java.util.Properties;

/**
 * Utility class provides the methods to read write the property files.
 */
public class PropertyUtil {

    private static PropertyUtil prop;
    private Properties properties;

    private PropertyUtil() {
        properties = new Properties();
    }

    /**
     * Creates a new property file object if already not exist and returns it.
     *
     * @return The property file object.
     */
    public static synchronized PropertyUtil getInstance() {
        if (prop == null) {
            prop = new PropertyUtil();
        }
        return prop;
    }

    /**
     * Loads the specified property file to the InputStream.
     *
     * @param fileName Exact filename of the property file.
     */
    public void load(String fileName) {
        InputStream input;
        input = getClass().getClassLoader().getResourceAsStream(fileName);
        try {
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the specified property file to the InputStream.
     *
     * @param file The file object property file.
     */
    public void load(File file) {
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Clears this Properties object so that it contains no keys.
     */
    public void clear() {
        properties.clear();
    }

    /**
     * Reads from the property file and provides the values of specified key.
     *
     * @param key The key whose value is required to be read.
     * @return The value of specified key.
     */
    public String getValue(String key) {
        return properties.getProperty(key).trim();
    }

    /**
     * Reads from the property file and provides the values of specified key, but if that key is not present then it will return the default value.
     *
     * @param key          The key whose value is required to be read.
     * @param defaultValue a defaultValue of the specified key if that key is not present or
     * @return The value of specified key or the default value.
     */
    public String getValue(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue).trim();
    }

    /**
     * Sets the value of specified key in the property file.
     *
     * @param key   The key whose values is required to be set.
     * @param value The value that needed to be set for the specified key.
     */
    public void setValue(String key, String value) {
        properties.setProperty(key, value);
    }
}
