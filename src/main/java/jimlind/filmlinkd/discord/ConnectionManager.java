package jimlind.filmlinkd.discord;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;

/** Handles the connecting and disconnecting of the Discord service. */
@Singleton
@Slf4j
public class ConnectionManager {
  private final AppConfig appConfig;
  private final EventListener eventListener;
  private final ShardManagerStorage shardManagerStorage;

  /**
   * Constructor for this class.
   *
   * @param appConfig Contains application and environment variables
   * @param eventListener Listener for On Ready and Slash Commands
   * @param shardManagerStorage A class that stores shard information
   */
  @Inject
  ConnectionManager(
      AppConfig appConfig, EventListener eventListener, ShardManagerStorage shardManagerStorage) {
    this.appConfig = appConfig;
    this.eventListener = eventListener;
    this.shardManagerStorage = shardManagerStorage;
  }

  /** Connects to the Discord service. */
  public void connect() {
    try {
      String token = appConfig.getDiscordBotToken();
      ShardManager shardManager =
          DefaultShardManagerBuilder.createLight(token).addEventListeners(eventListener).build();
      shardManagerStorage.set(shardManager);
    } catch (InvalidTokenException e) {
      log.error("Unable to connect to Discord API from Invalid Token");
    }
  }

  /** Disconnects from the Discord service. */
  public void disconnect() {
    @Nullable ShardManager shardManager = shardManagerStorage.get();
    if (shardManager != null) {
      shardManager.shutdown();
    }
  }
}
