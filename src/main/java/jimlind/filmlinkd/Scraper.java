package jimlind.filmlinkd;

import java.util.Optional;
import jimlind.filmlinkd.core.di.ApplicationComponent;
import jimlind.filmlinkd.core.di.DaggerApplicationComponent;
import jimlind.filmlinkd.scraper.scheduler.ScraperSchedulerFactory;
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

    // Create the DI Components
    ApplicationComponent component = DaggerApplicationComponent.create();
    component.appConfig().setMainClass(Bot.class.getName());

    // Configure the needed publishers and subscribers
    component.pubSubManager().buildLogEntryPublisher();
    component.pubSubManager().buildCommandSubscriber();

    // Start the Scrapers
    ScraperSchedulerFactory schedulerFactory = component.scraperSchedulerFactory();
    schedulerFactory.create(false).start();
    schedulerFactory.create(true).start();

    // Schedule system statistic logger
    component.statLogDispatcher().start();

    // Register shutdown events
    Runtime.getRuntime().addShutdownHook(component.shutdownThread());
  }
}
