package jimlind.filmlinkd.system.cache.clearer;

import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.system.cache.VipUserCache;

/** Runnable to clear the General User Cache. */
@Singleton
public class VipUserCacheClearer extends BaseUserCacheClearer {

  /**
   * Constructor for this class.
   *
   * @param vipUserCache Where we store in memory versions records of latest diary entry
   */
  @Inject
  public VipUserCacheClearer(VipUserCache vipUserCache) {
    userCache = vipUserCache;
  }
}
