package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.model.LbListsResponse;

public class ListApi {
  private final Client client;

  @Inject
  ListApi(Client client) {
    this.client = client;
  }

  public LbListsResponse fetch(String userId, int count) {
    String uriTemplate = "lists/?member=%s&memberRelationship=%s&perPage=%s&where=%s";
    String path = String.format(uriTemplate, userId, "Owner", 50, "Published");

    LbListsResponse listsResponse = client.getAuthorized(path, LbListsResponse.class);
    if (listsResponse == null || listsResponse.items.isEmpty()) {
      return null;
    }

    return listsResponse;
  }
}
