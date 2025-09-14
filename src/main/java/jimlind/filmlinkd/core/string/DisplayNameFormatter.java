package jimlind.filmlinkd.core.string;

/** Utility class for formatting display names. */
public final class DisplayNameFormatter {

  /** Private constructor to prevent instantiation of utility class. */
  private DisplayNameFormatter() {}

  /**
   * Formats the display name for use in Discord, which uses a Markdown-like syntax.
   *
   * @param displayName The raw display name.
   * @return A formatted display name with special characters escaped.
   */
  public static String format(String displayName) {
    return displayName.replace("_", "\\_");
  }
}
