/*
 * Copyright 2012 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.scraper.tmdb;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaCastMember;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.MetadataUtil;
import org.tinymediamanager.thirdparty.RingBuffer;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.model.Artwork;
import com.omertron.themoviedbapi.model.ArtworkType;
import com.omertron.themoviedbapi.model.Collection;
import com.omertron.themoviedbapi.model.CollectionInfo;
import com.omertron.themoviedbapi.model.Genre;
import com.omertron.themoviedbapi.model.Language;
import com.omertron.themoviedbapi.model.MovieDb;
import com.omertron.themoviedbapi.model.Person;
import com.omertron.themoviedbapi.model.PersonType;
import com.omertron.themoviedbapi.model.ProductionCompany;
import com.omertron.themoviedbapi.model.ReleaseInfo;
import com.omertron.themoviedbapi.model.Trailer;
import com.omertron.themoviedbapi.tools.ApiUrl;

/**
 * The Class TmdbMetadataProvider.
 * 
 * @author Manuel Laggner
 */
public class TmdbMetadataProvider implements IMediaMetadataProvider, IMediaArtworkProvider, IMediaTrailerProvider {

  /** The Constant logger. */
  private static final Logger           LOGGER            = Logger.getLogger(TmdbMetadataProvider.class);

  /** The tmdb. */
  private static TheMovieDbApi          tmdb;

  /** The Constant connectionCounter. */
  private static final RingBuffer<Long> connectionCounter = new RingBuffer<Long>(30);

  /** The provider info. */
  private static MediaProviderInfo      providerInfo      = new MediaProviderInfo("tmdb", "themoviedb.org",
                                                              "Scraper for themoviedb.org which is able to scrape movie metadata, artwork and trailers");

  /**
   * The Enum Languages.
   * 
   * @author Manuel Laggner
   */
  public enum Languages {

    /** The de. */
    de("Deutsch"),
    /** The en. */
    en("English");

    /** The title. */
    private String title;

    /**
     * Instantiates a new languages.
     * 
     * @param title
     *          the title
     */
    private Languages(String title) {
      this.title = title;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    public String toString() {
      return this.title;
    }
  }

  /**
   * The Enum PosterSizes.
   * 
   * @author Manuel Laggner
   */
  public enum PosterSizes {
    /** The original. */
    original,
    /** The w500. */
    w500,
    /** The w342. */
    w342,
    /** The w185. */
    w185,
    /** The w154. */
    w154,
    /** The w92. */
    w92
  }

  /**
   * The Enum FanartSizes.
   * 
   * @author Manuel Laggner
   */
  public enum FanartSizes {
    /** The original. */
    original,
    /** The w1280. */
    w1280,
    /** The w780. */
    w780,
    /** The w300. */
    w300
  }

  /**
   * Instantiates a new tmdb metadata provider.
   * 
   * @throws Exception
   *           the exception
   */
  public TmdbMetadataProvider() throws Exception {
    // create a new instance of the tmdb api
    if (tmdb == null) {
      try {
        tmdb = new TheMovieDbApi("6247670ec93f4495a36297ff88f7cd15");
      }
      catch (Exception e) {
        LOGGER.error("TmdbMetadataProvider", e);
        throw e;
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaMetadataProvider#getInfo()
   */
  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  /*
   * Starts a search for a movie in themoviedb.org
   * 
   * @see org.tinymediamanager.scraper.IMediaMetadataProvider#search(org.tinymediamanager .scraper.SearchQuery)
   */
  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaMetadataProvider#search(org.tinymediamanager .scraper.MediaSearchOptions)
   */
  @Override
  public List<MediaSearchResult> search(MediaSearchOptions query) throws Exception {
    LOGGER.debug("search() " + query.toString());
    List<MediaSearchResult> resultList = new ArrayList<MediaSearchResult>();
    String searchString = "";
    String baseUrl = "";
    int year = 0;

    // check type
    if (query.getMediaType() != MediaType.MOVIE) {
      throw new Exception("wrong media type for this scraper");
    }

    // detect the string to search
    if (StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.QUERY))) {
      searchString = query.get(MediaSearchOptions.SearchParam.QUERY);
    }

    if (StringUtils.isEmpty(searchString) && StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.TITLE))) {
      searchString = query.get(MediaSearchOptions.SearchParam.TITLE);
    }

    if (StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.YEAR))) {
      try {
        Integer.parseInt(query.get(MediaSearchOptions.SearchParam.YEAR));
      }
      catch (Exception e) {
        year = 0;
      }
    }

    if (StringUtils.isEmpty(searchString)) {
      LOGGER.debug("TMDB Scraper: empty searchString");
      return resultList;
    }

    // begin search
    LOGGER.debug("========= BEGIN TMDB Scraper Search for: " + searchString);
    ApiUrl tmdbSearchMovie = new ApiUrl(tmdb, "search/movie");
    tmdbSearchMovie.addArgument(ApiUrl.PARAM_LANGUAGE, Globals.settings.getScraperLanguage().name());
    URL url = tmdbSearchMovie.buildUrl();
    LOGGER.debug(url.toString().replace("&api_key=6247670ec93f4495a36297ff88f7cd15", "&<API_KEY>"));

    List<MovieDb> moviesFound = null;
    synchronized (tmdb) {
      // old api
      // moviesFound = tmdb.searchMovie(searchString,
      // Globals.settings.getScraperLanguage().name(), false);
      // new api
      trackConnections();
      moviesFound = tmdb.searchMovie(searchString, year, Globals.settings.getScraperLanguage().name(), false, 0);
      baseUrl = tmdb.getConfiguration().getBaseUrl();
    }

    if (moviesFound == null) {
      return resultList;
    }

    LOGGER.debug("found " + moviesFound.size() + " results");

    for (MovieDb movie : moviesFound) {
      MediaSearchResult sr = new MediaSearchResult(providerInfo.getId());
      sr.setId(Integer.toString(movie.getId()));
      sr.setIMDBId(movie.getImdbID());
      sr.setTitle(movie.getTitle());
      sr.setOriginalTitle(movie.getOriginalTitle());
      sr.setPosterUrl(baseUrl + "w342" + movie.getPosterPath());

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

  /**
   * Gets the meta data.
   * 
   * @param options
   *          the scrape options
   * @return the meta data
   * @throws Exception
   *           the exception
   */
  public MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getMetadata() " + options.toString());
    // check if there is a md in the result
    if (options.getResult() != null && options.getResult().getMetadata() != null) {
      LOGGER.debug("TMDB: getMetadata from cache: " + options.getResult());
      return options.getResult().getMetadata();
    }

    // get ids to scrape
    MediaMetadata md = new MediaMetadata(providerInfo.getId());

    int tmdbId = 0;

    // tmdbId from searchResult
    if (options.getResult() != null) {
      tmdbId = Integer.parseInt(options.getResult().getId());
    }

    // tmdbId from option
    if (tmdbId == 0) {
      tmdbId = options.getTmdbId();
    }

    // tmdbId via imdbId
    String imdbId = options.getImdbId();
    if (tmdbId == 0 && StringUtils.isNotEmpty(imdbId)) {
      // try to get tmdbId via imdbId
      tmdbId = getTmdbIdFromImdbId(imdbId);
    }

    if (tmdbId == 0) {
      LOGGER.warn("not possible to scrape from TMDB - no tmdbId found");
      return md;
    }

    // scrape
    LOGGER.debug("TMDB: getMetadata(tmdbId): " + tmdbId);
    MovieDb movie = null;
    String baseUrl = null;
    synchronized (tmdb) {
      trackConnections();
      movie = tmdb.getMovieInfo(tmdbId, Globals.settings.getScraperLanguage().name());
      baseUrl = tmdb.getConfiguration().getBaseUrl();
    }

    md.setTmdbId(movie.getId());
    if (movie.getBelongsToCollection() != null) {
      md.setTmdbIdSet(movie.getBelongsToCollection().getId());
    }
    md.setPlot(movie.getOverview());
    md.setTitle(movie.getTitle());
    md.setOriginalTitle(movie.getOriginalTitle());
    md.setRating(movie.getVoteAverage());
    md.setRuntime(movie.getRuntime());
    md.setTagline(movie.getTagline());
    md.setVoteCount(movie.getVoteCount());

    String spokenLanguages = "";
    for (Language lang : movie.getSpokenLanguages()) {
      if (StringUtils.isNotEmpty(spokenLanguages)) {
        spokenLanguages += ", ";
      }

      spokenLanguages += lang.getIsoCode();
    }
    md.setSpokenLanguages(spokenLanguages);

    if (movie.getImdbID() != null && MetadataUtil.isValidImdbId(movie.getImdbID())) {
      md.setImdbId(movie.getImdbID());
    }

    // production companies
    StringBuilder productionCompanies = new StringBuilder("");
    for (ProductionCompany company : movie.getProductionCompanies()) {
      if (!StringUtils.isEmpty(productionCompanies)) {

        productionCompanies.append(", ");
      }
      productionCompanies.append(company.getName().trim());
    }
    md.setProductionCompany(productionCompanies.toString());

    // parse release date to year
    String releaseDate = movie.getReleaseDate();
    if (!StringUtils.isEmpty(releaseDate) && releaseDate.length() > 3) {
      md.setYear(releaseDate.substring(0, 4));
    }
    md.setReleaseDate(releaseDate);

    // get certification
    List<ReleaseInfo> releaseInfo = null;
    synchronized (tmdb) {
      trackConnections();
      releaseInfo = tmdb.getMovieReleaseInfo(tmdbId, Globals.settings.getScraperLanguage().name());
    }

    for (ReleaseInfo info : releaseInfo) {
      // do not use any empty certifications
      if (StringUtils.isEmpty(info.getCertification())) {
        continue;
      }

      // only use the certification of the desired country (if any country has
      // been chosen)
      if (Globals.settings.getCertificationCountry() == null
          || Globals.settings.getCertificationCountry().getAlpha2().compareToIgnoreCase(info.getCountry()) == 0) {

        // Certification certification = new Certification(info.getCountry(),
        // info.getCertification());
        // md.addCertification(certification);
        md.addCertification(Certification.getCertification(info.getCountry(), info.getCertification()));
      }

      // // MPAA is an extra case for certification
      // if ("US".equals(info.getCountry())) {
      // MediaMetadata.updateMDValue(md, MetadataKey.MPAA_RATING,
      // info.getCertification());
      // }
    }

    // cast
    List<Person> cast = null;
    synchronized (tmdb) {
      trackConnections();
      cast = tmdb.getMovieCasts(tmdbId);
    }

    for (Person castMember : cast) {
      MediaCastMember cm = new MediaCastMember();
      if (castMember.getPersonType() == PersonType.CAST) {
        cm.setType(MediaCastMember.CastType.ACTOR);
        cm.setCharacter(castMember.getCharacter());
      }
      else if (castMember.getPersonType() == PersonType.CREW) {
        if ("Director".equals(castMember.getJob())) {
          cm.setType(MediaCastMember.CastType.DIRECTOR);
        }
        else if ("Writing".equals(castMember.getDepartment())) {
          cm.setType(MediaCastMember.CastType.WRITER);
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
      if (!StringUtils.isEmpty(castMember.getProfilePath())) {
        cm.setImageUrl(baseUrl + "w185" + castMember.getProfilePath());
      }
      md.addCastMember(cm);
    }

    // MediaGenres2
    List<Genre> MediaGenres2 = movie.getGenres();
    for (Genre genre : MediaGenres2) {
      addGenre(genre, md);
    }

    return md;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaArtworkProvider#getArtwork(org. tinymediamanager.scraper.MediaScrapeOptions)
   */
  @Override
  public List<MediaArtwork> getArtwork(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getArtwork() " + options.toString());
    MediaArtworkType artworkType = options.getArtworkType();

    int tmdbId = options.getTmdbId();
    String imdbId = options.getImdbId();

    if (tmdbId == 0 && StringUtils.isNotEmpty(imdbId)) {
      // try to get tmdbId via imdbId
      tmdbId = getTmdbIdFromImdbId(imdbId);
    }

    List<Artwork> movieImages = null;
    synchronized (tmdb) {
      trackConnections();
      // posters and fanart
      movieImages = tmdb.getMovieImages(tmdbId, "");
    }

    List<MediaArtwork> artwork = prepareArtwork(movieImages, artworkType, tmdbId);

    // buffer the artwork
    MediaMetadata md = options.getMetadata();
    if (md != null) {
      md.addMediaArt(artwork);
    }

    return artwork;
  }

  /**
   * Gets the trailers.
   * 
   * @param options
   *          the options
   * @return the trailers
   * @throws Exception
   *           the exception
   */
  public List<MediaTrailer> getTrailers(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getTrailers() " + options.toString());
    List<MediaTrailer> trailers = new ArrayList<MediaTrailer>();

    int tmdbId = options.getTmdbId();
    String imdbId = options.getImdbId();

    if (tmdbId == 0 && StringUtils.isNotEmpty(imdbId)) {
      // try to get tmdbId via imdbId
      tmdbId = getTmdbIdFromImdbId(imdbId);
    }

    if (tmdbId == 0) {
      LOGGER.warn("not possible to scrape from TMDB - no tmdbId found");
      return trailers;
    }

    LOGGER.debug("TMDB: getTrailers(tmdbId): " + tmdbId);

    try {
      synchronized (tmdb) {
        trackConnections();
        // get trailers from tmdb (with specified langu and without)
        List<Trailer> tmdbTrailers = tmdb.getMovieTrailers(tmdbId, Globals.settings.getScraperLanguage().name());
        List<Trailer> tmdbTrailersWoLang = tmdb.getMovieTrailers(tmdbId, "");
        tmdbTrailers.addAll(tmdbTrailersWoLang);

        for (Trailer tmdbTrailer : tmdbTrailers) {
          boolean addTrailer = true;

          // youtube support
          if ("youtube".equalsIgnoreCase(tmdbTrailer.getWebsite())) {
            MediaTrailer trailer = new MediaTrailer();
            trailer.setName(tmdbTrailer.getName());
            trailer.setQuality(tmdbTrailer.getSize());
            trailer.setProvider(tmdbTrailer.getWebsite());

            // build url for youtube trailer
            StringBuilder sb = new StringBuilder();
            sb.append("http://www.youtube.com/watch?v=");
            sb.append(tmdbTrailer.getSource());
            if ("hd".equalsIgnoreCase(tmdbTrailer.getSize()) && !tmdbTrailer.getSource().contains("&hd=1")) {
              sb.append("&hd=1");
            }
            trailer.setUrl(sb.toString());

            // check for duplicates
            for (MediaTrailer addedTrailer : trailers) {
              if (addedTrailer.getUrl().equals(trailer.getUrl())) {
                addTrailer = false;
                break;
              }
            }

            if (addTrailer) {
              trailers.add(trailer);
            }
          }
        }
      }
    }
    catch (MovieDbException e) {
      LOGGER.error(e.getMessage());
    }

    return trailers;
  }

  /**
   * Converts the imdbId to the tmdbId.
   * 
   * @param imdbId
   *          the imdb id
   * @return the tmdb id from imdb id
   * @throws Exception
   *           the exception
   */
  public int getTmdbIdFromImdbId(String imdbId) throws Exception {
    // get the tmdbid for this imdbid
    MovieDb movieInfo = null;
    synchronized (tmdb) {
      trackConnections();
      movieInfo = tmdb.getMovieInfoImdb(imdbId, Globals.settings.getScraperLanguage().name());
    }

    if (movieInfo != null) {
      return movieInfo.getId();
    }
    return 0;
  }

  /**
   * The Class ArtworkComparator.
   * 
   * @author Manuel Laggner
   */
  private static class ArtworkComparator implements Comparator<Artwork> {
    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     * 
     * sort artwork: primary by language: preferred lang (ie de), en, others; then: score
     */
    @Override
    public int compare(Artwork arg0, Artwork arg1) {
      String preferredLangu = Globals.settings.getScraperLanguage().name();

      // check if first image is preferred langu
      if (preferredLangu.equals(arg0.getLanguage()) && !preferredLangu.equals(arg1.getLanguage())) {
        return -1;
      }

      // check if second image is preferred langu
      if (!preferredLangu.equals(arg0.getLanguage()) && preferredLangu.equals(arg1.getLanguage())) {
        return 1;
      }

      // check if the first image is en
      if ("en".equals(arg0.getLanguage()) && !"en".equals(arg1.getLanguage())) {
        return -1;
      }

      // check if the second image is en
      if (!"en".equals(arg0.getLanguage()) && "en".equals(arg1.getLanguage())) {
        return 1;
      }

      // if rating is the same, return 0
      if (arg0.getVoteAverage() == arg1.getVoteAverage()) {
        return 0;
      }

      // we did not sort until here; so lets sort with the rating
      return arg0.getVoteAverage() > arg1.getVoteAverage() ? -1 : 1;
    }

  }

  /**
   * Adds the genre.
   * 
   * @param genre
   *          the genre
   * @param md
   *          the md
   */
  private void addGenre(Genre genre, MediaMetadata md) {
    switch (genre.getId()) {
      case 28:
        md.addGenre(MediaGenres.ACTION);
        break;

      case 12:
        md.addGenre(MediaGenres.ADVENTURE);
        break;

      case 16:
        md.addGenre(MediaGenres.ANIMATION);
        break;

      case 35:
        md.addGenre(MediaGenres.COMEDY);
        break;

      case 80:
        md.addGenre(MediaGenres.CRIME);
        break;

      case 105:
        md.addGenre(MediaGenres.DISASTER);
        break;

      case 99:
        md.addGenre(MediaGenres.DOCUMENTARY);
        break;

      case 18:
        md.addGenre(MediaGenres.DRAMA);
        break;

      case 82:
        md.addGenre(MediaGenres.EASTERN);
        break;

      case 2916:
        md.addGenre(MediaGenres.EROTIC);
        break;

      case 10751:
        md.addGenre(MediaGenres.FAMILY);
        break;

      case 10750:
        md.addGenre(MediaGenres.FAN_FILM);
        break;

      case 14:
        md.addGenre(MediaGenres.FANTASY);
        break;

      case 10753:
        md.addGenre(MediaGenres.FILM_NOIR);
        break;

      case 10769:
        md.addGenre(MediaGenres.FOREIGN);
        break;

      case 36:
        md.addGenre(MediaGenres.HISTORY);
        break;

      case 10595:
        md.addGenre(MediaGenres.HOLIDAY);
        break;

      case 27:
        md.addGenre(MediaGenres.HORROR);
        break;

      case 10756:
        md.addGenre(MediaGenres.INDIE);
        break;

      case 10402:
        md.addGenre(MediaGenres.MUSIC);
        break;

      case 22:
        md.addGenre(MediaGenres.MUSICAL);
        break;

      case 9648:
        md.addGenre(MediaGenres.MYSTERY);
        break;

      case 10754:
        md.addGenre(MediaGenres.NEO_NOIR);
        break;

      case 1115:
        md.addGenre(MediaGenres.ROAD_MOVIE);
        break;

      case 10749:
        md.addGenre(MediaGenres.ROMANCE);
        break;

      case 878:
        md.addGenre(MediaGenres.SCIENCE_FICTION);
        break;

      case 10755:
        md.addGenre(MediaGenres.SHORT);
        break;

      case 9805:
        md.addGenre(MediaGenres.SPORT);
        break;

      case 10758:
        md.addGenre(MediaGenres.SPORTING_EVENT);
        break;

      case 10757:
        md.addGenre(MediaGenres.SPORTS_FILM);
        break;

      case 10748:
        md.addGenre(MediaGenres.SUSPENSE);
        break;

      case 10770:
        md.addGenre(MediaGenres.TV_MOVIE);
        break;

      case 53:
        md.addGenre(MediaGenres.THRILLER);
        break;

      case 10752:
        md.addGenre(MediaGenres.WAR);
        break;

      case 37:
        md.addGenre(MediaGenres.WESTERN);
        break;

    }

  }

  /**
   * Search for movie sets.
   * 
   * @param setName
   *          the set name
   * @return the list
   */
  public List<Collection> searchMovieSets(String setName) {
    List<Collection> movieSetsFound = null;
    synchronized (tmdb) {
      trackConnections();
      try {
        movieSetsFound = tmdb.searchCollection(setName, Globals.settings.getScraperLanguage().name(), 0);
        String baseUrl = tmdb.getConfiguration().getBaseUrl();
        for (Collection collection : movieSetsFound) {
          collection.setPosterPath(baseUrl + "w342" + collection.getPosterPath());
          collection.setBackdropPath(baseUrl + "w1280" + collection.getBackdropPath());
        }

      }
      catch (MovieDbException e) {
        LOGGER.warn("search movieset", e);
      }
    }

    if (movieSetsFound == null) {
      return new ArrayList<Collection>();
    }

    return movieSetsFound;
  }

  /**
   * Gets the movie set metadata.
   * 
   * @param options
   *          the options
   * @return the movie set metadata
   * @throws Exception
   *           the exception
   */
  public CollectionInfo getMovieSetMetadata(MediaScrapeOptions options) throws Exception {
    CollectionInfo info = null;
    int tmdbId = 0;

    // search for tmdbId
    tmdbId = options.getTmdbId();
    if (tmdbId == 0) {
      LOGGER.warn("not possible to scrape from TMDB - no tmdbId found");
      return info;
    }

    info = tmdb.getCollectionInfo(tmdbId, Globals.settings.getScraperLanguage().name());
    String baseUrl = tmdb.getConfiguration().getBaseUrl();

    info.setPosterPath(baseUrl + "w342" + info.getPosterPath());
    info.setBackdropPath(baseUrl + "w1280" + info.getBackdropPath());
    return info;
  }

  /**
   * Gets the movie set artwork.
   * 
   * @param tmdbId
   *          the tmdb id
   * @param type
   *          the type
   * @return the movie set artwork
   * @throws Exception
   *           the exception
   */
  public List<MediaArtwork> getMovieSetArtwork(int tmdbId, MediaArtworkType type) throws Exception {
    List<Artwork> tmdbArtwork = null;
    synchronized (tmdb) {
      trackConnections();
      tmdbArtwork = tmdb.getCollectionImages(tmdbId, "");
    }

    List<MediaArtwork> artwork = prepareArtwork(tmdbArtwork, type, tmdbId);

    return artwork;
  }

  /**
   * Prepare different sizes of the artwork.
   * 
   * @param tmdbArtwork
   *          the tmdb artwork
   * @param artworkType
   *          the artwork type
   * @param tmdbId
   *          the tmdb id
   * @return the list
   */
  public List<MediaArtwork> prepareArtwork(List<Artwork> tmdbArtwork, MediaArtworkType artworkType, int tmdbId) {
    List<MediaArtwork> artwork = new ArrayList<MediaArtwork>();
    String baseUrl = tmdb.getConfiguration().getBaseUrl();

    // first sort the artwork
    Collections.sort(tmdbArtwork, new ArtworkComparator());

    // prepare all sizes
    for (Artwork image : tmdbArtwork) {
      if (image.getArtworkType() == ArtworkType.POSTER && (artworkType == MediaArtworkType.POSTER || artworkType == MediaArtworkType.ALL)) {
        MediaArtwork ma = new MediaArtwork();
        ma.setDefaultUrl(baseUrl + Globals.settings.getImageTmdbPosterSize() + image.getFilePath());
        ma.setPreviewUrl(baseUrl + PosterSizes.w185 + image.getFilePath());
        ma.setProviderId(getProviderInfo().getId());
        ma.setType(MediaArtworkType.POSTER);
        ma.setLanguage(image.getLanguage());
        ma.setTmdbId(tmdbId);

        // add different sizes
        // original
        ma.addImageSize(image.getWidth(), image.getHeight(), baseUrl + PosterSizes.original + image.getFilePath());
        // w500
        if (500 < image.getWidth()) {
          ma.addImageSize(500, image.getHeight() * 500 / image.getWidth(), baseUrl + PosterSizes.w500 + image.getFilePath());
        }
        // w342
        if (342 < image.getWidth()) {
          ma.addImageSize(342, image.getHeight() * 342 / image.getWidth(), baseUrl + PosterSizes.w342 + image.getFilePath());
        }
        // w185
        if (185 < image.getWidth()) {
          ma.addImageSize(185, image.getHeight() * 185 / image.getWidth(), baseUrl + PosterSizes.w185 + image.getFilePath());
        }

        artwork.add(ma);
      }

      if (image.getArtworkType() == ArtworkType.BACKDROP && (artworkType == MediaArtworkType.BACKGROUND || artworkType == MediaArtworkType.ALL)) {
        MediaArtwork ma = new MediaArtwork();
        ma.setDefaultUrl(baseUrl + Globals.settings.getImageTmdbFanartSize() + image.getFilePath());
        ma.setPreviewUrl(baseUrl + FanartSizes.w300 + image.getFilePath());
        ma.setProviderId(getProviderInfo().getId());
        ma.setType(MediaArtworkType.BACKGROUND);
        ma.setLanguage(image.getLanguage());
        ma.setTmdbId(tmdbId);

        // add different sizes
        // original (most of the time 1920x1080)
        ma.addImageSize(image.getWidth(), image.getHeight(), baseUrl + FanartSizes.original + image.getFilePath());
        // 1280x720
        if (1280 < image.getWidth()) {
          ma.addImageSize(1280, image.getHeight() * 1280 / image.getWidth(), baseUrl + FanartSizes.w1280 + image.getFilePath());
        }
        // w300
        if (300 < image.getWidth()) {
          ma.addImageSize(300, image.getHeight() * 300 / image.getWidth(), baseUrl + FanartSizes.w300 + image.getFilePath());
        }

        artwork.add(ma);
      }
    }

    return artwork;
  }

  /**
   * Track connections and throttle if needed.
   */
  private void trackConnections() {
    Long currentTime = System.currentTimeMillis();
    if (connectionCounter.count() == connectionCounter.maxSize()) {
      Long oldestConnection = connectionCounter.getTailItem();
      if (oldestConnection > (currentTime - 10000)) {
        LOGGER.debug("connection limit reached, throttling " + connectionCounter);
        try {
          Thread.sleep(11000 - (currentTime - oldestConnection));
        }
        catch (InterruptedException e) {
          LOGGER.warn(e.getMessage());
        }
      }
    }

    currentTime = System.currentTimeMillis();
    connectionCounter.add(currentTime);
  }
}
