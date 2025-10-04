package jimlind.filmlinkd.discord;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.Locale;
import javax.annotation.Nullable;
import jimlind.filmlinkd.discord.event.handler.Handler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/** Handles all slash commands. The process command here transfers logic to appropriate handler. */
public class SlashCommandManager {

  private final Injector injector;

  /**
   * Constructor for this class.
   *
   * @param injector The Guice injector with all dependencies
   */
  @Inject
  SlashCommandManager(Injector injector) {
    this.injector = injector;
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

  private @Nullable Handler getHandlerFromEventName(String name) {
    String fragment =
        name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1).toLowerCase(Locale.ROOT);
    String className = String.format("jimlind.filmlinkd.discord.event.handler.%sHandler", fragment);

    try {
      Class<?> clazz = Class.forName(className);
      Object handler = injector.getInstance(clazz);
      if (handler instanceof Handler) {
        return (Handler) handler;
      } else {
        return null;
      }
    } catch (ClassNotFoundException e) {
      return null;
    }
  }
}
