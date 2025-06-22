package jimlind.filmlinkd.system.discord.embedBuilder;

import com.google.inject.Inject;
import java.util.ArrayList;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringBuilder.DescriptionStringBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class HelpEmbedBuilder {
  private final DescriptionStringBuilder descriptionStringBuilder;
  private final EmbedBuilder embedBuilder;

  @Inject
  HelpEmbedBuilder(
      DescriptionStringBuilder descriptionStringBuilder, EmbedBuilderFactory embedBuilderFactory) {
    this.descriptionStringBuilder = descriptionStringBuilder;
    this.embedBuilder = embedBuilderFactory.create();
  }

  public ArrayList<MessageEmbed> create(
      String name, String version, long userCount, long guildCount) {

    // Set title
    embedBuilder.setTitle("(Help!) I Need Somebody", "https://jimlind.github.io/filmlinkd/");

    // Set description
    String descriptionText =
        String.format(
            "%s v%s\nTracking %s users on %s servers", name, version, userCount, guildCount);
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

    // Set fields for support links
    embedBuilder.addField(
        ":clap: Patreon", "[Support on Patreon](https://www.patreon.com/filmlinkd)", true);
    embedBuilder.addField(
        ":coffee: Ko-fi", "[Support on Ko-fi](https://ko-fi.com/filmlinkd)", true);
    embedBuilder.addField(
        ":left_speech_bubble: Discord", "[Join the Discord](https://discord.gg/deZ7EUguge)", true);

    ArrayList<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }

  public ArrayList<MessageEmbed> createTestMessage() {
    return this.createTestMessage(-1);
  }

  public ArrayList<MessageEmbed> createTestMessage(int step) {
    EmbedBuilder embedBuilder = new EmbedBuilder();
    switch (step) {
      case 0:
        String basicMessage =
            "This is a basic embed message.\nNext you should see an embed with a simple emoji.";
        embedBuilder.setDescription(
            descriptionStringBuilder.setDescriptionText(basicMessage).build());
        break;
      case 1:
        String simpleEmojiMessage =
            "This is a embed message with simple emoji :star::star::star:.\nNext you should see an embed with a custom emoji.";
        embedBuilder.setDescription(
            descriptionStringBuilder.setDescriptionText(simpleEmojiMessage).build());
        break;
      case 2:
        String customEmojiMessage =
            "This is a embed message with custom emoji <:s:851134022251970610><:s:851134022251970610><:s:851134022251970610>.\nNext you should see an embed with a custom emoji.";
        embedBuilder.setDescription(
            descriptionStringBuilder.setDescriptionText(customEmojiMessage).build());
        break;
      case 3:
        String formattedMessage =
            "This is a embed message *with* **formatted** ***text***.\nNext you should see an embed with an image.";
        embedBuilder.setDescription(
            descriptionStringBuilder.setDescriptionText(formattedMessage).build());
        break;
      case 4:
        String imageMessage =
            "This is a embed message with an image.\nThis concludes this test of the Emergency Broadcast System.";
        embedBuilder.setDescription(
            descriptionStringBuilder.setDescriptionText(imageMessage).build());
        embedBuilder.setThumbnail("https://jimlind.github.io/filmlinkd/images/filmlinkd-100.png");
      default:
        String introductionText =
            "This is the first of a series of test messages. If you can see this you know that basic command edit embeds work.\nIf you don't see any of the following messages you need to update your permissions as documented.\nNext you should see a basic embed message.";
        embedBuilder.setDescription(
            descriptionStringBuilder.setDescriptionText(introductionText).build());
        break;
    }

    ArrayList<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
