package jimlind.filmlinkd;

import jimlind.filmlinkd.config.SecretConfig;

public class Scraper {
    public static void main(String[] args) {
        System.out.println("Java Scraper");
        SecretConfig config = null;
        try {
            config = SecretConfig.getInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String apiKey = config.getSecret("DISCORD_DEV_CLIENT_ID", "latest");
        System.out.println("Client Id: " + apiKey);
    }
}
