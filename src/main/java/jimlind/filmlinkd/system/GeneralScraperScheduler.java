package jimlind.filmlinkd.system;

import com.google.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.runnable.GeneralUserCacheClearer;

public class GeneralScraperScheduler {
  private final AppConfig appConfig;
  private final jimlind.filmlinkd.runnable.GeneralScraper generalScraper;
  private final GeneralUserCache generalUserCache;
  private final GeneralUserCacheClearer generalUserCacheClearer;

  @Inject
  public GeneralScraperScheduler(
      AppConfig appConfig,
      jimlind.filmlinkd.runnable.GeneralScraper generalScraper,
      GeneralUserCache generalUserCache,
      GeneralUserCacheClearer generalUserCacheClearer) {
    this.appConfig = appConfig;
    this.generalScraper = generalScraper;
    this.generalUserCache = generalUserCache;
    this.generalUserCacheClearer = generalUserCacheClearer;
  }

  public void start() {
    generalUserCache.initializeRandomPage();

    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(
        generalScraper, 0, appConfig.getScraperGeneralPeriod(), TimeUnit.SECONDS);
    scheduler.scheduleAtFixedRate(
        generalUserCacheClearer, 0, appConfig.getScraperGeneralPeriod(), TimeUnit.HOURS);

    //    // Listen for Command PubSub messages posted and upsert appropriate follow outcome data
    //    this.container.resolve('pubSubMessageListener').onCommandMessage((message: any) => {
    //                const returnData = JSON.parse(message.data.toString());
    //      if (returnData.command == 'FOLLOW') {
    //        message.ack();
    //                    const subscribedUserList = this.container.resolve('subscribedUserList');
    //        subscribedUserList.upsert(returnData.user, returnData.entry);
    //      }
    //    });

  }
}
