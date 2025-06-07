package jimlind.filmlinkd.system.discord.eventHandler;
/*
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.factory.messageEmbed.LoggedEmbedFactory;
import jimlind.filmlinkd.system.discord.AccountHelper;
import jimlind.filmlinkd.system.letterboxd.api.FilmAPI;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoggedHandler implements Handler {
  @Autowired private AccountHelper accountHelper;
  @Autowired private FilmAPI filmAPI;
  @Autowired private LogEntriesAPI logEntriesAPI;
  @Autowired private LoggedEmbedFactory loggedEmbedFactory;

  public String getEventName() {
    return "logged";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LBMember member = this.accountHelper.getMember(event);

    OptionMapping filmNameMap = event.getInteraction().getOption("film-name");
    String filmAsString = filmNameMap != null ? filmNameMap.getAsString() : "";
    LBFilmSummary film = this.filmAPI.search(filmAsString);

    if (member == null || film == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    List<LBLogEntry> logEntryList = this.logEntriesAPI.getByUserAndFilm(member.id, film.id);
    if (logEntryList.isEmpty()) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList = this.loggedEmbedFactory.create(logEntryList);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
*/
