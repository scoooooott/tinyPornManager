package org.tinymediamanager.scraper.tmdb;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.CastMember;
import org.tinymediamanager.scraper.HasFindByIMDBID;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaArt;
import org.tinymediamanager.scraper.MediaArtifactType;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaMetadata.Genres;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.MetadataKey;
import org.tinymediamanager.scraper.MetadataUtil;
import org.tinymediamanager.scraper.ProviderInfo;
import org.tinymediamanager.scraper.SearchQuery;

import com.moviejukebox.themoviedb.MovieDbException;
import com.moviejukebox.themoviedb.TheMovieDb;
import com.moviejukebox.themoviedb.model.Artwork;
import com.moviejukebox.themoviedb.model.ArtworkType;
import com.moviejukebox.themoviedb.model.Genre;
import com.moviejukebox.themoviedb.model.MovieDb;
import com.moviejukebox.themoviedb.model.Person;
import com.moviejukebox.themoviedb.model.PersonType;
import com.moviejukebox.themoviedb.tools.ApiUrl;

public class TmdbMetadataProvider implements IMediaMetadataProvider, HasFindByIMDBID {

  private static final Logger               log      = Logger.getLogger(TmdbMetadataProvider.class);
  private static final TmdbMetadataProvider instance = new TmdbMetadataProvider();

  private TheMovieDb                        tmdb;

  public enum PosterSizes {
    w92, w154, w185, w342, w500, original
  }

  public enum FanartSizes {
    w300, w780, w1280, original
  }

  public enum Languages {
    de("Deutsch"), en("English");

    private String title;

    private Languages(String title) {
      this.title = title;
    }

    public String toString() {
      return this.title;
    }
  }

  private TmdbMetadataProvider() {
    try {
      tmdb = new TheMovieDb("6247670ec93f4495a36297ff88f7cd15");
    }
    catch (MovieDbException e) {
      e.printStackTrace();
    }
  }

  public static TmdbMetadataProvider getInstance() {
    return instance;
  }

  @Override
  public MediaMetadata getMetadataForIMDBId(String imdbId) throws Exception {
    log.debug("TMDB: getMetadataForIMDBId(imdbId): " + imdbId);

    // get the tmdbid for this imdbid
    MovieDb movieInfo = tmdb.getMovieInfoImdb(imdbId, Globals.settings.getScraperTmdbLanguage().name());
    int tmdbId = movieInfo.getId();

    // get images if a tmdb id has been found
    if (tmdbId > 0) {
      return getMetaData(tmdbId);
    }
    return null;
  }

  @Override
  public ProviderInfo getInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  public List<TmdbArtwork> getArtwork(String imdbId, MediaArtifactType type) throws Exception {
    log.debug("TMDB: getArtwork(imdbId): " + imdbId);

    List<TmdbArtwork> artwork = new ArrayList<TmdbArtwork>();

    // get the tmdbid for this imdbid
    MovieDb movieInfo = tmdb.getMovieInfoImdb(imdbId, Globals.settings.getScraperTmdbLanguage().name());
    int tmdbId = movieInfo.getId();

    // get images if a tmdb id has been found
    if (tmdbId > 0) {
      artwork = getArtwork(tmdbId, type);
    }

    return artwork;
  }

  public List<TmdbArtwork> getArtwork(int tmdbId, MediaArtifactType type) throws Exception {
    log.debug("TMDB: getArtwork(tmdbId): " + tmdbId);

    String baseUrl = tmdb.getConfiguration().getBaseUrl();
    List<TmdbArtwork> artwork = new ArrayList<TmdbArtwork>();

    // posters and fanart (first search with lang)
    List<Artwork> movieImages = tmdb.getMovieImages(tmdbId, Globals.settings.getImageTmdbLangugage().name());
    // posters and fanart (without lang)
    List<Artwork> movieImages_wo_lang = tmdb.getMovieImages(tmdbId, "");
    movieImages.addAll(movieImages_wo_lang);

    for (Artwork image : movieImages) {
      String path = "";

      // artwork is a poster
      if (image.getArtworkType() == ArtworkType.POSTER && type == MediaArtifactType.POSTER) {
        TmdbArtwork poster = new TmdbArtwork(MediaArtifactType.POSTER, baseUrl, image.getFilePath());
        artwork.add(poster);
      }

      // artwork is a fanart
      if (image.getArtworkType() == ArtworkType.BACKDROP && type == MediaArtifactType.BACKGROUND) {
        TmdbArtwork backdrop = new TmdbArtwork(MediaArtifactType.BACKGROUND, baseUrl, image.getFilePath());
        artwork.add(backdrop);
      }
    }

    return artwork;
  }

  public MediaMetadata getMetaData(int tmdbId) throws Exception {
    log.debug("TMDB: getMetadata(tmdbId): " + tmdbId);

    MediaMetadata md = new MediaMetadata();

    MovieDb movie = tmdb.getMovieInfo(tmdbId, Globals.settings.getScraperTmdbLanguage().name());
    String baseUrl = tmdb.getConfiguration().getBaseUrl();

    MediaMetadata.updateMDValue(md, MetadataKey.TMDB_ID, String.valueOf(movie.getId()));
    MediaMetadata.updateMDValue(md, MetadataKey.PLOT, movie.getOverview());
    MediaMetadata.updateMDValue(md, MetadataKey.MEDIA_TITLE, movie.getTitle());
    MediaMetadata.updateMDValue(md, MetadataKey.ORIGINAL_TITLE, movie.getOriginalTitle());
    MediaMetadata.updateMDValue(md, MetadataKey.USER_RATING, String.valueOf(movie.getVoteAverage()));
    MediaMetadata.updateMDValue(md, MetadataKey.RUNNING_TIME, String.valueOf(movie.getRuntime()));
    MediaMetadata.updateMDValue(md, MetadataKey.TAGLINE, movie.getTagline());
    if (movie.getImdbID() != null && movie.getImdbID().contains("tt")) {
      MediaMetadata.updateMDValue(md, MetadataKey.IMDB_ID, movie.getImdbID());
    }

    // parse release date to year
    String releaseDate = movie.getReleaseDate();
    if (releaseDate.length() > 3) {
      MediaMetadata.updateMDValue(md, MetadataKey.YEAR, releaseDate.substring(0, 4));
    }
    MediaMetadata.updateMDValue(md, MetadataKey.RELEASE_DATE, releaseDate);

    // posters and fanart (first search with lang)
    List<Artwork> movieImages = tmdb.getMovieImages(tmdbId, Globals.settings.getImageTmdbLangugage().name());
    // posters and fanart (without lang)
    List<Artwork> movieImages_wo_lang = tmdb.getMovieImages(tmdbId, "");
    movieImages.addAll(movieImages_wo_lang);

    for (Artwork image : movieImages) {
      if (image.getArtworkType() == ArtworkType.POSTER) {
        String path = baseUrl + Globals.settings.getImageTmdbPosterSize() + image.getFilePath();
        processMediaArt(md, MediaArtifactType.POSTER, "Poster", path);
      }

      if (image.getArtworkType() == ArtworkType.BACKDROP) {
        String path = baseUrl + Globals.settings.getImageTmdbFanartSize() + image.getFilePath();
        processMediaArt(md, MediaArtifactType.BACKGROUND, "Background", path);
      }

    }

    // cast
    List<Person> cast = tmdb.getMovieCasts(tmdbId);
    for (Person castMember : cast) {
      CastMember cm = new CastMember();
      if (castMember.getPersonType() == PersonType.CAST) {
        cm.setType(CastMember.ACTOR);
        cm.setCharacter(castMember.getCharacter());
      }
      else if (castMember.getPersonType() == PersonType.CREW) {
        if (castMember.getJob().equals("Director")) {
          cm.setType(CastMember.DIRECTOR);
        }
        else if (castMember.getJob().equals("Author")) {
          cm.setType(CastMember.WRITER);
        }
        else {
          continue;
        }
      }
      else {
        continue;
      }

      cm.setName(castMember.getName());
      cm.setPart(castMember.getDepartment());
      md.addCastMember(cm);
    }

    // genres
    List<Genre> genres = movie.getGenres();
    for (Genre genre : genres) {
      addGenre(genre, md);
    }

    return md;
  }

  private void addGenre(Genre genre, MediaMetadata md) {
    switch (genre.getId()) {
      case 28:
        md.addGenre(Genres.ACTION);
        break;

      case 12:
        md.addGenre(Genres.ADVENTURE);
        break;

      case 16:
        md.addGenre(Genres.ANIMATION);
        break;

      case 35:
        md.addGenre(Genres.COMEDY);
        break;

      case 80:
        md.addGenre(Genres.CRIME);
        break;

      case 105:
        md.addGenre(Genres.DISASTER);
        break;

      case 99:
        md.addGenre(Genres.DOCUMENTARY);
        break;

      case 18:
        md.addGenre(Genres.DRAMA);
        break;

      case 82:
        md.addGenre(Genres.EASTERN);
        break;

      case 2916:
        md.addGenre(Genres.EROTIC);
        break;

      case 10751:
        md.addGenre(Genres.FAMILY);
        break;

      case 10750:
        md.addGenre(Genres.FAN_FILM);
        break;

      case 14:
        md.addGenre(Genres.FANTASY);
        break;

      case 10753:
        md.addGenre(Genres.FILM_NOIR);
        break;

      case 10769:
        md.addGenre(Genres.FOREIGN);
        break;

      case 36:
        md.addGenre(Genres.HISTORY);
        break;

      case 10595:
        md.addGenre(Genres.HOLIDAY);
        break;

      case 27:
        md.addGenre(Genres.HORROR);
        break;

      case 10756:
        md.addGenre(Genres.INDIE);
        break;

      case 10402:
        md.addGenre(Genres.MUSIC);
        break;

      case 22:
        md.addGenre(Genres.MUSICAL);
        break;

      case 9648:
        md.addGenre(Genres.MYSTERY);
        break;

      case 10754:
        md.addGenre(Genres.NEO_NOIR);
        break;

      case 1115:
        md.addGenre(Genres.ROAD_MOVIE);
        break;

      case 10749:
        md.addGenre(Genres.ROMANCE);
        break;

      case 878:
        md.addGenre(Genres.SCIENCE_FICTION);
        break;

      case 10755:
        md.addGenre(Genres.SHORT);
        break;

      case 9805:
        md.addGenre(Genres.SPORT);
        break;

      case 10758:
        md.addGenre(Genres.SPORTING_EVENT);
        break;

      case 10757:
        md.addGenre(Genres.SPORTS_FILM);
        break;

      case 10748:
        md.addGenre(Genres.SUSPENSE);
        break;

      case 10770:
        md.addGenre(Genres.TV_MOVIE);
        break;

      case 53:
        md.addGenre(Genres.THRILLER);
        break;

      case 10752:
        md.addGenre(Genres.WAR);
        break;

      case 37:
        md.addGenre(Genres.WESTERN);
        break;

    }

  }

  @Override
  public MediaMetadata getMetaData(MediaSearchResult result) throws Exception {
    log.debug("TMDB: getMetadata(result): " + result);
    int tmdbId = Integer.parseInt(result.getId());

    return getMetaData(tmdbId);
  }

  @Override
  public List<MediaSearchResult> search(SearchQuery query) throws Exception {
    List<MediaSearchResult> resultList = new ArrayList<MediaSearchResult>();
    String searchString = query.get(SearchQuery.Field.QUERY);

    log.debug("========= BEGIN TMDB Scraper Search for: " + searchString);
    ApiUrl tmdbSearchMovie = new ApiUrl(tmdb, "search/movie");
    URL url = tmdbSearchMovie.getQueryUrl(searchString, Globals.settings.getScraperTmdbLanguage().name(), 1);
    log.debug(url.toString());

    List<MovieDb> moviesFound = tmdb.searchMovie(searchString, Globals.settings.getScraperTmdbLanguage().name(), false);

    if (moviesFound == null) {
      return resultList;
    }

    log.debug("found " + moviesFound.size() + " results");

    for (MovieDb movie : moviesFound) {
      MediaSearchResult sr = new MediaSearchResult();

      sr.setId(Integer.toString(movie.getId()));
      sr.setIMDBId(movie.getImdbID());
      sr.setTitle(movie.getTitle());
      sr.setOriginalTitle(movie.getOriginalTitle());

      // parse release date to year
      String releaseDate = movie.getReleaseDate();
      if (releaseDate != null && releaseDate.length() > 3) {
        sr.setYear(movie.getReleaseDate().substring(0, 4));
      }

      // populate extra args
      MetadataUtil.copySearchQueryToSearchResult(query, sr);

      sr.setScore(MetadataUtil.calculateScore(searchString, movie.getTitle()));
      resultList.add(sr);
    }
    Collections.sort(resultList);
    Collections.reverse(resultList);

    return resultList;
  }

  @Override
  public MediaType[] getSupportedSearchTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  private void processMediaArt(MediaMetadata md, MediaArtifactType type, String label, String image) {
    MediaArt ma = new MediaArt();
    ma.setDownloadUrl(image);
    ma.setLabel(label);
    // ma.setProviderId(getInfo().getId());
    ma.setType(type);
    md.addMediaArt(ma);
  }

}
