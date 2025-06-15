package jimlind.filmlinkd.system.discord.eventHandler;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.system.discord.AccountHelper;
import jimlind.filmlinkd.system.discord.embedBuilder.LoggedEmbedBuilder;
import jimlind.filmlinkd.system.letterboxd.api.FilmAPI;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class LoggedHandler implements Handler {
  private final AccountHelper accountHelper;
  private final FilmAPI filmAPI;
  private final LogEntriesAPI logEntriesAPI;
  private final LoggedEmbedBuilder loggedEmbedBuilder;

  @Inject
  LoggedHandler(
      AccountHelper accountHelper,
      FilmAPI filmAPI,
      LogEntriesAPI logEntriesAPI,
      LoggedEmbedBuilder loggedEmbedBuilder) {
    this.accountHelper = accountHelper;
    this.filmAPI = filmAPI;
    this.logEntriesAPI = logEntriesAPI;
    this.loggedEmbedBuilder = loggedEmbedBuilder;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LBMember member = accountHelper.getMember(event);

    OptionMapping filmNameMap = event.getInteraction().getOption("film-name");
    String filmAsString = filmNameMap != null ? filmNameMap.getAsString() : "";
    LBFilmSummary film = filmAPI.search(filmAsString);

    if (member == null || film == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    List<LBLogEntry> logEntryList = logEntriesAPI.getByUserAndFilm(member.id, film.id);
    if (logEntryList.isEmpty()) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList = loggedEmbedBuilder.build(logEntryList);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
