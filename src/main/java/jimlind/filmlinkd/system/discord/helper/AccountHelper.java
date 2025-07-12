package jimlind.filmlinkd.system.discord.helper;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.api.MemberApi;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.web.MemberWeb;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

public class AccountHelper {
  private final MemberApi memberApi;
  private final MemberWeb memberWeb;

  @Inject
  AccountHelper(MemberApi memberApi, MemberWeb memberWeb) {
    this.memberApi = memberApi;
    this.memberWeb = memberWeb;
  }

  @Nullable
  public LBMember getMember(SlashCommandInteractionEvent event) {
    OptionMapping accountMap = event.getInteraction().getOption("account");
    String userName = accountMap != null ? accountMap.getAsString() : "";
    String userLID = this.memberWeb.getMemberLIDFromUsername(userName);

    return this.memberApi.fetch(userLID);
  }
}
