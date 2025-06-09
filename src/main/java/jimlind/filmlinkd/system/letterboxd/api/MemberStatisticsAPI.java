package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.model.LBMemberStatistics;

public class MemberStatisticsAPI {
  private final Client client;

  @Inject
  MemberStatisticsAPI(Client client) {
    this.client = client;
  }

  public LBMemberStatistics fetch(String userLID) {
    if (userLID.isBlank()) {
      return null;
    }

    String memberDetailsPath = String.format("member/%s/statistics", userLID);

    return this.client.getAuthorized(memberDetailsPath, LBMemberStatistics.class);
  }
}
