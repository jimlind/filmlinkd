package jimlind.filmlinkd.system.letterboxd.api;

import static java.time.temporal.ChronoUnit.SECONDS;

import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jimlind.filmlinkd.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.jetbrains.annotations.Nullable;

/**
 * The basis for the rest of the Letterboxd API calls. Contains all the logic to make calls that
 * need to be authorized and those that don't.
 */
@Slf4j
public class Client {
  static final String BASE_URL = "https://api.letterboxd.com/api/v0/";
  private static final String URI_KEY = "uri";
  private static final String PATH_KEY = "path";
  private final AppConfig appConfig;

  /**
   * Constructor for this class.
   *
   * @param appConfig Contains application and environment variables
   */
  @Inject
  Client(AppConfig appConfig) {
    this.appConfig = appConfig;
  }

  /**
   * Builds an authorized URL from the input path and applies the streamProcessor method on the
   * stream.
   *
   * @param path The path portion of the URI for a letterboxd api call.
   * @param streamProcessor A function that is applied to the input stream from the http response.
   * @return A string coming from the streamProcess handling stream. Empty string if there is any
   *     error on the stream.
   */
  public String handleAuthorizedStream(String path, Function<InputStream, String> streamProcessor) {
    URL url;
    String authorization;
    try {
      URI uri =
          new URIBuilder(BASE_URL + path)
              .addParameter("apikey", appConfig.getLetterboxdApiKey())
              .addParameter("nonce", String.valueOf(UUID.randomUUID()))
              .addParameter("timestamp", String.valueOf(Instant.now().getEpochSecond()))
              .build();
      url = uri.toURL();
      authorization = "Signature " + this.buildSignature("GET", uri.toString());
    } catch (URISyntaxException | MalformedURLException e) {
      log.atError()
          .setMessage("Failed to build URL or Authorization Signature")
          .addKeyValue(PATH_KEY, path)
          .setCause(e)
          .log();
      return "";
    }

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Host", url.getHost());
      connection.setRequestProperty("User-Agent", "Filmlinkd - A Letterboxd Discord Bot");
      connection.setRequestProperty("Authorization", authorization);
      connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(60));
      connection.setReadTimeout((int) TimeUnit.SECONDS.toMillis(60));

      if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        connection.disconnect();
        return "";
      }

      try (InputStream inputStream = connection.getInputStream()) {
        return streamProcessor.apply(inputStream);
      }
    } catch (SocketTimeoutException ignore) {
      // Timeouts are expected. Don't log the exception to avoid noise but do log the path.
      log.atError()
          .setMessage("Timeout: Authorized HTTP stream timed out")
          .addKeyValue(PATH_KEY, path)
          .log();
    } catch (IOException e) {
      log.atError()
          .setMessage("Failed to handle HTTP stream")
          .addKeyValue(PATH_KEY, path)
          .setCause(e)
          .log();
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
    return "";
  }

  /**
   * Gets data from the Letterboxd API when the request does not need to be authorized so passes an
   * empty string for auth.
   *
   * @param path The partial path for the API request (everything after /v0/) including any path or
   *     query params
   * @param inputClass The datatype that is returned.
   * @return Returns the datatype as defined by the inputs.
   */
  public @Nullable <T> T get(String path, Class<T> inputClass) {
    try {
      URI uri = new URIBuilder(BASE_URL + path).build();
      return this.request(uri, "", inputClass);
    } catch (URISyntaxException e) {
      log.atError().setMessage("Failed to build public URI").addKeyValue(PATH_KEY, path).log();
      return null;
    }
  }

  /**
   * Gets data from the Letterboxd API when the request needs to be authorized so building that
   * auth.
   *
   * @param path The partial path for the API request (everything after /v0/) including any path or
   *     query params
   * @param inputClass The datatype that is returned.
   * @return Returns the datatype as defined by the inputs.
   */
  public @Nullable <T> T getAuthorized(String path, Class<T> inputClass) {
    String key = appConfig.getLetterboxdApiKey();
    String nonce = String.valueOf(UUID.randomUUID());
    String now = String.valueOf(Instant.now().getEpochSecond());

    // TODO: Are there other exceptions we should be trying to catch here?
    try {
      URI uri =
          new URIBuilder(BASE_URL + path)
              .addParameter("apikey", key)
              .addParameter("nonce", nonce)
              .addParameter("timestamp", now)
              .build();
      String authorization = "Signature " + this.buildSignature("GET", uri.toString());

      return this.request(uri, authorization, inputClass);
    } catch (URISyntaxException e) {
      log.atError().setMessage("Failed to build authorized URI").addKeyValue(PATH_KEY, path).log();
      return null;
    }
  }

  private @Nullable <T> T request(URI uri, String authorization, Class<T> inputClass) {
    HttpRequest request =
        HttpRequest.newBuilder()
            .header("User-Agent", "Filmlinkd - A Letterboxd Discord Bot")
            .header("Authorization", authorization)
            .uri(uri)
            .timeout(Duration.of(60, SECONDS))
            .GET()
            .build();

    String responseBody;
    try (HttpClient httpClient = HttpClient.newHttpClient()) {
      HttpResponse<String> httpResponse =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) {
        log.atError().setMessage("Incorrect Response Status Code").addKeyValue(URI_KEY, uri).log();
        return null;
      }
      responseBody = httpResponse.body();
      if (responseBody.isBlank()) {
        log.atError().setMessage("Response Body is Blank").addKeyValue(URI_KEY, uri).log();
        return null;
      }
    } catch (HttpConnectTimeoutException ignore) {
      // Timeouts are expected. Don't log the exception to avoid noise but do log the path.
      log.atError()
          .setMessage("Timeout: HttpClient request timed out")
          .addKeyValue(URI_KEY, uri)
          .log();
      return null;
    } catch (InterruptedException | IOException e) {
      log.atError().setMessage("Client Error").addKeyValue(URI_KEY, uri).setCause(e).log();
      return null;
    }

    // TODO: Check what sort of exception I can actually get out of here.
    try {
      return new GsonBuilder().create().fromJson(responseBody, inputClass);
    } catch (OutOfMemoryError e) {
      log.atError().setMessage("Error on Json Parsing").addKeyValue("body", responseBody).log();
      return null;
    }
  }

  private String bytesToHex(byte[] in) {
    final StringBuilder builder = new StringBuilder();
    for (final byte b : in) {
      builder.append(String.format("%02x", b));
    }
    return builder.toString();
  }

  private String buildSignature(String method, String url) {
    String shared = appConfig.getLetterboxdApiShared();
    SecretKeySpec secretKeySpec = new SecretKeySpec(shared.getBytes(), "HmacSHA256");

    // TODO: This used to have an unchecked try/catch wrapper
    try {
      Mac sha256Hmac = Mac.getInstance("HmacSHA256");
      sha256Hmac.init(secretKeySpec);
      String data = method.toUpperCase(Locale.ROOT) + "\u0000" + url + "\u0000"; // "�"
      return bytesToHex(sha256Hmac.doFinal(data.getBytes()));
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      return "";
    }
  }
}
