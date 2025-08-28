package jimlind.filmlinkd.discord.embed.factory;

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

/** Builds a Discord embed to display the result of an unfollow request. */
public class UnfollowEmbedFactory {
  private final DescriptionStringBuilder descriptionStringBuilder;
  private final EmbedBuilderFactory embedBuilderFactory;
  private final ImageUtils imageUtils;
  private final UserStringBuilder userStringBuilder;

  /**
   * Constructor for this class.
   *
   * @param descriptionStringBuilder Builds the description string truncating as necessary
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model
   * @param imageUtils Assists in finding optimal Letterboxd images
   * @param userStringBuilder Builds the user's name string formatting to properly escape characters
   */
  @Inject
  public UnfollowEmbedFactory(
      DescriptionStringBuilder descriptionStringBuilder,
      EmbedBuilderFactory embedBuilderFactory,
      ImageUtils imageUtils,
      UserStringBuilder userStringBuilder) {
    this.descriptionStringBuilder = descriptionStringBuilder;
    this.embedBuilderFactory = embedBuilderFactory;
    this.imageUtils = imageUtils;
    this.userStringBuilder = userStringBuilder;
  }

  /**
   * Builds the embed.
   *
   * @param member Member model from Letterboxd API
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  public List<MessageEmbed> create(LbMember member) {
    EmbedBuilder embedBuilder = embedBuilderFactory.create();

    String userName = userStringBuilder.setUsername(member.username).build();
    String description =
        String.format(
            "I unfollowed %s (%s).\nNo hard feelings I hope.", member.displayName, userName);
    embedBuilder.setDescription(descriptionStringBuilder.setDescriptionText(description).build());
    embedBuilder.setThumbnail(imageUtils.getTallest(member.avatar));

    List<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
