package jimlind.filmlinkd.system.letterboxd.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/** Scrapes the Letterboxd User Feed RSS for public member information. */
@Slf4j
public class UserFeedRss {
  /**
   * Get the full URL of the first diary item in a user's RSS feed.
   *
   * @param letterboxdId A Letterboxd ID for a user
   * @return The Letterboxd ID associated with the diary entry.
   */
  public String getMostRecentDiaryLinkFromLid(String letterboxdId) {
    String url = String.format("https://boxd.it/%s/rss", letterboxdId);

    HttpURLConnection connection;
    try {
      URI uri = URI.create(url);
      connection = (HttpURLConnection) uri.toURL().openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(6000);
      connection.setReadTimeout(6000);
      connection.connect();

      if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        connection.disconnect();
        return "";
      }
    } catch (IOException ignore) {
      return "";
    }

    try (InputStream inputStream = connection.getInputStream()) {
      return extractFilm(inputStream);
    } catch (IOException | ArrayIndexOutOfBoundsException e) {
      return "";
    }
  }

  private String extractFilm(InputStream inputStream) throws IOException {
    byte[] buffer = new byte[1024 * 4]; // 4KB
    int totalBytes = 0;
    byte[] fullContent = new byte[1024 * 16]; // 16KB
    Pattern pattern = Pattern.compile("https://letterboxd\\.com/[\\w-]+/film/[^<]+");

    int bytesRead = inputStream.read(buffer);
    while (bytesRead != -1) {
      System.arraycopy(buffer, 0, fullContent, totalBytes, bytesRead);
      totalBytes += bytesRead;

      String fullText = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(fullContent)).toString();
      Matcher matcher = pattern.matcher(fullText);
      if (matcher.find()) {
        return matcher.group();
      }

      bytesRead = inputStream.read(buffer);
    }

    return "";
  }
}
