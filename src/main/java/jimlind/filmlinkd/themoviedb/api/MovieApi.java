package jimlind.filmlinkd.themoviedb.api;

import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import jimlind.filmlinkd.themoviedb.model.MovieLatest;
import org.jetbrains.annotations.Nullable;

public class MovieApi {
  private final Client client;

  @Inject
  public MovieApi(Client client) {
    this.client = client;
  }

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
