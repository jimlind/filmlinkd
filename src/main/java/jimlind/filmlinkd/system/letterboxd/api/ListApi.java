package jimlind.filmlinkd.system.letterboxd.api;

import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.system.letterboxd.model.LbListsResponse;

/** Implements <a href="https://api-docs.letterboxd.com/#operation-GET-lists">GET /lists</a>. */
@Singleton
public class ListApi {
  private final Client client;

  /**
   * Constructor for this class.
   *
   * @param client The Letterboxd API client that does authorization and object casting.
   */
  @Inject
  ListApi(Client client) {
    this.client = client;
  }

  /**
   * Fetches lists data from the Letterboxd API.
   *
   * @param userId The Letterboxd user id
   * @param count The number of lists to return
   * @return The response from the lists API as {@link LbListsResponse}
   */
  public LbListsResponse fetch(String userId, int count) {
    String uriTemplate = "lists/?member=%s&memberRelationship=%s&perPage=%s&where=%s";
    String path = String.format(uriTemplate, userId, "Owner", count, "Published");

    LbListsResponse listsResponse = client.getAuthorized(path, LbListsResponse.class);
    if (listsResponse == null || listsResponse.getItems().isEmpty()) {
      return null;
    }

    return listsResponse;
  }
}
