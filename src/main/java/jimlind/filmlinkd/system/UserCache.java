package jimlind.filmlinkd.system;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.google.FirestoreManager;

@Singleton
public class UserCache {
  private final FirestoreManager firestoreManager;
  private final UserFactory userFactory;
  // This is a string/string key value store for user data
  // The key is the user's letterboxd id
  // The value is the last known letterboxd entry id for the user
  private final HashMap<String, String> userCache = new HashMap<String, String>();

  @Inject
  public UserCache(FirestoreManager firestoreManager, UserFactory userFactory) {
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

  public int getRandomIndex() {
    if (userCache.isEmpty()) {
      populateFromFirestore();
    }

    return new Random().nextInt(0, userCache.size());
  }

  private void populateFromFirestore() {
    List<QueryDocumentSnapshot> activeUsersList = firestoreManager.getActiveUsers();
    for (QueryDocumentSnapshot snapshot : activeUsersList) {
      User user = this.userFactory.createFromSnapshot(snapshot);
      userCache.put(user.letterboxdId, user.getMostRecentPrevious());
    }
  }
}
