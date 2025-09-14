package jimlind.filmlinkd.runnable;

import java.time.Instant;
import java.util.List;
import jimlind.filmlinkd.discord.ShardManagerStorage;
import jimlind.filmlinkd.discord.embed.factory.DiaryEntryEmbedFactory;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.model.ScrapedResult;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.ScrapedResultQueue;
import jimlind.filmlinkd.system.google.firestore.UserWriter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.sharding.ShardManager;

/**
 * This is a scheduled task that checks the ScrapedResultQueue and posts the appropriate messages to
 * channels as needed. The ScrapedResultQueue exists because I couldn't figure out how to throttle
 * PubSub responses using the actual documented process. I probably could and this would become a
 * listener instead.
 */
@Slf4j
public class ScrapedResultChecker implements Runnable {
  private final DiaryEntryEmbedFactory diaryEntryEmbedFactory;
  private final ScrapedResultQueue scrapedResultQueue;
  private final ShardManagerStorage shardManagerStorage;
  private final UserWriter userWriter;

  private final int shardId;
  private final int totalShards;

  /**
   * Constructor for this class.
   *
   * @param diaryEntryEmbedFactory A class that builds diaryEntryEmbed objects
   * @param scrapedResultQueue A class that stores results in local memory
   * @param shardManagerStorage A class that stores shard information
   * @param shardId The shard id to help us know which shard we are running this from
   * @param totalShards The total number of shards in use
   * @param userWriter Handles all write operations for user data in Firestore
   */
  public ScrapedResultChecker(
      DiaryEntryEmbedFactory diaryEntryEmbedFactory,
      ScrapedResultQueue scrapedResultQueue,
      ShardManagerStorage shardManagerStorage,
      UserWriter userWriter,
      int shardId,
      int totalShards) {
    this.diaryEntryEmbedFactory = diaryEntryEmbedFactory;
    this.scrapedResultQueue = scrapedResultQueue;
    this.shardManagerStorage = shardManagerStorage;
    this.userWriter = userWriter;
    this.shardId = shardId;
    this.totalShards = totalShards;
  }

  private static Message.Entry getEntry(ScrapedResult scrapedResult) {
    return scrapedResult.getEntry();
  }

  @Override
  public void run() {
    ScrapedResult result = scrapedResultQueue.get(shardId, totalShards);
    if (result == null) {
      return;
    }

    // Extract information from the scraped result object
    Message message = result.message();
    User user = result.user();

    List<MessageEmbed> embedList = diaryEntryEmbedFactory.create(message, user);
    ShardManager shardManager = shardManagerStorage.get();
    JDA shard = (shardManager != null) ? shardManager.getShardById(shardId) : null;

    if (shard == null) {
      log.atError().setMessage("Unable to Load Shard").addKeyValue("shard", shardId).log();
      return;
    }

    for (String channelId : result.getChannelList()) {
      GuildMessageChannel channel = shard.getChannelById(GuildMessageChannel.class, channelId);

      // Not finding a channel is extremely normal when running shards so we ignore the
      // possible issue and don't log anything
      if (channel == null) {
        continue;
      }

      // Not having proper permissions is more normal than it should be so we ignore the
      // possible issue and don't log anything
      Member self = channel.getGuild().getSelfMember();
      if (!self.hasPermission(
          channel,
          Permission.VIEW_CHANNEL,
          Permission.MESSAGE_SEND,
          Permission.MESSAGE_EMBED_LINKS)) {
        continue;
      }

      try {
        channel
            .sendMessageEmbeds(embedList)
            .queue(m -> sendSuccess(m, result, channel), m -> sendFailure(message, channel));
      } catch (PermissionException e) {
        log.atWarn()
            .setMessage(
                "Attempting to send message from ScrapedResultChecker failed and exception caught")
            .setCause(e)
            .log();
      }
    }
  }

  private void sendSuccess(
      net.dv8tion.jda.api.entities.Message jdaMessage,
      ScrapedResult scrapedResult,
      GuildMessageChannel channel) {
    Message.Entry entry = getEntry(scrapedResult);

    // Log delay time between now and published time
    log.atInfo()
        .setMessage("Entry Publish Delay")
        .addKeyValue("delay", Instant.now().toEpochMilli() - entry.getPublishedDate())
        .addKeyValue("source", String.valueOf(entry.getPublishSource()))
        .log();

    // Log a too much information about the successfully sent message
    log.atInfo()
        .setMessage("Successfully Sent Message")
        .addKeyValue("channelId", channel.getId())
        .addKeyValue("message", scrapedResult.message())
        .addKeyValue("channel", channel)
        .addKeyValue("jdaMessage", jdaMessage)
        .log();

    List<String> previousList = scrapedResult.getPreviousList();
    boolean entryIsNew =
        previousList == null || previousList.isEmpty() || !previousList.contains(entry.lid);

    // If the entry is new write attempt to write it to the database
    if (entryIsNew) {
      boolean updateSuccess =
          userWriter.updateUserPrevious(entry.userLid, entry.lid, entry.publishedDate, entry.link);

      if (!updateSuccess) {
        log.atError()
            .setMessage("Entry did not Update")
            .addKeyValue("entry", getEntry(scrapedResult))
            .log();
      }
    }
  }

  private void sendFailure(Message message, GuildMessageChannel channel) {
    log.atWarn()
        .setMessage("Failed to Send Message")
        .addKeyValue("message", message)
        .addKeyValue("channel", channel)
        .log();
  }
}
