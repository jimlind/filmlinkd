package jimlind.filmlinkd.discord.dispatcher;

import com.google.inject.Inject;
import jimlind.filmlinkd.core.scheduling.TimedTaskRunner;
import jimlind.filmlinkd.discord.ShardManagerStorage;
import jimlind.filmlinkd.discord.embed.factory.DiaryEntryEmbedFactory;
import jimlind.filmlinkd.runnable.ScrapedResultQueueChecker;
import jimlind.filmlinkd.system.ScrapedResultQueue;
import jimlind.filmlinkd.system.google.firestore.UserWriter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class ScrapedResultQueueDispatcher extends TimedTaskRunner {
  private static final int INITIAL_DELAY_SECONDS = 0;
  private static final int INTERVAL_SECONDS = 1;

  private final DiaryEntryEmbedFactory diaryEntryEmbedFactory;
  private final ScrapedResultQueue scrapedResultQueue;
  private final ShardManagerStorage shardManagerStorage;
  private final UserWriter userWriter;

  @Nullable private ScrapedResultQueueChecker scrapedResultQueueChecker;

  /**
   * Constructor for this class.
   *
   * @param diaryEntryEmbedFactory A class that builds diaryEntryEmbed objects
   * @param scrapedResultQueue A class that stores results in local memory
   * @param shardManagerStorage A class that stores shard information
   * @param userWriter Handles all write operations for user data in Firestore
   */
  @Inject
  public ScrapedResultQueueDispatcher(
      DiaryEntryEmbedFactory diaryEntryEmbedFactory,
      ScrapedResultQueue scrapedResultQueue,
      ShardManagerStorage shardManagerStorage,
      UserWriter userWriter) {
    super(INITIAL_DELAY_SECONDS, INTERVAL_SECONDS);

    this.diaryEntryEmbedFactory = diaryEntryEmbedFactory;
    this.scrapedResultQueue = scrapedResultQueue;
    this.shardManagerStorage = shardManagerStorage;
    this.userWriter = userWriter;
  }

  /**
   * Configure the ScrapedResultQueueDispatcher. This public configure method is used so that the
   * actual constructor can get dependencies properly injected. If this pattern becomes common I
   * should make some factories to avoid the edge case I'm creating.
   *
   * @param shardId The shard id to help us know which places have received data
   * @param totalShards The total number of shards in use
   */
  public void configure(int shardId, int totalShards) {
    scrapedResultQueueChecker =
        new ScrapedResultQueueChecker(
            diaryEntryEmbedFactory,
            scrapedResultQueue,
            shardManagerStorage,
            userWriter,
            shardId,
            totalShards);
  }

  @Override
  protected void runTask() {
    if (scrapedResultQueueChecker == null) {
      String message =
          "Attempting to run the ScrapedResultQueueDispatcher without proper configuration";
      log.atError().setMessage(message).log();
      this.stop();
    }

    scrapedResultQueueChecker.run();
  }
}
