package jimlind.filmlinkd.discord.event.handler;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.discord.embed.factory.LoggedEmbedFactory;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.letterboxd.api.FilmApi;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesApi;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/** Handles the /logged command to show the most recent times a user has logged a film. */
@Singleton
public class LoggedHandler implements Handler {
  private final AccountHelper accountHelper;
  private final FilmApi filmApi;
  private final LogEntriesApi logEntriesApi;
  private final LoggedEmbedFactory loggedEmbedFactory;

  /**
   * Constructor for this class.
   *
   * @param accountHelper Handles translating account names to proper data models
   * @param filmApi Fetches film data from Letterboxd API
   * @param logEntriesApi Fetches user entry data from Letterboxd API
   * @param loggedEmbedFactory Builds the embed for the /logged command
   */
  @Inject
  LoggedHandler(
      AccountHelper accountHelper,
      FilmApi filmApi,
      LogEntriesApi logEntriesApi,
      LoggedEmbedFactory loggedEmbedFactory) {
    this.accountHelper = accountHelper;
    this.filmApi = filmApi;
    this.logEntriesApi = logEntriesApi;
    this.loggedEmbedFactory = loggedEmbedFactory;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LbMember member = accountHelper.getMember(event);

    OptionMapping filmNameMap = event.getInteraction().getOption("film-name");
    String filmAsString = filmNameMap != null ? filmNameMap.getAsString() : "";
    LbFilmSummary film = filmApi.search(filmAsString);

    if (member == null || film == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    List<LbLogEntry> logEntryList = logEntriesApi.getByUserAndFilm(member.id, film.id);
    if (logEntryList.isEmpty()) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    List<MessageEmbed> messageEmbedList = loggedEmbedFactory.create(logEntryList);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
