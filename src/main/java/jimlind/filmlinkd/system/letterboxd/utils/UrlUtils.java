package jimlind.filmlinkd.system.letterboxd.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Utilities to translate URLs from Letterboxd to universally usable URLs. */
public class UrlUtils {
  /**
   * Encodes a given URL to remove all possible problematic symbols.
   *
   * @param path A URL available in the Letterboxd platform
   * @return A URL that has properly encoded all possible problematic symbols.
   */
  public static String encodePath(String path) {
    return URLEncoder.encode(path, StandardCharsets.UTF_8).replace("+", "%20");
  }
}
