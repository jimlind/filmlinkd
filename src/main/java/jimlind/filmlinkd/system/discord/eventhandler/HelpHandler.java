package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.inject.Inject;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.system.discord.embedbuilder.HelpEmbedBuilder;
import jimlind.filmlinkd.system.google.FirestoreManager;
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

/** Handles the /help command to show a help message and allow users to test the bot. */
@Slf4j
public class HelpHandler implements Handler {
  private static final int MAX_TEST_MESSAGES = 4;
  private final FirestoreManager firestoreManager;
  private final HelpEmbedBuilder helpEmbedBuilder;

  /**
   * Constructor for this class.
   *
   * @param firestoreManager Service that handles all Firestore interactions
   * @param helpEmbedBuilder Builds the embed for the /help command
   */
  @Inject
  HelpHandler(FirestoreManager firestoreManager, HelpEmbedBuilder helpEmbedBuilder) {
    this.firestoreManager = firestoreManager;
    this.helpEmbedBuilder = helpEmbedBuilder;
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
    CacheView<Guild> instanceGuildeCache = extractGuildCache(jda);
    CacheView<Guild> shardedGuildeCache = extractGuildCache(shardManager);

    long userCount = firestoreManager.getUserCount();
    long guildCount = shardManager != null ? shardedGuildeCache.size() : instanceGuildeCache.size();
    List<MessageEmbed> messageEmbedList = helpEmbedBuilder.create(userCount, guildCount);

    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }

  private void queueAdditionalTestMessages(MessageChannel channel) {
    try (ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)) {
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
            }
            count[0]++;
          };
      scheduler.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);
    }
  }

  private CacheView<Guild> extractGuildCache(JDA jda) {
    return jda.getGuildCache();
  }

  private CacheView<Guild> extractGuildCache(ShardManager shardManager) {
    return shardManager.getGuildCache();
  }

  private JDA extractJda(SlashCommandInteractionEvent event) {
    return event.getJDA();
  }

  private ShardManager extractShardManager(JDA jda) {
    return jda.getShardManager();
  }
}
