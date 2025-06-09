package jimlind.filmlinkd.system.letterboxd.web;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MemberWeb {
  public String getMemberLIDFromUsername(String username) {
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
