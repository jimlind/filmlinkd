package jimlind.filmlinkd.system.google.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.AggregateQuerySnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import jimlind.filmlinkd.config.AppConfig;
import org.jetbrains.annotations.Nullable;

/** Handles all read-only queries for user data from Firestore. */
public class UserReader {
  private final AppConfig appConfig;
  private final Firestore db;

  /**
   * The constructor for this class.
   *
   * @param appConfig Contains application and environment variables
   * @param firestoreProvider Wrapper for the Firestore database client
   */
  @Inject
  public UserReader(AppConfig appConfig, FirestoreProvider firestoreProvider) {
    this.appConfig = appConfig;
    this.db = firestoreProvider.get();
  }

  /**
   * Attempts to find a user document in the database.
   *
   * @param userLid The Letterboxd id for the user
   * @return The document snapshot from Firestore if available, otherwise null
   */
  public @Nullable QueryDocumentSnapshot getUserDocument(String userLid) {
    String collectionId = appConfig.getFirestoreUserCollectionId();
    ApiFuture<QuerySnapshot> query =
        this.db.collection(collectionId).whereEqualTo("letterboxdId", userLid).limit(1).get();

    try {
      return query.get().getDocuments().getFirst();
    } catch (ExecutionException | InterruptedException | NoSuchElementException e) {
      return null;
    }
  }

  /**
   * Gets the total number of user records in the database.
   *
   * @return The total number of user records
   */
  public long getUserCount() {
    String collectionId = appConfig.getFirestoreUserCollectionId();
    ApiFuture<AggregateQuerySnapshot> query = this.db.collection(collectionId).count().get();
    try {
      return query.get().getCount();
    } catch (InterruptedException | ExecutionException e) {
      return 0;
    }
  }

  /**
   * Gets all documents for all active users (those subscribed to at least one channel). This can be
   * an expensive operation.
   *
   * @return A List of documents for all active users
   */
  public List<QueryDocumentSnapshot> getActiveUsers() {
    String collectionId = appConfig.getFirestoreUserCollectionId();
    ApiFuture<QuerySnapshot> query =
        this.db
            .collection(collectionId)
            .whereNotEqualTo("channelList", Collections.emptyList())
            .get();
    try {
      return query.get().getDocuments();
    } catch (InterruptedException | ExecutionException e) {
      return Collections.emptyList();
    }
  }

  /**
   * Gets a page of documents for active users (those subscribed to at least one channel).
   *
   * @param pageSize Number of documents to fetch
   * @param pageIndex Zero-based page index (e.g., 0 for first page, 1 for second)
   * @return A List of documents for the current page
   */
  public List<QueryDocumentSnapshot> getActiveUsersPage(int pageSize, int pageIndex) {
    String collectionId = appConfig.getFirestoreUserCollectionId();
    ApiFuture<QuerySnapshot> query =
        this.db
            .collection(collectionId)
            .whereNotEqualTo("channelList", Collections.emptyList())
            .orderBy("created")
            .offset(pageSize * pageIndex)
            .limit(pageSize)
            .get();

    try {
      return query.get().getDocuments();
    } catch (InterruptedException | ExecutionException e) {
      return Collections.emptyList();
    }
  }

  /**
   * Gets all user documents for users followed in a specific channel.
   *
   * @param channelId The Discord channel id
   * @return A list of all user documents that have the specific channel
   */
  public List<QueryDocumentSnapshot> getUserDocumentListByChannelId(String channelId) {
    List<String> channelIdList = List.of(channelId);
    return getUserDocumentListByChannelIdList(channelIdList);
  }

  /**
   * Gets all user documents for users followed in a list of channel.
   *
   * @param channelIdList A list of Discord channel id
   * @return A list of all user documents that have the specific channel
   */
  public List<QueryDocumentSnapshot> getUserDocumentListByChannelIdList(
      List<String> channelIdList) {
    String collectionId = appConfig.getFirestoreUserCollectionId();
    List<Map<String, String>> channelMapList =
        channelIdList.stream()
            .filter(channelId -> channelId != null && !channelId.isBlank())
            .map(channelId -> Map.of("channelId", channelId))
            .toList();
    ApiFuture<QuerySnapshot> query =
        this.db.collection(collectionId).whereArrayContainsAny("channelList", channelMapList).get();
    try {
      return query.get().getDocuments();
    } catch (InterruptedException | ExecutionException e) {
      return new ArrayList<>();
    }
  }
}
