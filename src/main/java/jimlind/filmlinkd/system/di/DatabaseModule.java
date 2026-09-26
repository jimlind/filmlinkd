package jimlind.filmlinkd.system.di;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.database.channel.UserChannelDao;
import jimlind.filmlinkd.database.user.UserDao;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;

/** Dagger 2 module for providing local database components. */
@Module
public class DatabaseModule {
  private static HikariDataSource extractDataSource(HikariConfig hikariConfig) {
    return new HikariDataSource(hikariConfig);
  }

  private static HikariConfig extractHikariConfig(AppConfig appConfig) {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(appConfig.getDatabaseUrl());
    hikariConfig.setUsername(appConfig.getDatabaseUser());
    hikariConfig.setPassword(appConfig.getDatabasePassword());
    return hikariConfig;
  }

  private static void migrate(HikariDataSource dataSource) {
    Flyway.configure().dataSource(dataSource).load().migrate();
  }

  /**
   * Provides the connection pool for the local database.
   *
   * @param appConfig Contains application and environment variables
   * @return Build a Hikari database connection pool
   */
  @Provides
  @Singleton
  public HikariDataSource provideHikariDataSource(AppConfig appConfig) {
    HikariConfig hikariConfig = extractHikariConfig(appConfig);
    HikariDataSource dataSource = extractDataSource(hikariConfig);
    migrate(dataSource);
    return dataSource;
  }

  /**
   * Provides the JDBI database connection.
   *
   * @param dataSource The Hikari connection pool
   * @return A JDBI handle for the connection pool
   */
  @Provides
  @Singleton
  public Jdbi provideJdbi(HikariDataSource dataSource) {
    return Jdbi.create(dataSource);
  }

  /**
   * Provides the user data access object.
   *
   * @param jdbi The JDBI database connection
   * @return A DAO for user data queries
   */
  @Provides
  @Singleton
  public UserDao provideUserDao(Jdbi jdbi) {
    return new UserDao(jdbi);
  }

  /**
   * Provides the user channel data access object.
   *
   * @param jdbi The JDBI database connection
   * @return A DAO for user channel data queries
   */
  @Provides
  @Singleton
  public UserChannelDao provideUserChannelDao(Jdbi jdbi) {
    return new UserChannelDao(jdbi);
  }
}
