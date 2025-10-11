package jimlind.filmlinkd.runnable;

import jimlind.filmlinkd.scraper.cache.BaseUserCache;

/**
 * Runnable to clear the User Cache. It's good practice to clear this cache every so often to ensure
 * that the cache user data hasn't drifted too far from the production database.
 */
public class BaseUserCacheClearer implements Runnable {
  protected BaseUserCache userCache;

  @Override
  public void run() {
    userCache.clear();
  }
}
