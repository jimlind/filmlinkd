package jimlind.filmlinkd.system.letterboxd.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Singleton;

/** Scrapes the Letterboxd website for public member information. */
@Singleton
public class MemberWeb {
  /** Constructor for this class. */
  @Inject
  MemberWeb() {}

  /**
   * The best way to get the Letterboxd ID associated with a username is by loading the profile page
   * on the web and parsing the data from the headers. The Letterboxd ID is needed for all API
   * queries. The Letterboxd API to search for user data isn't as helpful.
   *
   * @param username A Letterboxd username
   * @return The Letterboxd ID associated with the username.
   */
  public String getMemberLidFromUsername(String username) {
    String url = String.format("https://letterboxd.com/%s/", username.toLowerCase(Locale.ROOT));

    try {
      URI uri = URI.create(url);
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
    if (letterboxdType != null && !"Member".equals(letterboxdType)) {
      return "";
    }

    String letterboxdId = connection.getHeaderField("x-letterboxd-identifier");
    return letterboxdId != null ? letterboxdId : "";
  }
}
