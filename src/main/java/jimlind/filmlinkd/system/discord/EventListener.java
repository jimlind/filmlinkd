package jimlind.filmlinkd.system.discord;

import com.google.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.factory.ScrapedResultCheckerFactory;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

/** Custom listener for Discord events. On Ready and Slash Commands are needed here. */
@Slf4j
public class EventListener extends ListenerAdapter {
  private final ScrapedResultCheckerFactory scrapedResultCheckerFactory;
  private final SlashCommandManager slashCommandManager;

  @Inject
  EventListener(
      ScrapedResultCheckerFactory scrapedResultCheckerFactory,
      SlashCommandManager slashCommandManager) {
    this.scrapedResultCheckerFactory = scrapedResultCheckerFactory;
    this.slashCommandManager = slashCommandManager;
  }

  @Override
  public void onReady(ReadyEvent readyEvent) {
    JDA jda = readyEvent.getJDA();

    ShardManager manager = jda.getShardManager();
    if (manager == null) {
      throw new IllegalStateException("Problem Getting ShardManager");
    }

    if (log.isInfoEnabled()) {
      log.info("Discord Client Logged In on {} Servers", jda.getGuildCache().size());
    }

    int shardId = jda.getShardInfo().getShardId();
    try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {
      scheduler.scheduleAtFixedRate(
          scrapedResultCheckerFactory.create(shardId, manager.getShardsTotal()),
          0,
          1,
          TimeUnit.SECONDS);
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
