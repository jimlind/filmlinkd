package jimlind.filmlinkd.model;

import com.google.gson.Gson;

/**
 * Command Model used to inform all shards and applications about current state.
 *
 * @param type The type of command
 * @param user The user who initiated the command
 * @param entry The primary argument or entry for the command
 */
public record Command(jimlind.filmlinkd.model.Command.Type type, String user, String entry) {
  /**
   * Get the model as a JSON String. Mostly for easy transit as a PubSub message.
   *
   * @return A JSON String
   */
  public String toJson() {
    return new Gson().toJson(this);
  }

  /** Currently only supported types of commands are FOLLOW. */
  public enum Type {
    FOLLOW,
  }
}
