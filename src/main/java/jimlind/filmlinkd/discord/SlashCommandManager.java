package jimlind.filmlinkd.discord;

import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import jimlind.filmlinkd.discord.event.handler.Handler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/** Handles all slash commands. The process command here transfers logic to appropriate handler. */
public class SlashCommandManager {

  private final Map<String, Provider<Handler>> handlerMap;

  /**
   * Constructor for this class.
   *
   * @param handlerMap Maps Command Handlers to common strings
   */
  @Inject
  SlashCommandManager(Map<String, Provider<Handler>> handlerMap) {
    this.handlerMap = handlerMap;
  }

  /**
   * Process the event. The handler needed to process the event is found via the
   * getHandlerFromEventName method.
   *
   * @param event the event
   * @return the boolean
   */
  public boolean process(SlashCommandInteractionEvent event) {
    Handler handler = getHandlerFromEventName(event.getName());
    if (handler != null) {
      handler.handleEvent(event);
      return true;
    }
    event.reply("No command handler available.").queue();
    return false;
  }

  @Nullable
  private Handler getHandlerFromEventName(String name) {
    Provider<Handler> provider = handlerMap.get(name.toLowerCase(Locale.ROOT));
    return provider != null ? provider.get() : null;
  }
}
