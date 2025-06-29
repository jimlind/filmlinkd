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
    System.out.println("Starting the Application");
    log.info("Starting the Application Info");
    log.debug("Starting the Application Debug");
    log.warn("Starting the Application Warn");
    log.error("Starting the Application Error");

    Injector injector = Guice.createInjector(new GuiceModule());

    // Start the Discord server
    injector.getInstance(DiscordSystem.class).start();

    // Start the Subscribers and build the Publishers
    injector.getInstance(PubSubManager.class).activate();

    // Register shutdown events
    Runtime.getRuntime().addShutdownHook(injector.getInstance(ShutdownThread.class));
  }
}
