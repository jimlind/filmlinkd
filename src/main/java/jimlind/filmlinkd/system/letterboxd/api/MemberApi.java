package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;

public class MemberApi {
  private final Client client;

  @Inject
  MemberApi(Client client) {
    this.client = client;
  }

  public LbMember fetch(String userLID) {
    if (userLID.isBlank()) {
      return null;
    }

    String memberDetailsPath = String.format("member/%s", userLID);

    return this.client.getAuthorized(memberDetailsPath, LbMember.class);
  }
}
