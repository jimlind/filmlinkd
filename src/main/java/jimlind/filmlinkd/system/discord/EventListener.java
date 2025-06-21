package jimlind.filmlinkd.system.discord;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class EventListener extends ListenerAdapter {
  private final SlashCommandManager slashCommandManager;

  @Inject
  EventListener(SlashCommandManager slashCommandManager) {
    this.slashCommandManager = slashCommandManager;
  }

  @Override
  public void onReady(ReadyEvent e) {
    JDA jda = e.getJDA();

    ShardManager manager = jda.getShardManager();
    if (manager == null) {
      throw new IllegalStateException("Problem Getting ShardManager");
    }

    if (log.isInfoEnabled()) {
      log.info("Discord Client Logged In on {} Servers", jda.getGuildCache().size());
    }
  }

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
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
