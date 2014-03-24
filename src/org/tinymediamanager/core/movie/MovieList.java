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
package org.tinymediamanager.core.movie;

import static org.tinymediamanager.core.Constants.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.observablecollections.ObservableCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchOptions.SearchParam;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.fanarttv.FanartTvMetadataProvider;
import org.tinymediamanager.scraper.hdtrailersnet.HDTrailersNet;
import org.tinymediamanager.scraper.imdb.ImdbMetadataProvider;
import org.tinymediamanager.scraper.moviemeternl.MoviemeterMetadataProvider;
import org.tinymediamanager.scraper.ofdb.OfdbMetadataProvider;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;
import org.tinymediamanager.scraper.zelluloid.ZelluloidMetadataProvider;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;

/**
 * The Class MovieList.
 * 
 * @author Manuel Laggner
 */
public class MovieList extends AbstractModelObject {
  private static final Logger          LOGGER                   = LoggerFactory.getLogger(MovieList.class);
  private static MovieList             instance;

  private ObservableElementList<Movie> movieList;
  private List<MovieSet>               movieSetList;
  private PropertyChangeListener       tagListener;
  private List<String>                 tagsObservable           = ObservableCollections.observableList(Collections
                                                                    .synchronizedList(new ArrayList<String>()));
  private List<String>                 videoCodecsObservable    = ObservableCollections.observableList(Collections
                                                                    .synchronizedList(new ArrayList<String>()));
  private List<String>                 audioCodecsObservable    = ObservableCollections.observableList(Collections
                                                                    .synchronizedList(new ArrayList<String>()));
  private List<Certification>          certificationsObservable = ObservableCollections.observableList(Collections
                                                                    .synchronizedList(new ArrayList<Certification>()));

  /**
   * Instantiates a new movie list.
   */
  private MovieList() {
    // the tag listener: its used to always have a full list of all tags used in tmm
    tagListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        // listen to changes of tags
        if ("tag".equals(evt.getPropertyName())) {
          Movie movie = (Movie) evt.getSource();
          updateTags(movie);
        }
        if (MEDIA_FILES.equals(evt.getPropertyName()) || MEDIA_INFORMATION.equals(evt.getPropertyName())) {
          Movie movie = (Movie) evt.getSource();
          updateMediaInformationLists(movie);
        }
        if (CERTIFICATION.equals(evt.getPropertyName())) {
          Movie movie = (Movie) evt.getSource();
          updateCertifications(movie);
        }
      }
    };
  }

  /**
   * Gets the single instance of MovieList.
   * 
   * @return single instance of MovieList
   */
  public static MovieList getInstance() {
    if (MovieList.instance == null) {
      MovieList.instance = new MovieList();
    }
    return MovieList.instance;
  }

  /**
   * Adds the movie.
   * 
   * @param movie
   *          the movie
   */
  public void addMovie(Movie movie) {
    if (!movieList.contains(movie)) {
      int oldValue = movieList.size();
      movieList.add(movie);

      updateTags(movie);
      movie.addPropertyChangeListener(tagListener);
      firePropertyChange("movies", null, movieList);
      firePropertyChange("movieCount", oldValue, movieList.size());
    }
  }

  /**
   * Removes the datasource.
   * 
   * @param path
   *          the path
   */
  public void removeDatasource(String path) {
    if (StringUtils.isEmpty(path)) {
      return;
    }

    List<Movie> moviesToRemove = new ArrayList<Movie>();
    for (int i = movieList.size() - 1; i >= 0; i--) {
      Movie movie = movieList.get(i);
      if (new File(path).equals(new File(movie.getDataSource()))) {
        moviesToRemove.add(movie);
      }
    }

    removeMovies(moviesToRemove);
  }

  /**
   * Gets the unscraped movies.
   * 
   * @return the unscraped movies
   */
  public List<Movie> getUnscrapedMovies() {
    List<Movie> unscrapedMovies = new ArrayList<Movie>();
    for (Movie movie : movieList) {
      if (!movie.isScraped()) {
        unscrapedMovies.add(movie);
      }
    }
    return unscrapedMovies;
  }

  /**
   * Gets the new movies or movies with new files
   * 
   * @return the new movies
   */
  public List<Movie> getNewMovies() {
    List<Movie> newMovies = new ArrayList<Movie>();
    for (Movie movie : movieList) {
      if (movie.isNewlyAdded()) {
        newMovies.add(movie);
      }
    }
    return newMovies;
  }

  // /**
  // * Removes the movie.
  // *
  // * @param movie
  // * the movie
  // */
  // public void removeMovie(Movie movie) {
  // int oldValue = movieList.size();
  // movieList.remove(movie);
  //
  // // remove movie also from moviesets
  // if (movie.getMovieSet() != null) {
  // movie.getMovieSet().removeMovie(movie);
  // movie.setMovieSet(null);
  // }
  //
  // Globals.entityManager.getTransaction().begin();
  // Globals.entityManager.remove(movie);
  // Globals.entityManager.getTransaction().commit();
  // firePropertyChange("movies", null, movieList);
  // firePropertyChange("movieCount", oldValue, movieList.size());
  // }

  /**
   * remove given movies from the database
   * 
   * @param movies
   *          list of movies to remove
   */
  public void removeMovies(List<Movie> movies) {
    if (movies == null || movies.size() == 0) {
      return;
    }
    Set<MovieSet> modifiedMovieSets = new HashSet<MovieSet>();
    int oldValue = movieList.size();

    boolean newTransaction = false;
    if (!MovieModuleManager.getInstance().getEntityManager().getTransaction().isActive()) {
      MovieModuleManager.getInstance().getEntityManager().getTransaction().begin();
      newTransaction = true;
    }

    // remove in inverse order => performance
    for (int i = movies.size() - 1; i >= 0; i--) {
      Movie movie = movies.get(i);
      movieList.remove(movie);
      if (movie.getMovieSet() != null) {
        MovieSet movieSet = movie.getMovieSet();
        movieSet.removeMovie(movie);
        modifiedMovieSets.add(movieSet);
        movie.setMovieSet(null);
      }
      MovieModuleManager.getInstance().getEntityManager().remove(movie);
    }

    if (newTransaction) {
      MovieModuleManager.getInstance().getEntityManager().getTransaction().commit();
    }

    // and now check if any of the modified moviesets are worth for deleting
    for (MovieSet movieSet : modifiedMovieSets) {
      removeMovieSet(movieSet);
    }

    firePropertyChange("movies", null, movieList);
    firePropertyChange("movieCount", oldValue, movieList.size());
  }

  /**
   * Gets the movies.
   * 
   * @return the movies
   */
  public ObservableElementList<Movie> getMovies() {
    if (movieList == null) {
      movieList = new ObservableElementList<Movie>(GlazedLists.threadSafeList(new BasicEventList<Movie>()), GlazedLists.beanConnector(Movie.class));
    }
    return movieList;
  }

  /**
   * Load movies from database.
   */
  public void loadMoviesFromDatabase(EntityManager entityManager) {
    List<Movie> movies = null;
    List<MovieSet> movieSets = null;
    try {
      // load movies
      TypedQuery<Movie> query = entityManager.createQuery("SELECT movie FROM Movie movie", Movie.class);
      movies = query.getResultList();
      if (movies != null) {
        LOGGER.info("found " + movies.size() + " movies in database");
        movieList = new ObservableElementList<Movie>(GlazedLists.threadSafeList(new BasicEventList<Movie>(movies.size())),
            GlazedLists.beanConnector(Movie.class));

        for (Object obj : movies) {
          if (obj instanceof Movie) {
            Movie movie = (Movie) obj;
            try {
              // movie.setObservables();
              movie.initializeAfterLoading();

              // for performance reasons we add movies directly
              // addMovie(movie);
              movieList.add(movie);
              updateTags(movie);
              updateMediaInformationLists(movie);
              updateCertifications(movie);
              movie.addPropertyChangeListener(tagListener);
            }
            catch (Exception e) {
              LOGGER.error("error loading movie/dropping it: " + e.getMessage());
              try {
                List<Movie> moviesToRemove = Arrays.asList(movie);
                removeMovies(moviesToRemove);
              }
              catch (Exception e1) {
              }
            }
          }
          else {
            LOGGER.error("retrieved no movie: " + obj);
          }
        }

      }
      else {
        LOGGER.debug("found no movies in database");
      }

      // load movie sets
      TypedQuery<MovieSet> querySets = entityManager.createQuery("SELECT movieSet FROM MovieSet movieSet", MovieSet.class);
      movieSets = querySets.getResultList();
      if (movieSets != null) {
        LOGGER.info("found " + movieSets.size() + " movieSets in database");
        movieSetList = ObservableCollections.observableList(Collections.synchronizedList(new ArrayList<MovieSet>(movieSets.size())));

        // load movie sets
        for (Object obj : movieSets) {
          if (obj instanceof MovieSet) {
            MovieSet movieSet = (MovieSet) obj;

            // for performance reasons we add moviesets directly
            // addMovieSet(movieSet);
            this.movieSetList.add(movieSet);
          }
        }
      }
      else {
        LOGGER.debug("found no movieSets in database");
      }

      // cross check movies and moviesets if linking is "stable"
      checkAndCleanupMovieSets();
    }
    catch (Exception e) {
      LOGGER.error("loadMoviesFromDatabase", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "", "message.database.loadmovies"));
    }
  }

  /**
   * Gets the movie by path.
   * 
   * @param path
   *          the path
   * @return the movie by path
   */
  public synchronized Movie getMovieByPath(File path) {

    for (Movie movie : movieList) {
      if (new File(movie.getPath()).compareTo(path) == 0) {
        LOGGER.debug("Ok, found already existing movie '" + movie.getTitle() + "' in DB (path: " + path + ")");
        return movie;
      }
    }

    return null;
  }

  /**
   * Gets a list of movies by same path.
   * 
   * @param path
   *          the path
   * @return the movie list
   */
  public synchronized List<Movie> getMoviesByPath(File path) {
    ArrayList<Movie> movies = new ArrayList<Movie>();
    for (Movie movie : movieList) {
      if (new File(movie.getPath()).compareTo(path) == 0) {
        movies.add(movie);
      }
    }
    return movies;
  }

  /**
   * Search movie.
   * 
   * @param searchTerm
   *          the search term
   * @param movie
   *          the movie
   * @param metadataProvider
   *          the metadata provider
   * @return the list
   */
  public List<MediaSearchResult> searchMovie(String searchTerm, Movie movie, IMediaMetadataProvider metadataProvider) {
    List<MediaSearchResult> sr = null;

    try {
      IMediaMetadataProvider provider = metadataProvider;
      // get a new metadataprovider if nothing is set
      if (provider == null) {
        provider = getMetadataProvider();
      }
      boolean idFound = false;
      // set what we have, so the provider could chose from all :)
      MediaSearchOptions options = new MediaSearchOptions(MediaType.MOVIE);
      options.set(SearchParam.LANGUAGE, Globals.settings.getMovieSettings().getScraperLanguage().name());
      options.set(SearchParam.COUNTRY, Globals.settings.getMovieSettings().getCertificationCountry().getAlpha2());
      options.set(SearchParam.COLLECTION_INFO, Boolean.toString(Globals.settings.getMovieScraperMetadataConfig().isCollection()));
      options.set(SearchParam.IMDB_FOREIGN_LANGUAGE, Boolean.toString(Globals.settings.getMovieSettings().isImdbScrapeForeignLanguage()));
      if (movie != null) {
        if (Utils.isValidImdbId(movie.getImdbId())) {
          options.set(SearchParam.IMDBID, movie.getImdbId());
          idFound = true;
        }
        if (movie.getTmdbId() != 0) {
          options.set(SearchParam.TMDBID, String.valueOf(movie.getTmdbId()));
          idFound = true;
        }
        options.set(SearchParam.TITLE, movie.getTitle());
        if (!movie.getYear().isEmpty()) {
          options.set(SearchParam.YEAR, movie.getYear());
        }
      }
      if (!searchTerm.isEmpty()) {
        if (idFound) {
          // id found, so search for it
          // except when searchTerm differs from movie title (we entered something to search for)
          if (!searchTerm.equals(movie.getTitle())) {
            options.set(SearchParam.QUERY, searchTerm);
          }
        }
        else {
          options.set(SearchParam.QUERY, searchTerm);
        }
      }

      sr = provider.search(options);
      // if result is empty, try all scrapers
      if (sr.isEmpty() && Globals.settings.getMovieSettings().isScraperFallback()) {
        LOGGER.debug("no result yet - trying alternate scrapers");

        for (MovieScrapers ms : MovieScrapers.values()) {
          IMediaMetadataProvider provider2 = getMetadataProvider(ms);
          if (provider.getProviderInfo().equals(provider2.getProviderInfo())) {
            continue;
          }
          sr = provider2.search(options);
          if (!sr.isEmpty()) {
            break;
          }
        }
      }
    }
    catch (Exception e) {
      LOGGER.error("searchMovie", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.movie.searcherror", new String[] { ":",
          e.getLocalizedMessage() }));
    }

    return sr;
  }

  // /**
  // * Search movie.
  // *
  // * @param searchTerm
  // * the search term
  // * @param ImdbId
  // * the imdb id
  // * @param metadataProvider
  // * the metadata provider
  // * @return the list
  // */
  // @Deprecated
  // public List<MediaSearchResult> searchMovie(String searchTerm, String year, String ImdbId, IMediaMetadataProvider metadataProvider) {
  // List<MediaSearchResult> sr = null;
  // if (ImdbId != null && !ImdbId.isEmpty()) {
  // sr = searchMovieByImdbId(ImdbId, metadataProvider);
  // }
  // if (sr == null || sr.size() == 0) {
  // sr = searchMovie(searchTerm, year, metadataProvider);
  // }
  //
  // return sr;
  // }

  // /**
  // * Search movie.
  // *
  // * @param searchTerm
  // * the search term
  // * @param metadataProvider
  // * the metadata provider
  // * @return the list
  // */
  // @Deprecated
  // private List<MediaSearchResult> searchMovie(String searchTerm, String year, IMediaMetadataProvider metadataProvider) {
  // // format searchstring
  // // searchTerm = MetadataUtil.removeNonSearchCharacters(searchTerm);
  //
  // List<MediaSearchResult> searchResult = null;
  // try {
  // IMediaMetadataProvider provider = metadataProvider;
  // // get a new metadataprovider if nothing is set
  // if (provider == null) {
  // provider = getMetadataProvider();
  // }
  // MediaSearchOptions options = new MediaSearchOptions(MediaType.MOVIE, MediaSearchOptions.SearchParam.QUERY, searchTerm);
  // options.set(MediaSearchOptions.SearchParam.YEAR, year);
  // searchResult = provider.search(options);
  // }
  // catch (Exception e) {
  // LOGGER.error("searchMovie", e);
  // MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, "", "message.movie.searcherror", new String[] { ":",
  // e.getLocalizedMessage() }));
  // }
  //
  // return searchResult;
  // }

  // /**
  // * Search movie.
  // *
  // * @param imdbId
  // * the imdb id
  // * @param metadataProvider
  // * the metadata provider
  // * @return the list
  // */
  // @Deprecated
  // private List<MediaSearchResult> searchMovieByImdbId(String imdbId, IMediaMetadataProvider metadataProvider) {
  //
  // List<MediaSearchResult> searchResult = null;
  // MediaSearchOptions options = new MediaSearchOptions(MediaType.MOVIE);
  // options.setMediaType(MediaType.MOVIE);
  // options.set(SearchParam.IMDBID, imdbId);
  //
  // try {
  // IMediaMetadataProvider provider = metadataProvider;
  // // get a new metadataProvider if no one is set
  // if (provider == null) {
  // provider = getMetadataProvider();
  // }
  // searchResult = provider.search(options);
  // }
  // catch (Exception e) {
  // LOGGER.warn("failed to search movie with imdbid", e);
  // searchResult = new ArrayList<MediaSearchResult>();
  // }
  //
  // return searchResult;
  // }

  /**
   * Gets the metadata provider.
   * 
   * @return the metadata provider
   */
  public IMediaMetadataProvider getMetadataProvider() {
    MovieScrapers scraper = Globals.settings.getMovieSettings().getMovieScraper();
    return getMetadataProvider(scraper);
  }

  /**
   * Gets the metadata provider.
   * 
   * @param scraper
   *          the scraper
   * @return the metadata provider
   */
  public IMediaMetadataProvider getMetadataProvider(MovieScrapers scraper) {
    IMediaMetadataProvider metadataProvider = null;
    switch (scraper) {
      case OFDB:
        LOGGER.debug("get instance of OfdbMetadataProvider");
        metadataProvider = new OfdbMetadataProvider();
        break;

      case ZELLULOID:
        LOGGER.debug("get instance of ZelluloidMetadataProvider");
        metadataProvider = new ZelluloidMetadataProvider();
        break;

      case MOVIEMETER:
        LOGGER.debug("get instance of MoviemeterMetadataProvider");
        try {
          metadataProvider = new MoviemeterMetadataProvider();
        }
        catch (Exception e) {
          LOGGER.warn("failed to get instance of MoviemeterMetadataProvider", e);
        }
        break;

      case IMDB:
        LOGGER.debug("get instance of ImdbMetadataProvider");
        metadataProvider = new ImdbMetadataProvider();
        break;

      case TMDB:
      default:
        LOGGER.debug("get instance of TmdbMetadataProvider");
        try {
          metadataProvider = new TmdbMetadataProvider();
        }
        catch (Exception e) {
          LOGGER.warn("failed to get instance of TmdbMetadataProvider", e);
        }
    }

    //
    // try {
    // metadataProvider = new XbmcMetadataProvider(new
    // XbmcScraperParser().parseScraper(new
    // File("xbmc_scraper/metadata.imdb.com/imdb.xml")));
    // metadataProvider = new XbmcMetadataProvider(new
    // XbmcScraperParser().parseScraper(new
    // File("xbmc_scraper/metadata.imdb.de/imdb_de.xml")));
    // } catch (Exception e) {
    // LOGGER.error("tried to get xmbc scraper", e);
    // }

    // }

    return metadataProvider;
  }

  /**
   * Gets the metadata provider from a searchresult's providerId.
   * 
   * @param providerId
   *          the scraper
   * @return the metadata provider
   */
  public IMediaMetadataProvider getMetadataProvider(String providerId) {
    // FIXME: rework scrapers/providerInfo to contain Movie(Tv)Scrapers enums
    if (providerId == null || providerId.isEmpty()) {
      // default
      return getMetadataProvider(MovieScrapers.TMDB);
    }
    if (providerId.equals("tmdb")) {
      return getMetadataProvider(MovieScrapers.TMDB);
    }
    else if (providerId.equals("imdb")) {
      return getMetadataProvider(MovieScrapers.IMDB);
    }
    else if (providerId.equals("moviemeter")) {
      return getMetadataProvider(MovieScrapers.MOVIEMETER);
    }
    else if (providerId.equals("ofdb")) {
      return getMetadataProvider(MovieScrapers.OFDB);
    }
    else if (providerId.equals("zelluloid")) {
      return getMetadataProvider(MovieScrapers.ZELLULOID);
    }
    else {
      // default
      return getMetadataProvider(MovieScrapers.TMDB);
    }
  }

  /**
   * Gets the artwork provider.
   * 
   * @return the artwork provider
   */
  public List<IMediaArtworkProvider> getArtworkProviders() {
    List<MovieArtworkScrapers> scrapers = new ArrayList<MovieArtworkScrapers>();
    if (Globals.settings.getMovieSettings().isImageScraperTmdb()) {
      scrapers.add(MovieArtworkScrapers.TMDB);
    }

    if (Globals.settings.getMovieSettings().isImageScraperFanartTv()) {
      scrapers.add(MovieArtworkScrapers.FANART_TV);
    }

    return getArtworkProviders(scrapers);
  }

  /**
   * Gets the artwork providers.
   * 
   * @param scrapers
   *          the scrapers
   * @return the artwork providers
   */
  public List<IMediaArtworkProvider> getArtworkProviders(List<MovieArtworkScrapers> scrapers) {
    List<IMediaArtworkProvider> artworkProviders = new ArrayList<IMediaArtworkProvider>();

    IMediaArtworkProvider artworkProvider = null;

    // tmdb
    if (scrapers.contains(MovieArtworkScrapers.TMDB)) {
      try {
        if (Globals.settings.getMovieSettings().isImageScraperTmdb()) {
          LOGGER.debug("get instance of TmdbMetadataProvider");
          artworkProvider = new TmdbMetadataProvider();
          artworkProviders.add(artworkProvider);
        }
      }
      catch (Exception e) {
        LOGGER.warn("failed to get instance of TmdbMetadataProvider", e);
      }
    }

    // fanart.tv
    if (scrapers.contains(MovieArtworkScrapers.FANART_TV)) {
      try {
        if (Globals.settings.getMovieSettings().isImageScraperFanartTv()) {
          LOGGER.debug("get instance of FanartTvMetadataProvider");
          artworkProvider = new FanartTvMetadataProvider();
          artworkProviders.add(artworkProvider);
        }
      }
      catch (Exception e) {
        LOGGER.warn("failed to get instance of FanartTvMetadataProvider", e);
      }
    }

    return artworkProviders;
  }

  /**
   * Gets the trailer providers.
   * 
   * @return the trailer providers
   */
  public List<IMediaTrailerProvider> getTrailerProviders() {
    List<MovieTrailerScrapers> scrapers = new ArrayList<MovieTrailerScrapers>();

    if (Globals.settings.getMovieSettings().isTrailerScraperTmdb()) {
      scrapers.add(MovieTrailerScrapers.TMDB);
    }

    if (Globals.settings.getMovieSettings().isTrailerScraperHdTrailers()) {
      scrapers.add(MovieTrailerScrapers.HDTRAILERS);
    }

    if (Globals.settings.getMovieSettings().isTrailerScraperOfdb()) {
      scrapers.add(MovieTrailerScrapers.OFDB);
    }

    return getTrailerProviders(scrapers);
  }

  /**
   * Gets the trailer providers.
   * 
   * @param scrapers
   *          the scrapers
   * @return the trailer providers
   */
  public List<IMediaTrailerProvider> getTrailerProviders(List<MovieTrailerScrapers> scrapers) {
    List<IMediaTrailerProvider> trailerProviders = new ArrayList<IMediaTrailerProvider>();

    // tmdb
    if (scrapers.contains(MovieTrailerScrapers.TMDB)) {
      try {
        IMediaTrailerProvider trailerProvider = new TmdbMetadataProvider();
        trailerProviders.add(trailerProvider);
      }
      catch (Exception e) {
        LOGGER.warn("failed to get instance of TmdbMetadataProvider", e);
      }
    }

    // hd-trailer.net
    if (scrapers.contains(MovieTrailerScrapers.HDTRAILERS)) {
      IMediaTrailerProvider trailerProvider = new HDTrailersNet();
      trailerProviders.add(trailerProvider);
    }

    // ofdb.de
    if (scrapers.contains(MovieTrailerScrapers.OFDB)) {
      IMediaTrailerProvider trailerProvider = new OfdbMetadataProvider();
      trailerProviders.add(trailerProvider);
    }

    return trailerProviders;
  }

  /**
   * Gets the movie count.
   * 
   * @return the movie count
   */
  public int getMovieCount() {
    int size = movieList.size();
    return size;
  }

  /**
   * Gets the movie set count.
   * 
   * @return the movie set count
   */
  public int getMovieSetCount() {
    int size = movieSetList.size();
    return size;
  }

  /**
   * Gets the tags in movies.
   * 
   * @return the tags in movies
   */
  public List<String> getTagsInMovies() {
    return tagsObservable;
  }

  /**
   * Update tags used in movies.
   * 
   * @param movie
   *          the movie
   */
  private void updateTags(Movie movie) {
    for (String tagInMovie : movie.getTags()) {
      boolean tagFound = false;
      for (String tag : tagsObservable) {
        if (tagInMovie.equals(tag)) {
          tagFound = true;
          break;
        }
      }
      if (!tagFound) {
        addTag(tagInMovie);
      }
    }
  }

  /**
   * Update media information used in movies.
   * 
   * @param movie
   *          the movie
   */
  private void updateMediaInformationLists(Movie movie) {
    // video codec
    for (MediaFile mf : movie.getMediaFiles(MediaFileType.VIDEO)) {
      String codec = mf.getVideoCodec();
      boolean codecFound = false;

      for (String mfCodec : videoCodecsObservable) {
        if (mfCodec.equals(codec)) {
          codecFound = true;
          break;
        }
      }

      if (!codecFound) {
        addVideoCodec(codec);
      }
    }

    // audio codec
    for (MediaFile mf : movie.getMediaFiles(MediaFileType.VIDEO)) {
      for (MediaFileAudioStream audio : mf.getAudioStreams()) {
        String codec = audio.getCodec();
        boolean codecFound = false;
        for (String mfCodec : audioCodecsObservable) {
          if (mfCodec.equals(codec)) {
            codecFound = true;
            break;
          }
        }

        if (!codecFound) {
          addAudioCodec(codec);
        }
      }
    }
  }

  private void updateCertifications(Movie movie) {
    if (!certificationsObservable.contains(movie.getCertification())) {
      addCertification(movie.getCertification());
    }
  }

  public List<String> getVideoCodecsInMovies() {
    return videoCodecsObservable;
  }

  public List<String> getAudioCodecsInMovies() {
    return audioCodecsObservable;
  }

  public List<Certification> getCertificationsInMovies() {
    return certificationsObservable;
  }

  /**
   * Adds the tag.
   * 
   * @param newTag
   *          the new tag
   */
  private void addTag(String newTag) {
    if (StringUtils.isBlank(newTag)) {
      return;
    }

    for (String tag : tagsObservable) {
      if (tag.equals(newTag)) {
        return;
      }
    }

    tagsObservable.add(newTag);
    firePropertyChange("tag", null, tagsObservable);
  }

  private void addVideoCodec(String newCodec) {
    if (StringUtils.isBlank(newCodec)) {
      return;
    }

    for (String codec : videoCodecsObservable) {
      if (codec.equals(newCodec)) {
        return;
      }
    }

    videoCodecsObservable.add(newCodec);
    firePropertyChange("videoCodec", null, videoCodecsObservable);
  }

  private void addAudioCodec(String newCodec) {
    if (StringUtils.isBlank(newCodec)) {
      return;
    }

    for (String codec : audioCodecsObservable) {
      if (codec.equals(newCodec)) {
        return;
      }
    }

    audioCodecsObservable.add(newCodec);
    firePropertyChange("audioCodec", null, audioCodecsObservable);
  }

  private void addCertification(Certification newCert) {
    if (newCert == null) {
      return;
    }

    if (!certificationsObservable.contains(newCert)) {
      certificationsObservable.add(newCert);
      firePropertyChange("certification", null, certificationsObservable);
    }
  }

  /**
   * Search duplicates.
   */
  public void searchDuplicates() {
    Map<String, Movie> imdbDuplicates = new HashMap<String, Movie>();
    Map<Integer, Movie> tmdbDuplicates = new HashMap<Integer, Movie>();

    for (Movie movie : movieList) {
      movie.clearDuplicate();

      // imdb duplicate search only works with given imdbid
      if (StringUtils.isNotEmpty(movie.getImdbId())) {
        // is there a movie with this imdbid sotred?
        if (imdbDuplicates.containsKey(movie.getImdbId())) {
          // yes - set duplicate flag on both movies
          movie.setDuplicate();
          Movie movie2 = imdbDuplicates.get(movie.getImdbId());
          movie2.setDuplicate();
        }
        else {
          // no, store movie
          imdbDuplicates.put(movie.getImdbId(), movie);
        }
      }

      // tmdb duplicate search only works with with given tmdb id
      if (movie.getTmdbId() > 0) {
        // is there a movie with this tmdbid sotred?
        if (tmdbDuplicates.containsKey(movie.getTmdbId())) {
          // yes - set duplicate flag on both movies
          movie.setDuplicate();
          Movie movie2 = tmdbDuplicates.get(movie.getTmdbId());
          movie2.setDuplicate();
        }
        else {
          // no, store movie
          tmdbDuplicates.put(movie.getTmdbId(), movie);
        }
      }
    }
  }

  /**
   * Gets the movie set list.
   * 
   * @return the movieSetList
   */
  public List<MovieSet> getMovieSetList() {
    if (movieSetList == null) {
      movieSetList = ObservableCollections.observableList(Collections.synchronizedList(new ArrayList<MovieSet>()));
    }
    return movieSetList;
  }

  /**
   * Sets the movie set list.
   * 
   * @param movieSetList
   *          the movieSetList to set
   */
  public void setMovieSetList(ObservableElementList<MovieSet> movieSetList) {
    this.movieSetList = movieSetList;
  }

  /**
   * Adds the movie set.
   * 
   * @param movieSet
   *          the movie set
   */
  public void addMovieSet(MovieSet movieSet) {
    int oldValue = movieSetList.size();
    this.movieSetList.add(movieSet);
    firePropertyChange("addedMovieSet", null, movieSet);
    firePropertyChange("movieSetCount", oldValue, movieSetList.size());
  }

  /**
   * Removes the movie set.
   * 
   * @param movieSet
   *          the movie set
   */
  public void removeMovieSet(MovieSet movieSet) {
    int oldValue = movieSetList.size();
    movieSet.removeAllMovies();

    movieSetList.remove(movieSet);

    boolean newTransaction = false;
    if (!MovieModuleManager.getInstance().getEntityManager().getTransaction().isActive()) {
      MovieModuleManager.getInstance().getEntityManager().getTransaction().begin();
      newTransaction = true;
    }

    MovieModuleManager.getInstance().getEntityManager().remove(movieSet);

    if (newTransaction) {
      MovieModuleManager.getInstance().getEntityManager().getTransaction().commit();
    }

    firePropertyChange("removedMovieSet", null, movieSet);
    firePropertyChange("movieSetCount", oldValue, movieSetList.size());
  }

  private MovieSet findMovieSet(String title, int tmdbId) {
    // first search by tmdbId
    if (tmdbId > 0) {
      for (MovieSet movieSet : movieSetList) {
        if (movieSet.getTmdbId() == tmdbId) {
          return movieSet;
        }
      }
    }

    // search for the movieset by name
    for (MovieSet movieSet : movieSetList) {
      if (movieSet.getTitle().equals(title)) {
        return movieSet;
      }
    }

    return null;
  }

  public synchronized MovieSet getMovieSet(String title, int tmdbId) {
    MovieSet movieSet = findMovieSet(title, tmdbId);

    if (movieSet == null) {
      movieSet = new MovieSet(title);
      movieSet.saveToDb();
      addMovieSet(movieSet);
    }

    return movieSet;
  }

  /**
   * Sort movies in movie set.
   * 
   * @param movieSet
   *          the movie set
   */
  public void sortMoviesInMovieSet(MovieSet movieSet) {
    if (movieSet.getMovies().size() > 1) {
      movieSet.sortMovies();
    }
    firePropertyChange("sortedMovieSets", null, movieSetList);
  }

  /**
   * invalidate the title sortable upon changes to the sortable prefixes
   */
  public void invalidateTitleSortable() {
    for (Movie movie : new ArrayList<Movie>(movieList)) {
      movie.clearTitleSortable();
    }
  }

  /**
   * cross check the linking between movies and moviesets and clean it
   */
  private void checkAndCleanupMovieSets() {
    for (Movie movie : movieList) {
      // first check if this movie is in the given movieset
      if (movie.getMovieSet() != null && !movie.getMovieSet().getMovies().contains(movie)) {
        // add it
        movie.getMovieSet().addMovie(movie);
        movie.getMovieSet().saveToDb();
      }
      // and check if this movie is in other moviesets
      for (MovieSet movieSet : movieSetList) {
        if (movieSet != movie.getMovieSet() && movieSet.getMovies().contains(movie)) {
          movieSet.removeMovie(movie);
          movieSet.saveToDb();
        }
      }
    }

    // second: check if there are some orphaned movies in moviesets
    for (MovieSet movieSet : movieSetList) {
      movieSet.cleanMovieSet();
    }
  }
}
