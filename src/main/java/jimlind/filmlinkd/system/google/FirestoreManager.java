package jimlind.filmlinkd.system.google;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class FirestoreManager {

  private final AppConfig appConfig;
  private final Firestore db;
  private final ImageUtils imageUtils;
  private final UserFactory userFactory;

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

    this.db = firestoreOptions.getService();
  }

  public void createUserDocument(LbMember member) {
    String collectionId = appConfig.getFirestoreCollectionId();
    User user = userFactory.createFromMember(member);

    try {
      this.db.collection(collectionId).document(user.id).set(user.toMap()).get();
    } catch (Exception e) {
      log.atError().setMessage("Unable to set user document").log();
    }
  }

  public @Nullable QueryDocumentSnapshot getUserDocument(String userLid) {
    String collectionId = appConfig.getFirestoreCollectionId();
    ApiFuture<QuerySnapshot> query =
        this.db.collection(collectionId).whereEqualTo("letterboxdId", userLid).limit(1).get();

    try {
      return query.get().getDocuments().get(0);
    } catch (Exception e) {
      return null;
    }
  }

  public long getUserCount() {
    String collectionId = appConfig.getFirestoreCollectionId();
    ApiFuture<AggregateQuerySnapshot> query = this.db.collection(collectionId).count().get();
    try {
      return query.get().getCount();
    } catch (Exception e) {
      return 0;
    }
  }

  public List<QueryDocumentSnapshot> getActiveUsers() {
    String collectionId = appConfig.getFirestoreCollectionId();
    ApiFuture<QuerySnapshot> query =
        this.db
            .collection(collectionId)
            .whereNotEqualTo("channelList", Collections.emptyList())
            .get();
    try {
      return query.get().getDocuments();
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  public List<QueryDocumentSnapshot> getUserDocumentListByChannelId(String channelId) {
    String collectionId = appConfig.getFirestoreCollectionId();
    Map<String, String> channelMap = Map.ofEntries(Map.entry("channelId", channelId));
    ApiFuture<QuerySnapshot> query =
        this.db.collection(collectionId).whereArrayContains("channelList", channelMap).get();
    try {
      return query.get().getDocuments();
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  public boolean addUserSubscription(String userLid, String channelId) {
    // Create user but also save snapshot to update with
    QueryDocumentSnapshot snapshot = this.getUserDocument(userLid);
    User user = this.userFactory.createFromSnapshot(snapshot);
    if (snapshot == null || user == null) {
      return false;
    }

    ArrayList<User.Channel> channelList = user.channelList;

    // If the channel is already subscribed then exit early positively
    for (User.Channel channel : channelList) {
      if (channel.channelId.equals(channelId)) {
        return true;
      }
    }

    User.Channel newChannel = new User.Channel();
    newChannel.channelId = channelId;
    user.channelList.add(newChannel);

    // Perform the update
    try {
      snapshot.getReference().update(user.toMap());
    } catch (Exception e) {
      log.atError()
          .setMessage("Unable to Add to Channel List: Update Failed")
          .addKeyValue("user", user)
          .addKeyValue("channelId", channelId)
          .addKeyValue("exception", e)
          .log();
      return false;
    }

    return true;
  }

  public boolean removeUserSubscription(String userLid, String channelId) {
    // Create user but also save snapshot to update with
    QueryDocumentSnapshot snapshot = this.getUserDocument(userLid);
    User user = this.userFactory.createFromSnapshot(snapshot);
    if (snapshot == null || user == null) {
      return false;
    }

    user.channelList =
        user.channelList.stream()
            .filter(channel -> !channel.channelId.equals(channelId))
            .collect(Collectors.toCollection(ArrayList::new));

    try {
      snapshot.getReference().update(user.toMap());
    } catch (Exception e) {
      log.atError()
          .setMessage("Unable to Remove from Channel List: Update Failed")
          .addKeyValue("user", user)
          .addKeyValue("channelId", channelId)
          .addKeyValue("exception", e)
          .log();
      return false;
    }

    return true;
  }

  public boolean updateUserDisplayData(LbMember member) {
    // Create user but also save snapshot to update with
    QueryDocumentSnapshot snapshot = this.getUserDocument(member.id);
    User user = this.userFactory.createFromSnapshot(snapshot);
    if (snapshot == null || user == null) {
      return false;
    }

    user.displayName = member.displayName;
    user.image = imageUtils.getTallest(member.avatar);
    user.userName = member.username;

    // Perform the update
    try {
      snapshot.getReference().update(user.toMap());
    } catch (Exception e) {
      log.atError()
          .setMessage("Unable to Update Display Data: Update Failed")
          .addKeyValue("user", user)
          .log();
      return false;
    }

    return true;
  }

  public boolean updateUserPrevious(
      String userLid, String diaryLid, long diaryPublishedDate, String diaryUri) {

    // Create user but also save snapshot to update with
    QueryDocumentSnapshot snapshot = this.getUserDocument(userLid);
    User user = this.userFactory.createFromSnapshot(snapshot);
    if (snapshot == null || user == null) {
      return false;
    }

    // Default to an empty list, but set it if not null
    ArrayList<String> previousList = new ArrayList<>();
    if (user.previous.list != null) {
      previousList = user.previous.list;
    }

    // Nothing to update. Return `true` as if the action succeeded.
    if (previousList.contains(diaryLid)) {
      return true;
    }

    // The previous list should be considered the primary source of truth
    user.previous.list = LidComparer.buildMostRecentList(previousList, diaryLid, 10);

    // The scraping process uses the previous lid as it's source of truth to determine which
    // entries are new. Maybe that should be changed eventually.
    user.previous.lid = user.getMostRecentPrevious();

    // This data is only used if I'm trying to debug publishing issues
    user.updated = Instant.now().toEpochMilli();
    user.previous.published = diaryPublishedDate;
    user.previous.uri = diaryUri;

    // Perform the update
    try {
      snapshot.getReference().update(user.toMap());
    } catch (Exception e) {
      log.atError()
          .setMessage("Unable to Update Previous: Update Failed")
          .addKeyValue("user", user)
          .addKeyValue("diaryLid", diaryLid)
          .log();
      return false;
    }

    return true;
  }
}
