package jimlind.filmlinkd;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Optional;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.config.GuiceModule;
import jimlind.filmlinkd.scraper.scheduler.ScraperSchedulerFactory;
import jimlind.filmlinkd.system.ShutdownThread;
import jimlind.filmlinkd.system.dispatcher.StatLogDispatcher;
import jimlind.filmlinkd.system.google.pubsub.PubSubManager;
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
    String env = Optional.ofNullable(System.getenv("FILMLINKD_ENVIRONMENT")).orElse("UNSET");
    log.atInfo().setMessage("Starting the Scraper Class (Environment: {})").addArgument(env).log();

    // Create the Injector
    Injector injector = Guice.createInjector(new GuiceModule());
    injector.getInstance(AppConfig.class).setMainClass(Scraper.class.getName());

    // Configure the needed publishers and subscribers
    injector.getInstance(PubSubManager.class).buildLogEntryPublisher();
    injector.getInstance(PubSubManager.class).buildCommandSubscriber();

    // Start the Scrapers
    ScraperSchedulerFactory schedulerFactory = injector.getInstance(ScraperSchedulerFactory.class);
    schedulerFactory.create(false).start();
    schedulerFactory.create(true).start();

    // Schedule system statistic logger
    injector.getInstance(StatLogDispatcher.class).start();

    // Register shutdown events
    Runtime.getRuntime().addShutdownHook(injector.getInstance(ShutdownThread.class));
  }
}
