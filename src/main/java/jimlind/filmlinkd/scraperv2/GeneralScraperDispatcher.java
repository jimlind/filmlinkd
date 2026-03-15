package jimlind.filmlinkd.scraperv2;

import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.core.scheduling.TimedTaskRunner;

/**
 * Executes the timed scraper tasks in a way that appropriately uses the TimedTaskRunner base to
 * ensure that things could be closed when not needed.
 */
@Singleton
public class GeneralScraperDispatcher extends TimedTaskRunner {
  private static final long INITIAL_DELAY_MILLISECONDS = 0;
  private static final long INTERVAL_MILLISECONDS = 600000; // 10 minutes
  private final Scraper scraper;

  /**
   * Constructor for this class.
   *
   * @param appConfig Stores application configuration
   * @param scraper Scrapers
   */
  @Inject
  public GeneralScraperDispatcher(AppConfig appConfig, GeneralScraper scraper) {
    super(0, appConfig.getScraperGeneralPeriod());
    this.scraper = scraper;
  }

  /** Triggers the General scraper execution. */
  @Override
  protected void runTask() {
    scraper.run();
  }
}
