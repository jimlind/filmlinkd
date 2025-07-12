package jimlind.filmlinkd;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.config.GuiceModule;
import jimlind.filmlinkd.runnable.StatLogger;
import jimlind.filmlinkd.system.DiscordSystem;
import jimlind.filmlinkd.system.ShutdownThread;
import jimlind.filmlinkd.system.google.PubSubManager;
import lombok.extern.slf4j.Slf4j;

/** The main entry point for the bot application. */
@Slf4j
public class Bot {
  /**
   * Initializes and starts all core application systems.
   *
   * @param args Command line arguments (not used).
   */
  public static void main(String[] args) {
    log.info("Starting the Bot Class");

    // Create the Injector
    Injector injector = Guice.createInjector(new GuiceModule());

    // Start the Discord server
    try {
      injector.getInstance(DiscordSystem.class).start();
    } catch (Exception e) {
      log.error("Failed To Start the Discord Server", e);
    }

    // Start the Subscribers and build the Publishers
    injector.getInstance(PubSubManager.class).activate();

    // Schedule Memory Logger
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(injector.getInstance(StatLogger.class), 0, 30, TimeUnit.MINUTES);

    // Register shutdown events
    Runtime.getRuntime().addShutdownHook(injector.getInstance(ShutdownThread.class));
  }
}
