package jimlind.filmlinkd.system;

import com.google.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.runnable.GeneralUserCacheClearer;

/** Schedules the scraper that runs over every user. */
public class GeneralScraperScheduler {
  private final AppConfig appConfig;
  private final jimlind.filmlinkd.runnable.GeneralScraper generalScraper;
  private final GeneralUserCache generalUserCache;
  private final GeneralUserCacheClearer generalUserCacheClearer;

  /**
   * Constructor for this class.
   *
   * @param appConfig Holds all the configs that determine how the system run
   * @param generalScraper The scraper task that we are scheduling
   * @param generalUserCache Where we store in memory versions records of latest diary entry
   * @param generalUserCacheClearer The user clear cache
   */
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

  /** Method that activates the scrapers so that the Class can be instantiated but inactive. */
  public void start() {
    generalUserCache.initializeRandomPage();

    try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {
      scheduler.scheduleAtFixedRate(
          generalScraper, 0, appConfig.getScraperGeneralPeriod(), TimeUnit.SECONDS);
      scheduler.scheduleAtFixedRate(
          generalUserCacheClearer, 0, appConfig.getScraperGeneralPeriod(), TimeUnit.HOURS);
    }

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
