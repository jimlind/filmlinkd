package jimlind.filmlinkd.scraper.runner;

import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.model.Message.PublishSource;
import jimlind.filmlinkd.scraper.ScraperCoordinatorFactory;
import jimlind.filmlinkd.scraper.cache.GeneralUserCache;

/**
 * Scrapes the next page from the GeneralUserCache publishes a message in PubSub with the source set
 * to "normal" to notify the other systems.
 */
@Singleton
public class GeneralScraper extends BaseScraper {
  /**
   * Constructor for this class.
   *
   * @param appConfig Application configuration
   * @param userCache Where we store in memory versions records of latest diary entry
   * @param scraperCoordinatorFactory Handles the creation of ScraperCoordinator tasks
   */
  @Inject
  public GeneralScraper(
      AppConfig appConfig,
      GeneralUserCache userCache,
      ScraperCoordinatorFactory scraperCoordinatorFactory) {
    super(appConfig, userCache, scraperCoordinatorFactory);
    this.concurrentClientLimit = appConfig.getScraperGeneralConcurrentLimit();
    this.source = PublishSource.Normal;
  }
}
