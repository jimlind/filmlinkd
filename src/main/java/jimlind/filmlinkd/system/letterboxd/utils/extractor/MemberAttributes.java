package jimlind.filmlinkd.system.letterboxd.utils.extractor;

import java.util.Locale;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;

/** Extract attributes from the LbMember Model. */
public final class MemberAttributes {
  /** Utility constructor. */
  private MemberAttributes() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Extracts the username from the member model as a lowercase string.
   *
   * @param member A Letterboxd member model
   * @return A lowercase string
   */
  public static String extractLowercaseUsername(LbMember member) {
    return getUsername(member).toLowerCase(Locale.ROOT);
  }

  private static String getUsername(LbMember member) {
    return member.getUsername();
  }
}
