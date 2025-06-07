package jimlind.filmlinkd.system.discord.eventHandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface Handler {
  String NO_RESULTS_FOUND = "No Results Found";
  String NO_CHANNEL_FOUND = "No Channel Found";

  void handleEvent(SlashCommandInteractionEvent event);
}
