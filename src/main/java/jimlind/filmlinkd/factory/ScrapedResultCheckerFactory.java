package jimlind.filmlinkd.factory;

import com.google.inject.Inject;
import jimlind.filmlinkd.discord.ShardManagerStorage;
import jimlind.filmlinkd.discord.embed.factory.DiaryEntryEmbedFactory;
import jimlind.filmlinkd.runnable.ScrapedResultQueueChecker;
import jimlind.filmlinkd.system.ScrapedResultQueue;
import jimlind.filmlinkd.system.google.firestore.UserWriter;

/** A factory for creating instances of the {@link ScrapedResultQueueChecker} model. */
public class ScrapedResultCheckerFactory {
  private final DiaryEntryEmbedFactory diaryEntryEmbedFactory;
  private final ScrapedResultQueue scrapedResultQueue;
  private final ShardManagerStorage shardManagerStorage;
  private final UserWriter userWriter;

  /**
   * Constructor for {@link ScrapedResultCheckerFactory}.
   *
   * @param diaryEntryEmbedFactory A class that builds diaryEntryEmbed objects
   * @param scrapedResultQueue A class that stores results in local memory
   * @param userWriter Handles all write operations for user data in Firestore
   */
  @Inject
  public ScrapedResultCheckerFactory(
      DiaryEntryEmbedFactory diaryEntryEmbedFactory,
      ScrapedResultQueue scrapedResultQueue,
      ShardManagerStorage shardManagerStorage,
      UserWriter userWriter) {
    this.diaryEntryEmbedFactory = diaryEntryEmbedFactory;
    this.scrapedResultQueue = scrapedResultQueue;
    this.shardManagerStorage = shardManagerStorage;
    this.userWriter = userWriter;
  }

  /**
   * Creates a {@link ScrapedResultQueueChecker}.
   *
   * @param shardId The shard id to help us know which places have received data
   * @param totalShards The total number of shards in use
   * @return A {@link ScrapedResultQueueChecker} appropriate for one shard
   */
  public ScrapedResultQueueChecker create(int shardId, int totalShards) {
    return new ScrapedResultQueueChecker(
        diaryEntryEmbedFactory,
        scrapedResultQueue,
        shardManagerStorage,
        userWriter,
        shardId,
        totalShards);
  }
}
