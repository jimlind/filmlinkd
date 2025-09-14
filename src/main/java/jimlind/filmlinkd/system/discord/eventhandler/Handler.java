package jimlind.filmlinkd.system.discord.eventhandler;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/** Interface defining how a class should handle a slash command. */
@FunctionalInterface
public interface Handler {
  String NO_RESULTS_FOUND = "No Results Found";
  String NO_CHANNEL_FOUND = "No Channel Found";

  /**
   * A user issues a slash command, and we need to do something with it. The definition is pretty
   * broad about what we should do with it so the rest is left up to the implementation of the
   * actual class.
   *
   * @param event The slash event that the user created in Discord that triggered this handler
   */
  void handleEvent(SlashCommandInteractionEvent event);
}
