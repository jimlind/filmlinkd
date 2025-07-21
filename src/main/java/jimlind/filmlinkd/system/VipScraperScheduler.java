package jimlind.filmlinkd.system;

import com.google.inject.Inject;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.runnable.GeneralScraper;
import jimlind.filmlinkd.runnable.GeneralUserCacheClearer;

/** Schedules the scraper that runs over VIP users. */
public class VipScraperScheduler extends GeneralScraperScheduler {

  /**
   * Constructor for this class.
   *
   * <p>Currently taking in the "general" dependencies but the start method also does nothing so
   * this is really a placeholder for now.
   *
   * @param appConfig Holds all the configs that determine how the system run
   * @param generalScraper The scraper task that we are scheduling
   * @param generalUserCache Where we store in memory versions records of latest diary entry
   * @param generalUserCacheClearer The user clear cache
   */
  @Inject
  public VipScraperScheduler(
      AppConfig appConfig,
      GeneralScraper generalScraper,
      GeneralUserCache generalUserCache,
      GeneralUserCacheClearer generalUserCacheClearer) {
    super(appConfig, generalScraper, generalUserCache, generalUserCacheClearer);
  }

  /** Does nothing. */
  public void start() {}
}
