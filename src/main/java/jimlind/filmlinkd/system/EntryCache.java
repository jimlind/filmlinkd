package jimlind.filmlinkd.system;

import com.github.benmanes.caffeine.cache.*;

public class EntryCache {
  Cache<String, Boolean> cache;

  public EntryCache() {
    this.cache = Caffeine.newBuilder().maximumSize(10_000).build();
  }

  public void set(String key) {
    this.cache.put(key, true);
  }

  public Boolean get(String key) {
    Boolean result = this.cache.getIfPresent(key);
    return result != null;
  }
}
