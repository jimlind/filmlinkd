package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import jimlind.filmlinkd.model.CombinedLbFilmModel;
import jimlind.filmlinkd.system.letterboxd.model.LbFilm;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbSearchResponse;
import jimlind.filmlinkd.system.letterboxd.utils.UrlUtils;
import org.jetbrains.annotations.Nullable;

/** Implements a number of API clients used specifically to return film data. */
public class FilmApi {
  private final Client client;

  /**
   * Constructor for this class.
   *
   * @param client The Letterboxd API client that does authorization and object casting.
   */
  @Inject
  FilmApi(Client client) {
    this.client = client;
  }

  /**
   * Searches for film with the Letterboxd API. Only returns a small amount of information about the
   * film for showing a snippet not the full film information available. Uses the <a
   * href="https://api-docs.letterboxd.com/#operation-GET-search">GET /search</a> API endpoint.
   *
   * @param searchTerm Search terms to use to find the film
   * @return The response from the search API as {@link LbFilmSummary}
   */
  public LbFilmSummary search(String searchTerm) {
    // Search for the film by name
    String uriTemplate = "search/?input=%s&include=%s&perPage=%s&searchMethod=%s";
    String input = UrlUtils.encodePath(searchTerm);
    String searchPath = String.format(uriTemplate, input, "FilmSearchItem", 1, "Autocomplete");

    LbSearchResponse searchResponse = client.get(searchPath, LbSearchResponse.class);
    if (searchResponse == null || searchResponse.getItems().isEmpty()) {
      return null;
    }

    return searchResponse.getItems().getFirst().film;
  }

  /**
   * Searches for film with the Letterboxd API. Then loads up details and statistics about the film
   * to return a more complete data set of what is available.
   *
   * @param searchTerm Search terms to use to find the film
   * @return The merged response from several API calls as {@link CombinedLbFilmModel}
   */
  public CombinedLbFilmModel fetch(String searchTerm) {
    // Load film summary
    LbFilmSummary filmSummary = this.search(searchTerm);
    if (filmSummary == null) {
      return null;
    }

    LbFilm filmDetailsResponse = getFilmDetailsByLid(filmSummary.getId());
    if (filmDetailsResponse == null) {
      return null;
    }
    LbFilmStatistics filmStatisticsResponse = getLbFilmStatistics(filmSummary.getId());
    if (filmStatisticsResponse == null) {
      return null;
    }

    CombinedLbFilmModel combinedLbFilmModel = new CombinedLbFilmModel();
    combinedLbFilmModel.setFilm(filmDetailsResponse);
    combinedLbFilmModel.setFilmStatistics(filmStatisticsResponse);
    combinedLbFilmModel.setFilmSummary(filmSummary);

    return combinedLbFilmModel;
  }

  /**
   * Implements <a href="https://api-docs.letterboxd.com/#operation-GET-film_id_statistics">GET
   * /film/{id}/statistics</a> to get film details.
   *
   * @param lid The Letterboxd ID for a film
   * @return The response from the film statistics API as {@link LbFilmStatistics} or null if
   *     failure
   */
  @Nullable
  public LbFilmStatistics getLbFilmStatistics(String lid) {
    return this.client.getAuthorized("film/" + lid + "/statistics", LbFilmStatistics.class);
  }

  /**
   * Implements <a href="https://api-docs.letterboxd.com/#operation-GET-film_id">GET film/{id}</a>
   * to get film details.
   *
   * @param lid The Letterboxd ID for a film
   * @return The response from the film details API as {@link LbFilm} or null if failure
   */
  @Nullable
  public LbFilm getFilmDetailsByLid(String lid) {
    return this.client.getAuthorized("film/" + lid, LbFilm.class);
  }
}
