package jimlind.filmlinkd.system.discord;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;

/** Handles the connecting and disconnecting of the Discord service. */
@Singleton
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
    String token = appConfig.getDiscordBotToken();
    ShardManager shardManager =
        DefaultShardManagerBuilder.createLight(token).addEventListeners(eventListener).build();
    shardManagerStorage.set(shardManager);
  }

  /**
   * Gets a discord service shard by its id. Due to the number of channels the bot uses it needs to
   * have a number of shards to process data efficiently.
   *
   * @param shardId The id of the shard to get
   * @return The shard as a JDA object
   */
  public @Nullable JDA getShardById(int shardId) {
    @Nullable ShardManager shardManager = shardManagerStorage.get();
    if (shardManager != null) {
      return shardManager.getShardById(shardId);
    } else {
      return null;
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
