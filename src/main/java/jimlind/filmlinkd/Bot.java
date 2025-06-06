package jimlind.filmlinkd;

import jimlind.filmlinkd.config.SecretConfig;

public class Bot {
  public static void main(String[] args) {
    System.out.println("Java Bot");
    String apiKey = SecretConfig.INSTANCE.getSecret("DISCORD_DEV_CLIENT_ID", "latest");
    System.out.println("Client Id: " + apiKey);
  }
}
