package jimlind.filmlinkd.system.discord.eventHandler;
/*
import static java.time.temporal.ChronoUnit.SECONDS;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import jimlind.filmlinkd.factory.messageEmbed.FilmEmbedFactory;
import jimlind.filmlinkd.model.CombinedLBFilmModel;
import jimlind.filmlinkd.system.letterboxd.api.FilmAPI;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RouletteHandler implements Handler {
  @Autowired FilmAPI filmAPI;
  @Autowired FilmEmbedFactory filmEmbedFactory;

  private static final String CHARACTERS =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

  public String getEventName() {
    return "roulette";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();
    String filmString = findAFilm(getRandomLID(), 0);

    CombinedLBFilmModel combinedLBFilmModel = this.filmAPI.fetch(filmString);
    if (combinedLBFilmModel == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList = this.filmEmbedFactory.create(combinedLBFilmModel);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }

  private static String findAFilm(String filmId, int count) {
    // Send the users to this wierd movie that Letterboxd tries to default to
    if (filmId.length() < 2) {
      return "undefined";
    }

    String uri = "https://boxd.it/" + filmId;

    try {
      HttpRequest request =
          HttpRequest.newBuilder().uri(new URI(uri)).timeout(Duration.of(9, SECONDS)).GET().build();
      HttpResponse<String> httpResponse =
          HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
      Optional<String> location = httpResponse.headers().firstValue("location");
      String locationString = location.toString();

      if (location.isPresent() && locationString.contains("/film/")) {
        return locationString.substring(
            locationString.lastIndexOf("/film/") + 6, locationString.lastIndexOf("/"));
      } else {
        return findAFilm(filmId.substring(0, filmId.length() - 1), ++count);
      }
    } catch (Exception e) {
      // Recurse even if there's a problem. It'll send the default for too short a string.
      return findAFilm("", ++count);
    }
  }

  private static String getRandomLID() {
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
*/
