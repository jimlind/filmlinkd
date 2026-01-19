package jimlind.filmlinkd.themoviedb.api;

import com.google.gson.GsonBuilder;
import javax.inject.Inject;
import jimlind.filmlinkd.themoviedb.model.MovieLatest;
import org.jetbrains.annotations.Nullable;

/**
 * A collection of APIs from the "Movie" section of the <a
 * href="https://developer.themoviedb.org/reference">TMDB API</a>.
 */
public class MovieApi {
  private final Client client;

  /**
   * Constructor for this class.
   *
   * @param client The TMDB API client handles authentication.
   */
  @Inject
  public MovieApi(Client client) {
    this.client = client;
  }

  /**
   * Implements <a href="https://developer.themoviedb.org/reference/movie-latest">GET
   * /movie/latest</a> to retrieve information about the most recent movie created in TMBD.
   *
   * @return The latest movie model or null if an error occurred.
   */
  public @Nullable MovieLatest getLatest() {
    String responseBody = client.get("movie/latest");
    if (responseBody.isBlank()) {
      return null;
    }

    try {
      return new GsonBuilder().create().fromJson(responseBody, MovieLatest.class);
    } catch (OutOfMemoryError e) {
      return null;
    }
  }
}
