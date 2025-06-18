package jimlind.filmlinkd.system.discord.helper;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.api.MemberAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.web.MemberWeb;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

public class AccountHelper {
  private final MemberAPI memberAPI;
  private final MemberWeb memberWeb;

  @Inject
  AccountHelper(MemberAPI memberAPI, MemberWeb memberWeb) {
    this.memberAPI = memberAPI;
    this.memberWeb = memberWeb;
  }

  @Nullable
  public LBMember getMember(SlashCommandInteractionEvent event) {
    OptionMapping accountMap = event.getInteraction().getOption("account");
    String userName = accountMap != null ? accountMap.getAsString() : "";
    String userLID = this.memberWeb.getMemberLIDFromUsername(userName);

    return this.memberAPI.fetch(userLID);
  }
}
