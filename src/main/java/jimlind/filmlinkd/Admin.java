package jimlind.filmlinkd;

import jimlind.filmlinkd.config.SecretConfig;

public class Admin {
    public static void main(String[] args) {
        System.out.println("Java Admin");
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
