package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.model.LbSearchResponse;
import jimlind.filmlinkd.system.letterboxd.utils.UrlUtils;

public class ContributorApi {
  private final Client client;

  @Inject
  ContributorApi(Client client) {
    this.client = client;
  }

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
