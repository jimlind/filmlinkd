package jimlind.filmlinkd;

import com.google.inject.Guice;
import com.google.inject.Injector;
import jimlind.filmlinkd.config.GuiceModule;
import jimlind.filmlinkd.system.DiscordSystem;
import jimlind.filmlinkd.system.discord.ConnectionManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Bot {
  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new GuiceModule());
    injector.getInstance(ConnectionManager.class).connect();

    // Start is just filler for right now.
    injector.getInstance(DiscordSystem.class).start();

    log.info("Java Bot");

    //    String apiKey = SecretManager.INSTANCE.getSecret("DISCORD_DEV_CLIENT_ID");
    //    System.out.println("Client Id: " + apiKey);
  }
}
