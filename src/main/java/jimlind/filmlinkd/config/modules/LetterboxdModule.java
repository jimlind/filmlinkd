package jimlind.filmlinkd.config.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.system.letterboxd.api.Client;
import jimlind.filmlinkd.system.letterboxd.api.ContributorApi;
import jimlind.filmlinkd.system.letterboxd.api.FilmApi;
import jimlind.filmlinkd.system.letterboxd.api.FilmsApi;
import jimlind.filmlinkd.system.letterboxd.api.ListApi;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesApi;
import jimlind.filmlinkd.system.letterboxd.api.MemberApi;
import jimlind.filmlinkd.system.letterboxd.api.MemberStatisticsApi;
import jimlind.filmlinkd.system.letterboxd.utils.DateUtils;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import jimlind.filmlinkd.system.letterboxd.utils.LinkUtils;
import jimlind.filmlinkd.system.letterboxd.web.LetterboxdIdWeb;
import jimlind.filmlinkd.system.letterboxd.web.MemberWeb;

/** Letterboxd modules for dependency injection. */
public class LetterboxdModule extends AbstractModule {
  @Override
  protected void configure() {
    // Letterboxd API Modules
    bind(Client.class).in(Scopes.SINGLETON);
    bind(ContributorApi.class).in(Scopes.SINGLETON);
    bind(FilmApi.class).in(Scopes.SINGLETON);
    bind(FilmsApi.class).in(Scopes.SINGLETON);
    bind(ListApi.class).in(Scopes.SINGLETON);
    bind(LogEntriesApi.class).in(Scopes.SINGLETON);
    bind(MemberApi.class).in(Scopes.SINGLETON);
    bind(MemberStatisticsApi.class).in(Scopes.SINGLETON);

    // Letterboxd Web Scraper Modules
    bind(LetterboxdIdWeb.class).in(Scopes.SINGLETON);
    bind(MemberWeb.class).in(Scopes.SINGLETON);

    // Letterboxd Utils
    bind(DateUtils.class).in(Scopes.SINGLETON);
    bind(ImageUtils.class).in(Scopes.SINGLETON);
    bind(LinkUtils.class).in(Scopes.SINGLETON);
  }
}
