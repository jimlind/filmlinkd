package jimlind.filmlinkd.google.db;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/** Handles all read-only queries for user data without a database. */
public class DummyUserReader implements UserReaderInterface {
  /**
   * Always returns null.
   *
   * @param userLid The Letterboxd id for the user
   * @return Always null
   */
  public @Nullable QueryDocumentSnapshot getUserDocument(String userLid) {
    return null;
  }

  /**
   * Always returns zero.
   *
   * @return Always zero
   */
  public long getUserCount() {
    return 0L;
  }

  /**
   * Always returns an empty list.
   *
   * @return Always empty list
   */
  public List<QueryDocumentSnapshot> getActiveUsers() {
    return List.of();
  }

  /**
   * Always returns an empty list.
   *
   * @param pageSize Number of documents to fetch
   * @param pageIndex Zero-based page index (e.g., 0 for first page, 1 for second)
   * @return Always empty list
   */
  public List<QueryDocumentSnapshot> getActiveUsersPage(int pageSize, int pageIndex) {
    return List.of();
  }

  /**
   * Always returns an empty list.
   *
   * @param channelId The Discord channel id
   * @return Always empty list
   */
  public List<QueryDocumentSnapshot> getUserDocumentListByChannelId(String channelId) {
    return List.of();
  }

  /**
   * Always returns an empty list.
   *
   * @param channelIdList A list of Discord channel id
   * @return Always empty list
   */
  public List<QueryDocumentSnapshot> getUserDocumentListByChannelIdList(
      List<String> channelIdList) {
    return List.of();
  }
}
