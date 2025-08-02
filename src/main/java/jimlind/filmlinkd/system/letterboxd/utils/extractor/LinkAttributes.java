package jimlind.filmlinkd.system.letterboxd.utils.extractor;

import jimlind.filmlinkd.system.letterboxd.model.LbLink;
import jimlind.filmlinkd.system.letterboxd.model.LbLinkType;

/** Extract attributes from the LbLink Model. */
public final class LinkAttributes {
  /** Utility constructor. */
  private LinkAttributes() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Extract the type from a link model.
   *
   * @param link A Letterboxd link
   * @return The type of the link
   */
  public static LbLinkType extractType(LbLink link) {
    return link.getType();
  }
}
