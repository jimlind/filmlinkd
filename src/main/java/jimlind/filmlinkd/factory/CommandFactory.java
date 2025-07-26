package jimlind.filmlinkd.factory;

import jimlind.filmlinkd.model.Command;

/** A factory for creating instances of the {@link Command} model. */
public class CommandFactory {

  /**
   * Creates and initializes a new Command object.
   *
   * @param type The type of the command to create.
   * @param user The user associated with the command
   * @param entry The primary argument or entry for the command
   * @return A new, populated {@link Command} object.
   */
  public Command create(Command.Type type, String user, String entry) {
    Command commandObject = new Command();
    commandObject.setType(type);
    commandObject.setUser(user);
    commandObject.setEntry(entry);

    return commandObject;
  }
}
