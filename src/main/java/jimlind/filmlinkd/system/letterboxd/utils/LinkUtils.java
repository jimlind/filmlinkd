package jimlind.filmlinkd.system.letterboxd.utils;

import java.util.List;
import jimlind.filmlinkd.system.letterboxd.model.LbLink;
import jimlind.filmlinkd.system.letterboxd.model.LbLinkType;
import jimlind.filmlinkd.system.letterboxd.utils.extractor.LinkAttributes;

/** Utilities to translate Letterboxd Link objects to other formats. */
public class LinkUtils {
  /**
   * Extract the URL for a Letterboxd link from a list of links.
   *
   * @param linkList A list of Letterboxd links
   * @return The URL for a Letterboxd link or a blank string if not found.
   */
  public static String getLetterboxd(List<LbLink> linkList) {
    if (linkList.isEmpty()) {
      return "";
    }

    for (LbLink link : linkList) {
      if (LinkAttributes.extractType(link) == LbLinkType.letterboxd) {
        return link.getUrl();
      }
    }

    return "";
  }
}
