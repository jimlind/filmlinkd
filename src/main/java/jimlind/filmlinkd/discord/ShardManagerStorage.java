package jimlind.filmlinkd.discord;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.Nullable;

/**
 * Stores a reference to the discord shard manager. Allows access to the shard manager via
 * dependency injection.
 */
@Singleton
public class ShardManagerStorage {
  private net.dv8tion.jda.api.sharding.ShardManager shardManager;

  /** Constructor for this class. */
  @Inject
  ShardManagerStorage() {}

  /**
   * Check if the shard manager is set.
   *
   * @return true if shard manager is set, false if otherwise
   */
  public boolean isSet() {
    return shardManager != null;
  }

  /**
   * Sets the shard manager.
   *
   * @param shardManager The shard manager to set
   */
  public void set(ShardManager shardManager) {
    this.shardManager = shardManager;
  }

  /**
   * Gets shard manager.
   *
   * @return Any possible shard manager value could be null
   */
  public @Nullable ShardManager get() {
    return shardManager;
  }
}
