package jimlind.filmlinkd.system;

import com.google.inject.Inject;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.runnable.GeneralScraperTask;
import jimlind.filmlinkd.runnable.GeneralUserCacheClearTask;

public class VipScraper extends GeneralScraper {

  @Inject
  public VipScraper(
      AppConfig appConfig,
      GeneralScraperTask generalScraperTask,
      GeneralUserCache generalUserCache,
      GeneralUserCacheClearTask generalUserCacheClearTask) {
    super(appConfig, generalScraperTask, generalUserCache, generalUserCacheClearTask);
  }

  public void start() {}
}
