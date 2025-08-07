package jimlind.filmlinkd.factory;

import com.google.inject.Inject;
import jimlind.filmlinkd.runnable.ScrapedResultChecker;
import jimlind.filmlinkd.system.ScrapedResultQueue;
import jimlind.filmlinkd.system.discord.ShardManagerStorage;
import jimlind.filmlinkd.system.discord.embedbuilder.DiaryEntryEmbedBuilder;
import jimlind.filmlinkd.system.google.FirestoreManager;

/** A factory for creating instances of the {@link ScrapedResultChecker} model. */
public class ScrapedResultCheckerFactory {
  private final DiaryEntryEmbedBuilder diaryEntryEmbedBuilder;
  private final FirestoreManager firestoreManager;
  private final ScrapedResultQueue scrapedResultQueue;
  private final ShardManagerStorage shardManagerStorage;

  /**
   * Constructor for {@link ScrapedResultCheckerFactory}.
   *
   * @param diaryEntryEmbedBuilder A class that builds diaryEntryEmbed objects
   * @param firestoreManager A class that handles FireStore data interactions
   * @param scrapedResultQueue A class that stores results in local memory
   */
  @Inject
  public ScrapedResultCheckerFactory(
      DiaryEntryEmbedBuilder diaryEntryEmbedBuilder,
      FirestoreManager firestoreManager,
      ScrapedResultQueue scrapedResultQueue,
      ShardManagerStorage shardManagerStorage) {
    this.diaryEntryEmbedBuilder = diaryEntryEmbedBuilder;
    this.firestoreManager = firestoreManager;
    this.scrapedResultQueue = scrapedResultQueue;
    this.shardManagerStorage = shardManagerStorage;
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
        firestoreManager,
        scrapedResultQueue,
        shardManagerStorage,
        shardId,
        totalShards);
  }
}
