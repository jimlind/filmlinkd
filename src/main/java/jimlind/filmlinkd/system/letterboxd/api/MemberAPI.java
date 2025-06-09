package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;

public class MemberAPI {
  private final Client client;

  @Inject
  MemberAPI(Client client) {
    this.client = client;
  }

  public LBMember fetch(String userLID) {
    if (userLID.isBlank()) {
      return null;
    }

    String memberDetailsPath = String.format("member/%s", userLID);

    return this.client.getAuthorized(memberDetailsPath, LBMember.class);
  }
}
