package jimlind.filmlinkd.google.db;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/** Handles all create, update, and delete operations for user data in Firestore. */
@Singleton
@Slf4j
public class UserWriter {
  private static final String USER_KEY = "user";

  private final AppConfig appConfig;
  private final Firestore db;
  private final ImageUtils imageUtils;
  private final UserFactory userFactory;
  private final UserReader userReader;

  /**
   * The constructor for this class.
   *
   * @param appConfig Contains application and environment variables
   * @param firestore The Firestore database client
   * @param imageUtils Assists in finding optimal Letterboxd images
   * @param userFactory Creates our universal user model from Letterboxd API data or Firestorm data
   * @param userReader Handles all read-only queries for user data from Firestore
   */
  @Inject
  public UserWriter(
      AppConfig appConfig,
      Firestore firestore,
      ImageUtils imageUtils,
      UserFactory userFactory,
      UserReader userReader) {
    this.appConfig = appConfig;
    this.db = firestore;
    this.imageUtils = imageUtils;
    this.userFactory = userFactory;
    this.userReader = userReader;
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
   * @return a fully populated user model
   */
  public User createUserDocument(LbMember member) {
    String collectionId = appConfig.getFirestoreUserCollectionId();
    User user = userFactory.createFromMember(member);

    try {
      this.db.collection(collectionId).document(user.id).set(user.toMap()).get();
    } catch (IllegalArgumentException | InterruptedException | ExecutionException e) {
      log.atError().setMessage("Unable to set user document").log();
    }

    return user;
  }

  /**
   * Adds a channel subscription to a user's document.
   *
   * @param userLid User's Letterboxd id
   * @param channelId Discord Channel id
   * @return true if the action succeeded or the channel was already present; false on failure
   */
  public boolean addUserSubscription(String userLid, String channelId) {
    QueryDocumentSnapshot snapshot = userReader.getUserDocument(userLid);
    User user = userFactory.createFromSnapshot(snapshot);
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

    try {
      DocumentReference reference = getReference(snapshot);
      reference.update(user.toMap());
    } catch (IllegalArgumentException e) {
      log.atError()
          .setMessage("Unable to Add to Channel List: Update Failed")
          .addKeyValue(USER_KEY, user)
          .addKeyValue("channelId", channelId)
          .setCause(e)
          .log();
      return false;
    }

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
    // Create user but also save snapshot to update with
    QueryDocumentSnapshot snapshot = userReader.getUserDocument(userLid);
    User user = userFactory.createFromSnapshot(snapshot);
    if (snapshot == null || user == null) {
      return false;
    }

    user.setChannelList(
        user.getChannelList().stream()
            .filter(channel -> !channel.channelId.equals(channelId))
            .collect(Collectors.toCollection(ArrayList::new)));

    try {
      getReference(snapshot).update(user.toMap());
    } catch (IllegalArgumentException e) {
      log.atError()
          .setMessage("Unable to Remove from Channel List: Update Failed")
          .addKeyValue(USER_KEY, user)
          .addKeyValue("channelId", channelId)
          .setCause(e)
          .log();
      return false;
    }

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
    // Create user but also save snapshot to update with
    QueryDocumentSnapshot snapshot = userReader.getUserDocument(userLid);
    User user = userFactory.createFromSnapshot(snapshot);
    if (snapshot == null || user == null) {
      return false;
    }

    user.setChannelList(
        user.getChannelList().stream()
            .filter(channel -> !channel.channelId.equals(channelId))
            .collect(Collectors.toCollection(ArrayList::new)));

    User.Channel channelModel = new User.Channel();
    channelModel.setChannelId(channelId);
    Stream<User.Channel> channelModelStream = Stream.of(channelModel);
    Stream<User.Channel> archivedChannelStream = user.getArchivedChannelList().stream();
    user.setArchivedChannelList(Stream.concat(archivedChannelStream, channelModelStream).toList());

    try {
      getReference(snapshot).update(user.toMap());
    } catch (IllegalArgumentException e) {
      log.atError()
          .setMessage("Unable to archive user's channel: Update failed")
          .addKeyValue(USER_KEY, user)
          .setCause(e)
          .log();
      return false;
    }
    return true;
  }

  /**
   * Removes all channel subscriptions from a user's document.
   *
   * @param userLid User's Letterboxd id
   * @return true if the action succeeded; false on failure
   */
  public boolean archiveAllUserSubscriptions(String userLid) {
    // Create user but also save snapshot to update with
    QueryDocumentSnapshot snapshot = userReader.getUserDocument(userLid);
    User user = userFactory.createFromSnapshot(snapshot);
    if (snapshot == null || user == null) {
      return false;
    }

    Stream<User.Channel> channelStream = user.getChannelList().stream();
    Stream<User.Channel> archivedChannelStream = user.getArchivedChannelList().stream();
    user.setArchivedChannelList(Stream.concat(channelStream, archivedChannelStream).toList());
    user.setChannelList(List.of());

    try {
      getReference(snapshot).update(user.toMap());
    } catch (IllegalArgumentException e) {
      log.atError()
          .setMessage("Unable to archive user's channel list: Update failed")
          .addKeyValue(USER_KEY, user)
          .setCause(e)
          .log();
      return false;
    }

    return true;
  }

  /**
   * Reverts an archived subscription from a user's document.
   *
   * @param userLid User's Letterboxd id
   * @param channelId Discord Channel id
   * @return true if the action succeeded; false on failure
   */
  public boolean revertUserArchivedSubscription(String userLid, String channelId) {
    // Create user but also save snapshot to update with
    QueryDocumentSnapshot snapshot = userReader.getUserDocument(userLid);
    User user = userFactory.createFromSnapshot(snapshot);
    if (snapshot == null || user == null) {
      return false;
    }

    // Remove the archived channel from the list of archived channels
    user.setArchivedChannelList(
        user.getArchivedChannelList().stream()
            .filter(channel -> !channel.channelId.equals(channelId))
            .toList());

    // Add the archived channel back to the list of subscribed channels if it isn't there
    if (!user.getChannelIdList().contains(channelId)) {
      User.Channel newChannel = new User.Channel();
      newChannel.setChannelId(channelId);
      user.getChannelList().add(newChannel);
    }

    try {
      getReference(snapshot).update(user.toMap());
    } catch (IllegalArgumentException e) {
      log.error("Unable to Remove from Archived Channel List");
      return false;
    }

    return true;
  }

  /**
   * Updates the username, display name, and image from recent Letterboxd data.
   *
   * @param member Letterboxd Member object from API.
   * @return true if the action succeeded; false on failure.
   */
  public boolean updateUserDisplayData(LbMember member) {
    QueryDocumentSnapshot snapshot = userReader.getUserDocument(member.id);
    User user = userFactory.createFromSnapshot(snapshot);
    if (snapshot == null || user == null) {
      return false;
    }

    user.setDisplayName(member.getDisplayName());
    user.setImage(imageUtils.getTallest(member.getAvatar()));
    user.setUserName(member.getUsername());

    try {
      DocumentReference reference = getReference(snapshot);
      reference.update(user.toMap());
    } catch (IllegalArgumentException e) {
      log.atError()
          .setMessage("Unable to Update Display Data: Update Failed")
          .addKeyValue(USER_KEY, user)
          .log();
      return false;
    }

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
    // Create user but also save snapshot to update with
    QueryDocumentSnapshot snapshot = userReader.getUserDocument(userLid);
    User user = userFactory.createFromSnapshot(snapshot);
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

    try {
      DocumentReference reference = getReference(snapshot);
      reference.update(user.toMap());
    } catch (IllegalArgumentException e) {
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
