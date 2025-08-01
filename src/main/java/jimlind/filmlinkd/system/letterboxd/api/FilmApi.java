package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import jimlind.filmlinkd.model.CombinedLbFilmModel;
import jimlind.filmlinkd.system.letterboxd.model.LbFilm;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbSearchResponse;
import jimlind.filmlinkd.system.letterboxd.utils.UrlUtils;

/**
 * Implements <a href="https://api-docs.letterboxd.com/#operation-GET-search">GET /search</a> for
 * films.
 */
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
   * film for showing a snipped not the full film information available.
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

    // Load film details
    String filmDetailsPath = String.format("film/%s", filmSummary.id);
    LbFilm filmDetailsResponse = this.client.getAuthorized(filmDetailsPath, LbFilm.class);
    if (filmDetailsResponse == null) {
      return null;
    }

    // Load film statistics
    String filmStatisticsPath = String.format("film/%s/statistics", filmSummary.id);
    LbFilmStatistics filmStatisticsResponse =
        this.client.getAuthorized(filmStatisticsPath, LbFilmStatistics.class);
    if (filmStatisticsResponse == null) {
      return null;
    }

    CombinedLbFilmModel combinedLbFilmModel = new CombinedLbFilmModel();
    combinedLbFilmModel.setFilm(filmDetailsResponse);
    combinedLbFilmModel.setFilmStatistics(filmStatisticsResponse);
    combinedLbFilmModel.setFilmSummary(filmSummary);

    return combinedLbFilmModel;
  }
}
