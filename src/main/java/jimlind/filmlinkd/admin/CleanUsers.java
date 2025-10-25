package jimlind.filmlinkd.admin;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.google.db.UserReader;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.google.firestore.UserWriter;

/** Admin command to clean up user records. */
public class CleanUsers {
  private static final int PAGE_SIZE = 100;

  private final UserFactory userFactory;
  private final UserReader userReader;
  private final UserWriter userWriter;

  /**
   * Constructor for this class.
   *
   * @param userFactory Create user models
   * @param userReader Handles reading user records
   * @param userWriter Handles writing user records
   */
  @Inject
  public CleanUsers(UserFactory userFactory, UserReader userReader, UserWriter userWriter) {
    this.userFactory = userFactory;
    this.userReader = userReader;
    this.userWriter = userWriter;
  }

  private static HttpURLConnection getHttpUrlConnection(User user) throws IOException {
    String url = String.format("https://boxd.it/%s", user.getLetterboxdId());
    URI uri = URI.create(url);
    HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
    connection.setRequestMethod("GET");
    connection.setConnectTimeout(6000);
    connection.setReadTimeout(6000);
    connection.connect();
    return connection;
  }

  /**
   * If a user is no longer available archive all the channels they were following.
   *
   * @param pageValue Allow the command to start on a specific page.
   */
  public void run(String pageValue) {
    try (PrintWriter out = new PrintWriter(System.out, true)) {
      boolean usersExist = true;
      int usersPage = pageValue == null ? 0 : Integer.parseInt(pageValue);

      while (usersExist) {
        out.print("Reading page " + usersPage + " of users...\n* ");
        List<QueryDocumentSnapshot> userList =
            userReader.getActiveUsersPage(PAGE_SIZE, usersPage++);
        if (userList.isEmpty()) {
          out.println("DONE!");
          usersExist = false;
        }

        int i = 1;
        for (QueryDocumentSnapshot snapshot : userList) {
          User user = userFactory.createFromSnapshot(snapshot);
          if (user != null) {
            try {
              HttpURLConnection connection = getHttpUrlConnection(user);
              processOutput(connection, out, user, i++);
            } catch (IOException e) {
              out.println("!");
              out.flush();
            }
          }
        }
        out.println(" -- ");
        out.flush();
      }
    }
  }

  private void processOutput(HttpURLConnection connection, PrintWriter out, User user, int i)
      throws IOException {
    String[] spinner = {"|", "/", "-", "\\"};

    int responseCode = connection.getResponseCode();
    String letterboxdType = connection.getHeaderField("x-letterboxd-type");

    if (responseCode == 404 || !"Member".equals(letterboxdType)) {
      out.print("Archiving channels for user: " + user.getUserName() + "\n*");
      boolean result = userWriter.archiveAllUserSubscriptions(user.getLetterboxdId());
      if (!result) {
        out.print("Archiving failed: " + user.getUserName() + "\n*");
      }
    } else {
      out.print("\r\r" + spinner[i % spinner.length] + " ");
      out.flush();
    }
  }
}
