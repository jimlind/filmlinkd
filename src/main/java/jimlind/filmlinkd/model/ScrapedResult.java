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

  /**
   * We expect duplicates to come in from the PubSub queue all the time so we need to limit when we
   * actually want them to be put in the queue for processing.
   *
   * @return Should this result be queued?
   */
  public boolean shouldBeQueued() {
    // If there is an override then it should always be queued
    if (getMessage().hasChannelOverride()) {
      return true;
    }

    // If entry matches the most recent previous do not queue, this is most common
    if (getUser().getMostRecentPrevious().equals(getMessage().getEntry().getLid())) {
      return false;
    }

    // If previous result list doesn't exist it can't contain the entry
    if (getUser().getPrevious().getList() == null || getUser().getPrevious().getList().isEmpty()) {
      return true;
    }

    // If entry matches any of the previous logged entries do not queue
    return !getUser().getPrevious().getList().contains(getMessage().getEntry().getLid());
  }
}
