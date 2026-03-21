package jimlind.filmlinkd.themoviedb.api;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import javax.inject.Inject;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.core.string.UrlBuilder;

/** The basis for The Movie Database API calls. */
public class Client {
  private static final String BASE_URL = "https://api.themoviedb.org/3/";

  private final String apiKey;

  /**
   * Constructor for this class.
   *
   * @param appConfig Contains application and environment variables
   */
  @Inject
  public Client(AppConfig appConfig) {
    this.apiKey = appConfig.getTheMovieDatabaseApiKey();
  }

  /**
   * Gets data from the TMDB API.
   *
   * @param path The local path to call excluding any parameters
   * @return The full string response from the API or an empty string if there is a failure
   */
  public String get(String path) {
    String url = new UrlBuilder(BASE_URL + path).add("api_key", apiKey).build();
    HttpRequest request =
        HttpRequest.newBuilder()
            .header("User-Agent", "Filmlinkd - A Letterboxd Discord Bot")
            .uri(URI.create(url))
            .timeout(Duration.of(60, SECONDS))
            .GET()
            .build();

    try (HttpClient httpClient = HttpClient.newHttpClient()) {
      HttpResponse<String> httpResponse =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (httpResponse.statusCode() != HttpURLConnection.HTTP_OK) {
        return "";
      }
      return httpResponse.body();
    } catch (InterruptedException | IOException e) {
      return "";
    }
  }
}
