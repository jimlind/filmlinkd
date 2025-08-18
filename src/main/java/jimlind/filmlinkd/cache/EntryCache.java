package jimlind.filmlinkd.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.inject.Singleton;

/**
 * A wrapper for a Caffeine cache limited to 10,000 entries. The key is the most important piece of
 * this we need so storing true.
 */
@Singleton
public class EntryCache {
  Cache<String, Boolean> cache;

  /** Constructor for this class. */
  public EntryCache() {
    cache = Caffeine.newBuilder().maximumSize(10_000).build();
  }

  /**
   * Sets the value in the cache so we know it was used.
   *
   * @param key Letterboxd ID that has been used
   */
  public void set(String key) {
    cache.put(key, true);
  }

  /**
   * Indicates that the specific ID for a Letterboxd entry exists in the cache.
   *
   * @param key The Letterboxd ID that we are looking for
   * @return Boolean that indicates if the key was found in the cache.
   */
  public Boolean get(String key) {
    return cache.getIfPresent(key) != null;
  }
}
