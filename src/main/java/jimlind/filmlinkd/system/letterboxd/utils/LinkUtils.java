package jimlind.filmlinkd.system.letterboxd.utils;

import java.util.List;
import jimlind.filmlinkd.system.letterboxd.model.LbLink;
import jimlind.filmlinkd.system.letterboxd.model.LbLinkType;

public class LinkUtils {

  public String getLetterboxd(List<LbLink> linkList) {
    if (linkList.isEmpty()) {
      return "";
    }

    for (LbLink link : linkList) {
      if (link.type == LbLinkType.letterboxd) {
        return link.url;
      }
    }

    return "";
  }
}
