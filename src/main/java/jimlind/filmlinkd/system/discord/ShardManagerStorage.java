package jimlind.filmlinkd.system.discord;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;

@Singleton
public class ShardManagerStorage {
  private net.dv8tion.jda.api.sharding.ShardManager shardManager = null;

  @Inject
  ShardManagerStorage() {}

  public boolean isSet() {
    return !(shardManager == null);
  }

  public void set(ShardManager shardManager) {
    this.shardManager = shardManager;
  }

  public @Nullable ShardManager get() {
    return shardManager;
  }
}
