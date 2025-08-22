package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.inject.Inject;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.factory.MessageFactory;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.system.discord.embedbuilder.HelpEmbedBuilder;
import jimlind.filmlinkd.system.google.PubSubManager;
import jimlind.filmlinkd.system.google.firestore.UserReader;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesApi;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheView;
import org.jetbrains.annotations.Nullable;

/** Handles the /help command to show a help message and allow users to test the bot. */
@Slf4j
public class HelpHandler implements Handler {
  private static final int MAX_TEST_MESSAGES = 4;
  private final HelpEmbedBuilder helpEmbedBuilder;
  private final LogEntriesApi logEntriesApi;
  private final MessageFactory messageFactory;
  private final PubSubManager pubSubManager;
  private final UserReader userReader;

  /**
   * Constructor for this class.
   *
   * @param helpEmbedBuilder Builds the embed for the /help command
   * @param logEntriesApi Fetches log entry data from Letterboxd API
   * @param messageFactory Builds the message object that is pushed into the PubSub system
   * @param pubSubManager Handles the PubSub system to accept commands and messages
   * @param userReader Handles all read-only queries for user data from Firestore
   */
  @Inject
  HelpHandler(
      HelpEmbedBuilder helpEmbedBuilder,
      LogEntriesApi logEntriesApi,
      MessageFactory messageFactory,
      PubSubManager pubSubManager,
      UserReader userReader) {
    this.helpEmbedBuilder = helpEmbedBuilder;
    this.logEntriesApi = logEntriesApi;
    this.messageFactory = messageFactory;
    this.pubSubManager = pubSubManager;
    this.userReader = userReader;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping optionMapping = event.getInteraction().getOption("test");
    boolean testStatus = optionMapping != null && optionMapping.getAsBoolean();
    if (testStatus) {
      event.getHook().sendMessageEmbeds(helpEmbedBuilder.createTestMessage()).queue();
      this.queueAdditionalTestMessages(event.getMessageChannel());
      return;
    }

    JDA jda = extractJda(event);
    ShardManager shardManager = extractShardManager(jda);
    CacheView<Guild> instanceGuildCache = extractGuildCache(jda);
    CacheView<Guild> shardedGuildCache = extractGuildCache(shardManager);

    long userCount = userReader.getUserCount();
    CacheView<Guild> guild = shardedGuildCache != null ? shardedGuildCache : instanceGuildCache;
    List<MessageEmbed> messageEmbedList = helpEmbedBuilder.create(userCount, guild.size());

    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }

  private void queueAdditionalTestMessages(MessageChannel channel) {
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    final int[] count = {0};
    Runnable task =
        () -> {
          try {
            channel.sendMessageEmbeds(helpEmbedBuilder.createTestMessage(count[0])).queue();
          } catch (PermissionException e) {
            log.atInfo()
                .setMessage("Unable to send test message")
                .addKeyValue("channel", channel)
                .addKeyValue("exception", e)
                .log();
          }
          if (count[0] >= MAX_TEST_MESSAGES) {
            scheduler.shutdown();
            sendSingleDiaryEntry(channel.getId());
          }
          count[0]++;
        };
    scheduler.scheduleAtFixedRate(task, 2, 1, TimeUnit.SECONDS);
  }

  private void sendSingleDiaryEntry(String channelId) {
    LbLogEntry logEntry = this.logEntriesApi.getRecentForUser("1e4Ab", 1).getFirst();
    Message message = messageFactory.createFromLogEntry(logEntry, Message.PublishSource.Help);
    message.setChannelId(channelId);
    pubSubManager.publishLogEntry(message);
  }

  private CacheView<Guild> extractGuildCache(JDA jda) {
    return jda.getGuildCache();
  }

  @Nullable
  private CacheView<Guild> extractGuildCache(ShardManager shardManager) {
    if (shardManager != null) {
      return shardManager.getGuildCache();
    }
    return null;
  }

  private JDA extractJda(SlashCommandInteractionEvent event) {
    return event.getJDA();
  }

  @Nullable
  private ShardManager extractShardManager(JDA jda) {
    return jda.getShardManager();
  }
}
