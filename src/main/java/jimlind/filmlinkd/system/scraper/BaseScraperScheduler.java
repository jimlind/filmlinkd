package jimlind.filmlinkd.system.scraper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.cache.BaseUserCache;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.runnable.BaseScraper;
import jimlind.filmlinkd.runnable.BaseUserCacheClearer;

public class BaseScraperScheduler {
  protected AppConfig appConfig;
  protected BaseScraper scraper;
  protected BaseUserCache userCache;
  protected BaseUserCacheClearer userCacheClearer;
  protected long scraperPeriod;
  protected long userCachePeriod;

  /** Method that activates the scrapers so that the Class can be instantiated but inactive. */
  public void start() {
    userCache.initializeRandomPage();

    // These should run forever so not closing them
    @SuppressWarnings("PMD.CloseResource")
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(scraper, 0, scraperPeriod, TimeUnit.SECONDS);
    scheduler.scheduleAtFixedRate(userCacheClearer, 0, userCachePeriod, TimeUnit.HOURS);
  }
}
