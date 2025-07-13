package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.model.LbMemberStatistics;

public class MemberStatisticsApi {
  private final Client client;

  @Inject
  MemberStatisticsApi(Client client) {
    this.client = client;
  }

  public LbMemberStatistics fetch(String userLID) {
    if (userLID.isBlank()) {
      return null;
    }

    String memberDetailsPath = String.format("member/%s/statistics", userLID);

    return this.client.getAuthorized(memberDetailsPath, LbMemberStatistics.class);
  }
}
