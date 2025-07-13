package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import jimlind.filmlinkd.model.CombinedLbFilmModel;
import jimlind.filmlinkd.system.letterboxd.model.LbFilm;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbSearchResponse;
import jimlind.filmlinkd.system.letterboxd.utils.UrlUtils;

public class FilmApi {

  private final Client client;

  @Inject
  FilmApi(Client client) {
    this.client = client;
  }

  public LbFilmSummary search(String searchTerm) {
    // Search for the film by name
    String uriTemplate = "search/?input=%s&include=%s&perPage=%s&searchMethod=%s";
    String input = UrlUtils.encodePath(searchTerm);
    String searchPath = String.format(uriTemplate, input, "FilmSearchItem", 1, "Autocomplete");

    LbSearchResponse searchResponse = client.get(searchPath, LbSearchResponse.class);
    if (searchResponse == null || searchResponse.items.isEmpty()) {
      return null;
    }

    return searchResponse.items.get(0).film;
  }

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

    CombinedLbFilmModel combinedLBFilmModel = new CombinedLbFilmModel();
    combinedLBFilmModel.film = filmDetailsResponse;
    combinedLBFilmModel.filmStatistics = filmStatisticsResponse;
    combinedLBFilmModel.filmSummary = filmSummary;

    return combinedLBFilmModel;
  }
}
