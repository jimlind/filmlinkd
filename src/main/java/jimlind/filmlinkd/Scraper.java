package jimlind.filmlinkd;

import com.google.inject.Guice;
import com.google.inject.Injector;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.config.GuiceModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Scraper {
  public static void main(String[] args) {
    log.info("Java Scraper");

    if (log.isInfoEnabled()) {
      Injector injector = Guice.createInjector(new GuiceModule());
      String apiKey = injector.getInstance(AppConfig.class).getDiscordBotToken();
      log.info("Client Id: " + apiKey);
    }
  }
}
