package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.inject.Inject;
import java.util.List;
import jimlind.filmlinkd.discord.factory.FilmEmbedFactory;
import jimlind.filmlinkd.model.CombinedLbFilmModel;
import jimlind.filmlinkd.system.letterboxd.api.FilmApi;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/** Handles the /film command to show a film matching the search input. */
public class FilmHandler implements Handler {

  private final FilmApi filmApi;
  private final FilmEmbedFactory filmEmbedFactory;

  /**
   * Constructor for this class.
   *
   * @param filmApi Fetches film data from Letterboxd API
   * @param filmEmbedFactory Builds the embed for the /film command
   */
  @Inject
  FilmHandler(FilmApi filmApi, FilmEmbedFactory filmEmbedFactory) {
    this.filmApi = filmApi;
    this.filmEmbedFactory = filmEmbedFactory;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping optionMapping = event.getInteraction().getOption("film-name");
    String filmName = optionMapping != null ? optionMapping.getAsString() : "";
    CombinedLbFilmModel combinedLbFilmModel = filmApi.fetch(filmName);

    if (combinedLbFilmModel == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    List<MessageEmbed> messageEmbedList =
        filmEmbedFactory.setFilmCombination(combinedLbFilmModel).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
