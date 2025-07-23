package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.inject.Inject;
import java.security.SecureRandom;
import java.util.ArrayList;
import jimlind.filmlinkd.model.CombinedLbFilmModel;
import jimlind.filmlinkd.system.discord.embedbuilder.FilmEmbedBuilder;
import jimlind.filmlinkd.system.letterboxd.api.FilmApi;
import jimlind.filmlinkd.system.letterboxd.web.LetterboxdIdWeb;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/** Handles the /roulette command to show a random film. */
public class RouletteHandler implements Handler {
  private static final String CHARACTERS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

  private final FilmApi filmApi;
  private final FilmEmbedBuilder filmEmbedBuilder;
  private final LetterboxdIdWeb letterboxdIdWeb;

  /**
   * Constructor for this class.
   *
   * @param filmApi Fetches film data from Letterboxd API
   * @param filmEmbedBuilder Builds the embed for the /filmEmbed command
   * @param letterboxdIdWeb Class that translates a Letterboxd id to another piece of data
   */
  @Inject
  RouletteHandler(
      FilmApi filmApi, FilmEmbedBuilder filmEmbedBuilder, LetterboxdIdWeb letterboxdIdWeb) {
    this.filmApi = filmApi;
    this.filmEmbedBuilder = filmEmbedBuilder;
    this.letterboxdIdWeb = letterboxdIdWeb;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();
    String filmString = findOneFilm(getRandomLid(), 0);

    CombinedLbFilmModel combinedLbFilmModel = filmApi.fetch(filmString);
    if (combinedLbFilmModel == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList =
        filmEmbedBuilder.setFilmCombination(combinedLbFilmModel).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }

  private String findOneFilm(String filmId, int count) {
    // Send the users to this wierd movie that Letterboxd tries to default to
    if (filmId.length() < 2) {
      return "undefined";
    }

    String location = letterboxdIdWeb.getLocationFromLid(filmId);
    if (location.contains("/film/")) {
      return location.substring(location.lastIndexOf("/film/") + 6, location.lastIndexOf("/"));
    } else {
      return findOneFilm(filmId.substring(0, filmId.length() - 1), count + 1);
    }
  }

  private String getRandomLid() {
    SecureRandom random = new SecureRandom();
    StringBuilder stringBuilder = new StringBuilder();

    for (int i = 0; i < 7; i++) {
      int randomIndex = random.nextInt(CHARACTERS.length());
      char randomChar = CHARACTERS.charAt(randomIndex);
      stringBuilder.append(randomChar);
    }

    return stringBuilder.toString();
  }
}
