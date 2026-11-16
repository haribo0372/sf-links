package ru.osipov.sf.links.config;

import lombok.Getter;

@Getter
public class ApplicationConfig {

  private final String userFile;
  private final String linkFile;
  private final long linkTtlSeconds;

  public ApplicationConfig() {
    this.userFile = requireEnv("FILE_USER");
    this.linkFile = requireEnv("FILE_LINK");
    this.linkTtlSeconds = requirePositiveLongEnv("LINK_TTL");
  }

  // для тестов — конструктор со значениями
  public ApplicationConfig(String userFile, String linkFile, long linkTtlSeconds) {
    this.userFile = userFile;
    this.linkFile = linkFile;
    this.linkTtlSeconds = linkTtlSeconds;
  }

  private static String requireEnv(String key) {
    String v = System.getenv(key);
    if (v == null || v.isBlank()) {
      throw new IllegalStateException("Env var " + key + " is not set");
    }
    return v.trim();
  }

  private static long requirePositiveLongEnv(String key) {
    String v = requireEnv(key);
    try {
      long parsed = Long.parseLong(v);
      if (parsed <= 0) throw new IllegalStateException(key + " must be positive, got: " + parsed);
      return parsed;
    } catch (NumberFormatException e) {
      throw new IllegalStateException(key + " must be a number, got: " + v, e);
    }
  }
}
