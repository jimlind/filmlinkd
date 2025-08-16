package jimlind.filmlinkd.system.google;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.google.firestore.UserReader;
import jimlind.filmlinkd.system.google.firestore.UserWriter;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import lombok.extern.slf4j.Slf4j;

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
