package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.model.LBListsResponse;

public class ListAPI {
  private final Client client;

  @Inject
  ListAPI(Client client) {
    this.client = client;
  }

  public LBListsResponse fetch(String userId, int count) {
    String uriTemplate = "lists/?member=%s&memberRelationship=%s&perPage=%s&where=%s";
    String path = String.format(uriTemplate, userId, "Owner", 50, "Published");

    LBListsResponse listsResponse = client.getAuthorized(path, LBListsResponse.class);
    if (listsResponse == null || listsResponse.items.isEmpty()) {
      return null;
    }

    return listsResponse;
  }
}
