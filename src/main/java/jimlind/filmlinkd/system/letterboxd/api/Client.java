package jimlind.filmlinkd.system.letterboxd.api;

import static java.time.temporal.ChronoUnit.SECONDS;

import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jimlind.filmlinkd.config.AppConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * The basis for the rest of the Letterboxd API calls. Contains all the logic to make calls that
 * need to be authorized and those that don't.
 */
@Slf4j
public class Client {
  static final String BASE_URL = "https://api.letterboxd.com/api/v0/";
  private static final String URI_KEY = "uri";
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
   * Gets data from the Letterboxd API when the request does not need to be authorized so passes an
   * empty string for auth.
   *
   * @param path The partial path for the API request (everything after /v0/) including any path or
   *     query params
   * @param inputClass The datatype that is returned.
   * @return Returns the datatype as defined by the inputs.
   */
  public <T> T get(String path, Class<T> inputClass) {
    return this.request(path, "", inputClass);
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
  public <T> T getAuthorized(String path, Class<T> inputClass) {
    String key = appConfig.getLetterboxdApiKey();
    String nonce = String.valueOf(java.util.UUID.randomUUID());
    String now = String.valueOf(Instant.now().getEpochSecond());

    // TODO: This feels like a terrible way to build a URL string
    // The actual JAVA URI Classes probably can do it better

    String symbol = path.contains("?") ? "&" : "?";
    String uri = path + symbol + String.format("apikey=%s&nonce=%s&timestamp=%s", key, nonce, now);
    String url = BASE_URL + uri;
    String authorization = "Signature " + this.buildSignature("GET", url);

    return this.request(uri, authorization, inputClass);
  }

  private <T> T request(String uri, String authorization, Class<T> inputClass) {
    URI requestUri;
    try {
      requestUri = new URI(BASE_URL + uri);
    } catch (URISyntaxException e) {
      log.atError().setMessage("Error building URI").addKeyValue(URI_KEY, uri).log();
      return null;
    }

    HttpRequest request =
        HttpRequest.newBuilder()
            .header("User-Agent", "Filmlinkd - A Letterboxd Discord Bot")
            .header("Authorization", authorization)
            .uri(requestUri)
            .timeout(Duration.of(6, SECONDS))
            .GET()
            .build();

    String responseBody;
    try (HttpClient httpClient = HttpClient.newHttpClient()) {
      HttpResponse<String> httpResponse =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (httpResponse.statusCode() < 200 && httpResponse.statusCode() >= 300) {
        log.atError().setMessage("Incorrect Response Status Code").addKeyValue(URI_KEY, uri).log();
        return null;
      }
      responseBody = httpResponse.body();
      if (responseBody.isBlank()) {
        log.atError().setMessage("Response Body is Blank").addKeyValue(URI_KEY, uri).log();
        return null;
      }
    } catch (InterruptedException | IOException e) {
      log.atError().setMessage("Client Error").addKeyValue(URI_KEY, uri).log();
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
