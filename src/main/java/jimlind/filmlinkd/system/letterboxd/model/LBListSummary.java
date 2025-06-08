package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

// https://api-docs.letterboxd.com/#/schemas/ListSummary
public class LBListSummary {
  public String id;
  public String name;
  public int version;
  public int filmCount;
  public boolean published;
  public boolean ranked;
  public String descriptionLbml;
  public boolean descriptionTruncated;
  public LBSharePolicy sharePolicy;
  public LBMemberSummary owner;
  public LBListIdentifier clonedFrom;
  public List<LBListEntrySummary> previewEntries;
  public List<LBListEntryOccurrence> entriesOfNote;
  public String description;
}
