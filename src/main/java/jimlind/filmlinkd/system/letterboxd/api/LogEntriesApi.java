package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import java.util.List;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntriesResponse;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;

public class LogEntriesApi {
  private final Client client;

  @Inject
  LogEntriesApi(Client client) {
    this.client = client;
  }

  public List<LbLogEntry> getRecentForUser(String userId, int count) {
    String uriTemplate = "log-entries/?member=%s&memberRelationship=%s&perPage=%s";
    String logEntriesPath = String.format(uriTemplate, userId, "Owner", count);

    LbLogEntriesResponse logEntriesResponse =
        this.client.getAuthorized(logEntriesPath, LbLogEntriesResponse.class);

    if (logEntriesResponse == null || logEntriesResponse.items.isEmpty()) {
      return List.of();
    }

    return logEntriesResponse.items;
  }

  public List<LbLogEntry> getByUserAndFilm(String userId, String filmId) {
    String uriTemplate = "log-entries/?member=%s&film=%s&memberRelationship=%s&perPage=%s";
    String logEntriesPath = String.format(uriTemplate, userId, filmId, "Owner", 5);

    LbLogEntriesResponse logEntriesResponse =
        this.client.getAuthorized(logEntriesPath, LbLogEntriesResponse.class);

    if (logEntriesResponse == null || logEntriesResponse.items.isEmpty()) {
      return List.of();
    }

    return logEntriesResponse.items;
  }
}
