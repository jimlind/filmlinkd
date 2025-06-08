package jimlind.filmlinkd.system.letterboxd.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlUtils {
  public static String encodePath(String path) {
    return URLEncoder.encode(path, StandardCharsets.UTF_8).replace("+", "%20");
  }
}
