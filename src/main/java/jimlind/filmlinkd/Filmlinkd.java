package jimlind.filmlinkd;

import jimlind.filmlinkd.system.di.ApplicationComponent;
import jimlind.filmlinkd.system.di.DaggerApplicationComponent;
import lombok.extern.slf4j.Slf4j;

/** The main entry point for the application. */
@Slf4j
public final class Filmlinkd {
  private Filmlinkd() {}

  /**
   * Initializes and starts the Filmlinkd bot.
   *
   * @param args Command line arguments (not used).
   */
  public static void main(String[] args) {
    log.atInfo().setMessage("Starting the Filmlinkd Class").log();

    ApplicationComponent component = DaggerApplicationComponent.create();
    component.appConfig().setMainClass(Filmlinkd.class.getName());

    // Start the Discord server
    component.connectionManager().connect();

    // Stop the Discord server on shutdown hook
    Runtime.getRuntime().addShutdownHook(component.shutdownThread());
  }
}
