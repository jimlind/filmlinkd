package jimlind.filmlinkd.system.discord;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;

@Singleton
public class ConnectionManager {
  private final AppConfig appConfig;
  private final EventListener eventListener;
  private ShardManager shardManager = null;

  @Inject
  ConnectionManager(AppConfig appConfig, EventListener eventListener) {
    this.appConfig = appConfig;
    this.eventListener = eventListener;
  }

  public void connect() {
    String token = appConfig.getDiscordBotToken();
    shardManager =
        DefaultShardManagerBuilder.createLight(token).addEventListeners(eventListener).build();
  }

  public void disconnect() {
    if (shardManager != null) {
      shardManager.shutdown();
    }
  }
}
