package snowmonkey.meeno;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class MeenoConfig {
    private final Properties properties;
    private final static String PROPERTIES_FILENAME = "credentials.properties";

    public MeenoConfig(Properties properties) {
        this.properties = properties;
    }

    public static MeenoConfig loadMeenoConfig() {
        File f = new File(PROPERTIES_FILENAME);
        if(f.exists() && f.isFile()) {
            return loadMeenoConfigFromPropertiesFile(Paths.get(PROPERTIES_FILENAME));
        } else {
            return loadMeenoConfigFromEnvironment();
        }

    }

    public static MeenoConfig loadMeenoConfigFromPropertiesFile(Path file) {
        Properties properties = new Properties();
        try {
            try (Reader reader = Files.newBufferedReader(file)) {
                properties.load(reader);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read config file " + file, e);
        }
        return new MeenoConfig(properties);
    }

    public static MeenoConfig loadMeenoConfigFromEnvironment() {
        Properties properties = new Properties();

        properties.put("app-key.delayed", System.getenv("BETFAIR_APPKEY_DELAYED"));
        properties.put("app-key", System.getenv("BETFAIR_APPKEY"));
        properties.put("certificate.file", System.getenv("BETFAIR_CERTIFICATE_FILE"));
        properties.put("certificate.password", System.getenv("BETFAIR_CERTIFICATE_PASSWORD"));
        properties.put("username", System.getenv("BETFAIR_USERNAME"));
        properties.put("password", System.getenv("BETFAIR_PASSWORD"));

        return new MeenoConfig(properties);

    }

    public AppKey delayedAppKey() {
        return new AppKey(properties.getProperty("app-key.delayed"));
    }

    public AppKey appKey() {
        return new AppKey(properties.getProperty("app-key"));
    }

    public String username() {
        return properties.getProperty("username");
    }

    public String password() {
        return properties.getProperty("password");
    }

    public File certificateFile() {
        return new File(properties.getProperty("certificate.file"));
    }

    public String certificatePassword() {
        return properties.getProperty("certificate.password");
    }
}
