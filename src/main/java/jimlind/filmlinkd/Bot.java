package jimlind.filmlinkd;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.config.GuiceModule;
import jimlind.filmlinkd.google.pubsub.PubSubManagerInterface;
import jimlind.filmlinkd.system.DiscordSystem;
import jimlind.filmlinkd.system.ShutdownThread;
import jimlind.filmlinkd.system.dispatcher.ScrapedResultQueueDispatcher;
import jimlind.filmlinkd.system.dispatcher.StatLogDispatcher;
import lombok.extern.slf4j.Slf4j;

/** The main entry point for the bot application. */
@Slf4j
public final class Bot {

  public static final String TRACING_MODE = "tracing";

  private Bot() {}

  /**
   * Initializes and starts all core application systems.
   *
   * @param args Command line arguments (not used).
   */
  public static void main(String[] args) {
    String env = Optional.ofNullable(System.getenv("FILMLINKD_ENVIRONMENT")).orElse("UNSET");
    log.atInfo().setMessage("Starting the Bot Class (Environment: {})").addArgument(env).log();

    // Create the Injector
    Injector injector = Guice.createInjector(new GuiceModule());
    injector.getInstance(AppConfig.class).setMainClass(Bot.class.getName());

    // Start the Discord server
    injector.getInstance(DiscordSystem.class).start();

    // Configure the needed publishers and subscribers
    injector.getInstance(PubSubManagerInterface.class).buildCommandPublisher();
    injector.getInstance(PubSubManagerInterface.class).buildLogEntryPublisher();
    injector.getInstance(PubSubManagerInterface.class).buildLogEntrySubscriber();

    // Schedule system statistic logger
    injector.getInstance(StatLogDispatcher.class).start();

    // Schedule the scraped result queue checker
    injector.getInstance(ScrapedResultQueueDispatcher.class).start();

    // Register shutdown events
    Runtime.getRuntime().addShutdownHook(injector.getInstance(ShutdownThread.class));

    // Shutdown After 60 seconds if in tracing mode
    if (TRACING_MODE.equals(System.getProperty("app.mode"))) {
      Executors.newSingleThreadScheduledExecutor()
          .schedule(
              () -> {
                System.out.println("Killing process after 120 seconds...");
                System.exit(0);
              },
              60,
              TimeUnit.SECONDS);
    }
  }
}
