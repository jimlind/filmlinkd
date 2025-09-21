package jimlind.filmlinkd;

import com.google.inject.Guice;
import com.google.inject.Injector;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.config.GuiceModule;
import jimlind.filmlinkd.system.DiscordSystem;
import jimlind.filmlinkd.system.ShutdownThread;
import jimlind.filmlinkd.system.dispatcher.StatLogDispatcher;
import jimlind.filmlinkd.system.google.pubsub.PubSubManager;
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
    log.info("Starting the Bot Class");

    // Create the Injector
    Injector injector = Guice.createInjector(new GuiceModule());
    injector.getInstance(AppConfig.class).setMainClass(Bot.class.getName());

    // Start the Discord server
    injector.getInstance(DiscordSystem.class).start();

    // Configure the needed publishers and subscribers
    injector.getInstance(PubSubManager.class).buildCommandPublisher();
    injector.getInstance(PubSubManager.class).buildLogEntryPublisher();
    injector.getInstance(PubSubManager.class).buildLogEntrySubscriber();

    // Schedule system statistic logger
    injector.getInstance(StatLogDispatcher.class).start();

    // Register shutdown events
    Runtime.getRuntime().addShutdownHook(injector.getInstance(ShutdownThread.class));
  }
}
