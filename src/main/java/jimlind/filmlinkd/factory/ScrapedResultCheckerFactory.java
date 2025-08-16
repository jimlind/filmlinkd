package jimlind.filmlinkd.factory;

import com.google.inject.Inject;
import jimlind.filmlinkd.runnable.ScrapedResultChecker;
import jimlind.filmlinkd.system.ScrapedResultQueue;
import jimlind.filmlinkd.system.discord.ShardManagerStorage;
import jimlind.filmlinkd.system.discord.embedbuilder.DiaryEntryEmbedBuilder;
import jimlind.filmlinkd.system.google.firestore.UserWriter;

/** A factory for creating instances of the {@link ScrapedResultChecker} model. */
public class ScrapedResultCheckerFactory {
  private final DiaryEntryEmbedBuilder diaryEntryEmbedBuilder;
  private final ScrapedResultQueue scrapedResultQueue;
  private final ShardManagerStorage shardManagerStorage;
  private final UserWriter userWriter;

  /**
   * Constructor for {@link ScrapedResultCheckerFactory}.
   *
   * @param diaryEntryEmbedBuilder A class that builds diaryEntryEmbed objects
   * @param scrapedResultQueue A class that stores results in local memory
   * @param userWriter Handles all write operations for user data in Firestore
   */
  @Inject
  public ScrapedResultCheckerFactory(
      DiaryEntryEmbedBuilder diaryEntryEmbedBuilder,
      ScrapedResultQueue scrapedResultQueue,
      ShardManagerStorage shardManagerStorage,
      UserWriter userWriter) {
    this.diaryEntryEmbedBuilder = diaryEntryEmbedBuilder;
    this.scrapedResultQueue = scrapedResultQueue;
    this.shardManagerStorage = shardManagerStorage;
    this.userWriter = userWriter;
  }

  /**
   * Creates a {@link ScrapedResultChecker}.
   *
   * @param shardId The shard id to help us know which places have received data
   * @param totalShards The total number of shards in use
   * @return A {@link ScrapedResultChecker} appropriate for one shard
   */
  public ScrapedResultChecker create(int shardId, int totalShards) {
    return new ScrapedResultChecker(
        diaryEntryEmbedBuilder,
        scrapedResultQueue,
        shardManagerStorage,
        userWriter,
        shardId,
        totalShards);
  }
}
