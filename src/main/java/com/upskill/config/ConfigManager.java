package com.upskill.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigManager - Loads environment-specific properties for the API framework.
 */
public class ConfigManager {

    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);
    private static Properties properties;

    private ConfigManager() {}

    public static synchronized String get(String key) {
        if (properties == null) loadProperties();
        String systemProp = System.getProperty(key);
        return systemProp != null ? systemProp : properties.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    private static void loadProperties() {
        properties = new Properties();
        String env = System.getProperty("env", "qa");
        String fileName = "config-" + env + ".properties";

        log.info("Loading API config for environment: {} (file: {})", env, fileName);

        try (InputStream input = ConfigManager.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                log.warn("Config file '{}' not found, trying config-qa.properties", fileName);
                try (InputStream fallback = ConfigManager.class.getClassLoader()
                        .getResourceAsStream("config-qa.properties")) {
                    if (fallback != null) properties.load(fallback);
                }
            } else {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load config: " + fileName, e);
        }

        log.info("Config loaded. Base URL: {}", properties.getProperty("api.base.url"));
    }
}
