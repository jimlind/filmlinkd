package jimlind.filmlinkd.system.letterboxd.utils;

import java.util.List;
import jimlind.filmlinkd.system.letterboxd.model.LBLink;
import jimlind.filmlinkd.system.letterboxd.model.LBLinkType;

public class LinkUtils {

  public String getLetterboxd(List<LBLink> linkList) {
    if (linkList.isEmpty()) {
      return "";
    }

    for (LBLink link : linkList) {
      if (link.type == LBLinkType.letterboxd) {
        return link.url;
      }
    }

    return "";
  }
}
