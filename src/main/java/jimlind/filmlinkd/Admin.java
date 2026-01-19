package jimlind.filmlinkd;

import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/** The main entry point for the admin application. */
@Slf4j
public final class Admin {
  public static final String CLEAN_USERS = "clean-users";
  public static final String CLEAN_CHANNELS = "clean-channels";
  public static final String UNDO_CHANNEL_ARCHIVE = "undo-channel-archive";
  private static final Logger logger = Logger.getLogger(Admin.class.getName());

  private Admin() {}

  /**
   * Initializes and starts all core application systems.
   *
   * @param args Command line arguments (not used).
   */
  public static void main(String[] args) {
    Options options = new Options();

    String commandDescription =
        "Command to run: clean-users, clean-channels, refresh-users, undo-channel-archive";
    Option commandOption = new Option("c", "command", true, commandDescription);
    commandOption.setRequired(true);

    String pageDescription = "Page to start on when running large data sets";
    Option pageOption = new Option("p", "page", true, pageDescription);

    String channelDescription = "Channel to use as input for commands";
    Option channelOption = new Option("k", "channel", true, channelDescription);

    options.addOption(commandOption);
    options.addOption(pageOption);
    options.addOption(channelOption);

//    Injector injector = Guice.createInjector(new GuiceModule());
//
//    try {
//      CommandLineParser commandLineParser = new DefaultParser();
//      CommandLine commandLine = commandLineParser.parse(options, args);
//      String commandValue = commandLine.getOptionValue("command");
//      String pageValue = commandLine.getOptionValue("page");
//      String channelValue = commandLine.getOptionValue("channel");
//
//      if (CLEAN_USERS.equals(commandValue)) {
//        injector.getInstance(CleanUsers.class).run(pageValue);
//      }
//      if (CLEAN_CHANNELS.equals(commandValue)) {
//        injector.getInstance(CleanChannels.class).run();
//      }
//      if (UNDO_CHANNEL_ARCHIVE.equals(commandValue)) {
//        injector.getInstance(UndoChannelArchive.class).run(channelValue);
//      }
//
//    } catch (ParseException e) {
//      if (logger.isLoggable(Level.SEVERE)) {
//        logger.severe("Error parsing command line: " + e.getMessage());
//      }
//    }
  }
}
