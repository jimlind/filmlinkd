package jimlind.filmlinkd.system.letterboxd.api;

import javax.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;

/**
 * Implements <a href="https://api-docs.letterboxd.com/#operation-GET-member_id">GET
 * /member/{id}</a>.
 */
public class MemberApi {
  private final Client client;

  /**
   * Constructor for this class.
   *
   * @param client The Letterboxd API client that does authorization and object casting.
   */
  @Inject
  MemberApi(Client client) {
    this.client = client;
  }

  /**
   * Fetches member data from the Letterboxd API.
   *
   * @param userLid The Letterboxd user id
   * @return The response from the search API as {@link LbMember}
   */
  public LbMember fetch(String userLid) {
    if (userLid.isBlank()) {
      return null;
    }

    String memberDetailsPath = String.format("member/%s", userLid);

    return this.client.getAuthorized(memberDetailsPath, LbMember.class);
  }
}
