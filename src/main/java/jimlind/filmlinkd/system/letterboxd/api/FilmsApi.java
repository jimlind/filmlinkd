package jimlind.filmlinkd.system.letterboxd.api;

import java.util.List;
import javax.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmsResponse;
import org.jetbrains.annotations.Nullable;

/**
 * Implements <a href="https://api-docs.letterboxd.com/#operation-GET-films">GET /films</a> for a
 * list of films.
 */
public class FilmsApi {
  private final Client client;

  /**
   * Constructor for this class.
   *
   * @param client The Letterboxd API client that does authorization and object casting.
   */
  @Inject
  FilmsApi(Client client) {
    this.client = client;
  }

  /**
   * Very specific implementation that accepts a list of Integers assuming they are TMDB film ids
   * and loading any matching Letterboxd films. Letterboxd films are a subset of TMDB film ids so
   * valid TMDB films may not return matches.
   *
   * @param input A list of TMDB film ids
   * @return The response from the films API as {@link LbFilmsResponse}
   */
  @Nullable
  public LbFilmsResponse fetch(List<Integer> input) {
    String filmIdParams =
        input.stream().map(id -> "filmId=tmdb:" + id).reduce((a, b) -> a + "&" + b).orElse("");
    String path = "films/?" + filmIdParams;
    return client.getAuthorized(path, LbFilmsResponse.class);
  }
}
