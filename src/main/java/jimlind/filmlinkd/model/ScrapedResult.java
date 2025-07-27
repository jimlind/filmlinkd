package jimlind.filmlinkd.model;

import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;
import lombok.Getter;
import lombok.Setter;

/**
 * Model representing a ScrapeResult that happens when the scraper announces a new scraped entry.
 */
@Getter
@Setter
public class ScrapedResult {
  public Message message;
  public User user;

  public Message.Entry getEntry() {
    return message.getEntry();
  }

  public User.Previous getPrevious() {
    return user.getPrevious();
  }

  public List<String> getPreviousList() {
    return getPrevious().getList();
  }

  public List<String> getChannelList() {
    List<String> channelList = new ArrayList<>();
    String previous = getUser().getMostRecentPrevious();
    boolean isNewerThanKnown = LidComparer.compare(previous, getMessage().getEntry().getLid()) < 0;

    if (getMessage().hasChannelOverride() && !isNewerThanKnown) {
      channelList.add(getMessage().channelId);
      return channelList;
    }

    return getUser().getChannelList();
  }
}
