package jimlind.filmlinkd.scraper.runner;

import com.google.inject.Inject;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.factory.ScraperCoordinatorFactory;
import jimlind.filmlinkd.model.Message.PublishSource;
import jimlind.filmlinkd.scraper.cache.VipUserCache;

/**
 * Scrapes the next page from the VipUserCache publishes a message in PubSub with the source set to
 * "vip" to notify the other systems.
 */
public class VipScraper extends BaseScraper {
  /**
   * Constructor for this class.
   *
   * @param appConfig Application configuration
   * @param userCache Where we store in memory versions records of latest diary entry
   * @param scraperCoordinatorFactory Handles the creation of ScraperCoordinator tasks
   */
  @Inject
  public VipScraper(
      AppConfig appConfig,
      VipUserCache userCache,
      ScraperCoordinatorFactory scraperCoordinatorFactory) {
    super(appConfig, userCache, scraperCoordinatorFactory);
    this.concurrentClientLimit = appConfig.getScraperVipConcurrentLimit();
    this.source = PublishSource.VIP;
    this.scrapeEntryWithRss = true;
  }
}
