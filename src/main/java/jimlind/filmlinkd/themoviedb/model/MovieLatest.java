package jimlind.filmlinkd.themoviedb.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** Data model returned from The Movie Database API for their latest movie. */
@Getter
@Setter
public class MovieLatest {

  private boolean adult;

  @SerializedName("backdrop_path")
  private String backdropPath;

  @SerializedName("belongs_to_collection")
  private BelongsToCollection belongsToCollection;

  private int budget;
  private List<Genre> genres;
  private String homepage;
  private int id;

  @SerializedName("imdb_id")
  private String imdbId;

  @SerializedName("original_language")
  private String originalLanguage;

  @SerializedName("original_title")
  private String originalTitle;

  private String overview;
  private double popularity;

  @SerializedName("poster_path")
  private String posterPath;

  @SerializedName("production_companies")
  private List<ProductionCompany> productionCompanies;

  @SerializedName("production_countries")
  private List<ProductionCountry> productionCountries;

  @SerializedName("release_date")
  private String releaseDate;

  private long revenue;
  private int runtime;

  @SerializedName("spoken_languages")
  private List<SpokenLanguage> spokenLanguages;

  private String status;
  private String tagline;
  private String title;
  private boolean video;

  @SerializedName("vote_average")
  private double voteAverage;

  @SerializedName("vote_count")
  private int voteCount;

  /** Data model reflecting TMDBs concept of a collection. */
  @Getter
  @Setter
  public static class BelongsToCollection {
    private int id;
    private String name;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("backdrop_path")
    private String backdropPath;
  }

  /** Data model reflecting TMDBs concept of a genre. */
  @Getter
  @Setter
  public static class Genre {
    private int id;
    private String name;
  }

  /** Data model for the film's production company. */
  @Getter
  @Setter
  public static class ProductionCompany {
    private int id;

    @SerializedName("logo_path")
    private String logoPath;

    private String name;

    @SerializedName("origin_country")
    private String originCountry;
  }

  /** Data model for the film's production country (String and ISO 3166-1 code). */
  @Getter
  @Setter
  public static class ProductionCountry {
    @SerializedName("iso_3166_1")
    private String iso31661;

    private String name;
  }

  /** Data model for the film's spoken language (Strings and ISO 3166-1 code). */
  @Getter
  @Setter
  public static class SpokenLanguage {
    @SerializedName("english_name")
    private String englishName;

    @SerializedName("iso_639_1")
    private String iso6391;

    private String name;
  }
}
