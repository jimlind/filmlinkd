package jimlind.filmlinkd;

import com.google.inject.Guice;
import com.google.inject.Injector;
import jimlind.filmlinkd.config.GuiceModule;
import jimlind.filmlinkd.system.DiscordSystem;
import jimlind.filmlinkd.system.ShutdownThread;
import jimlind.filmlinkd.system.google.PubSubManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Bot {
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

    // Register shutdown events
    Runtime.getRuntime().addShutdownHook(injector.getInstance(ShutdownThread.class));
  }
}
