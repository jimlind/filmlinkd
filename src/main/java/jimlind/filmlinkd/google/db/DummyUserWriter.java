package jimlind.filmlinkd.google.db;

import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;

/** Handles all create, update, and delete operations for user data. */
public class DummyUserWriter implements UserWriterInterface {
  /**
   * Creates a new user document from Letterboxd API data.
   *
   * @param member Letterboxd Member object from API
   * @return a fully populated user model
   */
  public User createUserDocument(LbMember member) {
    return null;
  }

  /**
   * Adds a channel subscription to a user's document.
   *
   * @param userLid User's Letterboxd id
   * @param channelId Discord Channel id
   * @return true if the action succeeded or the channel was already present; false on failure
   */
  public boolean addUserSubscription(String userLid, String channelId) {
    return true;
  }

  /**
   * Removes a channel subscription from a user's document.
   *
   * @param userLid User's Letterboxd id
   * @param channelId Discord Channel id
   * @return true if the action succeeded; false on failure
   */
  public boolean removeUserSubscription(String userLid, String channelId) {
    return true;
  }

  /**
   * Removes a channel subscription from a user's document.
   *
   * @param userLid User's Letterboxd id
   * @param channelId Discord Channel id
   * @return true if the action succeeded; false on failure
   */
  public boolean archiveUserSubscription(String userLid, String channelId) {
    return true;
  }

  /**
   * Removes all channel subscriptions from a user's document.
   *
   * @param userLid User's Letterboxd id
   * @return true if the action succeeded; false on failure
   */
  public boolean archiveAllUserSubscriptions(String userLid) {
    return true;
  }

  /**
   * Updates the username, display name, and image from recent Letterboxd data.
   *
   * @param member Letterboxd Member object from API.
   * @return true if the action succeeded; false on failure.
   */
  public boolean updateUserDisplayData(LbMember member) {
    return true;
  }

  /**
   * Updates the stored data for a user's most recent diary entries.
   *
   * @param userLid User's Letterboxd id.
   * @param diaryLid Diary's Letterboxd id.
   * @param diaryPublishedDate Diary's published date.
   * @param diaryUri Diary's URI.
   * @return true if the action succeeded; false on failure.
   */
  public boolean updateUserPrevious(
      String userLid, String diaryLid, long diaryPublishedDate, String diaryUri) {
    return true;
  }
}
