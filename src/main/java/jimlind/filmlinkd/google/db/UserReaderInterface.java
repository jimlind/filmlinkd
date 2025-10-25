package jimlind.filmlinkd.google.db;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/** Handles all read-only queries for user data. */
public interface UserReaderInterface {

  /**
   * Attempts to find a user document in the database.
   *
   * @param userLid The Letterboxd id for the user
   * @return The document snapshot from Firestore if available, otherwise null
   */
  @Nullable
  QueryDocumentSnapshot getUserDocument(String userLid);

  /**
   * Gets the total number of user records in the database.
   *
   * @return The total number of user records
   */
  long getUserCount();

  /**
   * Gets all documents for all active users (those subscribed to at least one channel). This can be
   * an expensive operation.
   *
   * @return A List of documents for all active users
   */
  List<QueryDocumentSnapshot> getActiveUsers();

  /**
   * Gets a page of documents for active users (those subscribed to at least one channel).
   *
   * @param pageSize Number of documents to fetch
   * @param pageIndex Zero-based page index (e.g., 0 for first page, 1 for second)
   * @return A List of documents for the current page
   */
  List<QueryDocumentSnapshot> getActiveUsersPage(int pageSize, int pageIndex);

  /**
   * Gets all user documents for users followed in a specific channel.
   *
   * @param channelId The Discord channel id
   * @return A list of all user documents that have the specific channel
   */
  List<QueryDocumentSnapshot> getUserDocumentListByChannelId(String channelId);

  /**
   * Gets all user documents for users followed in a list of channel.
   *
   * @param channelIdList A list of Discord channel id
   * @return A list of all user documents that have the specific channel
   */
  List<QueryDocumentSnapshot> getUserDocumentListByChannelIdList(List<String> channelIdList);
}
