package jimlind.filmlinkd.core.string;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** A utility class for constructing URLs with query parameters. */
public final class UrlBuilder {
  private String fullUrl;
  private boolean hasParams;

  /**
   * Constructs a new UrlBuilder.
   *
   * @param baseUrl The base URL to append parameters to.
   */
  public UrlBuilder(String baseUrl) {
    this.fullUrl = baseUrl;
    if (baseUrl.contains("?")) {
      hasParams = true;
    }
  }

  /**
   * Adds a query parameter to the URL.
   *
   * <p>Both the key and the value are URL encoded using UTF-8.
   *
   * @param key The parameter key.
   * @param value The parameter value.
   * @return This builder instance for method chaining.
   */
  public UrlBuilder add(String key, String value) {
    fullUrl += (hasParams ? '&' : '?');
    hasParams = true;

    fullUrl += URLEncoder.encode(key, StandardCharsets.UTF_8);
    fullUrl += "=";
    fullUrl += URLEncoder.encode(value, StandardCharsets.UTF_8);

    return this;
  }

  /**
   * Returns the constructed URL string.
   *
   * @return The full URL string.
   */
  public String build() {
    return fullUrl;
  }
}
