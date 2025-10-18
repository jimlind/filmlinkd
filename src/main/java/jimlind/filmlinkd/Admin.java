package jimlind.filmlinkd;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.logging.Level;
import java.util.logging.Logger;
import jimlind.filmlinkd.admin.CleanChannels;
import jimlind.filmlinkd.admin.CleanUsers;
import jimlind.filmlinkd.config.GuiceModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** The main entry point for the admin application. */
@Slf4j
public final class Admin {
  public static final String CLEAN_USERS = "clean-users";
  public static final String CLEAN_CHANNELS = "clean-channels";
  private static final Logger logger = Logger.getLogger(Admin.class.getName());

  private Admin() {}

  /**
   * Initializes and starts all core application systems.
   *
   * @param args Command line arguments (not used).
   */
  public static void main(String[] args) {
    Options options = new Options();

    String commandDescription = "Command to run: clean-users, clean-channels, refresh-users";
    Option commandOption = new Option("c", "command", true, commandDescription);
    commandOption.setRequired(true);

    String pageDescription = "Page to start on when running large data sets";
    Option pageOption = new Option("p", "page", true, pageDescription);

    options.addOption(commandOption);
    options.addOption(pageOption);

    Injector injector = Guice.createInjector(new GuiceModule());

    try {
      CommandLineParser commandLineParser = new DefaultParser();
      CommandLine commandLine = commandLineParser.parse(options, args);
      String commandValue = commandLine.getOptionValue("command");
      String pageValue = commandLine.getOptionValue("page");

      if (CLEAN_USERS.equals(commandValue)) {
        injector.getInstance(CleanUsers.class).run(pageValue);
      }
      if (CLEAN_CHANNELS.equals(commandValue)) {
        injector.getInstance(CleanChannels.class).run();
      }
    } catch (ParseException e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.severe("Error parsing command line: " + e.getMessage());
      }
    }
  }
}
