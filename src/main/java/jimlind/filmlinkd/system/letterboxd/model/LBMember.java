package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

// https://api-docs.letterboxd.com/#/schemas/Member
public class LBMember {
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
  public String twitterUsername;
  public String bioLbml;
  public String location;
  public String website;
  public LBImage backdrop;
  public float backdropFocalPoint;
  public List<LBFilmSummary> favoriteFilms;
  public List<LBListSummary> pinnedFilmLists;
  public List<LBLink> links;
  public boolean privateWatchlist;
  public LBListSummary featuredList;
  public List<LBMemberSummary> teamMembers;
  public String orgType;
  public String bio;
}
