package jimlind.filmlinkd.runnable;

import com.google.inject.Inject;
import jimlind.filmlinkd.cache.VipUserCache;

/** Runnable to clear the General User Cache. */
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
