package jimlind.filmlinkd.system.discord.embedbuilder;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.UserStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/** Builds a Discord embed to display the result of a follow request. */
public class FollowEmbedBuilder {
  private final DescriptionStringBuilder descriptionStringBuilder;
  private final EmbedBuilder embedBuilder;
  private final ImageUtils imageUtils;
  private final UserStringBuilder userStringBuilder;
  private LbMember member;

  /**
   * Constructor for this class.
   *
   * @param descriptionStringBuilder Builds the description string truncating as necessary
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model
   * @param imageUtils Assists in finding optimal Letterboxd images
   * @param userStringBuilder Builds the user's name string formatting to properly escape characters
   */
  @Inject
  public FollowEmbedBuilder(
      DescriptionStringBuilder descriptionStringBuilder,
      EmbedBuilderFactory embedBuilderFactory,
      ImageUtils imageUtils,
      UserStringBuilder userStringBuilder) {
    this.descriptionStringBuilder = descriptionStringBuilder;
    this.imageUtils = imageUtils;
    this.userStringBuilder = userStringBuilder;
    embedBuilder = embedBuilderFactory.create();
  }

  /**
   * Setter for the member attribute.
   *
   * @param member Member model from Letterboxd API
   * @return This class for chaining
   */
  public FollowEmbedBuilder setMember(LbMember member) {
    this.member = member;
    return this;
  }

  /**
   * Builds the embed.
   *
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  public List<MessageEmbed> build() {
    if (member == null) {
      return new ArrayList<>();
    }

    String userName = userStringBuilder.setUsername(member.username).build();
    String description =
        String.format("I am now following %s (%s).\n", member.displayName, userName);
    description += "I'll try to post their most recent entry in the appropriate channel.";

    embedBuilder.setDescription(descriptionStringBuilder.setDescriptionText(description).build());
    embedBuilder.setThumbnail(imageUtils.getTallest(member.avatar));

    List<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
