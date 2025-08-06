package jimlind.filmlinkd;

import com.google.inject.Guice;
import com.google.inject.Injector;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.config.GuiceModule;
import lombok.extern.slf4j.Slf4j;

/** The main entry point for the admin application. */
@Slf4j
public final class Admin {
  private Admin() {}

  /**
   * Initializes and starts all core application systems.
   *
   * @param args Command line arguments (not used).
   */
  public static void main(String[] args) {
    log.info("Java Admin");

    if (log.isInfoEnabled()) {
      Injector injector = Guice.createInjector(new GuiceModule());
      String apiKey = injector.getInstance(AppConfig.class).getDiscordBotToken();
      log.info("Client Id: {}", apiKey);
    }
  }
}
