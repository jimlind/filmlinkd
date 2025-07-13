package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

// https://api-docs.letterboxd.com/#/schemas/Member
public class LbMember {
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
  public String twitterUsername;
  public String bioLbml;
  public String location;
  public String website;
  public LbImage backdrop;
  public float backdropFocalPoint;
  public List<LbFilmSummary> favoriteFilms;
  public List<LbListSummary> pinnedFilmLists;
  public List<LbLink> links;
  public boolean privateWatchlist;
  public LbListSummary featuredList;
  public List<LbMemberSummary> teamMembers;
  public String orgType;
  public String bio;
}
