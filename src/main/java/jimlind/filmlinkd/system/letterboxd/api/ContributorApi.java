package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.model.LBSearchResponse;
import jimlind.filmlinkd.system.letterboxd.utils.UrlUtils;

public class ContributorApi {
  private final Client client;

  @Inject
  ContributorApi(Client client) {
    this.client = client;
  }

  public LBSearchResponse fetch(String searchTerm) {
    String uriTemplate = "search/?input=%s&include=%s&perPage=%s";
    String input = UrlUtils.encodePath(searchTerm);
    String path = String.format(uriTemplate, input, "ContributorSearchItem", 1);

    LBSearchResponse searchResponse = this.client.get(path, LBSearchResponse.class);
    if (searchResponse == null || searchResponse.items.isEmpty()) {
      return null;
    }

    return searchResponse;
  }
}
