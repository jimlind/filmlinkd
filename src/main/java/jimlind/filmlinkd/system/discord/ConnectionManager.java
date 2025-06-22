package jimlind.filmlinkd.system.discord;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;

@Singleton
public class ConnectionManager {
  private final AppConfig appConfig;
  private final EventListener eventListener;
  private final ShardManagerStorage shardManagerStorage;

  @Inject
  ConnectionManager(
      AppConfig appConfig, EventListener eventListener, ShardManagerStorage shardManagerStorage) {
    this.appConfig = appConfig;
    this.eventListener = eventListener;
    this.shardManagerStorage = shardManagerStorage;
  }

  public void connect() {
    String token = appConfig.getDiscordBotToken();
    ShardManager shardManager =
        DefaultShardManagerBuilder.createLight(token).addEventListeners(eventListener).build();
    shardManagerStorage.set(shardManager);
  }

  public @Nullable JDA getShardById(int shardId) {
    @Nullable ShardManager shardManager = shardManagerStorage.get();
    if (shardManager != null) {
      return shardManager.getShardById(shardId);
    } else {
      return null;
    }
  }

  public void disconnect() {
    @Nullable ShardManager shardManager = shardManagerStorage.get();
    if (shardManager != null) {
      shardManager.shutdown();
    }
  }
}
