package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.model.LbSearchResponse;
import jimlind.filmlinkd.system.letterboxd.utils.UrlUtils;

/**
 * Implements <a href="https://api-docs.letterboxd.com/#operation-GET-search">GET /search</a> for
 * contributors.
 */
public class ContributorApi {
  private final Client client;

  /**
   * Constructor for this class.
   *
   * @param client The Letterboxd API client that does authorization and object casting.
   */
  @Inject
  ContributorApi(Client client) {
    this.client = client;
  }

  /**
   * Fetches contributor data from the Letterboxd API.
   *
   * @param searchTerm Search terms to use to find the contributor
   * @return The response from the search API as {@link LbSearchResponse}
   */
  public LbSearchResponse fetch(String searchTerm) {
    String uriTemplate = "search/?input=%s&include=%s&perPage=%s";
    String input = UrlUtils.encodePath(searchTerm);
    String path = String.format(uriTemplate, input, "ContributorSearchItem", 1);

    LbSearchResponse searchResponse = this.client.get(path, LbSearchResponse.class);
    if (searchResponse == null || searchResponse.items.isEmpty()) {
      return null;
    }

    return searchResponse;
  }
}
