package jimlind.filmlinkd.system.discord;

import com.google.inject.Inject;
import com.google.inject.Injector;
import javax.annotation.Nullable;
import jimlind.filmlinkd.system.discord.eventhandler.Handler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SlashCommandManager {

  private final Injector injector;

  @Inject
  SlashCommandManager(Injector injector) {
    this.injector = injector;
  }

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
    String fragment = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    String className =
        String.format("jimlind.filmlinkd.system.discord.eventhandler.%sHandler", fragment);

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
