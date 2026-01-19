package jimlind.filmlinkd.scraper.cache.clearer;

import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.scraper.cache.GeneralUserCache;

/** Runnable to clear the General User Cache. */
@Singleton
public class GeneralUserCacheClearer extends BaseUserCacheClearer {

  /**
   * Constructor for this class.
   *
   * @param generalUserCache Where we store in memory versions records of latest diary entry
   */
  @Inject
  public GeneralUserCacheClearer(GeneralUserCache generalUserCache) {
    userCache = generalUserCache;
  }
}
