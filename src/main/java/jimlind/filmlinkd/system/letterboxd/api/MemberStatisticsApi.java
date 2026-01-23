package jimlind.filmlinkd.system.letterboxd.api;

import javax.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.model.LbMemberStatistics;

/**
 * Implements <a href="https://api-docs.letterboxd.com/#operation-GET-member_id_statistics">GET
 * /member/{id}/statistics</a>.
 */
public class MemberStatisticsApi {
  private final Client client;

  /**
   * Constructor for this class.
   *
   * @param client The Letterboxd API client that does authorization and object casting.
   */
  @Inject
  MemberStatisticsApi(Client client) {
    this.client = client;
  }

  /**
   * Fetches member statistics data from the Letterboxd API.
   *
   * @param userLid The Letterboxd user id
   * @return The response from the search API as {@link LbMemberStatistics}
   */
  public LbMemberStatistics fetch(String userLid) {
    if (userLid.isBlank()) {
      return null;
    }

    String memberDetailsPath = String.format("member/%s/statistics", userLid);

    return this.client.getAuthorized(memberDetailsPath, LbMemberStatistics.class);
  }
}
