package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * Implements the <a href="https://api-docs.letterboxd.com/#/schemas/LogEntry">LogEntry</a> schema
 * model.
 */
@Getter
public class LbLogEntry {
  public String id;
  public String name;
  public LbMemberSummary owner;
  public LbFilmSummary film;
  @Nullable public LbDiaryDetails diaryDetails;
  @Nullable public LbReview review;
  @Deprecated public List<String> tags;
  public List<LbTag> tags2;
  public String whenCreated;
  public String whenUpdated;
  public float rating;
  public boolean like;
  public boolean commentable;
  // commentPolicy - CommentPolicy
  public List<LbLink> links;
  @FirstParty public String posterPickerUrl;
  public LbImage backdrop;
  public float backdropFocalPoint;
  @FirstParty public List<String> targeting;
}
