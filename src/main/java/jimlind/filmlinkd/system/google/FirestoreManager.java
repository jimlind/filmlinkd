package jimlind.filmlinkd.system.google;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.inject.Inject;
import java.util.List;
import jimlind.filmlinkd.system.google.firestore.UserReader;
import jimlind.filmlinkd.system.google.firestore.UserWriter;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

/** Handles all Firestore communication. Currently, a wrapper for multiple classes. */
@Slf4j
public class FirestoreManager {
  private final UserReader userReader;
  private final UserWriter userWriter;

  /**
   * The constructor for this class.
   *
   * @param userReader Handles all read-only queries for user data from Firestore
   * @param userWriter Handles all write queries for user data from Firestore
   */
  @Inject
  FirestoreManager(UserReader userReader, UserWriter userWriter) {
    this.userReader = userReader;
    this.userWriter = userWriter;
  }

  /**
   * Attempts to find a user in the database.
   *
   * @param userLid The Letterboxd id for the user
   * @return Returns null if no data available or returns the document snapshot from Firestorm if
   *     available
   */
  public @Nullable QueryDocumentSnapshot getUserDocument(String userLid) {
    return userReader.getUserDocument(userLid);
  }

  /**
   * Gives access to the total number of user records in the database.
   *
   * @return The total number of user records in the database.
   */
  public long getUserCount() {
    return userReader.getUserCount();
  }

  /**
   * Gives access to all documents for all active users. This should not be called regularly because
   * it is expensive.
   *
   * @return A List of documents for all active users.
   */
  public List<QueryDocumentSnapshot> getActiveUsers() {
    return userReader.getActiveUsers();
  }

  /**
   * Get all users where the specific channel is available in the list of channels in the user data
   * record. This should associate with the users that are followed in a channel.
   *
   * @param channelId The discord channel id
   * @return A list of all user documents that have the specific channel
   */
  public List<QueryDocumentSnapshot> getUserDocumentListByChannelId(String channelId) {
    return userReader.getUserDocumentListByChannelId(channelId);
  }

  /**
   * Attempts to add a channel to the user's data and ensures that we don't create duplicates.
   *
   * @param userLid User's Letterboxd id
   * @param channelId Discord Channel id
   * @return true if the action succeeded or the user already had the channel and false if any
   *     number of failures
   */
  public boolean addUserSubscription(String userLid, String channelId) {
    return userWriter.addUserSubscription(userLid, channelId);
  }

  /**
   * Attempts to remove a channel to the user's data.
   *
   * @param userLid User's Letterboxd id
   * @param channelId Discord Channel id
   * @return true if the action succeeded and false if any number of failures
   */
  public boolean removeUserSubscription(String userLid, String channelId) {
    return userWriter.removeUserSubscription(userLid, channelId);
  }

  /**
   * Updates the username, display name, and image based on most recent data from Letterboxd.
   *
   * @param member Letterboxd Member object from API
   * @return true if the action succeeded and false if any number of failures
   */
  public boolean updateUserDisplayData(LbMember member) {
    return userWriter.updateUserDisplayData(member);
  }

  /**
   * Updates the stored data for the most recent diary entry for a user.
   *
   * @param userLid User's Letterboxd id
   * @param diaryLid Diary's Letterboxd id
   * @param diaryPublishedDate Diary's published date
   * @param diaryUri Diary's URI
   * @return true if the action succeeded and false if any number of failures
   */
  public boolean updateUserPrevious(
      String userLid, String diaryLid, long diaryPublishedDate, String diaryUri) {
    return userWriter.updateUserPrevious(userLid, diaryLid, diaryPublishedDate, diaryUri);
  }
}
