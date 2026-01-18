package jimlind.filmlinkd.core.di;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import jimlind.filmlinkd.discord.event.handler.ContributorHandler;
import jimlind.filmlinkd.discord.event.handler.DiaryHandler;
import jimlind.filmlinkd.discord.event.handler.FilmHandler;
import jimlind.filmlinkd.discord.event.handler.FollowHandler;
import jimlind.filmlinkd.discord.event.handler.Handler;

@Module
public class HandlerMapModule {
  @Provides
  @IntoMap
  @StringKey("contributor")
  Handler provideContributionHandler(ContributorHandler handler) {
    return handler;
  }

  @Provides
  @IntoMap
  @StringKey("diary")
  Handler provideDiaryHandler(DiaryHandler handler) {
    return handler;
  }

  @Provides
  @IntoMap
  @StringKey("film")
  Handler provideFilmHandler(FilmHandler handler) {
    return handler;
  }

  @Provides
  @IntoMap
  @StringKey("follow")
  Handler provideFollowHandler(FollowHandler handler) {
    return handler;
  }
}
