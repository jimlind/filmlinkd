package jimlind.filmlinkd.system;

import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.discord.ConnectionManager;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles shutting down, stopping, and deactivating things that need the extra work. Currently only
 * runs one shutdown command so the class may be overkill.
 */
@Singleton
@Slf4j
public class ShutdownThread extends Thread {
  private final ConnectionManager connectionManager;

  /**
   * Constructor for this class.
   *
   * @param connectionManager Manages connections to the Discord service
   */
  @Inject
  ShutdownThread(ConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }

  /** Shutdown the systems as meets the individual needs. */
  @Override
  public void run() {
    log.info("Shutting Things Down!");
    connectionManager.disconnect();
  }
}
