package jimlind.filmlinkd.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

/** Data model for user information directly mimicking the Firestore model form. */
@Getter
@Setter
public class User {
  public String id;

  public long checked;
  public long created;
  public String displayName;
  public String image;
  public String letterboxdId;
  @Deprecated public String lid;
  public long updated;
  public String userName;

  public List<Channel> channelList;
  public List<Channel> archivedChannelList;
  public Previous previous;
  public Footer footer;

  /**
   * Build a predictable ArrayList of channelIds strings from chaotic data in this model.
   *
   * @return A list of channelIds as strings. The contents are numeric, but we don't use these for
   *     calculations.
   */
  public List<String> getChannelIdList() {
    if (channelList == null) {
      return Collections.emptyList();
    }

    return channelList.stream()
        .map(channel -> channel.channelId)
        .filter(id -> id != null && !id.isBlank())
        .collect(Collectors.toList());
  }

  /**
   * Check both the `previous.list` and the `previous.lid` preferring the last item from
   * `previous.list` but falling back as needed.
   *
   * @return The LetterboxdId of the most recent previous entry.
   */
  public String getMostRecentPrevious() {
    List<String> list = previous.getList();
    return (list == null || list.isEmpty()) ? previous.lid : list.getLast();
  }

  /**
   * Gets the footer text from the user and ensures a string is always returned.
   *
   * @return The string of the footer text
   */
  public String getFooterText() {
    return (footer == null || footer.text == null || footer.text.isBlank()) ? "" : footer.text;
  }

  /**
   * Gets the footer icon from the user and ensures a string is always returned.
   *
   * @return The string of the footer icon
   */
  public String getFooterIcon() {
    return (footer == null || footer.icon == null || footer.icon.isBlank()) ? "" : footer.icon;
  }

  /**
   * Creates the user data as generic object matching the same structure so that Firestore can
   * process it natively.
   *
   * @return a {@link Map} containing the data outside the model
   */
  public Map<String, Object> toMap() {
    Gson gson = new Gson();
    String json = gson.toJson(this);
    return gson.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
  }

  /** Storage object for how channelIds are stored in Firestore. */
  @Getter
  @Setter
  public static class Channel {
    public String channelId;
  }

  /** Storage object for previous entries for this user. */
  @Getter
  @Setter
  public static class Previous {
    // This `id` is sometimes a string and sometimes a number. It is totally useless data.
    @Deprecated public Object id;
    public String lid;
    public List<String> list;
    public long published;
    public String uri;
  }

  /** Storage object for footer that is included with users posts. */
  public static class Footer {
    public String icon;
    public String text;
  }
}
