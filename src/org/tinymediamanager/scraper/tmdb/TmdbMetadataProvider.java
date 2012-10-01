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
import com.moviejukebox.themoviedb.model.MovieDb;
import com.moviejukebox.themoviedb.model.Person;
import com.moviejukebox.themoviedb.model.PersonType;
import com.moviejukebox.themoviedb.tools.ApiUrl;

public class TmdbMetadataProvider implements IMediaMetadataProvider, HasFindByIMDBID {

  private static final Logger               log      = Logger.getLogger(TmdbMetadataProvider.class);
  private static final TmdbMetadataProvider instance = new TmdbMetadataProvider();

  private TheMovieDb                        tmdb;

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
  public MediaMetadata getMetadataForIMDBId(String imdbid) {

    return null;
  }

  @Override
  public ProviderInfo getInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  public List<TmdbArtwork> getArtwork(int tmdbId, MediaArtifactType type) throws Exception {
    log.debug("TMDB: getArtwork(tmdbId): " + tmdbId);

    String baseUrl = tmdb.getConfiguration().getBaseUrl();
    List<TmdbArtwork> artwork = new ArrayList<TmdbArtwork>();

    // posters and fanart (first search with lang)
    List<Artwork> movieImages = tmdb.getMovieImages(tmdbId, Globals.searchLanguage);
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

  @Override
  public MediaMetadata getMetaData(MediaSearchResult result) throws Exception {
    log.debug("TMDB: getMetadata(): " + result);

    MediaMetadata md = new MediaMetadata();

    MovieDb movie = tmdb.getMovieInfo(Integer.parseInt(result.getId()), Globals.searchLanguage);
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
    List<Artwork> movieImages = tmdb.getMovieImages(Integer.parseInt(result.getId()), Globals.searchLanguage);
    // posters and fanart (without lang)
    List<Artwork> movieImages_wo_lang = tmdb.getMovieImages(Integer.parseInt(result.getId()), "");
    movieImages.addAll(movieImages_wo_lang);

    for (Artwork image : movieImages) {
      String path = baseUrl + "original" + image.getFilePath();
      if (image.getArtworkType() == ArtworkType.POSTER) {
        processMediaArt(md, MediaArtifactType.POSTER, "Poster", path);
      }

      if (image.getArtworkType() == ArtworkType.BACKDROP) {
        processMediaArt(md, MediaArtifactType.BACKGROUND, "Background", path);
      }

    }

    // cast
    List<Person> cast = tmdb.getMovieCasts(Integer.parseInt(result.getId()));
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

    // TODO Metadata fertig machen

    return md;
  }

  @Override
  public List<MediaSearchResult> search(SearchQuery query) throws Exception {
    List<MediaSearchResult> resultList = new ArrayList<MediaSearchResult>();
    String searchString = query.get(SearchQuery.Field.QUERY);

    log.debug("========= BEGIN TMDB Scraper Search for: " + searchString);
    ApiUrl tmdbSearchMovie = new ApiUrl(tmdb, "search/movie");
    URL url = tmdbSearchMovie.getQueryUrl(searchString, Globals.searchLanguage, 1);
    log.debug(url.toString());

    List<MovieDb> moviesFound = tmdb.searchMovie(searchString, Globals.searchLanguage, false);

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
