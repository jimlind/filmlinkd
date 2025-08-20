package jimlind.filmlinkd.system.scraper;

import com.google.inject.Inject;
import jimlind.filmlinkd.cache.VipUserCache;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.runnable.VipScraper;
import jimlind.filmlinkd.runnable.VipUserCacheClearer;

/** Schedules the scraper that runs over VIP users. */
public class VipScraperScheduler extends BaseScraperScheduler {
  /**
   * Constructor for this class.
   *
   * @param appConfig Holds all the configs that determine how the system run
   * @param vipScraper The scraper task that we are scheduling
   * @param vipUserCache Where we store in memory versions records of latest diary entry
   * @param vipUserCacheClearer The user clear cache
   */
  @Inject
  public VipScraperScheduler(
      AppConfig appConfig,
      VipScraper vipScraper,
      VipUserCache vipUserCache,
      VipUserCacheClearer vipUserCacheClearer) {
    this.appConfig = appConfig;
    this.scraper = vipScraper;
    this.userCache = vipUserCache;
    this.userCacheClearer = vipUserCacheClearer;

    this.scraperPeriod = appConfig.getScraperVipPeriod();
    this.userCachePeriod = appConfig.getScraperVipUserCachePeriod();
  }
}
