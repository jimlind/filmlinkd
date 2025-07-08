package jimlind.filmlinkd.system;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.*;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.google.FirestoreManager;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;

@Singleton
public class GeneralUserCache {
  private final AppConfig appConfig;
  private final FirestoreManager firestoreManager;
  private final UserFactory userFactory;
  // This is a string/string key value store for user data
  // The key is the user's letterboxd id
  // The value is the last known letterboxd entry id for the user
  private final HashMap<String, String> userCache = new HashMap<String, String>();
  private int paginationIndex = 0;

  @Inject
  public GeneralUserCache(
      AppConfig appConfig, FirestoreManager firestoreManager, UserFactory userFactory) {
    this.appConfig = appConfig;
    this.firestoreManager = firestoreManager;
    this.userFactory = userFactory;
  }

  public Collection<String> getAllUserIds() {
    if (userCache.isEmpty()) {
      populateFromFirestore();
    }

    return userCache.keySet();
  }

  public String getEntryId(String letterboxedId) {
    if (userCache.isEmpty()) {
      populateFromFirestore();
    }

    return userCache.get(letterboxedId);
  }

  public void setIfNewer(String letterboxedId, String entryId) {
    if (LidComparer.compare(entryId, userCache.get(letterboxedId)) > 0) {
      userCache.put(letterboxedId, entryId);
    }
  }

  public void initializeRandomPage() {
    if (userCache.isEmpty()) {
      populateFromFirestore();
    }

    paginationIndex = new Random().nextInt(0, userCache.size());
  }

  public List<Map.Entry<String, String>> getNextPage() {
    if (userCache.isEmpty()) {
      populateFromFirestore();
    }

    paginationIndex += appConfig.getScraperPaginationLimit();
    List<Map.Entry<String, String>> sublist = getSublist(paginationIndex);
    if (sublist.isEmpty()) {
      paginationIndex = 0;
      return getSublist(paginationIndex);
    }

    return sublist;
  }

  public void clear() {
    userCache.clear();
  }

  private List<Map.Entry<String, String>> getSublist(int index) {
    int pageSize = appConfig.getScraperPaginationLimit();
    return userCache.entrySet().stream().skip(index).limit(pageSize).toList();
  }

  private void populateFromFirestore() {
    List<QueryDocumentSnapshot> activeUsersList = firestoreManager.getActiveUsers();
    for (QueryDocumentSnapshot snapshot : activeUsersList) {
      User user = this.userFactory.createFromSnapshot(snapshot);
      userCache.put(user.letterboxdId, user.getMostRecentPrevious());
    }
  }
}
