package jimlind.filmlinkd.system.google;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.AggregateQuerySnapshot;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.inject.Inject;
import java.nio.channels.OverlappingFileLockException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Handles all Firestore communication. Should probably be split into multiple classes. */
@Slf4j
public class FirestoreManager {
  private static final String USER_KEY = "user";

  private final AppConfig appConfig;
  private final Firestore db;
  private final ImageUtils imageUtils;
  private final UserFactory userFactory;

  /**
   * The constructor for this class.
   *
   * @param appConfig Contains application and environment variables
   * @param imageUtils Assists in finding optimal Letterboxd images
   * @param userFactory Creates our universal user model from Letterboxd API data or Firestorm data
   */
  @Inject
  FirestoreManager(AppConfig appConfig, ImageUtils imageUtils, UserFactory userFactory) {
    this.appConfig = appConfig;
    this.imageUtils = imageUtils;
    this.userFactory = userFactory;

    FirestoreOptions.Builder builder =
        FirestoreOptions.getDefaultInstance().toBuilder()
            .setProjectId(appConfig.getGoogleProjectId())
            .setDatabaseId(this.appConfig.getFirestoreDatabaseId());
    FirestoreOptions firestoreOptions = builder.build();

    this.db = extractService(firestoreOptions);
  }

  private static Firestore extractService(FirestoreOptions firestoreOptions) {
    return firestoreOptions.getService();
  }

  @NotNull
  private static DocumentReference getReference(QueryDocumentSnapshot snapshot) {
    return snapshot.getReference();
  }

  private static User.Previous getPrevious(User user) {
    return user.getPrevious();
  }

  /**
   * Creates a new user document in Firestore from Letterboxd API data.
   *
   * @param member Letterboxd Member object from API
   */
  public void createUserDocument(LbMember member) {
    String collectionId = appConfig.getFirestoreCollectionId();
    User user = userFactory.createFromMember(member);

    // TODO: This used to have an unchecked try/catch wrapper
    try {
      this.db.collection(collectionId).document(user.id).set(user.toMap()).get();
    } catch (InterruptedException | ExecutionException e) {
      log.atError().setMessage("Unable to set user document").log();
    }
  }

  /**
   * Attempts to find a user in the database.
   *
   * @param userLid The Letterboxd id for the user
   * @return Returns null if no data available or returns the document snapshot from Firestorm if
   *     available
   */
  public @Nullable QueryDocumentSnapshot getUserDocument(String userLid) {
    String collectionId = appConfig.getFirestoreCollectionId();
    ApiFuture<QuerySnapshot> query =
        this.db.collection(collectionId).whereEqualTo("letterboxdId", userLid).limit(1).get();

    // TODO: This used to have an unchecked try/catch wrapper
    try {
      return query.get().getDocuments().getFirst();
    } catch (InterruptedException | ExecutionException e) {
      return null;
    }
  }

  /**
   * Gives access to the total number of user records in the database.
   *
   * @return The total number of user records in the database.
   */
  public long getUserCount() {
    String collectionId = appConfig.getFirestoreCollectionId();
    ApiFuture<AggregateQuerySnapshot> query = this.db.collection(collectionId).count().get();
    // TODO: This used to have an unchecked try/catch wrapper
    try {
      return query.get().getCount();
    } catch (InterruptedException | ExecutionException e) {
      return 0;
    }
  }

  /**
   * Gives access to all documents for all active users. This should not be called regularly because
   * it is expensive.
   *
   * @return A List of documents for all active users.
   */
  public List<QueryDocumentSnapshot> getActiveUsers() {
    String collectionId = appConfig.getFirestoreCollectionId();
    ApiFuture<QuerySnapshot> query =
        this.db
            .collection(collectionId)
            .whereNotEqualTo("channelList", Collections.emptyList())
            .get();
    // TODO: This used to have an unchecked try/catch wrapper
    try {
      return query.get().getDocuments();
    } catch (InterruptedException | ExecutionException e) {
      return Collections.emptyList();
    }
  }

  /**
   * Get all users where the specific channel is available in the list of channels in the user data
   * record. This should associate with the users that are followed in a channel.
   *
   * @param channelId The discord channel id
   * @return A list of all user documents that have the specific channel
   */
  public List<QueryDocumentSnapshot> getUserDocumentListByChannelId(String channelId) {
    String collectionId = appConfig.getFirestoreCollectionId();
    Map<String, String> channelMap = Map.ofEntries(Map.entry("channelId", channelId));
    ApiFuture<QuerySnapshot> query =
        this.db.collection(collectionId).whereArrayContains("channelList", channelMap).get();
    // TODO: This used to have an unchecked try/catch wrapper
    try {
      return query.get().getDocuments();
    } catch (InterruptedException | ExecutionException e) {
      return new ArrayList<>();
    }
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
    // Create user but also save snapshot to update with
    QueryDocumentSnapshot snapshot = this.getUserDocument(userLid);
    User user = this.userFactory.createFromSnapshot(snapshot);
    if (snapshot == null || user == null) {
      return false;
    }

    // If the channel is already subscribed then exit early positively
    for (String userChannelId : user.getChannelIdList()) {
      if (userChannelId.equals(channelId)) {
        return true;
      }
    }

    User.Channel newChannel = new User.Channel();
    newChannel.setChannelId(channelId);
    user.getChannelList().add(newChannel);

    // Perform the update
    // TODO: Check what sort of exception I can actually get out of here.
    try {
      DocumentReference reference = getReference(snapshot);
      reference.update(user.toMap());
    } catch (OverlappingFileLockException e) {
      log.atError()
          .setMessage("Unable to Add to Channel List: Update Failed")
          .addKeyValue(USER_KEY, user)
          .addKeyValue("channelId", channelId)
          .addKeyValue("exception", e)
          .log();
      return false;
    }

    return true;
  }

  /**
   * Attempts to remove a channel to the user's data.
   *
   * @param userLid User's Letterboxd id
   * @param channelId Discord Channel id
   * @return true if the action succeeded and false if any number of failures
   */
  public boolean removeUserSubscription(String userLid, String channelId) {
    // Create user but also save snapshot to update with
    QueryDocumentSnapshot snapshot = this.getUserDocument(userLid);
    User user = this.userFactory.createFromSnapshot(snapshot);
    if (snapshot == null || user == null) {
      return false;
    }

    user.setChannelList(
        user.getChannelList().stream()
            .filter(channel -> !channel.channelId.equals(channelId))
            .collect(Collectors.toCollection(ArrayList::new)));

    // TODO: Check what sort of exception I can actually get out of here.
    try {
      getReference(snapshot).update(user.toMap());
    } catch (OverlappingFileLockException e) {
      log.atError()
          .setMessage("Unable to Remove from Channel List: Update Failed")
          .addKeyValue(USER_KEY, user)
          .addKeyValue("channelId", channelId)
          .addKeyValue("exception", e)
          .log();
      return false;
    }

    return true;
  }

  /**
   * Updates the username, display name, and image based on most recent data from Letterboxd.
   *
   * @param member Letterboxd Member object from API
   * @return true if the action succeeded and false if any number of failures
   */
  public boolean updateUserDisplayData(LbMember member) {
    // Create user but also save snapshot to update with
    QueryDocumentSnapshot snapshot = this.getUserDocument(member.id);
    User user = this.userFactory.createFromSnapshot(snapshot);
    if (snapshot == null || user == null) {
      return false;
    }

    user.setDisplayName(member.getDisplayName());
    user.setImage(imageUtils.getTallest(member.getAvatar()));
    user.setUserName(member.getUsername());

    // Perform the update
    // TODO: Check what sort of exception I can actually get out of here.
    try {
      DocumentReference reference = getReference(snapshot);
      reference.update(user.toMap());
    } catch (OverlappingFileLockException e) {
      log.atError()
          .setMessage("Unable to Update Display Data: Update Failed")
          .addKeyValue(USER_KEY, user)
          .log();
      return false;
    }

    return true;
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

    // Create user but also save snapshot to update with
    QueryDocumentSnapshot snapshot = this.getUserDocument(userLid);
    User user = this.userFactory.createFromSnapshot(snapshot);
    if (snapshot == null || user == null) {
      return false;
    }

    // Default to an empty list, but set it if not null
    List<String> previousList = new ArrayList<>();
    User.Previous previous = getPrevious(user);
    if (previous.getList() != null) {
      previousList = getPrevious(user).getList();
    }

    // Nothing to update. Return `true` as if the action succeeded.
    if (previousList.contains(diaryLid)) {
      return true;
    }

    User.Previous updatedPrevious = getPrevious(user);
    updatedPrevious.setPublished(diaryPublishedDate); // Only used for debugging
    updatedPrevious.setUri(diaryUri); // Only used for debugging

    // The previous list should be considered the primary source of truth
    updatedPrevious.setList(LidComparer.buildMostRecentList(previousList, diaryLid, 10));

    // The scraping process uses the previous lid as it's source of truth to determine which
    // entries are new. Maybe that should be changed eventually.
    updatedPrevious.setLid(user.getMostRecentPrevious());

    user.setPrevious(updatedPrevious);
    user.setUpdated(Instant.now().toEpochMilli()); // Only used for debugging

    // Perform the update
    // TODO: Check what sort of exception I can actually get out of here.
    try {
      DocumentReference reference = getReference(snapshot);
      reference.update(user.toMap());
    } catch (OverlappingFileLockException e) {
      log.atError()
          .setMessage("Unable to Update Previous: Update Failed")
          .addKeyValue(USER_KEY, user)
          .addKeyValue("diaryLid", diaryLid)
          .log();
      return false;
    }

    return true;
  }
}
