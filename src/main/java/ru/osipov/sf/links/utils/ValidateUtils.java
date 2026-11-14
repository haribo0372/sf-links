package ru.osipov.sf.links.utils;

import java.net.URI;
import java.net.URISyntaxException;

public final class ValidateUtils {

  private static final String SHORT_LINK_PATTERN = "^clck\\.ru/[A-Za-z0-9]+$";

  public static boolean isValidHttpsUrl(String url) {
    if (url == null || url.isBlank()) {
      return false;
    }
    try {
      URI uri = new URI(url);
      return "https".equalsIgnoreCase(uri.getScheme())
          && uri.getHost() != null
          && !uri.getHost().isBlank();
    } catch (URISyntaxException e) {
      return false;
    }
  }

  public static boolean isValidShortLink(String shortLink) {
    return shortLink != null && !shortLink.isBlank() && shortLink.matches(SHORT_LINK_PATTERN);
  }
}
