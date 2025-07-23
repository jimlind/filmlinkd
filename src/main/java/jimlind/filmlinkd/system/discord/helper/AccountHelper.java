package jimlind.filmlinkd.system.discord.helper;

import com.google.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.api.MemberApi;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import jimlind.filmlinkd.system.letterboxd.web.MemberWeb;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

/** Interfaces with the web to handle account requests. */
public class AccountHelper {
  private final MemberApi memberApi;
  private final MemberWeb memberWeb;

  /**
   * Constructor for this class.
   *
   * @param memberApi Letterboxd API Service to access member data
   * @param memberWeb Letterboxd Web Wrapper to access member data
   */
  @Inject
  AccountHelper(MemberApi memberApi, MemberWeb memberWeb) {
    this.memberApi = memberApi;
    this.memberWeb = memberWeb;
  }

  /**
   * Get a Letterboxd API Member object based on a command event that contains the account option.
   * There are several different commands that use this same pattern so it's easy to target them. We
   * need to use the website to reliably translate the name to an id (this isn't found on the API).
   * Then use that id to grab the full model from teh Letterboxd API.
   *
   * @param event The SlashCommandInteractionEvent that contains the account option
   * @return null if not found or a full Letterboxd API Member class
   */
  @Nullable
  public LbMember getMember(SlashCommandInteractionEvent event) {
    OptionMapping accountMap = event.getInteraction().getOption("account");
    String userName = accountMap != null ? accountMap.getAsString() : "";
    String userLid = this.memberWeb.getMemberLidFromUsername(userName);

    return this.memberApi.fetch(userLid);
  }
}
