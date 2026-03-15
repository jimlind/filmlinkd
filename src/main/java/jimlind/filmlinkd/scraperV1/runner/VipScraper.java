package jimlind.filmlinkd.scraperv1.runner;

import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.model.Message.PublishSource;
import jimlind.filmlinkd.scraperv1.ScraperCoordinatorFactory;
import jimlind.filmlinkd.scraperv1.cache.VipUserCache;

/**
 * Scrapes the next page from the VipUserCache publishes a message in PubSub with the source set to
 * "vip" to notify the other systems.
 */
@Singleton
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
  }
}
