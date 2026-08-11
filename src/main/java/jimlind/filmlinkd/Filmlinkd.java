package jimlind.filmlinkd;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class Filmlinkd extends ListenerAdapter {
  public static void main(String[] args) {
    String token = System.getenv("DISCORD_BOT_TOKEN");
    if (token == null || token.isBlank()) {
      log.error("No Token!");
      System.exit(1);
    }

    try {
      JDABuilder builder =
          JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES)
              .addEventListeners(new Filmlinkd());
      builder.build();
    } catch (Exception e) {
      log.error("Can't Build JDA Instance", e);
      System.exit(1);
    }
  }

  @Override
  public void onReady(@NotNull ReadyEvent event) {
    JDA jda = event.getJDA();
    jda.upsertCommand("ping", "I will pong you.").queue();
    log.info("Discord Client Logged In on {} Servers", jda.getGuildCache().size());
  }

  @Override
  public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
    if ("ping".equals(event.getName())) {
      event.reply("pong").queue();
    }
  }
}
