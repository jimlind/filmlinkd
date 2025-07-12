package jimlind.filmlinkd.system.discord.embedbuilder;

import com.google.inject.Inject;
import java.util.ArrayList;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.UserStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class FollowEmbedBuilder {
  private final DescriptionStringBuilder descriptionStringBuilder;
  private final EmbedBuilder embedBuilder;
  private final ImageUtils imageUtils;
  private final UserStringBuilder userStringBuilder;
  private LBMember member = null;

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

  public FollowEmbedBuilder setMember(LBMember member) {
    this.member = member;
    return this;
  }

  public ArrayList<MessageEmbed> build() {
    if (member == null) {
      return new ArrayList<>();
    }

    String userName = userStringBuilder.setUserName(member.username).build();
    String description =
        String.format(
            "I am now following %s (%s).\nI'll try to post their most recent entry in the appropriate channel.",
            member.displayName, userName);

    embedBuilder.setDescription(descriptionStringBuilder.setDescriptionText(description).build());
    embedBuilder.setThumbnail(imageUtils.getTallest(member.avatar));

    ArrayList<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
