/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import static org.tinymediamanager.core.Constants.CERTIFICATION;
import static org.tinymediamanager.core.Constants.MEDIA_FILES;
import static org.tinymediamanager.core.Constants.MEDIA_INFORMATION;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.h2.mvstore.MVMap;
import org.jdesktop.observablecollections.ObservableCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.ScraperType;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ObservableElementList;

/**
 * The Class MovieList.
 * 
 * @author Manuel Laggner
 */
public class MovieList extends AbstractModelObject {
  private static final Logger          LOGGER             = LoggerFactory.getLogger(MovieList.class);
  private static MovieList             instance;

  private final MovieSettings          movieSettings;
  private final List<Movie>            movieList;
  private final List<MovieSet>         movieSetList;
  private final List<String>           tagsObservable;
  private final List<String>           videoCodecsObservable;
  private final List<String>           audioCodecsObservable;
  private final List<Certification>    certificationsObservable;

  private final PropertyChangeListener tagListener;
  private final Comparator<MovieSet>   movieSetComparator = new MovieSetComparator();

  /**
   * Instantiates a new movie list.
   */
  private MovieList() {
    // create all lists
    movieList = new ObservableElementList<>(GlazedLists.threadSafeList(new BasicEventList<Movie>()), GlazedLists.beanConnector(Movie.class));
    movieSetList = ObservableCollections.observableList(Collections.synchronizedList(new ArrayList<MovieSet>()));
    tagsObservable = ObservableCollections.observableList(new CopyOnWriteArrayList<String>());
    videoCodecsObservable = ObservableCollections.observableList(new CopyOnWriteArrayList<String>());
    audioCodecsObservable = ObservableCollections.observableList(new CopyOnWriteArrayList<String>());
    certificationsObservable = ObservableCollections.observableList(new CopyOnWriteArrayList<Certification>());

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

    movieSettings = MovieModuleManager.MOVIE_SETTINGS;
  }

  /**
   * Gets the single instance of MovieList.
   * 
   * @return single instance of MovieList
   */
  public synchronized static MovieList getInstance() {
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

    List<Movie> moviesToRemove = new ArrayList<>();
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
    List<Movie> unscrapedMovies = new ArrayList<>();
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
    List<Movie> newMovies = new ArrayList<>();
    for (Movie movie : movieList) {
      if (movie.isNewlyAdded()) {
        newMovies.add(movie);
      }
    }
    return newMovies;
  }

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
    Set<MovieSet> modifiedMovieSets = new HashSet<>();
    int oldValue = movieList.size();

    // remove in inverse order => performance
    for (int i = movies.size() - 1; i >= 0; i--) {
      Movie movie = movies.get(i);
      movieList.remove(movie);
      if (movie.getMovieSet() != null) {
        MovieSet movieSet = movie.getMovieSet();

        movieSet.removeMovie(movie, false);
        modifiedMovieSets.add(movieSet);
        movie.setMovieSet(null);
      }
      try {
        MovieModuleManager.getInstance().removeMovieFromDb(movie);
      }
      catch (Exception e) {
        LOGGER.error("Error removing movie from DB: " + e.getMessage());
      }
    }

    firePropertyChange("movies", null, movieList);
    firePropertyChange("movieCount", oldValue, movieList.size());
  }

  /**
   * delete the given movies from the database and physically
   * 
   * @param movies
   *          list of movies to delete
   */
  public void deleteMovies(List<Movie> movies) {
    if (movies == null || movies.size() == 0) {
      return;
    }
    Set<MovieSet> modifiedMovieSets = new HashSet<>();
    int oldValue = movieList.size();

    // remove in inverse order => performance
    for (int i = movies.size() - 1; i >= 0; i--) {
      Movie movie = movies.get(i);
      movie.deleteFilesSafely();
      movieList.remove(movie);
      if (movie.getMovieSet() != null) {
        MovieSet movieSet = movie.getMovieSet();
        movieSet.removeMovie(movie, false);
        modifiedMovieSets.add(movieSet);
        movie.setMovieSet(null);
      }
      try {
        MovieModuleManager.getInstance().removeMovieFromDb(movie);
      }
      catch (Exception e) {
        LOGGER.error("Error removing movie from DB: " + e.getMessage());
      }
    }

    firePropertyChange("movies", null, movieList);
    firePropertyChange("movieCount", oldValue, movieList.size());
  }

  /**
   * Gets the movies.
   * 
   * @return the movies
   */
  public List<Movie> getMovies() {
    return movieList;
  }

  /**
   * Load movies from database.
   */
  void loadMoviesFromDatabase(MVMap<UUID, String> movieMap, ObjectMapper objectMapper) {
    // load movies
    ObjectReader movieObjectReader = objectMapper.readerFor(Movie.class);

    for (UUID uuid : new ArrayList<>(movieMap.keyList())) {
      String json = "";
      try {
        json = movieMap.get(uuid);
        Movie movie = movieObjectReader.readValue(json);
        movie.setDbId(uuid);
        // for performance reasons we add movies directly
        movieList.add(movie);
      }
      catch (Exception e) {
        LOGGER.warn("problem decoding movie json string: " + e.getMessage());
        LOGGER.info("dropping corrupt movie");
        movieMap.remove(uuid);
      }
    }
    LOGGER.info("found " + movieList.size() + " movies in database");
  }

  void loadMovieSetsFromDatabase(MVMap<UUID, String> movieSetMap, ObjectMapper objectMapper) {
    // load movie sets
    ObjectReader movieSetObjectReader = objectMapper.readerFor(MovieSet.class);

    for (UUID uuid : new ArrayList<>(movieSetMap.keyList())) {
      try {
        MovieSet movieSet = movieSetObjectReader.readValue(movieSetMap.get(uuid));
        movieSet.setDbId(uuid);
        // for performance reasons we add movies sets directly
        movieSetList.add(movieSet);
      }
      catch (Exception e) {
        LOGGER.warn("problem decoding movie set json string: " + e.getMessage());
        LOGGER.info("dropping corrupt movie set");
        movieSetMap.remove(uuid);
      }
    }

    LOGGER.info("found " + movieSetList.size() + " movieSets in database");
  }

  void initDataAfterLoading() {
    // remove invalid movies which have no VIDEO files
    checkAndCleanupMediaFiles();

    // 3. initialize movies/movie sets (e.g. link with each others)
    for (Movie movie : movieList) {
      movie.initializeAfterLoading();
      updateTags(movie);
      updateMediaInformationLists(movie);
      updateCertifications(movie);
      movie.addPropertyChangeListener(tagListener);
    }

    for (MovieSet movieSet : movieSetList) {
      movieSet.initializeAfterLoading();
    }
  }

  public void persistMovie(Movie movie) {
    // remove this movie from the database
    try {
      MovieModuleManager.getInstance().persistMovie(movie);
    }
    catch (Exception e) {
      LOGGER.error("failed to persist movie: " + movie.getTitle());
    }
  }

  public void removeMovieFromDb(Movie movie) {
    // remove this movie from the database
    try {
      MovieModuleManager.getInstance().removeMovieFromDb(movie);
    }
    catch (Exception e) {
      LOGGER.error("failed to remove movie: " + movie.getTitle());
    }
  }

  public void persistMovieSet(MovieSet movieSet) {
    // remove this movie set from the database
    try {
      MovieModuleManager.getInstance().persistMovieSet(movieSet);
    }
    catch (Exception e) {
      LOGGER.error("failed to persist movie set: " + movieSet.getTitle());
    }
  }

  public void removeMovieSetFromDb(MovieSet movieSet) {
    // remove this movie set from the database
    try {
      MovieModuleManager.getInstance().removeMovieSetFromDb(movieSet);
    }
    catch (Exception e) {
      LOGGER.error("failed to remove movie set: " + movieSet.getTitle());
    }
  }

  public MovieSet lookupMovieSet(UUID uuid) {
    for (MovieSet movieSet : movieSetList) {
      if (movieSet.getDbId().equals(uuid)) {
        return movieSet;
      }
    }
    return null;
  }

  public Movie lookupMovie(UUID uuid) {
    for (Movie movie : movieList) {
      if (movie.getDbId().equals(uuid)) {
        return movie;
      }
    }
    return null;
  }

  /**
   * Gets the movie by path.
   * 
   * @param path
   *          the path
   * @return the movie by path
   * @deprecated use Java7 getMovieByPath(Path path) instead.
   */
  @Deprecated
  public synchronized Movie getMovieByPath(File path) {
    return getMovieByPath(path.toPath());
  }

  /**
   * Gets the movie by path.
   * 
   * @param path
   *          the path
   * @return the movie by path
   */
  public synchronized Movie getMovieByPath(Path path) {

    for (Movie movie : movieList) {
      if (movie.getPathNIO().compareTo(path.toAbsolutePath()) == 0) {
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
   * @deprecated use Java7 getMoviesByPath(Path path) instead.
   */
  @Deprecated
  public synchronized List<Movie> getMoviesByPath(File path) {
    return getMoviesByPath(path.toPath());
  }

  /**
   * Gets a list of movies by same path.
   * 
   * @param path
   *          the path
   * @return the movie list
   */
  public synchronized List<Movie> getMoviesByPath(Path path) {
    ArrayList<Movie> movies = new ArrayList<>();
    for (Movie movie : movieList) {
      if (Paths.get(movie.getPath()).compareTo(path) == 0) {
        movies.add(movie);
      }
    }
    return movies;
  }

  /**
   * Search for a movie with the default settings.
   * 
   * @param searchTerm
   *          the search term
   * @param movie
   *          the movie
   * @param metadataScraper
   *          the media scraper
   * @return the list
   */
  public List<MediaSearchResult> searchMovie(String searchTerm, Movie movie, MediaScraper metadataScraper) {
    return searchMovie(searchTerm, movie, metadataScraper, movieSettings.getScraperLanguage());
  }

  /**
   * Search movie with the chosen language.
   * 
   * @param searchTerm
   *          the search term
   * @param movie
   *          the movie
   * @param mediaScraper
   *          the media scraper
   * @param langu
   *          the language to search with
   * @return the list
   */
  public List<MediaSearchResult> searchMovie(String searchTerm, Movie movie, MediaScraper mediaScraper, MediaLanguages langu) {
    List<MediaSearchResult> sr = null;
    try {
      IMovieMetadataProvider provider;
      if (mediaScraper == null) {
        provider = (IMovieMetadataProvider) getDefaultMediaScraper().getMediaProvider();
      }
      else {
        provider = (IMovieMetadataProvider) mediaScraper.getMediaProvider();
      }

      boolean idFound = false;
      // set what we have, so the provider could chose from all :)
      MediaSearchOptions options = new MediaSearchOptions(MediaType.MOVIE);
      options.setLanguage(LocaleUtils.toLocale(langu.name()));
      options.setCountry(movieSettings.getCertificationCountry());
      if (movie != null) {
        if (Utils.isValidImdbId(movie.getImdbId())) {
          options.setImdbId(movie.getImdbId());
          idFound = true;
        }
        if (movie.getTmdbId() != 0) {
          options.setTmdbId(movie.getTmdbId());
          idFound = true;
        }
        options.setQuery(movie.getTitle());
        if (!movie.getYear().isEmpty()) {
          try {
            options.setYear(Integer.parseInt(movie.getYear()));
          }
          catch (Exception ignored) {
          }
        }
      }
      if (!searchTerm.isEmpty()) {
        if (idFound) {
          // id found, so search for it
          // except when searchTerm differs from movie title (we entered something to search for)
          if (!searchTerm.equals(movie.getTitle())) {
            options.setQuery(searchTerm);
          }
        }
        else {
          options.setQuery(searchTerm);
        }
      }

      LOGGER.info("=====================================================");
      LOGGER.info("Searching with scraper: " + provider.getProviderInfo().getId() + ", " + provider.getProviderInfo().getVersion());
      LOGGER.info(options.toString());
      LOGGER.info("=====================================================");
      sr = provider.search(options);
      // if result is empty, try all scrapers
      if (sr.isEmpty() && movieSettings.isScraperFallback()) {
        for (MediaScraper ms : getAvailableMediaScrapers()) {
          if (!ms.isEnabled() || provider.getProviderInfo().equals(ms.getMediaProvider().getProviderInfo())
              || ms.getMediaProvider().getProviderInfo().getName().startsWith("Kodi")) {
            continue;
          }
          LOGGER.info("no result yet - trying alternate scraper: " + ms.getName());
          try {
            LOGGER.info("=====================================================");
            LOGGER.info("Searching with alternate scraper: " + ms.getMediaProvider().getProviderInfo().getId() + ", "
                + provider.getProviderInfo().getVersion());
            LOGGER.info(options.toString());
            LOGGER.info("=====================================================");
            sr = ((IMovieMetadataProvider) ms.getMediaProvider()).search(options);
          }
          catch (Exception e) {
            LOGGER.error("searchMovieFallback", e);
            MessageManager.instance
                .pushMessage(new Message(MessageLevel.ERROR, movie, "message.movie.searcherror", new String[] { ":", e.getLocalizedMessage() }));
          }
          if (!sr.isEmpty()) {
            break;
          }
        }
      }
    }
    catch (Exception e) {
      LOGGER.error("searchMovie", e);
      MessageManager.instance
          .pushMessage(new Message(MessageLevel.ERROR, movie, "message.movie.searcherror", new String[] { ":", e.getLocalizedMessage() }));
    }

    return sr;
  }

  public List<MediaScraper> getAvailableMediaScrapers() {
    List<MediaScraper> availableScrapers = MediaScraper.getMediaScrapers(ScraperType.MOVIE);
    Collections.sort(availableScrapers, new MovieMediaScraperComparator());
    return availableScrapers;
  }

  public MediaScraper getDefaultMediaScraper() {
    MediaScraper scraper = MediaScraper.getMediaScraperById(movieSettings.getMovieScraper(), ScraperType.MOVIE);
    if (scraper == null) {
      scraper = MediaScraper.getMediaScraperById(Constants.TMDB, ScraperType.MOVIE);
    }
    return scraper;
  }

  public MediaScraper getMediaScraperById(String providerId) {
    return MediaScraper.getMediaScraperById(providerId, ScraperType.MOVIE);
  }

  /**
   * get all available artwork scrapers.
   * 
   * @return the artwork scrapers
   */
  public List<MediaScraper> getAvailableArtworkScrapers() {
    List<MediaScraper> availableScrapers = MediaScraper.getMediaScrapers(ScraperType.MOVIE_ARTWORK);
    // we can use the MovieMediaScraperComparator here too, since TMDB should also be first
    Collections.sort(availableScrapers, new MovieMediaScraperComparator());
    return availableScrapers;
  }

  /**
   * get all specified artwork scrapers
   * 
   * @param providerIds
   *          a list of all specified scraper ids
   * @return the specified artwork scrapers
   */
  public List<MediaScraper> getArtworkScrapers(List<String> providerIds) {
    List<MediaScraper> artworkScrapers = new ArrayList<>();

    for (String providerId : providerIds) {
      if (StringUtils.isBlank(providerId)) {
        continue;
      }
      MediaScraper artworkScraper = MediaScraper.getMediaScraperById(providerId, ScraperType.MOVIE_ARTWORK);
      if (artworkScraper != null) {
        artworkScrapers.add(artworkScraper);
      }
    }

    return artworkScrapers;
  }

  /**
   * get all default (specified via settings) artwork scrapers
   * 
   * @return the specified artwork scrapers
   */
  public List<MediaScraper> getDefaultArtworkScrapers() {
    return getArtworkScrapers(movieSettings.getMovieArtworkScrapers());
  }

  /**
   * all available trailer scrapers.
   * 
   * @return the trailer scrapers
   */
  public List<MediaScraper> getAvailableTrailerScrapers() {
    List<MediaScraper> availableScrapers = MediaScraper.getMediaScrapers(ScraperType.MOVIE_TRAILER);
    // we can use the MovieMediaScraperComparator here too, since TMDB should also be first
    Collections.sort(availableScrapers, new MovieMediaScraperComparator());
    return availableScrapers;
  }

  /**
   * get all default (specified via settings) trailer scrapers
   * 
   * @return the specified trailer scrapers
   */
  public List<MediaScraper> getDefaultTrailerScrapers() {
    return getTrailerScrapers(movieSettings.getMovieTrailerScrapers());
  }

  /**
   * get all specified trailer scrapers.
   * 
   * @param providerIds
   *          the scrapers
   * @return the trailer providers
   */
  public List<MediaScraper> getTrailerScrapers(List<String> providerIds) {
    List<MediaScraper> trailerScrapers = new ArrayList<>();

    for (String providerId : providerIds) {
      if (StringUtils.isBlank(providerId)) {
        continue;
      }
      MediaScraper trailerScraper = MediaScraper.getMediaScraperById(providerId, ScraperType.MOVIE_TRAILER);
      if (trailerScraper != null) {
        trailerScrapers.add(trailerScraper);
      }
    }

    return trailerScrapers;
  }

  /**
   * all available subtitle scrapers.
   *
   * @return the subtitle scrapers
   */
  public List<MediaScraper> getAvailableSubtitleScrapers() {
    List<MediaScraper> availableScrapers = MediaScraper.getMediaScrapers(ScraperType.SUBTITLE);
    Collections.sort(availableScrapers, new MovieMediaScraperComparator());
    return availableScrapers;
  }

  /**
   * get all default (specified via settings) subtitle scrapers
   *
   * @return the specified subtitle scrapers
   */
  public List<MediaScraper> getDefaultSubtitleScrapers() {
    return getSubtitleScrapers(movieSettings.getMovieSubtitleScrapers());
  }

  /**
   * get all specified subtitle scrapers.
   *
   * @param providerIds
   *          the scrapers
   * @return the subtitle scrapers
   */
  public List<MediaScraper> getSubtitleScrapers(List<String> providerIds) {
    List<MediaScraper> subtitleScrapers = new ArrayList<>();

    for (String providerId : providerIds) {
      if (StringUtils.isBlank(providerId)) {
        continue;
      }
      MediaScraper subtitleScraper = MediaScraper.getMediaScraperById(providerId, ScraperType.SUBTITLE);
      if (subtitleScraper != null) {
        subtitleScrapers.add(subtitleScraper);
      }
    }

    return subtitleScrapers;
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
    List<String> availableTags = new ArrayList<>(tagsObservable);
    for (String tagInMovie : new ArrayList<>(movie.getTags())) {
      boolean tagFound = false;
      for (String tag : availableTags) {
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
    List<String> availableCodecs = new ArrayList<>(videoCodecsObservable);
    for (MediaFile mf : movie.getMediaFiles(MediaFileType.VIDEO)) {
      String codec = mf.getVideoCodec();
      boolean codecFound = false;

      for (String mfCodec : availableCodecs) {
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
    availableCodecs = new ArrayList<>(audioCodecsObservable);
    for (MediaFile mf : movie.getMediaFiles(MediaFileType.VIDEO)) {
      for (MediaFileAudioStream audio : mf.getAudioStreams()) {
        String codec = audio.getCodec();
        boolean codecFound = false;
        for (String mfCodec : availableCodecs) {
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

    synchronized (tagsObservable) {
      if (tagsObservable.contains(newTag)) {
        return;
      }
      tagsObservable.add(newTag);
    }

    firePropertyChange("tag", null, tagsObservable);
  }

  private void addVideoCodec(String newCodec) {
    if (StringUtils.isBlank(newCodec)) {
      return;
    }

    synchronized (videoCodecsObservable) {
      if (videoCodecsObservable.contains(newCodec)) {
        return;
      }
      videoCodecsObservable.add(newCodec);
    }

    firePropertyChange("videoCodec", null, videoCodecsObservable);
  }

  private void addAudioCodec(String newCodec) {
    if (StringUtils.isBlank(newCodec)) {
      return;
    }

    synchronized (audioCodecsObservable) {
      if (audioCodecsObservable.contains(newCodec)) {
        return;
      }
      audioCodecsObservable.add(newCodec);
    }

    firePropertyChange("audioCodec", null, audioCodecsObservable);
  }

  private void addCertification(Certification newCert) {
    if (newCert == null) {
      return;
    }

    synchronized (certificationsObservable) {
      if (certificationsObservable.contains(newCert)) {
        return;
      }
      certificationsObservable.add(newCert);
    }

    firePropertyChange("certification", null, certificationsObservable);
  }

  /**
   * Search duplicates.
   */
  public void searchDuplicates() {
    Map<String, Movie> imdbDuplicates = new HashMap<>();
    Map<Integer, Movie> tmdbDuplicates = new HashMap<>();

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
    return movieSetList;
  }

  /**
   * get the movie set list in a sorted order
   * 
   * @return the movie set list (sorted)
   */
  public List<MovieSet> getSortedMovieSetList() {
    List<MovieSet> sortedMovieSets = new ArrayList<>(getMovieSetList());
    Collections.sort(sortedMovieSets, movieSetComparator);
    return sortedMovieSets;
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

    try {
      movieSetList.remove(movieSet);
      MovieModuleManager.getInstance().removeMovieSetFromDb(movieSet);
    }
    catch (Exception e) {
      LOGGER.error("Error removing movie set from DB: " + e.getMessage());
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

    if (movieSet == null && StringUtils.isNotBlank(title)) {
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
   * check if there are movies without (at least) one VIDEO mf
   */
  private void checkAndCleanupMediaFiles() {
    List<Movie> moviesToRemove = new ArrayList<>();
    for (Movie movie : movieList) {
      List<MediaFile> mfs = movie.getMediaFiles(MediaFileType.VIDEO);
      if (mfs.isEmpty()) {
        // mark movie for removal
        moviesToRemove.add(movie);
      }
    }

    if (!moviesToRemove.isEmpty()) {
      removeMovies(moviesToRemove);
      LOGGER.warn("movies without VIDEOs detected");

      // and push a message
      // also delay it so that the UI has time to start up
      Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(15000);
          }
          catch (Exception ignored) {
          }
          Message message = new Message(MessageLevel.SEVERE, "tmm.movies", "message.database.corrupteddata");
          MessageManager.instance.pushMessage(message);
        }
      });
      thread.start();
    }
  }

  /**
   * invalidate the title sortable upon changes to the sortable prefixes
   */
  public void invalidateTitleSortable() {
    for (Movie movie : new ArrayList<>(movieList)) {
      movie.clearTitleSortable();
    }
  }

  /**
   * create a new offline movie with the given title in the specified data source
   * 
   * @param title
   *          the given title
   * @param datasource
   *          the data source to create the offline movie in
   */
  public void addOfflineMovie(String title, String datasource) {
    addOfflineMovie(title, datasource, MediaSource.UNKNOWN);
  }

  /**
   * create a new offline movie with the given title in the specified data source
   * 
   * @param title
   *          the given title
   * @param datasource
   *          the data source to create the offline movie in
   * @param mediaSource
   *          the media source to be set for the offline movie
   */
  public void addOfflineMovie(String title, String datasource, MediaSource mediaSource) {
    // first crosscheck if the data source is in our settings
    if (!movieSettings.getMovieDataSource().contains(datasource)) {
      return;
    }

    // check if there is already an identical stub folder
    int i = 1;
    Path stubFolder = Paths.get(datasource, title);
    while (Files.exists(stubFolder)) {
      stubFolder = Paths.get(datasource, title + "(" + i++ + ")");
    }

    Path stubFile = stubFolder.resolve(title + ".disc");

    // create the stub file
    try {
      Files.createDirectory(stubFolder);
      Files.createFile(stubFile);
    }
    catch (IOException e) {
      LOGGER.error("could not create stub file: " + e.getMessage());
      return;
    }

    // create a movie and set it as MF
    MediaFile mf = new MediaFile(stubFile);
    mf.gatherMediaInformation();
    Movie movie = new Movie();

    movie.setTitle(title);
    movie.setPath(stubFolder.toAbsolutePath().toString());
    movie.setDataSource(datasource);
    movie.setMediaSource(mediaSource);
    movie.setDateAdded(new Date());
    movie.addToMediaFiles(mf);
    movie.setOffline(true);
    movie.setNewlyAdded(true);
    addMovie(movie);
    movie.saveToDb();
  }

  private class MovieSetComparator implements Comparator<MovieSet> {
    @Override
    public int compare(MovieSet o1, MovieSet o2) {
      if (o1 == null || o2 == null || o1.getTitleSortable() == null || o2.getTitleSortable() == null) {
        return 0;
      }
      return o1.getTitleSortable().compareToIgnoreCase(o2.getTitleSortable());
    }
  }

  private class MovieMediaScraperComparator implements Comparator<MediaScraper> {
    @Override
    public int compare(MediaScraper o1, MediaScraper o2) {
      return o1.getId().compareTo(o2.getId());
    }
  }
}
