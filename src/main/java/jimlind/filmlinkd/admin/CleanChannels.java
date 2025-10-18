package jimlind.filmlinkd.admin;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.inject.Inject;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import jimlind.filmlinkd.admin.channels.Archiver;
import jimlind.filmlinkd.admin.channels.LogFileWriter;
import jimlind.filmlinkd.admin.channels.NullFilter;
import jimlind.filmlinkd.admin.channels.WrongPermissionsFilter;
import jimlind.filmlinkd.admin.channels.WrongTypeFilter;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.google.firestore.UserReader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

/** Admin command to clean up channel records. */
public class CleanChannels {
  private final String timestamp = String.valueOf(System.currentTimeMillis());

  private final AppConfig appConfig;
  private final Archiver archiver;
  private final LogFileWriter logFileWriter;
  private final NullFilter nullFilter;
  private final UserFactory userFactory;
  private final UserReader userReader;
  private final WrongPermissionsFilter wrongPermissionsFilter;
  private final WrongTypeFilter wrongTypeFilter;

  private List<String> sortedChannelIdList = new ArrayList<>();
  private ShardManager globalShardManager;

  /**
   * Constructor for this class.
   *
   * @param appConfig Contains application and environment variables
   * @param archiver Handles archiving channels
   * @param logFileWriter Handles writing to log files
   * @param nullFilter Filters out null channels
   * @param userFactory Creates user models
   * @param userReader Handles reading user records
   * @param wrongPermissionsFilter Filters out wrong permissions channels
   * @param wrongTypeFilter Filters out wrong type channels
   */
  @Inject
  public CleanChannels(
      AppConfig appConfig,
      Archiver archiver,
      LogFileWriter logFileWriter,
      NullFilter nullFilter,
      UserFactory userFactory,
      UserReader userReader,
      WrongPermissionsFilter wrongPermissionsFilter,
      WrongTypeFilter wrongTypeFilter) {
    this.appConfig = appConfig;
    this.archiver = archiver;
    this.logFileWriter = logFileWriter;
    this.nullFilter = nullFilter;
    this.userFactory = userFactory;
    this.userReader = userReader;
    this.wrongPermissionsFilter = wrongPermissionsFilter;
    this.wrongTypeFilter = wrongTypeFilter;
  }

  /** Collects all channels and initiates the cleaning process when ready. */
  public void run() {
    List<QueryDocumentSnapshot> activeUsers = userReader.getActiveUsers();
    List<String> channelIdList = new ArrayList<>();

    for (QueryDocumentSnapshot userSnapshot : activeUsers) {
      User userModel = userFactory.createFromSnapshot(userSnapshot);
      if (userModel == null) {
        continue;
      }
      for (User.Channel channel : userModel.getChannelList()) {
        channelIdList.add(channel.getChannelId());
      }
    }
    Set<String> sortedUnique = new TreeSet<>(Comparator.comparing(BigInteger::new));
    sortedUnique.addAll(channelIdList);
    sortedChannelIdList = new ArrayList<>(sortedUnique);

    String token = appConfig.getDiscordBotToken();
    globalShardManager =
        DefaultShardManagerBuilder.createLight(token)
            .addEventListeners(new EventListener())
            .build();
  }

  /**
   * Run the data processor. It's a bit odd because it uses global data because of the event
   * triggering. Could be done in a better architectural way if it was going to be resued outside
   * this temporary admin file.
   */
  public void processData() {
    try (PrintWriter out = new PrintWriter(System.out, true)) {
      String[] spinner = {"|", "/", "-", "\\"};
      int progress = 0;

      List<String> nullChannelList = nullFilter.filter(globalShardManager, sortedChannelIdList);
      out.print("Archiving null channels...\n* ");
      out.flush();
      for (String channelId : nullChannelList) {
        out.print("\r\r" + spinner[progress % spinner.length] + " ");
        out.flush();
        logFileWriter.write(channelId, "null-channel", timestamp);
        archiver.archive(channelId);
        progress++;
      }
      out.println("\n");
      out.flush();

      sortedChannelIdList.removeAll(nullChannelList);
      List<String> wrongTypeChannelList =
          wrongTypeFilter.filter(globalShardManager, sortedChannelIdList);
      out.print("Archiving wrong type channels...\n* ");
      out.flush();
      for (String channelId : wrongTypeChannelList) {
        out.print("\r\r" + spinner[progress % spinner.length] + " ");
        out.flush();
        logFileWriter.write(channelId, "wrong-type-channel", timestamp);
        archiver.archive(channelId);
        progress++;
      }
      out.println("\n");
      out.flush();

      sortedChannelIdList.removeAll(wrongTypeChannelList);
      List<String> wrongPermissionsChannelList =
          wrongPermissionsFilter.filter(globalShardManager, sortedChannelIdList);
      out.print("Archiving wrong permissions channels...\n* ");
      out.flush();
      for (String channelId : wrongPermissionsChannelList) {
        out.print("\r\r" + spinner[progress % spinner.length] + " ");
        out.flush();
        logFileWriter.write(channelId, "wrong-permissions-channel", timestamp);
        archiver.archive(channelId);
        progress++;
      }
      out.println("\n");
      out.flush();

      globalShardManager.shutdown();
    }
  }

  /** Custom listener for Discord events. On Ready is needed here. */
  public class EventListener extends ListenerAdapter {
    @NotNull
    private static JDA extractJda(@NotNull ReadyEvent readyEvent) {
      return readyEvent.getJDA();
    }

    private static JDA.ShardInfo extractShardInfo(JDA jda) {
      return jda.getShardInfo();
    }

    @Override
    public void onReady(@NotNull ReadyEvent readyEvent) {
      JDA jda = extractJda(readyEvent);
      JDA.ShardInfo shardInfo = extractShardInfo(jda);
      int id = shardInfo.getShardId();
      int total = shardInfo.getShardTotal() - 1;
      if (id != total) {
        return;
      }
      processData();
    }
  }
}
