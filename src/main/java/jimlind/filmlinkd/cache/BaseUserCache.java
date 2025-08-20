package jimlind.filmlinkd.cache;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.google.firestore.UserReader;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;

/**
 * An in-memory data store that stores a list of user Letterboxd Ids to scrape and what we recognize
 * as the most recent diary entry. This reduces the need for constant database calls and gives us
 * the ability to paginate as needed.
 */
public abstract class BaseUserCache {
  protected final AppConfig appConfig;
  protected final UserFactory userFactory;
  protected final UserReader userReader;
  // This is a string/string key value store for user data
  // The key is the user's letterboxd id
  // The value is the last known letterboxd entry id for the user
  protected Map<String, String> userCache = new HashMap<>();
  protected int paginationIndex;

  /**
   * Constructor for this class.
   *
   * @param appConfig Holds all the configs that determine how the system run
   * @param userFactory Factory for creating {@link User} model
   * @param userReader Handles all read-only queries for user data from Firestore
   */
  public BaseUserCache(AppConfig appConfig, UserFactory userFactory, UserReader userReader) {
    this.appConfig = appConfig;
    this.userFactory = userFactory;
    this.userReader = userReader;
  }

  /**
   * Sets the current index at a random value available in the user cache. Method will populate the
   * cache from Firestore if it is empty.
   */
  public void initializeRandomPage() {
    if (userCache.isEmpty()) {
      populateFromFirestore();
    }
    if (userCache.isEmpty()) {
      return;
    }
    paginationIndex = new Random().nextInt(0, userCache.size());
  }

  /**
   * Get the complete list of user ids. Method will populate the cache from Firestore if it is
   * empty.
   *
   * @return A list of LetterboxdId user ids strings.
   */
  public Collection<String> getAllUserIds() {
    if (userCache.isEmpty()) {
      populateFromFirestore();
    }

    return userCache.keySet();
  }

  /**
   * Get the recorded diary entry for a user. This is the most recent that we know of. Method will
   * populate the cache from Firestore if it is empty.
   *
   * @param letterboxedId The User's Letterboxd ID that we want to find a diary entry for.
   * @return The string for the Letterboxd ID that we think is the most recent diary entry for the
   *     user.
   */
  public String getEntryId(String letterboxedId) {
    if (userCache.isEmpty()) {
      populateFromFirestore();
    }

    return userCache.get(letterboxedId);
  }

  /**
   * Gets the next page of user data. Always increments the pagination index. There is currently no
   * way to get the "current" page as there isn't a use for that. Method will populate the cache
   * from Firestore if it is empty.
   *
   * @return A sublist of User's LetterboxdId and Diary Entry's LetterboxdId
   */
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

  /**
   * Set the recorded diary entry for a user. This is the most recent that we know of. We will run
   * it through the LidComparer to ensure that we aren't setting something older.
   *
   * @param letterboxedId The User's LetterboxdId that we want to set a diary entry for.
   * @param entryId The Diary Entry's LetterboxdId that we want to set as the most recent.
   */
  public void setIfNewer(String letterboxedId, String entryId) {
    if (LidComparer.compare(entryId, userCache.get(letterboxedId)) > 0) {
      userCache.put(letterboxedId, entryId);
    }
  }

  /**
   * Clears out the user cache so that we have a forcing function for things to not drift too far
   * from database to local cache.
   */
  public void clear() {
    userCache.clear();
  }

  private List<Map.Entry<String, String>> getSublist(int index) {
    int pageSize = appConfig.getScraperPaginationLimit();
    return userCache.entrySet().stream().skip(index).limit(pageSize).toList();
  }

  /** Fills the userCache variable from documents from firestore retrieved via getUserList. */
  protected void populateFromFirestore() {
    List<QueryDocumentSnapshot> activeUsersList = getUserList();
    for (QueryDocumentSnapshot snapshot : activeUsersList) {
      User user = this.userFactory.createFromSnapshot(snapshot);
      if (user != null) {
        userCache.put(user.letterboxdId, user.getMostRecentPrevious());
      }
    }
  }

  /**
   * Get the User list from Firestore. This is the primary differentiator between General and Vip
   * user caches.
   *
   * @return List of documents from firestore.
   */
  protected abstract List<QueryDocumentSnapshot> getUserList();
}
