package jimlind.filmlinkd.scraper.scheduler;

import com.google.inject.Inject;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.scraper.cache.GeneralUserCache;
import jimlind.filmlinkd.scraper.cache.VipUserCache;
import jimlind.filmlinkd.scraper.cache.clearer.GeneralUserCacheClearer;
import jimlind.filmlinkd.scraper.cache.clearer.VipUserCacheClearer;
import jimlind.filmlinkd.scraper.runner.GeneralScraper;
import jimlind.filmlinkd.scraper.runner.VipScraper;

public class ScraperSchedulerFactory {
  private final AppConfig appConfig;
  private final GeneralScraper generalScraper;
  private final GeneralUserCache generalUserCache;
  private final GeneralUserCacheClearer generalUserCacheClearer;
  private final VipScraper vipScraper;
  private final VipUserCache vipUserCache;
  private final VipUserCacheClearer vipUserCacheClearer;

  /**
   * Constructor for this class.
   *
   * @param appConfig Holds all the configs that determine how the system run
   * @param generalScraper The scheduler task that we are scheduling
   * @param generalUserCache Where we store in memory versions records of latest diary entry
   * @param generalUserCacheClearer The user clear cache
   * @param vipScraper The scheduler task that we are scheduling
   * @param vipUserCache Where we store in memory versions records of latest diary entry
   * @param vipUserCacheClearer The user clear cache
   */
  @Inject
  public ScraperSchedulerFactory(
      AppConfig appConfig,
      GeneralScraper generalScraper,
      GeneralUserCache generalUserCache,
      GeneralUserCacheClearer generalUserCacheClearer,
      VipScraper vipScraper,
      VipUserCache vipUserCache,
      VipUserCacheClearer vipUserCacheClearer) {
    this.appConfig = appConfig;
    this.generalScraper = generalScraper;
    this.generalUserCache = generalUserCache;
    this.generalUserCacheClearer = generalUserCacheClearer;
    this.vipScraper = vipScraper;
    this.vipUserCache = vipUserCache;
    this.vipUserCacheClearer = vipUserCacheClearer;
  }

  public ScraperScheduler create(boolean isVip) {
    if (isVip) {
      return new ScraperScheduler(
          vipScraper,
          vipUserCache,
          vipUserCacheClearer,
          this.appConfig.getScraperVipPeriod(),
          this.appConfig.getScraperVipUserCachePeriod());
    }

    return new ScraperScheduler(
        generalScraper,
        generalUserCache,
        generalUserCacheClearer,
        this.appConfig.getScraperGeneralPeriod(),
        this.appConfig.getScraperGeneralUserCachePeriod());
  }
}
