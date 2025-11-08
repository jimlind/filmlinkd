package jimlind.filmlinkd.system.letterboxd.api;

import com.google.inject.Inject;
import java.util.List;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmsResponse;
import org.jetbrains.annotations.Nullable;

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

  @Nullable
  public LbFilmsResponse fetch(List<Integer> input) {
    String filmIdParams =
        input.stream().map(id -> "filmId=tmdb:" + id).reduce((a, b) -> a + "&" + b).orElse("");
    String path = "films/?" + filmIdParams;
    return client.getAuthorized(path, LbFilmsResponse.class);
  }
}
