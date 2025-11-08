package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;
import lombok.Getter;

@Getter
public class LbFilmsResponse {
  public String next;
  public List<LbFilmSummary> items;
  public int itemCount;
}
