package org.tinymediamanager.thirdparty.upnp;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.fourthline.cling.support.model.DIDLObject.Property.DC;
import org.fourthline.cling.support.model.PersonWithRole;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.item.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.MovieActor;
import org.tinymediamanager.core.movie.entities.MovieProducer;
import org.tinymediamanager.core.tvshow.entities.TvShowActor;
import org.tinymediamanager.scraper.entities.MediaGenres;

public class Metadata {

  // https://github.com/4thline/cling/tree/master/support/src/main/java/org/fourthline/cling/support/model/item

  private static final Logger LOGGER = LoggerFactory.getLogger(Metadata.class);

  /**
   * wraps a TMM movie into a UPNP movie/video item object
   * 
   * @param tmmMovie
   *          our movie
   * @param full
   *          full details, or when false just the mandatory for a directory listing (title, and a few others)
   * @return
   */
  public static Movie getUpnpMovie(org.tinymediamanager.core.movie.entities.Movie tmmMovie, boolean full) {

    LOGGER.trace(tmmMovie.getTitle());
    Movie m = new Movie();
    try {
      m.setId(Upnp.ID_MOVIES + "/" + tmmMovie.getDbId().toString());
      m.setParentID(Upnp.ID_MOVIES);
      if (!tmmMovie.getYear().isEmpty()) {
        m.addProperty(new DC.DATE(tmmMovie.getYear())); // no setDate on Movie (but on other items)???
      }
      m.setTitle(tmmMovie.getTitle());

      for (MediaFile mf : tmmMovie.getMediaFiles(MediaFileType.VIDEO)) {
        String rel = tmmMovie.getPathNIO().relativize(mf.getFileAsPath()).toString().replaceAll("\\\\", "/");
        String url = "http://" + Upnp.IP + ":8008/upnp/movies/" + tmmMovie.getDbId().toString() + "/" + URLEncoder.encode(rel, "UTF-8");
        Res r = new Res(MimeTypes.getMimeType(mf.getExtension()), mf.getFilesize(), url);
        m.addResource(r);
      }

      if (full) {
        // TODO: m.setDirectors();
        m.setDescription(tmmMovie.getPlot());
        m.setLanguage(tmmMovie.getSpokenLanguages());
        m.setRating(String.valueOf(tmmMovie.getRating()));

        List<String> genres = new ArrayList<>();
        for (MediaGenres g : tmmMovie.getGenres()) {
          genres.add(g.getLocalizedName());
        }
        if (!genres.isEmpty()) {
          String[] arr = genres.toArray(new String[genres.size()]);
          m.setGenres(arr);
        }

        List<PersonWithRole> persons = new ArrayList<>();
        for (MovieActor a : tmmMovie.getActors()) {
          persons.add(new PersonWithRole(a.getName(), a.getCharacter()));
        }
        if (!persons.isEmpty()) {
          PersonWithRole[] arr = persons.toArray(new PersonWithRole[persons.size()]);
          m.setActors(arr);
        }

        persons = new ArrayList<>();
        for (MovieProducer a : tmmMovie.getProducers()) {
          persons.add(new PersonWithRole(a.getName(), a.getCharacter()));
        }
        if (!persons.isEmpty()) {
          PersonWithRole[] arr = persons.toArray(new PersonWithRole[persons.size()]);
          m.setProducers(arr);
        }
      }

    }
    catch (Exception e) {
      LOGGER.error("Error getting TMM movie", e);
    }
    return m;
  }

  /**
   * wraps a TMM TvShowEpisode into a UPNP tvshow/video item object
   * 
   * @param show
   *          our TvShow
   * @param full
   *          full details, or when false just the mandatory for a directory listing (title, and a few others)
   * @return
   */
  public static Movie getUpnpTvShowEpisode(org.tinymediamanager.core.tvshow.entities.TvShow show,
      org.tinymediamanager.core.tvshow.entities.TvShowEpisode ep, boolean full) {
    LOGGER.trace(ep.getTitle());
    Movie m = new Movie(); // yes, it is a UPNP movie object!

    try {
      // 2/UUID/S/E
      m.setId(Upnp.ID_TVSHOWS + "/" + show.getDbId().toString() + "/" + ep.getSeason() + "/" + ep.getEpisode());
      m.setParentID(Upnp.ID_TVSHOWS + "/" + show.getDbId().toString());
      if (!ep.getYear().isEmpty()) {
        m.addProperty(new DC.DATE(ep.getYear())); // no setDate on Movie (but on other items)???
      }
      m.setTitle("S" + lz(ep.getSeason()) + "E" + lz(ep.getEpisode()) + " " + ep.getTitle());

      for (MediaFile mf : ep.getMediaFiles(MediaFileType.VIDEO)) {
        String rel = show.getPathNIO().relativize(mf.getFileAsPath()).toString().replaceAll("\\\\", "/");
        String url = "http://" + Upnp.IP + ":8008/upnp/tvshows/" + show.getDbId().toString() + "/" + URLEncoder.encode(rel, "UTF-8");
        Res r = new Res(MimeTypes.getMimeType(mf.getExtension()), mf.getFilesize(), url);
        m.addResource(r);
      }

      if (full) {
        m.setDescription(ep.getPlot());
        m.setRating(String.valueOf(ep.getRating()));

        List<String> genres = new ArrayList<>();
        for (MediaGenres g : show.getGenres()) {
          genres.add(g.getLocalizedName());
        }
        if (!genres.isEmpty()) {
          String[] arr = genres.toArray(new String[genres.size()]);
          m.setGenres(arr);
        }

        List<PersonWithRole> persons = new ArrayList<>();
        for (TvShowActor a : ep.getActors()) {
          persons.add(new PersonWithRole(a.getName(), a.getCharacter()));
        }
        if (!persons.isEmpty()) {
          PersonWithRole[] arr = persons.toArray(new PersonWithRole[persons.size()]);
          m.setActors(arr);
        }
      }

    }
    catch (Exception e) {
      LOGGER.error("Error getting TMM show", e);
    }

    return m;
  }

  /**
   * add leadingZero if only 1 char
   * 
   * @param num
   *          the number
   * @return the string with a leading 0
   */
  private static String lz(int num) {
    return String.format("%02d", num);
  }
}
