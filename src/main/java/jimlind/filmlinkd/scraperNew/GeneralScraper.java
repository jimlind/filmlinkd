package jimlind.filmlinkd.scraperNew;

import javax.inject.Inject;

import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.model.Message.PublishSource;
import jimlind.filmlinkd.scraper.ScraperCoordinatorFactory;
import jimlind.filmlinkd.scraper.cache.GeneralUserCache;

public class GeneralScraper extends Scraper {

  @Inject
  GeneralScraper(
      AppConfig appConfig, ScraperCoordinatorFactory factory, GeneralUserCache userCache) {
    super(factory, userCache, PublishSource.Normal);
    this.concurrentClientLimit = appConfig.getScraperVipConcurrentLimit();
  }
}
