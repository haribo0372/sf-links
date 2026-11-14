package ru.osipov.sf.links.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import lombok.Getter;

@Getter
public class ApplicationConfig {

  private final String userFile;
  private final String linkFile;
  private final long linkTtlSeconds;

  public ApplicationConfig() {
    this(loadProperties());
  }

  public ApplicationConfig(Properties props) {
    this.userFile = requireProperty(props, "files.user");
    this.linkFile = requireProperty(props, "files.link");
    this.linkTtlSeconds = requirePositiveLong(props, "link.ttl");
  }

  private static Properties loadProperties() {
    Properties props = new Properties();
    try (InputStream in =
        ApplicationConfig.class.getClassLoader().getResourceAsStream("application.properties")) {

      if (in == null) {
        throw new IllegalStateException("application.properties not found in classpath");
      }
      props.load(in);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load application.properties", e);
    }
    return props;
  }

  private static String requireProperty(Properties props, String key) {
    String value = props.getProperty(key);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(key + " is not set");
    }
    return value.trim();
  }

  private static long requirePositiveLong(Properties props, String key) {
    String value = requireProperty(props, key);
    long parsed;
    try {
      parsed = Long.parseLong(value);
    } catch (NumberFormatException e) {
      throw new IllegalStateException(key + " must be a number, got: " + value, e);
    }
    if (parsed <= 0) {
      throw new IllegalStateException(key + " must be positive, got: " + parsed);
    }
    return parsed;
  }
}
