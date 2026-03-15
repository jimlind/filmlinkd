package jimlind.filmlinkd.scraperV2;

import javax.inject.Inject;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.model.Message.PublishSource;
import jimlind.filmlinkd.scraperV1.ScraperCoordinatorFactory;
import jimlind.filmlinkd.scraperV1.cache.VipUserCache;

public class VipScraper extends Scraper {

  @Inject
  VipScraper(AppConfig appConfig, ScraperCoordinatorFactory factory, VipUserCache userCache) {
    super(factory, userCache, PublishSource.VIP);
    this.concurrentClientLimit = appConfig.getScraperVipConcurrentLimit();
  }
}
