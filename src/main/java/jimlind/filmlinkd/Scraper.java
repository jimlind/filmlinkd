package jimlind.filmlinkd;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.config.GuiceModule;
import jimlind.filmlinkd.runnable.StatLogger;
import jimlind.filmlinkd.system.GeneralScraperScheduler;
import jimlind.filmlinkd.system.ShutdownThread;
import jimlind.filmlinkd.system.VipScraperScheduler;
import jimlind.filmlinkd.system.google.PubSubManager;
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

    // Build the Publishers
    injector.getInstance(PubSubManager.class).buildLogEntryPublisher();

    // Start the Scrapers
    injector.getInstance(GeneralScraperScheduler.class).start();
    injector.getInstance(VipScraperScheduler.class).start();

    // Schedule Memory Logger
    try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {
      scheduler.scheduleAtFixedRate(
          injector.getInstance(StatLogger.class), 0, 30, TimeUnit.MINUTES);
      new CountDownLatch(1).await();
    } catch (InterruptedException event) {
      log.atInfo()
              .setMessage("Stat Logger Interrupted")
              .addKeyValue("event", event)
              .log();
    }

    // Register shutdown events
    Runtime.getRuntime().addShutdownHook(injector.getInstance(ShutdownThread.class));
  }
}
