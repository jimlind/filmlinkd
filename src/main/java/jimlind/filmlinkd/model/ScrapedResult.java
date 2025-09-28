package jimlind.filmlinkd.model;

import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;

/**
 * Model representing a ScrapeResult that happens when the scraper announces a new scraped entry.
 */
public record ScrapedResult(Message message, User user) {
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
    String previous = user.getMostRecentPrevious();
    boolean isNewerThanKnown = LidComparer.compare(previous, message.getEntry().getLid()) < 0;

    // Only send this message to one channel if it would otherwise post duplicates
    if (message.getChannelId() != null && message.hasChannelOverride() && !isNewerThanKnown) {
      return List.of(message.getChannelId());
    }

    // Get all channels for a user
    List<String> channelList = new ArrayList<>(user.getChannelIdList());
    // Add the specified channel if it is not in the list.
    if (message.getChannelId() != null && !channelList.contains(message.getChannelId())) {
      channelList.add(message.getChannelId());
    }

    return channelList;
  }

  /**
   * We expect duplicates to come in from the PubSub queue all the time so we need to limit when we
   * actually want them to be put in the queue for processing.
   *
   * @return Should this result be queued?
   */
  public boolean shouldBeQueued() {
    // If there is an override then it should always be queued
    if (message.hasChannelOverride()) {
      return true;
    }

    // If entry matches the most recent previous do not queue, this is most common
    if (user.getMostRecentPrevious().equals(message.getEntry().getLid())) {
      return false;
    }

    // If previous result list doesn't exist it can't contain the entry
    if (user.getPrevious().getList() == null || user.getPrevious().getList().isEmpty()) {
      return true;
    }

    // If entry matches any of the previous logged entries do not queue
    return !user.getPrevious().getList().contains(message.getEntry().getLid());
  }
}
