package jimlind.filmlinkd.system.discord.embedBuilder;

import com.google.inject.Inject;
import java.util.AbstractList;
import java.util.ArrayList;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringBuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.discord.stringBuilder.UserStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class UnfollowEmbedBuilder {
  private final DescriptionStringBuilder descriptionStringBuilder;
  private final EmbedBuilder embedBuilder;
  private final ImageUtils imageUtils;
  private final UserStringBuilder userStringBuilder;
  private LBMember member = null;

  @Inject
  public UnfollowEmbedBuilder(
      DescriptionStringBuilder descriptionStringBuilder,
      EmbedBuilderFactory embedBuilderFactory,
      ImageUtils imageUtils,
      UserStringBuilder userStringBuilder) {
    this.descriptionStringBuilder = descriptionStringBuilder;
    this.imageUtils = imageUtils;
    this.userStringBuilder = userStringBuilder;
    embedBuilder = embedBuilderFactory.create();
  }

  public UnfollowEmbedBuilder setMember(LBMember member) {
    this.member = member;
    return this;
  }

  public AbstractList<MessageEmbed> build() {
    String userName = userStringBuilder.setUserName(member.username).build();
    String description =
        String.format(
            "I unfollowed %s (%s).\nNo hard feelings I hope.", member.displayName, userName);
    embedBuilder.setDescription(descriptionStringBuilder.setDescriptionText(description).build());
    embedBuilder.setThumbnail(imageUtils.getTallest(member.avatar));

    ArrayList<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
