package jimlind.filmlinkd.runnable;

import com.google.inject.Inject;
import jimlind.filmlinkd.cache.VipUserCache;
import jimlind.filmlinkd.factory.ScraperCoordinatorFactory;
import jimlind.filmlinkd.model.Message.PublishSource;

/**
 * Scrapes the next page from the VipUserCache publishes a message in PubSub with the source set to
 * "vip" to notify the other systems.
 */
public class VipScraper extends BaseScraper {
  /**
   * Constructor for this class.
   *
   * @param userCache Where we store in memory versions records of latest diary entry
   * @param scraperCoordinatorFactory Handles the creation of ScraperCoordinator tasks
   */
  @Inject
  public VipScraper(VipUserCache userCache, ScraperCoordinatorFactory scraperCoordinatorFactory) {
    super(userCache, scraperCoordinatorFactory);
    this.source = PublishSource.VIP;
  }
}
