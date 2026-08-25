package vn.edu.eaut.qlhocphi.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Doc cau hinh tu file application.properties (DB, AI, Payment). */
public class AppConfig {
    private static final Properties PROPS = new Properties();
    private static boolean loaded = false;

    private static synchronized void ensureLoaded() {
        if (loaded) return;
        try (InputStream in = AppConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) {
                PROPS.load(in);
            }
        } catch (IOException e) {
            System.err.println("Khong the doc application.properties: " + e.getMessage());
        }
        loaded = true;
    }

    public static String get(String key) {
        ensureLoaded();
        return PROPS.getProperty(key, "");
    }

    public static String get(String key, String defaultValue) {
        ensureLoaded();
        return PROPS.getProperty(key, defaultValue);
    }
}