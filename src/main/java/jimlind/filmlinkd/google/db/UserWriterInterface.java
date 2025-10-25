package jimlind.filmlinkd.google.db;

import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;

/** Handles all create, update, and delete operations for user data in Firestore. */
public interface UserWriterInterface {
  /**
   * Creates a new user document in Firestore from Letterboxd API data.
   *
   * @param member Letterboxd Member object from API
   * @return a fully populated user model
   */
  User createUserDocument(LbMember member);

  /**
   * Adds a channel subscription to a user's document.
   *
   * @param userLid User's Letterboxd id
   * @param channelId Discord Channel id
   * @return true if the action succeeded or the channel was already present; false on failure
   */
  boolean addUserSubscription(String userLid, String channelId);

  /**
   * Removes a channel subscription from a user's document.
   *
   * @param userLid User's Letterboxd id
   * @param channelId Discord Channel id
   * @return true if the action succeeded; false on failure
   */
  boolean removeUserSubscription(String userLid, String channelId);

  /**
   * Removes a channel subscription from a user's document.
   *
   * @param userLid User's Letterboxd id
   * @param channelId Discord Channel id
   * @return true if the action succeeded; false on failure
   */
  boolean archiveUserSubscription(String userLid, String channelId);

  /**
   * Removes all channel subscriptions from a user's document.
   *
   * @param userLid User's Letterboxd id
   * @return true if the action succeeded; false on failure
   */
  boolean archiveAllUserSubscriptions(String userLid);

  /**
   * Updates the username, display name, and image from recent Letterboxd data.
   *
   * @param member Letterboxd Member object from API.
   * @return true if the action succeeded; false on failure.
   */
  boolean updateUserDisplayData(LbMember member);

  /**
   * Updates the stored data for a user's most recent diary entries.
   *
   * @param userLid User's Letterboxd id.
   * @param diaryLid Diary's Letterboxd id.
   * @param diaryPublishedDate Diary's published date.
   * @param diaryUri Diary's URI.
   * @return true if the action succeeded; false on failure.
   */
  boolean updateUserPrevious(
      String userLid, String diaryLid, long diaryPublishedDate, String diaryUri);
}
