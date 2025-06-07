package jimlind.filmlinkd.system.discord;

import com.google.inject.Inject;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SlashCommandManager {
  @Inject
  SlashCommandManager() {}

  public boolean process(SlashCommandInteractionEvent event) {
    event.reply("Nothing available to handle event request.").queue();
    return false;
  }
}

/*

import java.util.HashMap;
import java.util.Map;
import jimlind.filmlinkd.system.discord.eventHandler.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SlashCommandManager {
  private final ContributorHandler contributorHandler;
  private final DiaryHandler diaryHandler;
  private final FilmHandler filmHandler;
  private final FollowHandler followHandler;
  private final FollowingHandler followingHandler;
  private final HelpHandler helpHandler;
  private final ListHandler listHandler;
  private final LoggedHandler loggedHandler;
  private final RefreshHandler refreshHandler;
  private final RouletteHandler rouletteHandler;
  private final UnfollowHandler unfollowHandler;
  private final UserHandler userHandler;

  HashMap<String, Handler> handlerMap = new HashMap<>();

  @Autowired
  public SlashCommandManager(
      ContributorHandler contributorHandler,
      DiaryHandler diaryHandler,
      FilmHandler filmHandler,
      FollowHandler followHandler,
      FollowingHandler followingHandler,
      HelpHandler helpHandler,
      ListHandler listHandler,
      LoggedHandler loggedHandler,
      RefreshHandler refreshHandler,
      RouletteHandler rouletteHandler,
      UnfollowHandler unfollowHandler,
      UserHandler userHandler) {
    this.contributorHandler = contributorHandler;
    this.diaryHandler = diaryHandler;
    this.filmHandler = filmHandler;
    this.followHandler = followHandler;
    this.followingHandler = followingHandler;
    this.helpHandler = helpHandler;
    this.listHandler = listHandler;
    this.loggedHandler = loggedHandler;
    this.refreshHandler = refreshHandler;
    this.rouletteHandler = rouletteHandler;
    this.unfollowHandler = unfollowHandler;
    this.userHandler = userHandler;

    this.handlerMap = this.buildHandlerMap();
  }

  public boolean process(SlashCommandInteractionEvent event) {
    Handler handler = this.handlerMap.get(event.getName());
    if (handler == null) {
      event.reply("Nothing available to handle event request.").queue();
      return false;
    }
    handler.handleEvent(event);
    return true;
  }

  private HashMap<String, Handler> buildHandlerMap() {
    HashMap<String, Handler> handlerHashMap = new HashMap<>();

    this.putInMap(handlerHashMap, this.contributorHandler);
    this.putInMap(handlerHashMap, this.diaryHandler);
    this.putInMap(handlerHashMap, this.filmHandler);
    this.putInMap(handlerHashMap, this.followHandler);
    this.putInMap(handlerHashMap, this.followingHandler);
    this.putInMap(handlerHashMap, this.helpHandler);
    this.putInMap(handlerHashMap, this.listHandler);
    this.putInMap(handlerHashMap, this.loggedHandler);
    this.putInMap(handlerHashMap, this.refreshHandler);
    this.putInMap(handlerHashMap, this.rouletteHandler);
    this.putInMap(handlerHashMap, this.unfollowHandler);
    this.putInMap(handlerHashMap, this.userHandler);

    return handlerHashMap;
  }

  private void putInMap(Map<String, Handler> map, Handler handler) {
    map.put(handler.getEventName(), handler);
  }
}

 */
