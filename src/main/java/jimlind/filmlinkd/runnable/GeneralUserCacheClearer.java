package jimlind.filmlinkd.runnable;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.GeneralUserCache;
import lombok.extern.slf4j.Slf4j;

/**
 * Runnable to clear the General User Cache. It's good practice to clear this cache every so often
 * to ensure that the cache user data hasn't drifted from the production database.
 */
@Slf4j
public class GeneralUserCacheClearer implements Runnable {

  private final GeneralUserCache generalUserCache;

  /**
   * Constructor for this class.
   *
   * @param generalUserCache Where we store in memory versions records of latest diary entry
   */
  @Inject
  public GeneralUserCacheClearer(GeneralUserCache generalUserCache) {
    this.generalUserCache = generalUserCache;
  }

  @Override
  public void run() {
    generalUserCache.clear();
  }
}
