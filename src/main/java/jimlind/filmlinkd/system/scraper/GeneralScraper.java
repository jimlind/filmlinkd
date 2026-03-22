package jimlind.filmlinkd.system.scraper;

import javax.inject.Inject;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.model.Message.PublishSource;
import jimlind.filmlinkd.scraperv1.ScraperCoordinatorFactory;
import jimlind.filmlinkd.scraperv1.cache.GeneralUserCache;

/** Scraper implementation for general (non-VIP) users. */
public class GeneralScraper extends Scraper {

  /**
   * Constructs a GeneralScraper.
   *
   * @param appConfig Configuration provider for concurrency limits
   * @param factory Factory for creating scraper tasks
   * @param userCache Cache for general users
   */
  @Inject
  GeneralScraper(
      AppConfig appConfig, ScraperCoordinatorFactory factory, GeneralUserCache userCache) {
    super(factory, userCache, PublishSource.Normal, appConfig.getScraperGeneralPeriodMillis());
    this.concurrentClientLimit = appConfig.getScraperVipConcurrentLimit();
  }
}
