package jimlind.filmlinkd.system.discord.eventHandler;

import com.google.inject.Inject;
import java.security.SecureRandom;
import java.util.ArrayList;
import jimlind.filmlinkd.model.CombinedLBFilmModel;
import jimlind.filmlinkd.system.discord.embedBuilder.FilmEmbedBuilder;
import jimlind.filmlinkd.system.letterboxd.api.FilmAPI;
import jimlind.filmlinkd.system.letterboxd.web.LetterboxdIdWeb;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class RouletteHandler implements Handler {
  private static final String CHARACTERS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

  private final FilmAPI filmAPI;
  private final FilmEmbedBuilder filmEmbedBuilder;
  private final LetterboxdIdWeb letterboxdIdWeb;

  @Inject
  RouletteHandler(
      FilmAPI filmAPI, FilmEmbedBuilder filmEmbedBuilder, LetterboxdIdWeb letterboxdIdWeb) {
    this.filmAPI = filmAPI;
    this.filmEmbedBuilder = filmEmbedBuilder;
    this.letterboxdIdWeb = letterboxdIdWeb;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();
    String filmString = findAFilm(getRandomLID(), 0);

    CombinedLBFilmModel combinedLBFilmModel = filmAPI.fetch(filmString);
    if (combinedLBFilmModel == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList =
        filmEmbedBuilder.setFilmCombination(combinedLBFilmModel).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }

  private String findAFilm(String filmId, int count) {
    // Send the users to this wierd movie that Letterboxd tries to default to
    if (filmId.length() < 2) {
      return "undefined";
    }

    String location = letterboxdIdWeb.getLocationFromLID(filmId);
    if (location.contains("/film/")) {
      return location.substring(location.lastIndexOf("/film/") + 6, location.lastIndexOf("/"));
    } else {
      return findAFilm(filmId.substring(0, filmId.length() - 1), ++count);
    }
  }

  private String getRandomLID() {
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
