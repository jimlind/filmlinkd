package jimlind.filmlinkd.scraperV2;

import javax.inject.Inject;

import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.model.Message.PublishSource;
import jimlind.filmlinkd.scraperV1.ScraperCoordinatorFactory;
import jimlind.filmlinkd.scraperV1.cache.GeneralUserCache;

public class GeneralScraper extends Scraper {

  @Inject
  GeneralScraper(
      AppConfig appConfig, ScraperCoordinatorFactory factory, GeneralUserCache userCache) {
    super(factory, userCache, PublishSource.Normal);
    this.concurrentClientLimit = appConfig.getScraperVipConcurrentLimit();
  }
}
