package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;
import lombok.Getter;

/**
 * Implements the <a href="https://api-docs.letterboxd.com/#/schemas/ListSummary">ListSummary</a>
 * schema model.
 */
@Getter
public class LbListSummary {
  public String id;
  public String name;
  public int version;
  public int filmCount;
  public boolean published;
  public boolean ranked;
  public String descriptionLbml;
  public boolean descriptionTruncated;
  public LbSharePolicy sharePolicy;
  public LbMemberSummary owner;
  public LbListIdentifier clonedFrom;
  public List<LbListEntrySummary> previewEntries;
  public List<LbListEntryOccurrence> entriesOfNote;
  public String description;
}
