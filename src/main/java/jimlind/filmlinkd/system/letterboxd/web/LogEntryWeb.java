package jimlind.filmlinkd.system.letterboxd.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

/** Scrapes the Letterboxd website for public log entry information. */
public class LogEntryWeb {
  /**
   * The only way to get a Letterboxd ID associated with a review URL is by parsing the header data
   * after a connection was initiated. The connection can be closed down quickly after.
   *
   * @param input A fully qualified URI
   * @return The Letterboxd ID associated with the log entry.
   */
  public String getLidFromLogEntryPath(String input) {
    try {
      URI uri = URI.create(input);
      HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(6000);
      connection.setReadTimeout(6000);
      connection.connect();

      return validateResponse(connection);
    } catch (IOException | IllegalArgumentException e) {
      return "";
    }
  }

  private String validateResponse(HttpURLConnection connection) {
    if (connection == null) {
      return "";
    }

    String letterboxdType = connection.getHeaderField("x-letterboxd-type");
    if (letterboxdType != null && !"LogEntry".equals(letterboxdType)) {
      return "";
    }

    String letterboxdId = connection.getHeaderField("x-letterboxd-identifier");
    return letterboxdId != null ? letterboxdId : "";
  }
}
