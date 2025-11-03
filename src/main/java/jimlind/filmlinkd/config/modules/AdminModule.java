package jimlind.filmlinkd.config.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.admin.CleanChannels;
import jimlind.filmlinkd.admin.CleanUsers;
import jimlind.filmlinkd.admin.UndoChannelArchive;
import jimlind.filmlinkd.admin.channels.Archiver;
import jimlind.filmlinkd.admin.channels.LogFileWriter;
import jimlind.filmlinkd.admin.channels.NullFilter;
import jimlind.filmlinkd.admin.channels.WrongPermissionsFilter;
import jimlind.filmlinkd.admin.channels.WrongTypeFilter;

/** Admin modules for dependency injection. */
public class AdminModule extends AbstractModule {
  @Override
  protected void configure() {
    // Primary Interfaces
    bind(CleanChannels.class).in(Scopes.SINGLETON);
    bind(CleanUsers.class).in(Scopes.SINGLETON);
    bind(UndoChannelArchive.class).in(Scopes.SINGLETON);

    // Channel Related Classes
    bind(Archiver.class).in(Scopes.SINGLETON);
    bind(LogFileWriter.class).in(Scopes.SINGLETON);
    bind(NullFilter.class).in(Scopes.SINGLETON);
    bind(WrongPermissionsFilter.class).in(Scopes.SINGLETON);
    bind(WrongTypeFilter.class).in(Scopes.SINGLETON);
  }
}
