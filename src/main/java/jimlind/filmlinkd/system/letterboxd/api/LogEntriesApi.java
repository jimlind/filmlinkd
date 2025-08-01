package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import java.util.List;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntriesResponse;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;

/**
 * Implements <a href="https://api-docs.letterboxd.com/#operation-GET-log-entries">GET
 * /log-entries</a>.
 */
public class LogEntriesApi {
  private final Client client;

  /**
   * Constructor for this class.
   *
   * @param client The Letterboxd API client that does authorization and object casting.
   */
  @Inject
  LogEntriesApi(Client client) {
    this.client = client;
  }

  /**
   * Gets the most recent films logged by a user.
   *
   * @param userId The Letterboxd user id
   * @param count The number of recent films to be returned
   * @return The response from the log-entries API as a list of {@link LbLogEntry}
   */
  public List<LbLogEntry> getRecentForUser(String userId, int count) {
    String uriTemplate = "log-entries/?member=%s&memberRelationship=%s&perPage=%s";
    String logEntriesPath = String.format(uriTemplate, userId, "Owner", count);

    LbLogEntriesResponse logEntriesResponse =
        this.client.getAuthorized(logEntriesPath, LbLogEntriesResponse.class);

    if (logEntriesResponse == null || logEntriesResponse.getItems().isEmpty()) {
      return List.of();
    }

    return logEntriesResponse.items;
  }

  /**
   * Get the most recent 5 times that the user has logged the film.
   *
   * @param userId The Letterboxd user id
   * @param filmId The Letterboxd film id
   * @return The response from the log-entries API as list of {@link LbLogEntry}
   */
  public List<LbLogEntry> getByUserAndFilm(String userId, String filmId) {
    String uriTemplate = "log-entries/?member=%s&film=%s&memberRelationship=%s&perPage=%s";
    String logEntriesPath = String.format(uriTemplate, userId, filmId, "Owner", 5);

    LbLogEntriesResponse logEntriesResponse =
        this.client.getAuthorized(logEntriesPath, LbLogEntriesResponse.class);

    if (logEntriesResponse == null || logEntriesResponse.getItems().isEmpty()) {
      return List.of();
    }

    return logEntriesResponse.items;
  }
}
