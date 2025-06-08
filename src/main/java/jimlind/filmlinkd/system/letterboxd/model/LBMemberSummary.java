package jimlind.filmlinkd.system.letterboxd.model;

// https://api-docs.letterboxd.com/#/schemas/MemberSummary
public class LBMemberSummary {
  public String id;
  public String username;
  public String givenName;
  public String familyName;
  public String displayName;
  public String shortName;
  public LBPronoun pronoun;
  public LBImage avatar;
  public LBMemberStatus memberStatus;
  public boolean hideAdsInContent;
  // @Deprecated commentPolicy - CommentPolicy
  // @Deprecated accountStatus - AccountStatus
  @Deprecated public boolean hideAds;
}
