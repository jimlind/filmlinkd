package jimlind.filmlinkd;

import jimlind.filmlinkd.config.SecretConfig;

public class Scraper {
  public static void main(String[] args) {
    System.out.println("Java Scraper");
    String apiKey = SecretConfig.INSTANCE.getSecret("DISCORD_DEV_CLIENT_ID", "latest");
    System.out.println("Client Id: " + apiKey);
  }
}
