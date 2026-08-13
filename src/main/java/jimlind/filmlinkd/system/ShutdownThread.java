package jimlind.filmlinkd.system;

import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.discord.ConnectionManager;
import jimlind.filmlinkd.google.pubsub.PubSubManager;
import lombok.extern.slf4j.Slf4j;

/** Handles shutting down, stopping, and deactivating things that need the extra work. */
@Singleton
@Slf4j
public class ShutdownThread extends Thread {
  private final ConnectionManager connectionManager;
  private final PubSubManager pubSubManager;

  /**
   * Constructor for this class.
   *
   * @param connectionManager Manages connections to the Discord service
   * @param pubSubManager Serves as the primary interface for PubSub systems
   */
  @Inject
  ShutdownThread(ConnectionManager connectionManager, PubSubManager pubSubManager) {
    this.connectionManager = connectionManager;
    this.pubSubManager = pubSubManager;
  }

  /** Shutdown the systems as meets the individual needs. */
  @Override
  public void run() {
    log.info("Shutting Things Down!");
    connectionManager.disconnect();
    pubSubManager.deactivateAll();
  }
}
