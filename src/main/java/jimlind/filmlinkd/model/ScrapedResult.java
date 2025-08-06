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

  /**
   * Gets diary entry object from scraped result.
   *
   * @return The object containing all necessary diary entry data
   */
  public Message.Entry getEntry() {
    return message.getEntry();
  }

  /**
   * Gets previous entry object from scraped result.
   *
   * @return The object containing the data about a user's previous entry
   */
  public User.Previous getPrevious() {
    return user.getPrevious();
  }

  /**
   * Gets list of all previous entry ids from scraped result.
   *
   * @return A list of previous entry ids
   */
  public List<String> getPreviousList() {
    return getPrevious().getList();
  }

  /**
   * Gets the list of channels to send a scraped result to. Get all channels if the entry is brand
   * new. If it isn't brand new, and it has a channel override set, use that channel instead.
   *
   * @return A list of channel ids as strings
   */
  public List<String> getChannelList() {
    List<String> channelList = new ArrayList<>();
    String previous = getUser().getMostRecentPrevious();
    boolean isNewerThanKnown = LidComparer.compare(previous, getMessage().getEntry().getLid()) < 0;

    if (getMessage().hasChannelOverride() && !isNewerThanKnown) {
      channelList.add(getMessage().channelId);
      return channelList;
    }

    // TODO: This used to have a try/catch wrapper
    return getUser().getChannelIdList();
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
