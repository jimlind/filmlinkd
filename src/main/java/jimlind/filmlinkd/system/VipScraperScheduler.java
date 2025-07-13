package jimlind.filmlinkd.system;

import com.google.inject.Inject;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.runnable.GeneralScraper;
import jimlind.filmlinkd.runnable.GeneralUserCacheClearer;

/** Sets up the scraper that runs over VIP users */
public class VipScraperScheduler extends GeneralScraperScheduler {

  @Inject
  public VipScraperScheduler(
      AppConfig appConfig,
      GeneralScraper generalScraper,
      GeneralUserCache generalUserCache,
      GeneralUserCacheClearer generalUserCacheClearer) {
    super(appConfig, generalScraper, generalUserCache, generalUserCacheClearer);
  }

  public void start() {}
}
