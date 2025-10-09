package jimlind.filmlinkd;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.logging.Logger;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.config.GuiceModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/** The main entry point for the admin application. */
@Slf4j
public final class Admin {
  private static final Logger logger = Logger.getLogger(Admin.class.getName());

  private Admin() {}

  /**
   * Initializes and starts all core application systems.
   *
   * @param args Command line arguments (not used).
   */
  public static void main(String[] args) {
    Options options = new Options();
    //    String[] spinner = {"|", "/", "-", "\\"};

    String commandDescription = "Command to run: clean-users, clean-channels, refresh-users";
    Option command = new Option("c", "command", true, commandDescription);
    command.setRequired(true);

    String pageDescription = "Page to start on when running large data sets";
    Option page = new Option("p", "page", true, pageDescription);

    options.addOption(command);
    options.addOption(page);

    Injector injector = Guice.createInjector(new GuiceModule());
    //    UserFactory userFactory = injector.getInstance(UserFactory.class);

    /*
    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(options, args);
      String cmdName = cmd.getOptionValue("command");
      if (cmdName.equals("clean-users")) {
        boolean usersExist = true;
        int usersPage = 0;

        while (usersExist) {
          UserReader userReader = injector.getInstance(UserReader.class);
          List<QueryDocumentSnapshot> userList = userReader.getActiveUsersPage(20, usersPage++);
          if (userList.isEmpty()) {
            usersExist = false;
          }

          int i = 1;
          System.out.println("New Group of Users *");

          for (QueryDocumentSnapshot snapshot : userList) {
            System.out.print("\b" + spinner[i++ % spinner.length]);

            User user = userFactory.createFromSnapshot(snapshot);
            if (user != null) {
              try {
                String url = String.format("https://boxd.it/%s", user.getLetterboxdId());
                URI uri = URI.create(url);
                HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(6000);
                connection.setReadTimeout(6000);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == 404) {
                  System.out.println("User Not Found: " + user.getUserName());
                } else {
                  System.out.print("\b" + spinner[i % spinner.length]);
                }

              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            }
          }
        }
      }
    } catch (ParseException e) {
      logger.severe("Error parsing command line: " + e.getMessage());
    }
    */

    if (log.isInfoEnabled()) {
      String apiKey = injector.getInstance(AppConfig.class).getDiscordBotToken();
      logger.info("Client Id:" + apiKey);
    }
  }
}
