package jimlind.filmlinkd.system;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShutdownThread extends Thread {
  private final DiscordSystem discordSystem;

  @Inject
  ShutdownThread(DiscordSystem discord) {
    discordSystem = discord;
  }

  public void run() {
    log.info("Shutting Things Down!");
    discordSystem.stop();
  }
}
