package jimlind.filmlinkd;

import com.google.inject.Guice;
import com.google.inject.Injector;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.config.GuiceModule;
import jimlind.filmlinkd.system.ShutdownThread;
import jimlind.filmlinkd.system.dispatcher.StatLogDispatcher;
import jimlind.filmlinkd.system.google.pubsub.PubSubManager;
import jimlind.filmlinkd.system.scraper.GeneralScraperScheduler;
import jimlind.filmlinkd.system.scraper.VipScraperScheduler;
import lombok.extern.slf4j.Slf4j;

/** The main entry point for the scraper application. */
@Slf4j
public final class Scraper {
  private Scraper() {}

  /**
   * Initializes and starts all core application systems.
   *
   * @param args Command line arguments (not used).
   */
  public static void main(String[] args) {
    log.info("Starting the Scraper Class");

    // Create the Injector
    Injector injector = Guice.createInjector(new GuiceModule());
    injector.getInstance(AppConfig.class).setMainClass(Scraper.class.getName());

    // Configure the needed publishers and subscribers
    injector.getInstance(PubSubManager.class).buildLogEntryPublisher();
    injector.getInstance(PubSubManager.class).buildCommandSubscriber();

    // Start the Scrapers
    injector.getInstance(GeneralScraperScheduler.class).start();
    injector.getInstance(VipScraperScheduler.class).start();

    // Schedule system statistic logger
    injector.getInstance(StatLogDispatcher.class).start();

    // Register shutdown events
    Runtime.getRuntime().addShutdownHook(injector.getInstance(ShutdownThread.class));
  }
}
