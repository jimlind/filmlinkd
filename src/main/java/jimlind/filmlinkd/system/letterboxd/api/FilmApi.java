package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import jimlind.filmlinkd.model.CombinedLBFilmModel;
import jimlind.filmlinkd.system.letterboxd.model.LBFilm;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LBSearchResponse;
import jimlind.filmlinkd.system.letterboxd.utils.UrlUtils;

public class FilmApi {

  private final Client client;

  @Inject
  FilmApi(Client client) {
    this.client = client;
  }

  public LBFilmSummary search(String searchTerm) {
    // Search for the film by name
    String uriTemplate = "search/?input=%s&include=%s&perPage=%s&searchMethod=%s";
    String input = UrlUtils.encodePath(searchTerm);
    String searchPath = String.format(uriTemplate, input, "FilmSearchItem", 1, "Autocomplete");

    LBSearchResponse searchResponse = client.get(searchPath, LBSearchResponse.class);
    if (searchResponse == null || searchResponse.items.isEmpty()) {
      return null;
    }

    return searchResponse.items.get(0).film;
  }

  public CombinedLBFilmModel fetch(String searchTerm) {
    // Load film summary
    LBFilmSummary filmSummary = this.search(searchTerm);
    if (filmSummary == null) {
      return null;
    }

    // Load film details
    String filmDetailsPath = String.format("film/%s", filmSummary.id);
    LBFilm filmDetailsResponse = this.client.getAuthorized(filmDetailsPath, LBFilm.class);
    if (filmDetailsResponse == null) {
      return null;
    }

    // Load film statistics
    String filmStatisticsPath = String.format("film/%s/statistics", filmSummary.id);
    LBFilmStatistics filmStatisticsResponse =
        this.client.getAuthorized(filmStatisticsPath, LBFilmStatistics.class);
    if (filmStatisticsResponse == null) {
      return null;
    }

    CombinedLBFilmModel combinedLBFilmModel = new CombinedLBFilmModel();
    combinedLBFilmModel.film = filmDetailsResponse;
    combinedLBFilmModel.filmStatistics = filmStatisticsResponse;
    combinedLBFilmModel.filmSummary = filmSummary;

    return combinedLBFilmModel;
  }
}
