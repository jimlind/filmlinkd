package jimlind.filmlinkd.system.letterboxd.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/** Scrapes the Letterboxd website for public member information. */
public class MemberWeb {
  /**
   * The best way to get the Letterboxd ID associated with a username is by loading the profile page
   * on the web and parsing the data from the headers. The Letterboxd ID is needed for all API
   * queries. The Letterboxd API to search for user data isn't as helpful.
   *
   * @param username A Letterboxd username
   * @return The Letterboxd ID associated with the username.
   */
  public String getMemberLidFromUsername(String username) {
    String url = String.format("https://letterboxd.com/%s/", username.toLowerCase());

    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(6000);
      connection.setReadTimeout(6000);
      connection.connect();

      return validateResponse(connection);
    } catch (IOException e) {
      return "";
    }
  }

  private String validateResponse(HttpURLConnection connection) {
    if (connection == null) {
      return "";
    }

    String letterboxdType = connection.getHeaderField("x-letterboxd-type");
    if (letterboxdType != null && !letterboxdType.equals("Member")) {
      return "";
    }

    String letterboxdId = connection.getHeaderField("x-letterboxd-identifier");
    return letterboxdId != null ? letterboxdId : "";
  }
}
