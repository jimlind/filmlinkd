package jimlind.filmlinkd.system.letterboxd.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

/** Scrapes the Letterboxd website for public Letterboxd ID redirect information. */
public class LetterboxdIdWeb {
  /**
   * Gets the canonical location of a Letterboxd ID redirect.
   *
   * @param letterboxdId Any Letterboxd ID (could represent any data model)
   * @return String representing the canonical location of the redirect
   */
  public String getLocationFromLid(String letterboxdId) {
    String url = String.format("https://boxd.it/%s", letterboxdId);

    try {
      URI uri = URI.create(url);
      HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(6000);
      connection.setReadTimeout(6000);
      connection.connect();

      return parseLocation(connection);
    } catch (IOException | IllegalArgumentException e) {
      return "";
    }
  }

  private String parseLocation(HttpURLConnection connection) {
    if (connection == null) {
      return "";
    }

    String location = connection.getHeaderField("location");
    return location != null ? location : "";
  }
}
