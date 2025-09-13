package jimlind.filmlinkd.core.string;

import java.util.Locale;

public class UsernameFormatter {
  public static String format(String username) {
    return username.replace("_", "\\_").toLowerCase(Locale.ENGLISH);
  }
}
