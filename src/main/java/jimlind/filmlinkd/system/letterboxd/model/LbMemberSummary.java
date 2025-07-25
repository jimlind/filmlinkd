package jimlind.filmlinkd.system.letterboxd.model;

/**
 * Implements the <a
 * href="https://api-docs.letterboxd.com/#/schemas/MemberSummary">MemberSummary</a> schema model.
 */
public class LbMemberSummary {
  public String id;
  public String username;
  public String givenName;
  public String familyName;
  public String displayName;
  public String shortName;
  public LbPronoun pronoun;
  public LbImage avatar;
  public LbMemberStatus memberStatus;
  public boolean hideAdsInContent;
  // @Deprecated commentPolicy - CommentPolicy
  // @Deprecated accountStatus - AccountStatus
  @Deprecated public boolean hideAds;
}
