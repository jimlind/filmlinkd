package jimlind.filmlinkd.system;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.discord.ConnectionManager;

public class DiscordSystem {
  private final ConnectionManager connectionManager;

  @Inject
  DiscordSystem(ConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }

  public void start() {
    connectionManager.connect();
  }

  public void stop() {
    connectionManager.disconnect();
  }
}
