package jimlind.filmlinkd.system;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.google.PubSubManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShutdownThread extends Thread {
  private final DiscordSystem discordSystem;
  private final PubSubManager pubSubManager;

  @Inject
  ShutdownThread(DiscordSystem discordSystem, PubSubManager pubSubManager) {
    this.discordSystem = discordSystem;
    this.pubSubManager = pubSubManager;
  }

  public void run() {
    log.info("Shutting Things Down!");
    discordSystem.stop();
    pubSubManager.deactivate();
  }
}
