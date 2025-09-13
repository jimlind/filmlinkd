package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.List;
import jimlind.filmlinkd.discord.dispatcher.HelpEmbedDispatcher;
import jimlind.filmlinkd.discord.embed.factory.HelpEmbedFactory;
import jimlind.filmlinkd.system.google.firestore.UserReader;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.cache.CacheView;
import org.jetbrains.annotations.Nullable;

/** Handles the /help command to show a help message and allow users to test the bot. */
@Slf4j
public class HelpHandler implements Handler {
  private final HelpEmbedFactory helpEmbedFactory;
  private final Injector injector;
  private final UserReader userReader;

  /**
   * Constructor for this class.
   *
   * @param helpEmbedFactory Builds the embed for the /help command
   * @param injector Guice injector allowing creation of new instances at will
   * @param userReader Handles all read-only queries for user data from Firestore
   */
  @Inject
  HelpHandler(HelpEmbedFactory helpEmbedFactory, Injector injector, UserReader userReader) {
    this.helpEmbedFactory = helpEmbedFactory;
    this.injector = injector;
    this.userReader = userReader;
  }

  private static ChannelType extractType(GuildMessageChannel channel) {
    return channel.getType();
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping optionMapping = event.getInteraction().getOption("test");
    boolean testStatus = optionMapping != null && optionMapping.getAsBoolean();
    if (testStatus) {
      event.getHook().sendMessageEmbeds(helpEmbedFactory.createTestMessage()).queue();

      HelpEmbedDispatcher helpEmbedDispatcher = injector.getInstance(HelpEmbedDispatcher.class);
      helpEmbedDispatcher.configure(event.getMessageChannel());
      helpEmbedDispatcher.start();

      return;
    }

    JDA jda = extractJda(event);
    ShardManager shardManager = extractShardManager(jda);
    CacheView<Guild> instanceGuildCache = extractGuildCache(jda);
    CacheView<Guild> shardedGuildCache = extractGuildCache(shardManager);

    long userCount = userReader.getUserCount();
    CacheView<Guild> guild = shardedGuildCache != null ? shardedGuildCache : instanceGuildCache;

    boolean viewChannelEnabled = false;
    boolean sendMessageEnabled = false;
    boolean embedLinkEnabled = false;

    if (event.getGuild() != null) {
      Member member = event.getGuild().getSelfMember();
      GuildMessageChannel channel = event.getChannel().asGuildMessageChannel();
      viewChannelEnabled = member.hasPermission(channel, Permission.VIEW_CHANNEL);
      sendMessageEnabled =
          extractType(channel).isThread()
              ? member.hasPermission(channel, Permission.MESSAGE_SEND_IN_THREADS)
              : member.hasPermission(channel, Permission.MESSAGE_SEND);
      embedLinkEnabled = member.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS);
    }
    List<MessageEmbed> messageEmbedList =
        helpEmbedFactory.create(
            userCount, guild.size(), viewChannelEnabled, sendMessageEnabled, embedLinkEnabled);

    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
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
