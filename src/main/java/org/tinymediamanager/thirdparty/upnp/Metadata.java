/*
 * Copyright 2012 - 2020 Manuel Laggner
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
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.Person;

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
      m.setId(tmmMovie.getDbId().toString());
      m.setParentID(Upnp.ID_MOVIES);
      if (tmmMovie.getYear() > 0) {
        m.addProperty(new DC.DATE(Integer.toString(tmmMovie.getYear()))); // no setDate on Movie (but on other items)???
      }
      m.setTitle(tmmMovie.getTitle());

      List<MediaFile> posters = tmmMovie.getMediaFiles(MediaFileType.POSTER);
      MediaFile poster = posters.isEmpty() ? null : posters.get(0);
      if (poster != null) {
        String rel = tmmMovie.getPathNIO().relativize(poster.getFileAsPath()).toString().replaceAll("\\\\", "/");
        String url = "http://" + Upnp.IP + ":" + Upnp.WEBSERVER_PORT + "/upnp/movies/" + tmmMovie.getDbId().toString() + "/"
            + URLEncoder.encode(rel, "UTF-8");
        Res r = new Res(MimeTypes.getMimeType(poster.getExtension()), poster.getFilesize(), url);
        m.addResource(r);
      }

      for (MediaFile mf : tmmMovie.getMediaFiles(MediaFileType.VIDEO)) {
        String rel = tmmMovie.getPathNIO().relativize(mf.getFileAsPath()).toString().replaceAll("\\\\", "/");
        String url = "http://" + Upnp.IP + ":" + Upnp.WEBSERVER_PORT + "/upnp/movies/" + tmmMovie.getDbId().toString() + "/"
            + URLEncoder.encode(rel, "UTF-8");
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
        for (Person a : tmmMovie.getActors()) {
          persons.add(new PersonWithRole(a.getName(), a.getRole()));
        }
        if (!persons.isEmpty()) {
          PersonWithRole[] arr = persons.toArray(new PersonWithRole[persons.size()]);
          m.setActors(arr);
        }

        persons = new ArrayList<>();
        for (Person a : tmmMovie.getProducers()) {
          persons.add(new PersonWithRole(a.getName(), a.getRole()));
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
      m.setParentID(Upnp.ID_TVSHOWS + "/" + show.getDbId().toString() + "/" + ep.getSeason());
      if (ep.getYear() > 0) {
        m.addProperty(new DC.DATE(Integer.toString(ep.getYear()))); // no setDate on Movie (but on other items)???
      }
      m.setTitle("S" + lz(ep.getSeason()) + "E" + lz(ep.getEpisode()) + " " + ep.getTitle());

      for (MediaFile mf : ep.getMediaFiles(MediaFileType.VIDEO)) {
        String rel = show.getPathNIO().relativize(mf.getFileAsPath()).toString().replaceAll("\\\\", "/");
        String url = "http://" + Upnp.IP + ":" + Upnp.WEBSERVER_PORT + "/upnp/tvshows/" + show.getDbId().toString() + "/"
            + URLEncoder.encode(rel, "UTF-8");
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
        for (Person a : ep.getActors()) {
          persons.add(new PersonWithRole(a.getName(), a.getRole()));
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
