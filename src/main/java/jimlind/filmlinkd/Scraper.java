package jimlind.filmlinkd;

import java.util.Optional;
import jimlind.amaranth.Scheduler;
import jimlind.filmlinkd.system.di.ApplicationComponent;
import jimlind.filmlinkd.system.di.DaggerApplicationComponent;
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
    component.appConfig().setMainClass(Scraper.class.getName());

    // Configure the needed publishers and subscribers
    component.pubSubManager().buildLogEntryPublisher();
    component.pubSubManager().buildCommandSubscriber();

    // Start the Scrapers
    component.generalScraperDispatcher().start();

    // Schedule system statistic logger
    Scheduler scheduler = new Scheduler();
    scheduler.addTask(component.memoryInformationLogger());
    scheduler.addTask(component.vipScraper());
    scheduler.startAll();

    // Register shutdown events
    Runtime.getRuntime().addShutdownHook(component.shutdownThread());
  }
}
