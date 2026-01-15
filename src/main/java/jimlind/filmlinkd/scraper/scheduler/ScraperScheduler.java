package jimlind.filmlinkd.scraper.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.scraper.cache.BaseUserCache;
import jimlind.filmlinkd.scraper.cache.clearer.BaseUserCacheClearer;
import jimlind.filmlinkd.scraper.runner.BaseScraper;
import lombok.extern.slf4j.Slf4j;

/** Schedules the scheduler that runs over every user. */
@Slf4j
public class ScraperScheduler {
  protected BaseScraper scraper;
  protected BaseUserCache userCache;
  protected BaseUserCacheClearer userCacheClearer;
  protected long scraperPeriod;
  protected long userCachePeriod;

  /**
   * Constructor for this class.
   *
   * @param scraper The scraper task to schedule
   * @param userCache In memory cache of latest diary entry
   * @param userCacheClearer The cache clearing task to schedule
   * @param scraperPeriod Delay between scraping events in seconds
   * @param userCachePeriod Delay between cache clearing events in hours
   */
  public ScraperScheduler(
      BaseScraper scraper,
      BaseUserCache userCache,
      BaseUserCacheClearer userCacheClearer,
      long scraperPeriod,
      long userCachePeriod) {
    this.scraper = scraper;
    this.userCache = userCache;
    this.userCacheClearer = userCacheClearer;
    this.scraperPeriod = scraperPeriod;
    this.userCachePeriod = userCachePeriod;
  }

  /** Method that activates the scraper and clearer. */
  @SuppressWarnings("PMD.AvoidCatchingThrowable")
  public void start() {
    // Set up the user cache with an appropriate random starting point before the scrapers start
    userCache.initializeRandomPage();

    // These should run forever so not closing them
    @SuppressWarnings("PMD.CloseResource")
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleWithFixedDelay(
        () -> {
          try {
            scraper.run();
          } catch (Throwable t) {
            log.error("Error running scraper", t);
          }
        },
        0,
        scraperPeriod,
        TimeUnit.SECONDS);
    scheduler.scheduleWithFixedDelay(
        () -> {
          try {
            userCacheClearer.run();
          } catch (Throwable t) {
            log.error("Error running user cache clearer", t);
          }
        },
        userCachePeriod,
        userCachePeriod,
        TimeUnit.HOURS);
  }
}
