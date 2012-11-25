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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.CastMember;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.IHasFindByIMDBID;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaArt;
import org.tinymediamanager.scraper.MediaArtifactType;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;
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
import com.moviejukebox.themoviedb.model.ProductionCompany;
import com.moviejukebox.themoviedb.model.ReleaseInfo;
import com.moviejukebox.themoviedb.tools.ApiUrl;

/**
 * The Class TmdbMetadataProvider.
 */
public class TmdbMetadataProvider implements IMediaMetadataProvider, IHasFindByIMDBID {

  /** The Constant logger. */
  private static final Logger         LOGGER = Logger.getLogger(TmdbMetadataProvider.class);

  /** The Constant instance. */
  private static TmdbMetadataProvider instance;

  /** The tmdb. */
  private TheMovieDb                  tmdb;

  /**
   * The Enum Languages.
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
   * Instantiates a new tmdb metadata provider.
   */
  private TmdbMetadataProvider() {
    try {
      tmdb = new TheMovieDb("6247670ec93f4495a36297ff88f7cd15");
    }
    catch (Exception e) {
      LOGGER.error("TmdbMetadataProvider", e);
    }
  }

  /**
   * Gets the single instance of TmdbMetadataProvider.
   * 
   * @return single instance of TmdbMetadataProvider
   */
  public static TmdbMetadataProvider getInstance() {
    if (instance == null) {
      instance = new TmdbMetadataProvider();
    }
    return instance;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.tinymediamanager.scraper.HasFindByIMDBID#getMetadataForIMDBId(java.
   * lang.String)
   */
  @Override
  public MediaMetadata getMetadataForIMDBId(String imdbId) throws Exception {
    LOGGER.debug("TMDB: getMetadataForIMDBId(imdbId): " + imdbId);
    if (!Utils.isValidImdbId(imdbId)) {
      return null;
    }

    // get the tmdbid for this imdbid
    MovieDb movieInfo = null;
    synchronized (tmdb) {
      movieInfo = tmdb.getMovieInfoImdb(imdbId, Globals.settings.getScraperTmdbLanguage().name());
    }

    int tmdbId = movieInfo.getId();

    // get images if a tmdb id has been found
    if (tmdbId > 0) {
      return getMetaData(tmdbId);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaMetadataProvider#getInfo()
   */
  @Override
  public ProviderInfo getInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Gets the artwork.
   * 
   * @param imdbId
   *          the imdb id
   * @param type
   *          the type
   * @return the artwork
   * @throws Exception
   *           the exception
   */
  public List<TmdbArtwork> getArtwork(String imdbId, MediaArtifactType type) throws Exception {
    LOGGER.debug("TMDB: getArtwork(imdbId): " + imdbId);

    List<TmdbArtwork> artwork = new ArrayList<TmdbArtwork>();

    // get the tmdbid for this imdbid
    MovieDb movieInfo = null;
    synchronized (tmdb) {
      movieInfo = tmdb.getMovieInfoImdb(imdbId, Globals.settings.getScraperTmdbLanguage().name());
    }

    int tmdbId = movieInfo.getId();

    // get images if a tmdb id has been found
    if (tmdbId > 0) {
      artwork = getArtwork(tmdbId, type);
    }

    return artwork;
  }

  /**
   * Gets the artwork.
   * 
   * @param imdbId
   *          the imdb id
   * @return the artwork
   * @throws Exception
   *           the exception
   */
  public List<TmdbArtwork> getArtwork(String imdbId) throws Exception {
    LOGGER.debug("TMDB: getArtwork(imdbId): " + imdbId);

    List<TmdbArtwork> artwork = new ArrayList<TmdbArtwork>();

    // get the tmdbid for this imdbid
    MovieDb movieInfo = null;
    synchronized (tmdb) {
      movieInfo = tmdb.getMovieInfoImdb(imdbId, Globals.settings.getScraperTmdbLanguage().name());
    }

    int tmdbId = movieInfo.getId();

    // get images if a tmdb id has been found
    if (tmdbId > 0) {
      artwork = getArtwork(tmdbId);
    }

    return artwork;
  }

  /**
   * Gets the artwork.
   * 
   * @param tmdbId
   *          the tmdb id
   * @param type
   *          the type
   * @return the artwork
   * @throws Exception
   *           the exception
   */
  public List<TmdbArtwork> getArtwork(int tmdbId, MediaArtifactType type) throws Exception {
    LOGGER.debug("TMDB: getArtwork(tmdbId): " + tmdbId);

    String baseUrl = tmdb.getConfiguration().getBaseUrl();
    List<TmdbArtwork> artwork = new ArrayList<TmdbArtwork>();

    // posters and fanart
    List<Artwork> movieImages = getArtworkFromTmdb(tmdbId);

    for (Artwork image : movieImages) {
      String path = "";

      // artwork is a poster
      if (image.getArtworkType() == ArtworkType.POSTER && type == MediaArtifactType.POSTER) {
        TmdbArtwork poster = new TmdbArtwork(MediaArtifactType.POSTER, baseUrl, image.getFilePath());
        poster.setWidth(image.getWidth());
        poster.setHeight(image.getHeight());
        artwork.add(poster);
      }

      // artwork is a fanart
      if (image.getArtworkType() == ArtworkType.BACKDROP && type == MediaArtifactType.BACKGROUND) {
        TmdbArtwork backdrop = new TmdbArtwork(MediaArtifactType.BACKGROUND, baseUrl, image.getFilePath());
        backdrop.setWidth(image.getWidth());
        backdrop.setHeight(image.getHeight());
        artwork.add(backdrop);
      }
    }

    return artwork;
  }

  /**
   * Gets the artwork.
   * 
   * @param tmdbId
   *          the tmdb id
   * @return the artwork
   * @throws Exception
   *           the exception
   */
  public List<TmdbArtwork> getArtwork(int tmdbId) throws Exception {
    LOGGER.debug("TMDB: getArtwork(tmdbId): " + tmdbId);

    String baseUrl = tmdb.getConfiguration().getBaseUrl();
    List<TmdbArtwork> artwork = new ArrayList<TmdbArtwork>();

    // posters and fanart
    List<Artwork> movieImages = getArtworkFromTmdb(tmdbId);

    for (Artwork image : movieImages) {
      String path = "";
      // artwork is a poster
      if (image.getArtworkType() == ArtworkType.POSTER) {
        TmdbArtwork poster = new TmdbArtwork(MediaArtifactType.POSTER, baseUrl, image.getFilePath());
        poster.setWidth(image.getWidth());
        poster.setHeight(image.getHeight());
        artwork.add(poster);
      }

      // artwork is a fanart
      if (image.getArtworkType() == ArtworkType.BACKDROP) {
        TmdbArtwork backdrop = new TmdbArtwork(MediaArtifactType.BACKGROUND, baseUrl, image.getFilePath());
        backdrop.setWidth(image.getWidth());
        backdrop.setHeight(image.getHeight());
        artwork.add(backdrop);
      }
    }

    return artwork;
  }

  /**
   * @param tmdbId
   * @return
   * @throws MovieDbException
   */
  private List<Artwork> getArtworkFromTmdb(int tmdbId) throws MovieDbException {
    List<Artwork> movieImages = null;
    List<Artwork> movieImages_wo_lang = null;
    synchronized (tmdb) {
      // posters and fanart (first search with lang)
      movieImages = tmdb.getMovieImages(tmdbId, Globals.settings.getImageTmdbLangugage().name());
      // posters and fanart (without lang)
      movieImages_wo_lang = tmdb.getMovieImages(tmdbId, "");
    }

    movieImages.addAll(movieImages_wo_lang);
    return movieImages;
  }

  /**
   * Gets the meta data.
   * 
   * @param tmdbId
   *          the tmdb id
   * @return the meta data
   * @throws Exception
   *           the exception
   */
  public MediaMetadata getMetaData(int tmdbId) throws Exception {
    LOGGER.debug("TMDB: getMetadata(tmdbId): " + tmdbId);

    MediaMetadata md = new MediaMetadata();

    MovieDb movie = null;
    String baseUrl = null;
    synchronized (tmdb) {
      movie = tmdb.getMovieInfo(tmdbId, Globals.settings.getScraperTmdbLanguage().name());
      baseUrl = tmdb.getConfiguration().getBaseUrl();
    }

    MediaMetadata.updateMDValue(md, MetadataKey.TMDB_ID, String.valueOf(movie.getId()));
    MediaMetadata.updateMDValue(md, MetadataKey.PLOT, movie.getOverview());
    MediaMetadata.updateMDValue(md, MetadataKey.MEDIA_TITLE, movie.getTitle());
    MediaMetadata.updateMDValue(md, MetadataKey.ORIGINAL_TITLE, movie.getOriginalTitle());
    MediaMetadata.updateMDValue(md, MetadataKey.USER_RATING, String.valueOf(movie.getVoteAverage()));
    MediaMetadata.updateMDValue(md, MetadataKey.RUNNING_TIME, String.valueOf(movie.getRuntime()));
    MediaMetadata.updateMDValue(md, MetadataKey.TAGLINE, movie.getTagline());

    md.setVoteCount(movie.getVoteCount());

    if (movie.getImdbID() != null && movie.getImdbID().contains("tt")) {
      MediaMetadata.updateMDValue(md, MetadataKey.IMDB_ID, movie.getImdbID());
    }

    // production companies
    StringBuilder productionCompanies = new StringBuilder("");
    for (ProductionCompany company : movie.getProductionCompanies()) {
      if (!StringUtils.isEmpty(productionCompanies)) {

        productionCompanies.append(", ");
      }
      productionCompanies.append(company.getName().trim());
    }
    MediaMetadata.updateMDValue(md, MetadataKey.COMPANY, productionCompanies.toString());

    // parse release date to year
    String releaseDate = movie.getReleaseDate();
    if (!StringUtils.isEmpty(releaseDate) && releaseDate.length() > 3) {
      MediaMetadata.updateMDValue(md, MetadataKey.YEAR, releaseDate.substring(0, 4));
    }
    MediaMetadata.updateMDValue(md, MetadataKey.RELEASE_DATE, releaseDate);

    // get certification
    List<ReleaseInfo> releaseInfo = null;
    synchronized (tmdb) {
      releaseInfo = tmdb.getMovieReleaseInfo(tmdbId, Globals.settings.getScraperTmdbLanguage().name());
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

      // MPAA is an extra case for certification
      if ("US".equals(info.getCountry())) {
        MediaMetadata.updateMDValue(md, MetadataKey.MPAA_RATING, info.getCertification());
      }
    }

    List<Artwork> movieImages = getArtworkFromTmdb(tmdbId);

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
    List<Person> cast = null;
    synchronized (tmdb) {
      cast = tmdb.getMovieCasts(tmdbId);
    }

    for (Person castMember : cast) {
      CastMember cm = new CastMember();
      if (castMember.getPersonType() == PersonType.CAST) {
        cm.setType(CastMember.ACTOR);
        cm.setCharacter(castMember.getCharacter());
      }
      else if (castMember.getPersonType() == PersonType.CREW) {
        if ("Director".equals(castMember.getJob())) {
          cm.setType(CastMember.DIRECTOR);
        }
        else if ("Writing".equals(castMember.getDepartment())) {
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
      if (!StringUtils.isEmpty(castMember.getProfilePath())) {
        cm.setImageUrl(baseUrl + "w185" + castMember.getProfilePath());
      }
      md.addCastMember(cm);
    }

    // MediaGenres
    List<Genre> MediaGenres = movie.getGenres();
    for (Genre genre : MediaGenres) {
      addGenre(genre, md);
    }

    return md;
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

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaMetadataProvider#getMetaData(org.
   * tinymediamanager.scraper.MediaSearchResult)
   */
  @Override
  public MediaMetadata getMetaData(MediaSearchResult result) throws Exception {
    if (result.getMetadata() != null) {
      LOGGER.debug("TMDB: getMetadata(result) from cache: " + result);
      return result.getMetadata();
    }
    else {
      LOGGER.debug("TMDB: getMetadata(result): " + result);
      int tmdbId = Integer.parseInt(result.getId());
      return getMetaData(tmdbId);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.tinymediamanager.scraper.IMediaMetadataProvider#search(org.tinymediamanager
   * .scraper.SearchQuery)
   */
  @Override
  public List<MediaSearchResult> search(SearchQuery query) throws Exception {
    List<MediaSearchResult> resultList = new ArrayList<MediaSearchResult>();
    String searchString = query.get(SearchQuery.Field.QUERY);

    LOGGER.debug("========= BEGIN TMDB Scraper Search for: " + searchString);
    ApiUrl tmdbSearchMovie = new ApiUrl(tmdb, "search/movie");
    URL url = tmdbSearchMovie.getQueryUrl(searchString, Globals.settings.getScraperTmdbLanguage().name(), 1);
    LOGGER.debug(url.toString().replace("&api_key=6247670ec93f4495a36297ff88f7cd15", "&<API_KEY>"));

    List<MovieDb> moviesFound = null;
    synchronized (tmdb) {
      moviesFound = tmdb.searchMovie(searchString, Globals.settings.getScraperTmdbLanguage().name(), false);
    }

    if (moviesFound == null) {
      return resultList;
    }

    LOGGER.debug("found " + moviesFound.size() + " results");

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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.tinymediamanager.scraper.IMediaMetadataProvider#getSupportedSearchTypes
   * ()
   */
  @Override
  public MediaType[] getSupportedSearchTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Process media art.
   * 
   * @param md
   *          the md
   * @param type
   *          the type
   * @param label
   *          the label
   * @param image
   *          the image
   */
  private void processMediaArt(MediaMetadata md, MediaArtifactType type, String label, String image) {
    MediaArt ma = new MediaArt();
    ma.setDownloadUrl(image);
    ma.setLabel(label);
    // ma.setProviderId(getInfo().getId());
    ma.setType(type);
    md.addMediaArt(ma);
  }

  @Override
  public MediaSearchResult searchByImdbId(String imdbId) throws Exception {
    LOGGER.debug("========= BEGIN TMDB Scraper Search for IMDB Id: " + imdbId);
    if (!Utils.isValidImdbId(imdbId)) {
      return null;
    }

    MediaMetadata md = getMetadataForIMDBId(imdbId);
    if (md == null) {
      return null;
    }

    MediaSearchResult sr = new MediaSearchResult();
    sr.setId(md.getTMDBID());
    sr.setIMDBId(md.getIMDBID());
    sr.setTitle(md.getMediaTitle());
    sr.setOriginalTitle(md.getOriginalTitle());
    sr.setYear(md.getYear());
    sr.setMetadata(md);
    sr.setScore(1);

    return sr;
  }

  public List<MediaArt> getMediaArt(String imdbId) throws Exception {
    LOGGER.debug("TMDB: getMediaArt(imdbId): " + imdbId);
    if (!Utils.isValidImdbId(imdbId)) {
      return null;
    }

    // get the tmdbid for this imdbid
    MovieDb movieInfo = null;
    synchronized (tmdb) {
      movieInfo = tmdb.getMovieInfoImdb(imdbId, Globals.settings.getScraperTmdbLanguage().name());
    }
    int tmdbId = movieInfo.getId();

    // get images if a tmdb id has been found
    if (tmdbId > 0) {
      return getMediaArt(tmdbId);
    }
    return null;
  }

  public List<MediaArt> getMediaArt(int tmdbId) throws Exception {
    List<MediaArt> mediaArt = new ArrayList<MediaArt>();

    String baseUrl = tmdb.getConfiguration().getBaseUrl();

    // posters and fanart
    List<Artwork> movieImages = getArtworkFromTmdb(tmdbId);

    for (Artwork image : movieImages) {
      // poster
      if (image.getArtworkType() == ArtworkType.POSTER) {
        String path = baseUrl + Globals.settings.getImageTmdbPosterSize() + image.getFilePath();
        MediaArt ma = new MediaArt();
        ma.setDownloadUrl(path);
        ma.setLabel("Poster");
        ma.setType(MediaArtifactType.POSTER);
        ma.setTmdbId(tmdbId);
        mediaArt.add(ma);
      }

      // backdrop
      if (image.getArtworkType() == ArtworkType.BACKDROP) {
        String path = baseUrl + Globals.settings.getImageTmdbFanartSize() + image.getFilePath();
        MediaArt ma = new MediaArt();
        ma.setDownloadUrl(path);
        ma.setLabel("Poster");
        ma.setType(MediaArtifactType.BACKGROUND);
        mediaArt.add(ma);
      }
    }

    return mediaArt;
  }

  public int getTmdbId(String imdbId) {
    LOGGER.debug("TMDB: getTmdbId(imdbId): " + imdbId);
    int tmdbId = 0;

    if (!Utils.isValidImdbId(imdbId)) {
      return tmdbId;
    }

    // get the tmdbid for this imdbid
    MovieDb movieInfo;
    try {
      synchronized (tmdb) {
        movieInfo = tmdb.getMovieInfoImdb(imdbId, Globals.settings.getScraperTmdbLanguage().name());
      }
      tmdbId = movieInfo.getId();
    }
    catch (MovieDbException e) {
    }

    return tmdbId;
  }

}
