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
import java.time.Duration;
import jimlind.filmlinkd.config.AppConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Client {
  static final String BASE_URL = "https://api.letterboxd.com/api/v0/";
  private final AppConfig appConfig;

  @Inject
  Client(AppConfig appConfig) {
    this.appConfig = appConfig;
  }

  public <T> T get(String path, Class<T> inputClass) {
    return this.request(path, "", inputClass);
  }

  private <T> T request(String uri, String authorization, Class<T> inputClass) {
    URI requestUri = null;
    try {
      requestUri = new URI(BASE_URL + uri);
    } catch (URISyntaxException e) {
      log.atError().setMessage("Error building URI").addKeyValue("uri", uri).log();
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

    String responseBody = "";
    try (HttpClient httpClient = HttpClient.newHttpClient()) {
      HttpResponse<String> httpResponse =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (httpResponse.statusCode() < 200 && httpResponse.statusCode() >= 300) {
        log.atError().setMessage("Incorrect Response Status Code").addKeyValue("uri", uri).log();
        return null;
      }
      responseBody = httpResponse.body();
      if (responseBody.isBlank()) {
        log.atError().setMessage("Response Body is Blank").addKeyValue("uri", uri).log();
        return null;
      }
    } catch (InterruptedException | IOException e) {
      log.atError().setMessage("Client Error").addKeyValue("uri", uri).log();
      return null;
    }

    try {
      return new GsonBuilder().create().fromJson(responseBody, inputClass);
    } catch (Exception e) {
      log.atError().setMessage("Error on Json Parsing").addKeyValue("body", responseBody).log();
      return null;
    }
  }

  private String bytesToHex(byte[] in) {
    final StringBuilder builder = new StringBuilder();
    for (final byte b : in) builder.append(String.format("%02x", b));
    return builder.toString();
  }

  /*
  public <T> T getAuthorized(String path, Class<T> inputClass) {
    String key = this.config.getLetterboxdApiKey();
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
  */

  /*
  private String buildSignature(String method, String url) {
    method = method.toUpperCase();
    String shared = this.config.getLetterboxdApiShared();
    SecretKeySpec secretKeySpec = new SecretKeySpec(shared.getBytes(), "HmacSHA256");

    try {
      Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
      sha256_HMAC.init(secretKeySpec);
      String data = method + "\u0000" + url + "\u0000"; // "�"
      return bytesToHex(sha256_HMAC.doFinal(data.getBytes()));
    } catch (Exception e) {
      return "";
    }
  }
  */
}
