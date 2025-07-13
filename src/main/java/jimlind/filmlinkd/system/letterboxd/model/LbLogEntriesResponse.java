package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

// https://api-docs.letterboxd.com/#/schemas/LogEntriesResponse
public class LbLogEntriesResponse {
  public String next;
  public List<LbLogEntry> items;
  public int itemCount;
}
