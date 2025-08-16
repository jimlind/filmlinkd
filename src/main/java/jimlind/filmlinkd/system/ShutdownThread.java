package jimlind.filmlinkd.system;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.google.PubSubManager;
import lombok.extern.slf4j.Slf4j;

/** Handles shutting down, stopping, and deactivating things that need the extra work. */
@Slf4j
public class ShutdownThread extends Thread {
  private final DiscordSystem discordSystem;
  private final PubSubManager pubSubManager;

  /**
   * Constructor for this class.
   *
   * @param discordSystem Serves as the primary interface for Discord systems
   * @param pubSubManager Serves as the primary interface for PubSub systems
   */
  @Inject
  ShutdownThread(DiscordSystem discordSystem, PubSubManager pubSubManager) {
    this.discordSystem = discordSystem;
    this.pubSubManager = pubSubManager;
  }

  /** Shutdown the systems as meets the individual needs. */
  @Override
  public void run() {
    log.info("Shutting Things Down!");
    discordSystem.stop();
    pubSubManager.deactivateAll();
  }
}
