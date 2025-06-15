package jimlind.filmlinkd.system.letterboxd.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class LetterboxdIdWeb {
  public String getLocationFromLID(String letterboxdId) {
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
