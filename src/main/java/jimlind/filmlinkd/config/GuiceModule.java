package jimlind.filmlinkd.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.factory.ScrapedResultCheckerFactory;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.runnable.GeneralScraper;
import jimlind.filmlinkd.runnable.GeneralUserCacheClearer;
import jimlind.filmlinkd.runnable.StatLogger;
import jimlind.filmlinkd.system.DiscordSystem;
import jimlind.filmlinkd.system.EntryCache;
import jimlind.filmlinkd.system.GeneralScraperScheduler;
import jimlind.filmlinkd.system.GeneralUserCache;
import jimlind.filmlinkd.system.MessageReceiver;
import jimlind.filmlinkd.system.ScrapedResultQueue;
import jimlind.filmlinkd.system.ShutdownThread;
import jimlind.filmlinkd.system.VipScraperScheduler;
import jimlind.filmlinkd.system.discord.ConnectionManager;
import jimlind.filmlinkd.system.discord.EventListener;
import jimlind.filmlinkd.system.discord.ShardManagerStorage;
import jimlind.filmlinkd.system.discord.SlashCommandManager;
import jimlind.filmlinkd.system.discord.embedbuilder.ContributorEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.DiaryEntryEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.DiaryListEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.FilmEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.FollowEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.FollowingEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.HelpEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.ListEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.LoggedEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.RefreshEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.UnfollowEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.UserEmbedBuilder;
import jimlind.filmlinkd.system.discord.eventhandler.ContributorHandler;
import jimlind.filmlinkd.system.discord.eventhandler.DiaryHandler;
import jimlind.filmlinkd.system.discord.eventhandler.FilmHandler;
import jimlind.filmlinkd.system.discord.eventhandler.FollowHandler;
import jimlind.filmlinkd.system.discord.eventhandler.FollowingHandler;
import jimlind.filmlinkd.system.discord.eventhandler.HelpHandler;
import jimlind.filmlinkd.system.discord.eventhandler.ListHandler;
import jimlind.filmlinkd.system.discord.eventhandler.LoggedHandler;
import jimlind.filmlinkd.system.discord.eventhandler.RefreshHandler;
import jimlind.filmlinkd.system.discord.eventhandler.RouletteHandler;
import jimlind.filmlinkd.system.discord.eventhandler.UnfollowHandler;
import jimlind.filmlinkd.system.discord.eventhandler.UserHandler;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.discord.helper.ChannelHelper;
import jimlind.filmlinkd.system.discord.stringbuilder.CountStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.DirectorsStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.RuntimeStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.StarsStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.TextStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.UserStringBuilder;
import jimlind.filmlinkd.system.google.FirestoreManager;
import jimlind.filmlinkd.system.google.PubSubSubscriberListener;
import jimlind.filmlinkd.system.google.SecretManager;
import jimlind.filmlinkd.system.letterboxd.api.Client;
import jimlind.filmlinkd.system.letterboxd.api.ContributorApi;
import jimlind.filmlinkd.system.letterboxd.api.FilmApi;
import jimlind.filmlinkd.system.letterboxd.api.ListApi;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesApi;
import jimlind.filmlinkd.system.letterboxd.api.MemberApi;
import jimlind.filmlinkd.system.letterboxd.api.MemberStatisticsApi;
import jimlind.filmlinkd.system.letterboxd.utils.DateUtils;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import jimlind.filmlinkd.system.letterboxd.utils.LinkUtils;
import jimlind.filmlinkd.system.letterboxd.web.LetterboxdIdWeb;
import jimlind.filmlinkd.system.letterboxd.web.MemberWeb;

/** Contains all the guts for dependency injection to work. */
public class GuiceModule extends AbstractModule {
  @Override
  protected void configure() {
    // Application Level Modules
    bind(AppConfig.class).in(Scopes.SINGLETON);
    bind(EntryCache.class).in(Scopes.SINGLETON);
    bind(MessageReceiver.class).in(Scopes.SINGLETON);
    bind(ScrapedResultQueue.class).in(Scopes.SINGLETON);
    bind(ShutdownThread.class).in(Scopes.SINGLETON);

    // Factories
    bind(EmbedBuilderFactory.class).in(Scopes.SINGLETON);
    bind(ScrapedResultCheckerFactory.class).in(Scopes.SINGLETON);
    bind(UserFactory.class).in(Scopes.SINGLETON);

    // General System Modules
    bind(EntryCache.class).in(Scopes.SINGLETON);
    bind(PubSubSubscriberListener.class).in(Scopes.SINGLETON);
    bind(GeneralScraperScheduler.class).in(Scopes.SINGLETON);
    bind(GeneralScraper.class).in(Scopes.SINGLETON);
    bind(GeneralUserCache.class).in(Scopes.SINGLETON);
    bind(GeneralUserCacheClearer.class).in(Scopes.SINGLETON);
    bind(VipScraperScheduler.class).in(Scopes.SINGLETON);
    bind(StatLogger.class).in(Scopes.SINGLETON);

    // Discord System Modules
    bind(DiscordSystem.class).in(Scopes.SINGLETON);
    bind(ConnectionManager.class).in(Scopes.SINGLETON);
    bind(EventListener.class).in(Scopes.SINGLETON);
    bind(ShardManagerStorage.class).in(Scopes.SINGLETON);
    bind(SlashCommandManager.class).in(Scopes.SINGLETON);

    // Discord Embed Builders
    bind(ContributorEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(DiaryEntryEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(DiaryListEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(FilmEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(FollowEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(FollowingEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(HelpEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(ListEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(LoggedEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(RefreshEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(UnfollowEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(UserEmbedBuilder.class).in(Scopes.NO_SCOPE);

    // Discord Event Handlers
    bind(ContributorHandler.class).in(Scopes.SINGLETON);
    bind(DiaryHandler.class).in(Scopes.SINGLETON);
    bind(FilmHandler.class).in(Scopes.SINGLETON);
    bind(FollowHandler.class).in(Scopes.SINGLETON);
    bind(FollowingHandler.class).in(Scopes.SINGLETON);
    bind(HelpHandler.class).in(Scopes.SINGLETON);
    bind(ListHandler.class).in(Scopes.SINGLETON);
    bind(LoggedHandler.class).in(Scopes.SINGLETON);
    bind(RefreshHandler.class).in(Scopes.SINGLETON);
    bind(RouletteHandler.class).in(Scopes.SINGLETON);
    bind(UnfollowHandler.class).in(Scopes.SINGLETON);
    bind(UserHandler.class).in(Scopes.SINGLETON);

    // Discord Helper Modules
    bind(AccountHelper.class).in(Scopes.SINGLETON);
    bind(ChannelHelper.class).in(Scopes.SINGLETON);

    // Discord Embed String Builders
    bind(CountStringBuilder.class).in(Scopes.NO_SCOPE);
    bind(DescriptionStringBuilder.class).in(Scopes.NO_SCOPE);
    bind(DirectorsStringBuilder.class).in(Scopes.NO_SCOPE);
    bind(RuntimeStringBuilder.class).in(Scopes.NO_SCOPE);
    bind(StarsStringBuilder.class).in(Scopes.NO_SCOPE);
    bind(TextStringBuilder.class).in(Scopes.NO_SCOPE);
    bind(UserStringBuilder.class).in(Scopes.NO_SCOPE);

    // Google System Modules
    bind(SecretManager.class).in(Scopes.SINGLETON);
    bind(FirestoreManager.class).in(Scopes.SINGLETON);

    // Letterboxd API Modules
    bind(Client.class).in(Scopes.SINGLETON);
    bind(ContributorApi.class).in(Scopes.SINGLETON);
    bind(FilmApi.class).in(Scopes.SINGLETON);
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
