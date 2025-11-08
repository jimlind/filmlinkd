package jimlind.filmlinkd.discord.event.handler;

import com.google.inject.Inject;
import java.util.List;
import java.util.Random;
import jimlind.filmlinkd.discord.embed.factory.FilmEmbedFactory;
import jimlind.filmlinkd.model.CombinedLbFilmModel;
import jimlind.filmlinkd.system.letterboxd.api.FilmApi;
import jimlind.filmlinkd.system.letterboxd.api.FilmsApi;
import jimlind.filmlinkd.system.letterboxd.model.LbFilm;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmsResponse;
import jimlind.filmlinkd.themoviedb.MovieApi;
import jimlind.filmlinkd.themoviedb.model.MovieLatest;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/** Handles the /roulette command to show a random film. */
public class RouletteHandler implements Handler {
  private final FilmApi filmApi;
  private final FilmsApi filmsApi;
  private final FilmEmbedFactory filmEmbedFactory;
  private final MovieApi movieApi;

  /**
   * Constructor for this class.
   *
   * @param filmApi Fetches film data from Letterboxd API
   * @param filmsApi Fetches a collection of film data from Letterboxd API
   * @param filmEmbedFactory Builds the embed for the /filmEmbed command
   * @param movieApi Fetches movie data from TheMovieDatabase API
   */
  @Inject
  RouletteHandler(
      FilmApi filmApi, FilmsApi filmsApi, FilmEmbedFactory filmEmbedFactory, MovieApi movieApi) {
    this.filmApi = filmApi;
    this.filmsApi = filmsApi;
    this.filmEmbedFactory = filmEmbedFactory;
    this.movieApi = movieApi;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    MovieLatest response = this.movieApi.getLatest();
    if (response == null) {
      sendUndefined(event);
      return;
    }

    // Get 6 random numbers in the range of TMDB movie ids and fetch the films from Letterboxd
    List<Integer> tmdbIdList = new Random().ints(6, 2, response.getId() + 1).boxed().toList();
    LbFilmsResponse filmsResponse = filmsApi.fetch(tmdbIdList);
    if (filmsResponse == null || filmsResponse.items.isEmpty()) {
      sendUndefined(event);
      return;
    }

    LbFilmSummary filmSummary = filmsResponse.items.getFirst();
    LbFilm filmDetailsResponse = filmApi.getFilmDetailsByLid(filmSummary.getId());
    LbFilmStatistics filmStatisticsResponse = filmApi.getLbFilmStatistics(filmSummary.getId());
    if (filmDetailsResponse == null || filmStatisticsResponse == null) {
      sendUndefined(event);
      return;
    }

    CombinedLbFilmModel combinedLbFilmModel = new CombinedLbFilmModel();
    combinedLbFilmModel.setFilm(filmDetailsResponse);
    combinedLbFilmModel.setFilmStatistics(filmStatisticsResponse);
    combinedLbFilmModel.setFilmSummary(filmSummary);

    sendCombinedFilmMessage(event, combinedLbFilmModel);
  }

  private void sendUndefined(SlashCommandInteractionEvent event) {
    sendCombinedFilmMessage(event, filmApi.fetch("undefined"));
  }

  private void sendCombinedFilmMessage(
      SlashCommandInteractionEvent event, CombinedLbFilmModel combinedLbFilmModel) {
    List<MessageEmbed> messageEmbedList = filmEmbedFactory.create(combinedLbFilmModel);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
