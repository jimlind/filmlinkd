package jimlind.filmlinkd.discord.embed.factory;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 * Builds a Discord embed to display help and test information. This doesn't follow the same pattern
 * as the other Builders because there is a pretty wide variety of content to send. It could be
 * written similarly to the other Builders if it makes sense.
 */
public class HelpEmbedFactory {
  private final AppConfig appConfig;
  private final DescriptionStringBuilder descriptionStringBuilder;
  private final EmbedBuilderFactory embedBuilderFactory;

  /**
   * Constructor for this class.
   *
   * @param appConfig Contains application and environment variables
   * @param descriptionStringBuilder Builds the description string truncating as necessary
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model
   */
  @Inject
  HelpEmbedFactory(
      AppConfig appConfig,
      DescriptionStringBuilder descriptionStringBuilder,
      EmbedBuilderFactory embedBuilderFactory) {
    this.appConfig = appConfig;
    this.descriptionStringBuilder = descriptionStringBuilder;
    this.embedBuilderFactory = embedBuilderFactory;
  }

  /**
   * Builds the embed.
   *
   * @param userCount Total number of accounts being tracked
   * @param guildCount Total number of servers installed on
   * @param viewChannelEnabled Can the bot view the channel used for the command
   * @param sendMessageEnabled Can the bot send messages in the channel used for the command
   * @param embedLinkEnabled Can the bot embed links in the channel used for the command
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  public List<MessageEmbed> create(
      long userCount,
      long guildCount,
      boolean viewChannelEnabled,
      boolean sendMessageEnabled,
      boolean embedLinkEnabled) {
    EmbedBuilder embedBuilder = embedBuilderFactory.create();

    // Set title
    embedBuilder.setTitle("(Help!) I Need Somebody", "https://jimlind.github.io/filmlinkd/");

    // Set description
    String descriptionText =
        String.format(
            "%s v%s\nTracking %s users on %s servers",
            appConfig.getApplicationName(),
            appConfig.getApplicationVersion(),
            userCount,
            guildCount);
    embedBuilder.setDescription(
        descriptionStringBuilder.setDescriptionText(descriptionText).build());

    // Set fields for slash commands
    embedBuilder.addField("/help", "Shows this message", false);
    embedBuilder.addField("/follow account [channel]", "Start listening for new entries", false);
    embedBuilder.addField("/unfollow account [channel]", "Stops listening for new entries", false);
    embedBuilder.addField("/following", "List all users followed in this channel", false);
    embedBuilder.addField(
        "/refresh account", "Refreshes the Filmlinkd cache for the account", false);
    embedBuilder.addField(
        "/contributor contributor-name", "Shows a film contributor's information", false);
    embedBuilder.addField("/diary account", "Shows a user's 5 most recent entries", false);
    embedBuilder.addField("/film film-name", "Shows a film's information", false);
    embedBuilder.addField("/list account list-name", "Shows a user's list summary", false);
    embedBuilder.addField("/logged account film-name", "Shows a user's entries for a film", false);
    embedBuilder.addField("/roulette", "Shows random film information", false);
    embedBuilder.addField("/user account", "Shows a users's information", false);

    // Set fields for permissions data
    String viewStatus = viewChannelEnabled ? "✅" : "❌";
    String sendStatus = sendMessageEnabled ? "✅" : "❌";
    String embedStatus = embedLinkEnabled ? "✅" : "❌";
    String permissions =
        String.format(
            "``` %s View channel\n %s Send message in channel/thread\n %s Embed link```",
            viewStatus, sendStatus, embedStatus);
    embedBuilder.addField(":gear:️ Permissions", permissions, false);

    // Set fields for support links
    embedBuilder.addField(
        ":clap: Patreon", "[Support on Patreon](https://www.patreon.com/filmlinkd)", true);
    embedBuilder.addField(
        ":coffee: Ko-fi", "[Support on Ko-fi](https://ko-fi.com/filmlinkd)", true);
    embedBuilder.addField(
        ":left_speech_bubble: Discord", "[Join the Discord](https://discord.gg/deZ7EUguge)", true);

    List<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }

  /**
   * Builds the embed for the introductory test message.
   *
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list *
   *     contains only one embed.
   */
  public List<MessageEmbed> createTestMessage() {
    return this.createTestMessage(0);
  }

  /**
   * Builds the embed for a variety of test messages depending on the step input.
   *
   * @param step An integer to help decide which in the series of test messages to send.
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  public List<MessageEmbed> createTestMessage(int step) {
    EmbedBuilder embedBuilder = embedBuilderFactory.create();

    switch (step) {
      case 0:
        String introductionText =
            """
              This is the first of a series of test messages.
              If you can see this you know that basic command edit embeds work. If you don't see \
              any of the following messages your permissions need to be updated as documented.
              Next you should see a basic embed message.""";
        embedBuilder.setDescription(
            descriptionStringBuilder.setDescriptionText(introductionText).build());
        break;
      case 1:
        String basicMessage =
            """
          This is a basic embed message.
          Next you should see an embed with a simple emoji.""";
        embedBuilder.setDescription(
            descriptionStringBuilder.setDescriptionText(basicMessage).build());
        break;
      case 2:
        String simpleEmojiMessage =
            """
          This is a embed message with simple emoji
          :star::star::star:.
          Next you should see an embed with a custom emoji.""";
        embedBuilder.setDescription(
            descriptionStringBuilder.setDescriptionText(simpleEmojiMessage).build());
        break;
      case 3:
        String customEmojiMessage =
            """
          This is a embed message with custom emoji
          <:s:851134022251970610><:s:851134022251970610><:s:851134022251970610>
          Next you should see an embed with formatted text.""";
        embedBuilder.setDescription(
            descriptionStringBuilder.setDescriptionText(customEmojiMessage).build());
        break;
      case 4:
        String formattedMessage =
            """
          This is a embed message *with* **formatted** ***text***.
          Next you should see an embed with an image.""";
        embedBuilder.setDescription(
            descriptionStringBuilder.setDescriptionText(formattedMessage).build());
        break;
      case 5:
        String imageMessage =
            """
          This is a embed message with an image.
          Next you should see an embed with a diary entry.""";
        embedBuilder.setDescription(
            descriptionStringBuilder.setDescriptionText(imageMessage).build());
        embedBuilder.setThumbnail("https://jimlind.github.io/filmlinkd/images/filmlinkd-100.png");
        break;
    }

    List<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
