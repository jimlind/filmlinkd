package jimlind.filmlinkd.core.string;

import java.util.Locale;

/** Utility class for formatting usernames. */
public final class UsernameFormatter {

  /** Private constructor to prevent instantiation of utility class. */
  private UsernameFormatter() {}

  /**
   * Formats the username for use in Discord, which uses a Markdown-like syntax.
   *
   * @param username The raw username.
   * @return A formatted display name with special characters escaped.
   */
  public static String format(String username) {
    return username.replace("_", "\\_").toLowerCase(Locale.ENGLISH);
  }
}
