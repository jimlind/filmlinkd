package jimlind.filmlinkd.system.discord;

// import jimlind.filmlinkd.Config;
// import jimlind.filmlinkd.listener.DiscordListener;
// import lombok.Getter;
// import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
// import net.dv8tion.jda.api.sharding.ShardManager;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;

// @Component
@Singleton
public class ConnectionManager {
  private final AppConfig appConfig;

  @Inject
  ConnectionManager(AppConfig appConfig) {
    this.appConfig = appConfig;
  }

  public void connect() {
    String token = appConfig.getDiscordBotToken();
    DefaultShardManagerBuilder.createLight(token).build();
  }

  //  @Autowired private Config config;
  //  @Autowired private DiscordListener discordListeners;
  //  @Getter
  //  private ShardManager shardManager;
  //
  //  public void connect() {
  //    String token = config.getDiscordBotToken();
  //    this.shardManager =
  //
  // DefaultShardManagerBuilder.createLight(token).addEventListeners(discordListeners).build();
  //  }
  //
  //  public void disconnect() {
  //    if (this.shardManager != null) {
  //      this.shardManager.shutdown();
  //    }
  //  }
}
