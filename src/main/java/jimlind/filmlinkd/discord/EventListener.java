package jimlind.filmlinkd.discord;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.cache.CacheView;
import org.jetbrains.annotations.NotNull;

/** Custom listener for Discord events. On Ready and Slash Commands are needed here. */
@Slf4j
public class EventListener extends ListenerAdapter {
  private final SlashCommandManager slashCommandManager;

  @Inject
  EventListener(SlashCommandManager slashCommandManager) {
    this.slashCommandManager = slashCommandManager;
  }

  private static CacheView<Guild> extractGuildCache(JDA jda) {
    return jda.getGuildCache();
  }

  private static JDA extractJda(ReadyEvent event) {
    return event.getJDA();
  }

  private static ChannelType extractType(MessageChannelUnion channel) {
    return channel.getType();
  }

  @Override
  public void onReady(@NotNull ReadyEvent readyEvent) {
    JDA jda = extractJda(readyEvent);
    CacheView<Guild> guildCache = extractGuildCache(jda);
    if (log.isInfoEnabled()) {
      log.info("Discord Client Logged In on {} Servers", guildCache.size());
    }
  }

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
    if (extractType(event.getChannel()) == ChannelType.PRIVATE) {
      event.reply("Filmlinkd Does Not Support Direct Messages").queue();
      return;
    }

    boolean processSuccess = this.slashCommandManager.process(event);
    if (processSuccess) {
      log.atInfo()
          .setMessage("Successfully processed slash command.")
          .addKeyValue("event", event.getName())
          .log();
    } else {
      log.atInfo()
          .setMessage("Failed to process slash command.")
          .addKeyValue("event", event.getName())
          .log();
    }
  }
}
