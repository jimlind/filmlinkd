package jimlind.filmlinkd.themoviedb;

import static java.time.temporal.ChronoUnit.SECONDS;

import com.google.inject.Inject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import jimlind.filmlinkd.config.AppConfig;
import org.apache.http.client.utils.URIBuilder;

public class Client {
  private static final String BASE_URL = "https://api.themoviedb.org/3/";

  private final String apiKey;

  @Inject
  public Client(AppConfig appConfig) {
    this.apiKey = appConfig.getTheMovieDatabaseApiKey();
  }

  public String get(String path) {
    URI uri;
    try {
      uri = new URIBuilder(BASE_URL + path).addParameter("api_key", apiKey).build();
    } catch (URISyntaxException ignore) {
      return "";
    }

    HttpRequest request =
        HttpRequest.newBuilder()
            .header("User-Agent", "Filmlinkd - A Letterboxd Discord Bot")
            .uri(uri)
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
