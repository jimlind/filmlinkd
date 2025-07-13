package jimlind.filmlinkd.system.letterboxd.model;

// https://api-docs.letterboxd.com/#/schemas/AbstractSearchItem
public class LbAbstractSearchItem {
  public String type;
  public float score;
  public LbContributor contributor;
  public LbFilmSummary film;
  public LbListSummary list;
}
