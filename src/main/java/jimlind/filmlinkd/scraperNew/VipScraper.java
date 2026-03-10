package jimlind.filmlinkd.scraperNew;

import javax.inject.Inject;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.model.Message.PublishSource;
import jimlind.filmlinkd.scraper.ScraperCoordinatorFactory;
import jimlind.filmlinkd.scraper.cache.VipUserCache;

public class VipScraper extends Scraper {

  @Inject
  VipScraper(AppConfig appConfig, ScraperCoordinatorFactory factory, VipUserCache userCache) {
    super(factory, userCache, PublishSource.VIP);
    this.concurrentClientLimit = appConfig.getScraperVipConcurrentLimit();
  }
}
