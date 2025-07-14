package jimlind.filmlinkd.system;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.discord.ConnectionManager;

/**
 * Serves as the primary interface for Discord systems. This is really just a dumb wrapper now. I'm
 * not sure that it needs to continue to exist. Maybe it should expand to be something that
 * periodically checks the connection status and attempts to act on that.
 */
public class DiscordSystem {
  private final ConnectionManager connectionManager;

  /**
   * The constructor for this class.
   *
   * @param connectionManager Manages connections to the Discord server
   */
  @Inject
  DiscordSystem(ConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }

  /** Tells the connection manager to connect to the Discord API. */
  public void start() {
    connectionManager.connect();
  }

  /** Tells the connection manager to disconnect from the Discord API. */
  public void stop() {
    connectionManager.disconnect();
  }
}
