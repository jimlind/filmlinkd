package jimlind.filmlinkd.system.letterboxd.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(6000);
      connection.setReadTimeout(6000);
      connection.setInstanceFollowRedirects(false);
      connection.connect();

      return parseLocation(connection);
    } catch (IOException e) {
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
