package jimlind.filmlinkd.system.discord.eventHandler;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.system.discord.embedBuilder.LoggedEmbedBuilder;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.letterboxd.api.FilmApi;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesApi;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class LoggedHandler implements Handler {
  private final AccountHelper accountHelper;
  private final FilmApi filmApi;
  private final LogEntriesApi logEntriesApi;
  private final LoggedEmbedBuilder loggedEmbedBuilder;

  @Inject
  LoggedHandler(
      AccountHelper accountHelper,
      FilmApi filmApi,
      LogEntriesApi logEntriesApi,
      LoggedEmbedBuilder loggedEmbedBuilder) {
    this.accountHelper = accountHelper;
    this.filmApi = filmApi;
    this.logEntriesApi = logEntriesApi;
    this.loggedEmbedBuilder = loggedEmbedBuilder;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LBMember member = accountHelper.getMember(event);

    OptionMapping filmNameMap = event.getInteraction().getOption("film-name");
    String filmAsString = filmNameMap != null ? filmNameMap.getAsString() : "";
    LBFilmSummary film = filmApi.search(filmAsString);

    if (member == null || film == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    List<LBLogEntry> logEntryList = logEntriesApi.getByUserAndFilm(member.id, film.id);
    if (logEntryList.isEmpty()) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList =
        loggedEmbedBuilder.setLogEntryList(logEntryList).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
