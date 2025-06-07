package jimlind.filmlinkd;

import com.google.inject.Guice;
import com.google.inject.Injector;
import jimlind.filmlinkd.config.GuiceModule;
import jimlind.filmlinkd.system.google.SecretManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Scraper {
  public static void main(String[] args) {
    log.info("Java Scraper");

    Injector injector = Guice.createInjector(new GuiceModule());
    String apiKey = injector.getInstance(SecretManager.class).getSecret("DISCORD_DEV_CLIENT_ID");
    System.out.println("Client Id: " + apiKey);
  }
}
