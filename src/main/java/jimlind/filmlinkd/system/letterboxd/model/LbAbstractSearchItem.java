package jimlind.filmlinkd.system.letterboxd.model;

/**
 * Implements the <a
 * href="https://api-docs.letterboxd.com/#/schemas/AbstractSearchItem">AbstractSearchItem</a> schema
 * model.
 */
public class LbAbstractSearchItem {
  public String type;
  public float score;
  public LbContributor contributor;
  public LbFilmSummary film;
  public LbListSummary list;
}
