package com.scr.journal.util;

import com.scr.journal.config.PropertyModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public class PropertyLoader {

    public static Properties loadProperties(String firstPath, String... rest) {
        Properties properties = loadProperties(firstPath);
        for (String path : rest) {
            properties = loadProperties(path, properties);
        }
        return properties;
    }

    private static Properties loadProperties(String path) {
        return loadProperties(path, (Properties) null);
    }

    public static Properties loadProperties(String path, Properties fallbackProperties) {
        try (InputStream inputStream = PropertyModule.class.getResourceAsStream(path)) {
            Properties properties = new Properties(fallbackProperties);
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
