package jimlind.filmlinkd.factory;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.ScrapedResultChecker;
import jimlind.filmlinkd.system.ScrapedResultQueue;
import jimlind.filmlinkd.system.discord.ShardManagerStorage;
import jimlind.filmlinkd.system.discord.embedBuilder.DiaryEntryEmbedBuilder;
import jimlind.filmlinkd.system.google.FirestoreManager;

public class ScrapedResultCheckerFactory {
  private final DiaryEntryEmbedBuilder diaryEntryEmbedBuilder;
  private final FirestoreManager firestoreManager;
  private final ScrapedResultQueue scrapedResultQueue;
  private final ShardManagerStorage shardManagerStorage;

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
