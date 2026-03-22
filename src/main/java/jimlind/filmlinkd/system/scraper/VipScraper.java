package jimlind.filmlinkd.system.scraper;

import javax.inject.Inject;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.model.Message.PublishSource;
import jimlind.filmlinkd.system.cache.VipUserCache;

/** Scraper implementation specifically for VIP users. */
public class VipScraper extends Scraper {

  /**
   * Constructs a VipScraper.
   *
   * @param appConfig Configuration provider for concurrency limits
   * @param factory Factory for creating scraper tasks
   * @param userCache Cache for VIP users
   */
  @Inject
  VipScraper(AppConfig appConfig, ScraperCoordinatorFactory factory, VipUserCache userCache) {
    super(factory, userCache, PublishSource.VIP, appConfig.getScraperVipPeriodMillis());
    this.concurrentClientLimit = appConfig.getScraperVipConcurrentLimit();
  }
}
