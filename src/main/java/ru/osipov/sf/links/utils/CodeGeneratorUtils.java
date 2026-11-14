package ru.osipov.sf.links.utils;

import java.security.SecureRandom;

public final class CodeGeneratorUtils {

  private static final String ALPHABET =
      "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final int DEFAULT_LENGTH = 18;

  private CodeGeneratorUtils() {}

  public static String generate() {
    return generate(DEFAULT_LENGTH);
  }

  public static String generate(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
    }
    return sb.toString();
  }
}
