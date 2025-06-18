package jimlind.filmlinkd.factory;

import java.awt.*;
import jimlind.filmlinkd.model.Command;

public class CommandFactory {
  public Command create(Command.Type type, String user, String entry) {
    Command commandObject = new Command();
    commandObject.type = type;
    commandObject.user = user;
    commandObject.entry = entry;

    return commandObject;
  }
}
