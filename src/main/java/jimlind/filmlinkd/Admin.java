package jimlind.filmlinkd;

import jimlind.filmlinkd.config.SecretGetter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Admin {
  public static void main(String[] args) {
    log.info("Java Admin");
    String apiKey = SecretGetter.INSTANCE.getSecret("DISCORD_DEV_CLIENT_ID", "latest");
    System.out.println("Client Id: " + apiKey);
  }
}
