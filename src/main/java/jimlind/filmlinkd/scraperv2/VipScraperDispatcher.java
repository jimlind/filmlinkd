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
public class VipScraperDispatcher extends TimedTaskRunner {
  private final Scraper scraper;

  /**
   * Constructor for this class.
   *
   * @param appConfig Stores application configuration
   * @param scraper Scrapes
   */
  @Inject
  public VipScraperDispatcher(AppConfig appConfig, VipScraper scraper) {
    super(0, appConfig.getScraperVipPeriodMillis());
    this.scraper = scraper;
  }

  /** Triggers the VIP scraper execution. */
  @Override
  protected void runTask() {
    scraper.run();
  }
}
