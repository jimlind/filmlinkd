package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.inject.Inject;
import java.util.ArrayList;
import jimlind.filmlinkd.model.CombinedLbFilmModel;
import jimlind.filmlinkd.system.discord.embedbuilder.FilmEmbedBuilder;
import jimlind.filmlinkd.system.letterboxd.api.FilmApi;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/** Handles the /film command to show a film matching the search input. */
public class FilmHandler implements Handler {

  private final FilmApi filmApi;
  private final FilmEmbedBuilder filmEmbedBuilder;

  /**
   * Constructor for this class.
   *
   * @param filmApi Fetches film data from Letterboxd API
   * @param filmEmbedBuilder Builds the embed for the /film command
   */
  @Inject
  FilmHandler(FilmApi filmApi, FilmEmbedBuilder filmEmbedBuilder) {
    this.filmApi = filmApi;
    this.filmEmbedBuilder = filmEmbedBuilder;
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

    ArrayList<MessageEmbed> messageEmbedList =
        filmEmbedBuilder.setFilmCombination(combinedLbFilmModel).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
