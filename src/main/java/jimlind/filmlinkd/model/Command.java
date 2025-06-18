package jimlind.filmlinkd.model;

import com.google.gson.Gson;

public class Command {
  public Type type = Type.FOLLOW;
  public String user;
  public String entry;

  public String toJson() {
    return new Gson().toJson(this);
  }

  public enum Type {
    FOLLOW,
  }
}
