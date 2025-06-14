package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import java.util.List;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntriesResponse;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;

public class LogEntriesAPI {
  private final Client client;

  @Inject
  LogEntriesAPI(Client client) {
    this.client = client;
  }

  public List<LBLogEntry> getRecentForUser(String userId, int count) {
    String uriTemplate = "log-entries/?member=%s&memberRelationship=%s&perPage=%s";
    String logEntriesPath = String.format(uriTemplate, userId, "Owner", count);

    LBLogEntriesResponse logEntriesResponse =
        this.client.getAuthorized(logEntriesPath, LBLogEntriesResponse.class);

    if (logEntriesResponse == null || logEntriesResponse.items.isEmpty()) {
      return List.of();
    }

    return logEntriesResponse.items;
  }

  public List<LBLogEntry> getByUserAndFilm(String userId, String filmId) {
    String uriTemplate = "log-entries/?member=%s&film=%s&memberRelationship=%s&perPage=%s";
    String logEntriesPath = String.format(uriTemplate, userId, filmId, "Owner", 5);

    LBLogEntriesResponse logEntriesResponse =
        this.client.getAuthorized(logEntriesPath, LBLogEntriesResponse.class);

    if (logEntriesResponse == null || logEntriesResponse.items.isEmpty()) {
      return List.of();
    }

    return logEntriesResponse.items;
  }
}
