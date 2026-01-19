package jimlind.filmlinkd;

import java.util.Optional;
import jimlind.filmlinkd.core.di.ApplicationComponent;
import jimlind.filmlinkd.core.di.DaggerApplicationComponent;
import lombok.extern.slf4j.Slf4j;

/** The main entry point for the bot application. */
@Slf4j
public final class Bot {
  private Bot() {}

  /**
   * Initializes and starts all core application systems.
   *
   * @param args Command line arguments (not used).
   */
  public static void main(String[] args) {
    String env = Optional.ofNullable(System.getenv("FILMLINKD_ENVIRONMENT")).orElse("UNSET");
    log.atInfo().setMessage("Starting the Bot Class (Environment: {})").addArgument(env).log();

    // Create the DI Components
    ApplicationComponent component = DaggerApplicationComponent.create();
    component.appConfig().setMainClass(Bot.class.getName());

    // Start the Discord server
    component.discordSystem().start();

    // Configure the needed publishers and subscribers
    component.pubSubManager().buildCommandPublisher();
    component.pubSubManager().buildLogEntryPublisher();
    component.pubSubManager().buildLogEntrySubscriber();

    // Schedule system statistic logger
    component.statLogDispatcher().start();

    // Schedule the scraped result queue checker
    component.scrapedResultQueueDispatcher().start();

    // Register shutdown events
    Runtime.getRuntime().addShutdownHook(component.shutdownThread());
  }
}
