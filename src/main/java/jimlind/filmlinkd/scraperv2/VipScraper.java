package jimlind.filmlinkd.scraperv2;

import javax.inject.Inject;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.model.Message.PublishSource;
import jimlind.filmlinkd.scraperv1.ScraperCoordinatorFactory;
import jimlind.filmlinkd.scraperv1.cache.VipUserCache;

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
    super(factory, userCache, PublishSource.VIP);
    this.concurrentClientLimit = appConfig.getScraperVipConcurrentLimit();
  }
}
