package jimlind.filmlinkd;

import com.google.inject.Guice;
import com.google.inject.Injector;
import jimlind.filmlinkd.config.GuiceModule;
import jimlind.filmlinkd.system.GeneralScraper;
import jimlind.filmlinkd.system.ShutdownThread;
import jimlind.filmlinkd.system.VipScraper;
import jimlind.filmlinkd.system.google.PubSubManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Scraper {
  public static void main(String[] args) {
    log.info("Starting the Scraper Class");

    // Create the Injector
    Injector injector = Guice.createInjector(new GuiceModule());

    // Build the Publishers
    injector.getInstance(PubSubManager.class).buildLogEntryPublisher();

    // Start the Scrapers
    injector.getInstance(GeneralScraper.class).start();
    injector.getInstance(VipScraper.class).start();

    // Register shutdown events
    Runtime.getRuntime().addShutdownHook(injector.getInstance(ShutdownThread.class));
  }
}
