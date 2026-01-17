package jimlind.filmlinkd.system.letterboxd.api;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntriesResponse;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements <a href="https://api-docs.letterboxd.com/#operation-GET-log-entries">GET
 * /log-entries</a>.
 */
@Singleton
@Slf4j
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

  private static String getString(String input) {
    Pattern pattern = Pattern.compile("\"type\":\"FilmLogEntry\",\"id\":\"(\\w+)\"");
    Matcher matcher = pattern.matcher(input);

    if (matcher.find()) {
      return matcher.group(1);
    } else {
      return "";
    }
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
   * Gets the id for the most recent diary entry for a user. Uses a stream to ensure it only
   * processes the smallest amount of information as possible.
   *
   * @param userId The Letterboxd user id
   * @return The Letterboxd ID or an empty string if nothing found
   */
  public String getMostRecentEntryLetterboxdId(String userId) {
    String uriTemplate = "log-entries/?member=%s&memberRelationship=%s&perPage=1";
    String logEntriesPath = String.format(uriTemplate, userId, "Owner");
    return this.client.handleAuthorizedStream(
        logEntriesPath,
        stream -> {
          try {
            int data;
            StringBuilder responseString = new StringBuilder();
            data = stream.read();
            while (data != -1) {
              responseString.append((char) data);
              String value = getString(responseString.toString());
              if (value != null && !value.isBlank()) {
                return value;
              }
              data = stream.read();
            }
          } catch (IOException e) {
            log.atInfo()
                .setMessage("Failed to get most recent entry id from stream.")
                .setCause(e)
                .log();
          }
          return "";
        });
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
