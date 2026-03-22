package jimlind.filmlinkd.scraperv1.scheduler;

import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.scraperv1.cache.GeneralUserCache;
import jimlind.filmlinkd.scraperv1.cache.VipUserCache;
import jimlind.filmlinkd.scraperv1.cache.clearer.GeneralUserCacheClearer;
import jimlind.filmlinkd.scraperv1.cache.clearer.VipUserCacheClearer;
import jimlind.filmlinkd.scraperv1.runner.GeneralScraper;
import jimlind.filmlinkd.scraperv1.runner.VipScraper;

/** A factory for creating instances of the {@link ScraperScheduler} model. */
@Singleton
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

  /**
   * Creates a scraper scheduler. Using the factory pattern because the general and vip versions of
   * the scraper are very similar.
   *
   * @param isVip Create a VIP scraper scheduler if set to true
   * @return A scraper scheduler
   */
  public ScraperScheduler create(boolean isVip) {
    if (isVip) {
      return new ScraperScheduler(
          vipScraper,
          vipUserCache,
          vipUserCacheClearer,
          this.appConfig.getScraperVipPeriodMillis(),
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
