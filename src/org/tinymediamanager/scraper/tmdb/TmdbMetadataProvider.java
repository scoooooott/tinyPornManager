/*
 * Copyright 2012 - 2013 Manuel Laggner
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.FanartSizes;
import org.tinymediamanager.scraper.MediaArtwork.ImageSizeAndUrl;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaArtwork.PosterSizes;
import org.tinymediamanager.scraper.MediaCastMember;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaLanguages;
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
import com.omertron.themoviedbapi.model.ProductionCountry;
import com.omertron.themoviedbapi.model.ReleaseInfo;
import com.omertron.themoviedbapi.model.Trailer;

/**
 * The Class TmdbMetadataProvider.
 * 
 * @author Manuel Laggner
 */
public class TmdbMetadataProvider implements IMediaMetadataProvider, IMediaArtworkProvider, IMediaTrailerProvider {
  private static final Logger           LOGGER            = LoggerFactory.getLogger(TmdbMetadataProvider.class);
  private static final RingBuffer<Long> connectionCounter = new RingBuffer<Long>(30);
  private static TheMovieDbApi          tmdb;
  private static MediaProviderInfo      providerInfo      = new MediaProviderInfo("tmdb", "themoviedb.org",
                                                              "Scraper for themoviedb.org which is able to scrape movie metadata, artwork and trailers");

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
  @Override
  public List<MediaSearchResult> search(MediaSearchOptions query) throws Exception {
    LOGGER.debug("search() " + query.toString());
    List<MediaSearchResult> resultList = new ArrayList<MediaSearchResult>();
    String searchString = "";
    String baseUrl = tmdb.getConfiguration().getBaseUrl();
    int year = 0;

    // check type
    if (query.getMediaType() != MediaType.MOVIE) {
      throw new Exception("wrong media type for this scraper");
    }

    if (StringUtils.isEmpty(searchString) && StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.QUERY))) {
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

    searchString = MetadataUtil.removeNonSearchCharacters(searchString);

    // begin search
    LOGGER.info("========= BEGIN TMDB Scraper Search for: " + searchString);
    // ApiUrl tmdbSearchMovie = new ApiUrl(tmdb, "search/movie");
    // tmdbSearchMovie.addArgument(ApiUrl.PARAM_LANGUAGE, Globals.settings.getMovieSettings().getScraperLanguage().name());

    List<MovieDb> moviesFound = new ArrayList<MovieDb>();

    String imdbId = "";
    int tmdbId = 0;
    synchronized (tmdb) {
      // 1. try with TMDBid
      if (StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.TMDBID))) {
        trackConnections();
        // if we have already an ID, get this result and do not search
        tmdbId = Integer.valueOf(query.get(MediaSearchOptions.SearchParam.TMDBID));
        try {
          moviesFound.add(tmdb.getMovieInfo(tmdbId, query.get(MediaSearchOptions.SearchParam.LANGUAGE)));
        }
        catch (MovieDbException e) {
          LOGGER.warn("problem getting data vom tmdb: " + e.getMessage());
        }
        LOGGER.debug("found " + moviesFound.size() + " results with TMDB id");
      }

      // 2. try with IMDBid
      if (moviesFound.size() == 0 && StringUtils.isNotEmpty(query.get(MediaSearchOptions.SearchParam.IMDBID))) {
        trackConnections();
        imdbId = query.get(MediaSearchOptions.SearchParam.IMDBID);
        try {
          moviesFound.add(tmdb.getMovieInfoImdb(imdbId, query.get(MediaSearchOptions.SearchParam.LANGUAGE)));
        }
        catch (MovieDbException e) {
          LOGGER.warn("problem getting data vom tmdb: " + e.getMessage());
        }
        LOGGER.debug("found " + moviesFound.size() + " results with IMDB id");
      }

      // 3. try with search string and year
      if (moviesFound.size() == 0) {
        trackConnections();
        try {
          moviesFound = tmdb.searchMovie(searchString, year, query.get(MediaSearchOptions.SearchParam.LANGUAGE), false, 0).getResults();
        }
        catch (MovieDbException e) {
          LOGGER.warn("problem getting data vom tmdb: " + e.getMessage());
        }
        LOGGER.debug("found " + moviesFound.size() + " results with search string");
      }

      // 4. if the last token in search string seems to be a year, try without :)
      if (searchString.matches(".*\\s\\d{4}$") && (moviesFound == null || moviesFound.size() == 0)) {
        // nada found & last part seems to be date; strip off and try again
        searchString = searchString.replaceFirst("\\s\\d{4}$", "");
        try {
          moviesFound = tmdb.searchMovie(searchString, year, query.get(MediaSearchOptions.SearchParam.LANGUAGE), false, 0).getResults();
        }
        catch (MovieDbException e) {
          LOGGER.warn("problem getting data vom tmdb: " + e.getMessage());
        }
        LOGGER.debug("found " + moviesFound.size() + " results with search string removed year");
      }
    }

    LOGGER.info("found " + moviesFound.size() + " results");

    if (moviesFound == null || moviesFound.size() == 0) {
      return resultList;
    }

    for (MovieDb movie : moviesFound) {
      if (movie != null) {
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

        if (imdbId.equals(sr.getIMDBId()) || String.valueOf(tmdbId).equals(sr.getId())) {
          // perfect match
          sr.setScore(1);
        }
        else {
          // compare score based on names
          sr.setScore(MetadataUtil.calculateScore(searchString, movie.getTitle()));
        }
        resultList.add(sr);
      }
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

    // imdbId from option
    String imdbId = options.getImdbId();
    if (tmdbId == 0 && !Utils.isValidImdbId(imdbId)) {
      LOGGER.warn("not possible to scrape from TMDB - no tmdbId/imdbId found");
      return md;
    }

    // scrape
    LOGGER.debug("TMDB: getMetadata: tmdbId = " + tmdbId + "; imdbId = " + imdbId);
    MovieDb movie = null;
    String baseUrl = null;
    synchronized (tmdb) {
      trackConnections();
      if (tmdbId == 0 && Utils.isValidImdbId(imdbId)) {
        try {
          movie = tmdb.getMovieInfoImdb(imdbId, options.getLanguage().name());
        }
        catch (MovieDbException e) {
          LOGGER.warn("problem getting data vom tmdb: " + e.getMessage());
        }
      }
      if (movie == null && tmdbId != 0) {
        try {
          movie = tmdb.getMovieInfo(tmdbId, options.getLanguage().name());
        }
        catch (MovieDbException e) {
          LOGGER.warn("problem getting data vom tmdb: " + e.getMessage());
        }
      }

      if (movie == null) {
        LOGGER.warn("no result found");
        return md;
      }

      baseUrl = tmdb.getConfiguration().getBaseUrl();

      if (movie.getBelongsToCollection() != null) {
        md.storeMetadata(MediaMetadata.TMDBID_SET, movie.getBelongsToCollection().getId());
        md.storeMetadata(MediaMetadata.COLLECTION_NAME, movie.getBelongsToCollection().getName());
      }
    }

    // check if there was translateable content
    if (StringUtils.isBlank(movie.getOverview())) {
      // plot was empty - scrape in english
      MediaLanguages oldLang = options.getLanguage();
      try {
        options.setLanguage(MediaLanguages.en);
        md = getLocalizedContent(options, md);
      }
      catch (Exception e) {
      }
      finally {
        options.setLanguage(oldLang);
      }
    }

    md.setId(MediaMetadata.TMDBID, movie.getId());
    md.storeMetadata(MediaMetadata.PLOT, movie.getOverview());
    md.storeMetadata(MediaMetadata.TITLE, movie.getTitle());
    md.storeMetadata(MediaMetadata.ORIGINAL_TITLE, movie.getOriginalTitle());
    md.storeMetadata(MediaMetadata.TAGLINE, movie.getTagline());

    md.storeMetadata(MediaMetadata.RATING, movie.getVoteAverage());
    md.storeMetadata(MediaMetadata.RUNTIME, movie.getRuntime());
    md.storeMetadata(MediaMetadata.VOTE_COUNT, movie.getVoteCount());

    String spokenLanguages = "";
    for (Language lang : movie.getSpokenLanguages()) {
      if (StringUtils.isNotEmpty(spokenLanguages)) {
        spokenLanguages += ", ";
      }

      spokenLanguages += lang.getIsoCode();
    }
    md.storeMetadata(MediaMetadata.SPOKEN_LANGUAGES, spokenLanguages);

    String countries = "";
    for (ProductionCountry country : movie.getProductionCountries()) {
      if (StringUtils.isNotBlank(countries)) {
        countries += ", ";
      }
      countries += country.getIsoCode();
    }
    md.storeMetadata(MediaMetadata.COUNTRY, countries);

    if (movie.getImdbID() != null && MetadataUtil.isValidImdbId(movie.getImdbID())) {
      md.setId(MediaMetadata.IMDBID, movie.getImdbID());
    }

    // production companies
    StringBuilder productionCompanies = new StringBuilder("");
    for (ProductionCompany company : movie.getProductionCompanies()) {
      if (!StringUtils.isEmpty(productionCompanies)) {

        productionCompanies.append(", ");
      }
      productionCompanies.append(company.getName().trim());
    }
    md.storeMetadata(MediaMetadata.PRODUCTION_COMPANY, productionCompanies.toString());

    // parse release date to year
    String releaseDate = movie.getReleaseDate();
    if (!StringUtils.isEmpty(releaseDate) && releaseDate.length() > 3) {
      md.storeMetadata(MediaMetadata.YEAR, releaseDate.substring(0, 4));
    }
    md.storeMetadata(MediaMetadata.RELEASE_DATE, releaseDate);

    // get certification
    List<ReleaseInfo> releaseInfo = null;
    synchronized (tmdb) {
      trackConnections();
      releaseInfo = tmdb.getMovieReleaseInfo(movie.getId(), options.getLanguage().name()).getResults();
    }

    for (ReleaseInfo info : releaseInfo) {
      // do not use any empty certifications
      if (StringUtils.isEmpty(info.getCertification())) {
        continue;
      }

      // only use the certification of the desired country (if any country has
      // been chosen)
      if (options.getCountry() == null || options.getCountry().getAlpha2().compareToIgnoreCase(info.getCountry()) == 0) {

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
      cast = tmdb.getMovieCasts(movie.getId()).getResults();
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
          cm.setPart(castMember.getDepartment());
        }
        else if ("Writing".equals(castMember.getDepartment())) {
          cm.setType(MediaCastMember.CastType.WRITER);
          cm.setPart(castMember.getDepartment());
        }
        else if ("Production".equals(castMember.getDepartment())) {
          cm.setType(MediaCastMember.CastType.PRODUCER);
          cm.setPart(castMember.getJob());
        }
        else {
          continue;
        }
      }
      else {
        continue;
      }

      cm.setName(castMember.getName());

      if (!StringUtils.isEmpty(castMember.getProfilePath())) {
        cm.setImageUrl(baseUrl + "w185" + castMember.getProfilePath());
      }
      md.addCastMember(cm);
    }

    // MediaGenres2
    List<Genre> MediaGenres2 = movie.getGenres();
    for (Genre genre : MediaGenres2) {
      md.addGenre(getTmmGenre(genre));
    }

    return md;
  }

  public MediaMetadata getLocalizedContent(MediaScrapeOptions options, MediaMetadata md) throws Exception {
    LOGGER.debug("getMetadata() " + options.toString());
    // check if there is a md in the result
    if (options.getResult() != null && options.getResult().getMetadata() != null) {
      LOGGER.debug("TMDB: getMetadata from cache: " + options.getResult());
      return options.getResult().getMetadata();
    }

    if (md == null) {
      md = new MediaMetadata(providerInfo.getId());
    }

    int tmdbId = 0;

    // tmdbId from searchResult
    if (options.getResult() != null) {
      tmdbId = Integer.parseInt(options.getResult().getId());
    }

    // tmdbId from option
    if (tmdbId == 0) {
      tmdbId = options.getTmdbId();
    }

    // imdbId from option
    String imdbId = options.getImdbId();
    if (tmdbId == 0 && !Utils.isValidImdbId(imdbId)) {
      LOGGER.warn("not possible to scrape from TMDB - no tmdbId/imdbId found");
      return md;
    }

    // scrape
    LOGGER.debug("TMDB: getMetadata: tmdbId = " + tmdbId + "; imdbId = " + imdbId);
    MovieDb movie = null;

    synchronized (tmdb) {
      trackConnections();
      if (tmdbId == 0 && Utils.isValidImdbId(imdbId)) {
        movie = tmdb.getMovieInfoImdb(imdbId, options.getLanguage().name());
      }
      if (movie == null && tmdbId != 0) {
        movie = tmdb.getMovieInfo(tmdbId, options.getLanguage().name());
      }
    }

    if (movie == null) {
      LOGGER.warn("no result found");
      return md;
    }

    md.setId(MediaMetadata.TMDBID, movie.getId());
    md.storeMetadata(MediaMetadata.PLOT, movie.getOverview());
    md.storeMetadata(MediaMetadata.TITLE, movie.getTitle());
    md.storeMetadata(MediaMetadata.ORIGINAL_TITLE, movie.getOriginalTitle());
    md.storeMetadata(MediaMetadata.TAGLINE, movie.getTagline());

    if (movie.getBelongsToCollection() != null) {
      md.storeMetadata(MediaMetadata.TMDBID_SET, movie.getBelongsToCollection().getId());
      md.storeMetadata(MediaMetadata.COLLECTION_NAME, movie.getBelongsToCollection().getName());
    }

    return md;
  }

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
      movieImages = tmdb.getMovieImages(tmdbId, "").getResults();
    }

    List<MediaArtwork> artwork = prepareArtwork(movieImages, artworkType, tmdbId);

    // buffer the artwork
    MediaMetadata md = options.getMetadata();
    if (md != null) {
      md.addMediaArt(artwork);
    }

    return artwork;
  }

  @Override
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
        List<Trailer> tmdbTrailers = tmdb.getMovieTrailers(tmdbId, options.getLanguage().name()).getResults();
        List<Trailer> tmdbTrailersWoLang = tmdb.getMovieTrailers(tmdbId, "").getResults();
        tmdbTrailers.addAll(tmdbTrailersWoLang);

        for (Trailer tmdbTrailer : tmdbTrailers) {
          if (tmdbTrailer.getSource() == null) {
            // no url somehow...?
            continue;
          }

          MediaTrailer trailer = new MediaTrailer();
          trailer.setName(tmdbTrailer.getName());
          trailer.setQuality(tmdbTrailer.getSize());
          trailer.setProvider(tmdbTrailer.getWebsite());
          trailer.setUrl(tmdbTrailer.getSource());

          // youtube support
          if ("youtube".equalsIgnoreCase(tmdbTrailer.getWebsite())) {
            // build url for youtube trailer
            StringBuilder sb = new StringBuilder();
            sb.append("http://www.youtube.com/watch?v=");
            sb.append(tmdbTrailer.getSource());
            if ("hd".equalsIgnoreCase(tmdbTrailer.getSize()) && !tmdbTrailer.getSource().contains("&hd=1")) {
              sb.append("&hd=1");
            }
            trailer.setUrl(sb.toString());
          }

          if (!trailers.contains(trailer)) {
            trailers.add(trailer);
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
      movieInfo = tmdb.getMovieInfoImdb(imdbId, "en");
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
     * sort artwork: primary by language: preferred lang (ie de), en, others; then: score
     */
    @Override
    public int compare(Artwork arg0, Artwork arg1) {
      String preferredLangu = Globals.settings.getMovieSettings().getScraperLanguage().name();

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
   * Maps scraper Genres to internal TMM genres
   * 
   * @param genre
   *          as stinr
   * @return TMM genre
   */
  private MediaGenres getTmmGenre(Genre genre) {
    MediaGenres g = null;
    switch (genre.getId()) {
    // @formatter:off
      case 28:    g = MediaGenres.ACTION; break; 
      case 12:    g = MediaGenres.ADVENTURE; break; 
      case 16:    g = MediaGenres.ANIMATION; break; 
      case 35:    g = MediaGenres.COMEDY; break; 
      case 80:    g = MediaGenres.CRIME; break; 
      case 105:   g = MediaGenres.DISASTER; break; 
      case 99:    g = MediaGenres.DOCUMENTARY; break; 
      case 18:    g = MediaGenres.DRAMA; break; 
      case 82:    g = MediaGenres.EASTERN; break; 
      case 2916:  g = MediaGenres.EROTIC; break; 
      case 10751: g = MediaGenres.FAMILY; break; 
      case 10750: g = MediaGenres.FAN_FILM; break; 
      case 14:    g = MediaGenres.FANTASY; break; 
      case 10753: g = MediaGenres.FILM_NOIR; break; 
      case 10769: g = MediaGenres.FOREIGN; break; 
      case 36:    g = MediaGenres.HISTORY; break; 
      case 10595: g = MediaGenres.HOLIDAY; break; 
      case 27:    g = MediaGenres.HORROR; break; 
      case 10756: g = MediaGenres.INDIE; break; 
      case 10402: g = MediaGenres.MUSIC; break; 
      case 22:    g = MediaGenres.MUSICAL; break; 
      case 9648:  g = MediaGenres.MYSTERY; break; 
      case 10754: g = MediaGenres.NEO_NOIR; break; 
      case 1115:  g = MediaGenres.ROAD_MOVIE; break; 
      case 10749: g = MediaGenres.ROMANCE; break; 
      case 878:   g = MediaGenres.SCIENCE_FICTION; break; 
      case 10755: g = MediaGenres.SHORT; break; 
      case 9805:  g = MediaGenres.SPORT; break; 
      case 10758: g = MediaGenres.SPORTING_EVENT; break; 
      case 10757: g = MediaGenres.SPORTS_FILM; break; 
      case 10748: g = MediaGenres.SUSPENSE; break; 
      case 10770: g = MediaGenres.TV_MOVIE; break; 
      case 53:    g = MediaGenres.THRILLER; break; 
      case 10752: g = MediaGenres.WAR; break; 
      case 37:    g = MediaGenres.WESTERN; break; 
      // @formatter:on
    }
    if (g == null) {
      g = MediaGenres.getGenre(genre.getName());
    }
    return g;
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
        movieSetsFound = tmdb.searchCollection(setName, Globals.settings.getMovieSettings().getScraperLanguage().name(), 0).getResults();
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

    synchronized (tmdb) {
      trackConnections();
      info = tmdb.getCollectionInfo(tmdbId, Globals.settings.getMovieSettings().getScraperLanguage().name());
      if (StringUtils.isBlank(info.getOverview())) {
        // fallback to en
        info = tmdb.getCollectionInfo(tmdbId, "en");
      }
    }
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
      tmdbArtwork = tmdb.getCollectionImages(tmdbId, "").getResults();
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
        ma.setPreviewUrl(baseUrl + "w185" + image.getFilePath());
        ma.setProviderId(getProviderInfo().getId());
        ma.setType(MediaArtworkType.POSTER);
        ma.setLanguage(image.getLanguage());
        ma.setTmdbId(tmdbId);

        // add different sizes
        // original
        ma.addImageSize(image.getWidth(), image.getHeight(), baseUrl + "original" + image.getFilePath());
        // w500
        if (500 < image.getWidth()) {
          ma.addImageSize(500, image.getHeight() * 500 / image.getWidth(), baseUrl + "w500" + image.getFilePath());
        }
        // w342
        if (342 < image.getWidth()) {
          ma.addImageSize(342, image.getHeight() * 342 / image.getWidth(), baseUrl + "w342" + image.getFilePath());
        }
        // w185
        if (185 < image.getWidth()) {
          ma.addImageSize(185, image.getHeight() * 185 / image.getWidth(), baseUrl + "w185" + image.getFilePath());
        }

        // categorize image size and write default url
        prepareDefaultPoster(ma);

        artwork.add(ma);
      }

      if (image.getArtworkType() == ArtworkType.BACKDROP && (artworkType == MediaArtworkType.BACKGROUND || artworkType == MediaArtworkType.ALL)) {
        MediaArtwork ma = new MediaArtwork();
        ma.setPreviewUrl(baseUrl + "w300" + image.getFilePath());
        ma.setProviderId(getProviderInfo().getId());
        ma.setType(MediaArtworkType.BACKGROUND);
        ma.setLanguage(image.getLanguage());
        ma.setTmdbId(tmdbId);

        // add different sizes
        // original (most of the time 1920x1080)
        ma.addImageSize(image.getWidth(), image.getHeight(), baseUrl + "original" + image.getFilePath());
        // 1280x720
        if (1280 < image.getWidth()) {
          ma.addImageSize(1280, image.getHeight() * 1280 / image.getWidth(), baseUrl + "w1280" + image.getFilePath());
        }
        // w300
        if (300 < image.getWidth()) {
          ma.addImageSize(300, image.getHeight() * 300 / image.getWidth(), baseUrl + "w300" + image.getFilePath());
        }

        // categorize image size and write default url
        prepareDefaultFanart(ma);

        artwork.add(ma);
      }
    }

    return artwork;
  }

  /**
   * Prepare default poster.
   * 
   * @param ma
   *          the ma
   */
  private void prepareDefaultPoster(MediaArtwork ma) {
    for (ImageSizeAndUrl image : ma.getImageSizes()) {
      // LARGE
      if (image.getWidth() >= 1000) {
        if (Globals.settings.getMovieSettings().getImagePosterSize().getOrder() >= PosterSizes.LARGE.getOrder()) {
          ma.setDefaultUrl(image.getUrl());
          ma.setSizeOrder(PosterSizes.LARGE.getOrder());
          break;
        }
        continue;
      }
      // BIG
      if (image.getWidth() >= 500) {
        if (Globals.settings.getMovieSettings().getImagePosterSize().getOrder() >= PosterSizes.BIG.getOrder()) {
          ma.setDefaultUrl(image.getUrl());
          ma.setSizeOrder(PosterSizes.BIG.getOrder());
          break;
        }
        continue;
      }
      // MEDIUM
      if (image.getWidth() >= 342) {
        if (Globals.settings.getMovieSettings().getImagePosterSize().getOrder() >= PosterSizes.MEDIUM.getOrder()) {
          ma.setDefaultUrl(image.getUrl());
          ma.setSizeOrder(PosterSizes.MEDIUM.getOrder());
          break;
        }
        continue;
      }
      // SMALL
      if (image.getWidth() >= 185) {
        if (Globals.settings.getMovieSettings().getImagePosterSize() == PosterSizes.SMALL) {
          ma.setDefaultUrl(image.getUrl());
          ma.setSizeOrder(PosterSizes.SMALL.getOrder());
          break;
        }
        continue;
      }
    }
  }

  /**
   * Prepare default fanart.
   * 
   * @param ma
   *          the ma
   */
  private void prepareDefaultFanart(MediaArtwork ma) {
    for (ImageSizeAndUrl image : ma.getImageSizes()) {
      // LARGE
      if (image.getWidth() >= 1920) {
        if (Globals.settings.getMovieSettings().getImageFanartSize().getOrder() >= FanartSizes.LARGE.getOrder()) {
          ma.setDefaultUrl(image.getUrl());
          ma.setSizeOrder(FanartSizes.LARGE.getOrder());
          break;
        }
        continue;
      }
      // MEDIUM
      if (image.getWidth() >= 1280) {
        if (Globals.settings.getMovieSettings().getImageFanartSize().getOrder() >= FanartSizes.MEDIUM.getOrder()) {
          ma.setDefaultUrl(image.getUrl());
          ma.setSizeOrder(FanartSizes.MEDIUM.getOrder());
          break;
        }
        continue;
      }
      // SMALL
      if (image.getWidth() >= 300) {
        if (Globals.settings.getMovieSettings().getImageFanartSize().getOrder() >= FanartSizes.SMALL.getOrder()) {
          ma.setDefaultUrl(image.getUrl());
          ma.setSizeOrder(FanartSizes.SMALL.getOrder());
          break;
        }
        continue;
      }
    }
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
