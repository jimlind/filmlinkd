package jimlind.filmlinkd.system.letterboxd.model;

import java.util.List;

/**
 * Implements the <a
 * href="https://api-docs.letterboxd.com/#/schemas/LogEntriesResponse">LogEntriesResponse</a> schema
 * model.
 */
public class LbLogEntriesResponse {
  public String next;
  public List<LbLogEntry> items;
  public int itemCount;
}
