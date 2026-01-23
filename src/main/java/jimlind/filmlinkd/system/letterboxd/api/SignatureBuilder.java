package jimlind.filmlinkd.system.letterboxd.api;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import jimlind.filmlinkd.config.AppConfig;

/** Helper class to build the signature for Letterboxd API calls. */
public class SignatureBuilder {
  private final AppConfig appConfig;

  /**
   * Constructor for this class.
   *
   * @param appConfig Contains application and environment variables
   */
  @Inject
  public SignatureBuilder(AppConfig appConfig) {
    this.appConfig = appConfig;
  }

  /**
   * Builds the signature for the Letterboxd API.
   *
   * @param method The HTTP method (GET, POST, etc.)
   * @param url The full URL of the request
   * @return The signature
   */
  public String buildSignature(String method, String url) {
    String sharedSecret = appConfig.getLetterboxdApiShared();
    SecretKeySpec secretKeySpec =
        new SecretKeySpec(sharedSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    // TODO: This used to have an unchecked try/catch wrapper
    try {
      Mac sha256Hmac = Mac.getInstance("HmacSHA256");
      sha256Hmac.init(secretKeySpec);
      String data = method.toUpperCase(Locale.ROOT) + "\u0000" + url + "\u0000";
      return bytesToHex(sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    } catch (InvalidKeyException | NoSuchAlgorithmException e) {
      return "";
    }
  }

  private String bytesToHex(byte[] in) {
    final StringBuilder builder = new StringBuilder();
    for (final byte b : in) {
      builder.append(String.format("%02x", b));
    }
    return builder.toString();
  }
}
