package jimlind.filmlinkd.system.letterboxd.api;

import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntriesResponse;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;
import lombok.extern.slf4j.Slf4j;

/**
 * Implements <a href="https://api-docs.letterboxd.com/#operation-GET-log-entries">GET
 * /log-entries</a>.
 */
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

  private static String getFirstId(InputStream stream) {
    JsonReader reader = new JsonReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
    try {
      reader.beginObject();
      // Loop over top level elements in the JSON object
      while (reader.hasNext()) {
        // When we find the "items" element
        if ("items".equals(reader.nextName())) {
          // Items is an array of objects so enter the first object in an array
          reader.beginArray();
          reader.beginObject();

          // Loop over elements in that first object
          while (reader.hasNext()) {
            // When we find the "id" element return it
            if ("id".equals(reader.nextName())) {
              return reader.nextString();
            }
            // Otherwise skip the value and find the next
            reader.skipValue();
          }
          // If the "id" element is not found anywhere in the object return an empty string
          return "";
        }
        reader.skipValue();
      }
    } catch (IOException | IllegalStateException exception) {
      log.atInfo()
          .setMessage("Failed to get most recent entry id from stream.")
          .setCause(exception)
          .log();
    }
    // If the JSON object doesn't contain an "items" object or we have an exception we exit here
    return "";
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

    return logEntriesResponse.getItems();
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
    return this.client.handleAuthorizedStream(logEntriesPath, LogEntriesApi::getFirstId);
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

    return logEntriesResponse.getItems();
  }
}
