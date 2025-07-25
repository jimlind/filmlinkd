package jimlind.filmlinkd.runnable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.model.ScrapedResult;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.ScrapedResultQueue;
import jimlind.filmlinkd.system.discord.ShardManagerStorage;
import jimlind.filmlinkd.system.discord.embedbuilder.DiaryEntryEmbedBuilder;
import jimlind.filmlinkd.system.google.FirestoreManager;
import jimlind.filmlinkd.system.letterboxd.utils.LidComparer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

/**
 * This is a scheduled task that checks the ScrapedResultQueue and posts the appropriate messages to
 * channels as needed. The ScrapedResultQueue exists because I couldn't figure out how to throttle
 * PubSub responses using the actual documented process. I probably could and this would become a
 * listener instead.
 */
@Slf4j
public class ScrapedResultChecker implements Runnable {
  private final DiaryEntryEmbedBuilder diaryEntryEmbedBuilder;
  private final FirestoreManager firestoreManager;
  private final ScrapedResultQueue scrapedResultQueue;
  private final ShardManagerStorage shardManagerStorage;

  private final int shardId;
  private final int totalShards;

  /**
   * Constructor for this class.
   *
   * @param diaryEntryEmbedBuilder A class that builds diaryEntryEmbed objects
   * @param firestoreManager A class that handles FireStore data interactions
   * @param scrapedResultQueue A class that stores results in local memory
   * @param shardManagerStorage A class that stores shard information
   * @param shardId The shard id to help us know which shard we are running this from
   * @param totalShards The total number of shards in use
   */
  public ScrapedResultChecker(
      DiaryEntryEmbedBuilder diaryEntryEmbedBuilder,
      FirestoreManager firestoreManager,
      ScrapedResultQueue scrapedResultQueue,
      ShardManagerStorage shardManagerStorage,
      int shardId,
      int totalShards) {
    this.diaryEntryEmbedBuilder = diaryEntryEmbedBuilder;
    this.firestoreManager = firestoreManager;
    this.scrapedResultQueue = scrapedResultQueue;
    this.shardManagerStorage = shardManagerStorage;
    this.shardId = shardId;
    this.totalShards = totalShards;
  }

  @Override
  public void run() {
    ScrapedResult result = scrapedResultQueue.get(shardId, totalShards);
    if (result == null) {
      return;
    }

    Message message = result.message;
    User user = result.user;

    List<MessageEmbed> embedList = diaryEntryEmbedBuilder.setMessage(message).setUser(user).build();

    JDA shard = shardManagerStorage.get().getShardById(shardId);
    if (shard == null) {
      log.atError().setMessage("Unable to Load Shard").addKeyValue("shard", shardId).log();
      return;
    }

    for (String channelId : getChannelListFromScrapeResult(result)) {
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

      channel
          .sendMessageEmbeds(embedList)
          .queue(m -> sendSuccess(m, result, channel), m -> sendFailure(message, channel));
    }
  }

  private void sendSuccess(
      net.dv8tion.jda.api.entities.Message jdaMessage,
      ScrapedResult scrapedResult,
      GuildMessageChannel channel) {
    Message.Entry entry = scrapedResult.getMessage().getEntry();

    // Log delay time between now and published time
    log.atInfo()
        .setMessage("Entry Publish Delay")
        .addKeyValue("delay", Instant.now().toEpochMilli() - entry.getPublishedDate())
        .addKeyValue("source", entry.getPublishSource().toString())
        .log();

    // Log a too much information about the successfully sent message
    log.atInfo()
        .setMessage("Successfully Sent Message")
        .addKeyValue("channelId", channel.getId())
        .addKeyValue("message", scrapedResult.getMessage())
        .addKeyValue("channel", channel)
        .addKeyValue("jdaMessage", jdaMessage)
        .log();

    List<String> previousList = scrapedResult.getUser().getPrevious().getList();
    boolean entryIsNew =
        previousList == null || previousList.isEmpty() || !previousList.contains(entry.lid);

    // If the entry is new write attempt to write it to the database
    if (entryIsNew) {
      boolean updateSuccess =
          firestoreManager.updateUserPrevious(
              entry.userLid, entry.lid, entry.publishedDate, entry.link);

      if (!updateSuccess) {
        log.atError()
            .setMessage("Entry did not Update")
            .addKeyValue("entry", scrapedResult.getMessage().getEntry())
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

  private List<String> getChannelListFromScrapeResult(ScrapedResult scrapedResult) {
    List<String> channelList = new ArrayList<>();
    Message message = scrapedResult.getMessage();
    String previous = scrapedResult.getUser().getMostRecentPrevious();
    boolean isNewerThanKnown =
        LidComparer.compare(previous, scrapedResult.getMessage().getEntry().getLid()) < 0;

    if (message.hasChannelOverride() && !isNewerThanKnown) {
      channelList.add(message.channelId);
      return channelList;
    }

    return scrapedResult.getUser().getChannelList();
  }
}
