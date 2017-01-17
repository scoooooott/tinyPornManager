package org.tinymediamanager.thirdparty;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieActor;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.tvshow.entities.TvShowActor;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaCastMember.CastType;
import org.tinymediamanager.scraper.entities.MediaGenres;

import com.google.gson.stream.JsonReader;

/**
 * Class to parse Synology .VSMETA files as additional nfo source
 * 
 * @author Myron Boyle
 *
 */
public class VSMeta {

  private static final Logger     LOGGER        = LoggerFactory.getLogger(VSMeta.class);

  private HashMap<String, Object> ids           = new HashMap<>(0);
  private String                  title1        = "";                                   // show/movie
  private String                  title2        = "";                                   // season?
  private String                  title3        = "";                                   // episode/movie tagline
  private String                  description   = "";
  private String                  json          = "";
  private MovieSet                movieSet      = null;
  private float                   rating        = 0.0f;
  private String                  year          = "";
  /** yyyy-mm-dd **/
  private String                  date          = "";
  private Certification           certification = Certification.NOT_RATED;
  private List<MediaArtwork>      artworks      = new ArrayList<MediaArtwork>(0);
  private List<MediaGenres>       genres        = new ArrayList<MediaGenres>(0);
  private List<MediaCastMember>   cast          = new ArrayList<MediaCastMember>(0);

  // global array & position
  private byte[]                  fileArray     = new byte[0];
  private int                     currentpos;

  /**
   * tries to parse a .VSMETA file
   * 
   * @param file
   */
  public void parseFile(Path file) {
    try {
      fileArray = Files.readAllBytes(file);
      if (fileArray.length < 30) {
        LOGGER.warn("SYNO: Invalid file", file);
        return;
      }
      LOGGER.debug("SYNO: found valid .vsmeta - try to parse metadata...");

      int maxLength = fileArray.length > 5000 ? 5000 : fileArray.length - 1;

      for (currentpos = 0; currentpos < maxLength; currentpos++) {
        int b = fileArray[currentpos] & 0xff; // unsigned int

        // =================================================
        // get the values from VSMETA file
        // =================================================
        String ret = "";
        switch (b) {
          case 0x12: // Show/Movie Title
          case 0x1A: // Season Title
          case 0x22: // Episode Title/MoviePlot
          case 0x32: // first aired / release
          case 0x42: // desc
          case 0x4A: // json
          case 0x5A: // certification
            ret = parseLengthString();
            break;

          case 0x52: // length of cast+genre array
            ret = parseLengthString();
            parseCaseGenre(ret.getBytes());
            break;

          case 0x28: // year
            currentpos++;
            int length = getLength(fileArray[currentpos], fileArray[currentpos + 1]);
            if (length > 127) {
              currentpos++;
            }
            ret = String.valueOf(length);
            break;

          case 0x60:
            // TODO: base64 decode images and write to file system
            // here comes the graphics base64 decoding - step out here
            currentpos = maxLength + 1; // haa-haa
            break;

          default:
            LOGGER.trace("*** skip " + currentpos);
            break;
        }

        // =================================================
        // set the value into correct object
        // =================================================
        switch (b) {
          case 0x12:// show/movie title
            title1 = ret;
            break;
          case 0x1A:// season title
            title2 = ret;
            break;
          case 0x22: // episode title / movie plot
            title3 = ret;
            break;
          case 0x28: // year
            year = ret;
            break;
          case 0x32: // first aired
            date = ret;
            break;
          case 0x42: // desc
            description = ret;
            break;
          case 0x4A: // json
            json = ret;
            break;
          case 0x5A: // certification
            certification = Certification.findCertification(ret);
            break;

          default:
            break;
        }

      } // end loop
      fileArray = null;

      // =================================================
      parseJSON();
      // =================================================
    }
    catch (Exception e) {
      LOGGER.warn("SYNO: Error parsing file", e);
    }
  }

  /**
   * parses a length prefixed String, and forwards the counter to the end
   * 
   * @return
   */
  private String parseLengthString() {
    String ret = "";
    int length = 0;

    LOGGER.trace("SYNO: Pos: " + currentpos + " Byt: 0x" + Integer.toHexString(fileArray[currentpos]));
    currentpos++;
    length = getLength(fileArray[currentpos], fileArray[currentpos + 1]);
    LOGGER.trace("SYNO: Len: " + length);
    currentpos++;
    if (length > 127) {
      // if 2 bytes, then skip an additional byte
      currentpos++;
    }
    ret = new String(Arrays.copyOfRange(fileArray, currentpos, currentpos + length));
    if ("null".equals(ret)) {
      ret = "";
    }
    currentpos += length - 1;
    LOGGER.trace("SYNO: " + ret);

    return ret;
  }

  private void parseCaseGenre(byte[] array) {
    MediaCastMember mcm = null;
    int length = 0;
    String ret = "";

    for (int i = 0; i < array.length - 1; i++) {
      int b = array[i] & 0xff; // unsigned int

      LOGGER.trace("SYNO: Pos: " + currentpos + " Byt: 0x" + Integer.toHexString(fileArray[currentpos]));
      i++;
      length = getLength(array[i], array[i + 1]);
      LOGGER.trace("SYNO: Len: " + length);
      i++;
      if (length > 127) {
        i++;
      }
      ret = new String(Arrays.copyOfRange(array, i, i + length));
      i += length - 1;
      LOGGER.trace("SYNO: " + ret);

      switch (b) {
        case 0x0A:
          mcm = new MediaCastMember(CastType.ACTOR);
          mcm.setName(ret);
          cast.add(mcm);
          break;
        case 0x12:
          mcm = new MediaCastMember(CastType.DIRECTOR);
          mcm.setName(ret);
          cast.add(mcm);
          break;
        case 0x22:
          mcm = new MediaCastMember(CastType.WRITER);
          mcm.setName(ret);
          cast.add(mcm);
          break;

        case 0x1A: // Genre
          genres.add(MediaGenres.getGenre(ret));
          break;
        default:
          break;
      }
    }
  }

  private void parseJSON() {
    if (StringUtils.isBlank(json)) {
      return;
    }
    try {
      // parse JSON
      LOGGER.trace("SYNO: try to parse additional JSON info...");
      JsonReader reader = new JsonReader(new StringReader(json));
      reader.beginObject();
      while (reader.hasNext()) {
        String name = reader.nextName();

        if (name.startsWith("com.synology")) {
          reader.beginObject();
          while (reader.hasNext()) {
            name = reader.nextName();

            if (name.equals("backdrop")) {
              reader.beginArray();
              while (reader.hasNext()) {
                String value = reader.nextString();
                LOGGER.trace("SYNO: found backdrop: " + value);
                MediaArtwork ma = new MediaArtwork("com.synology", MediaArtworkType.BACKGROUND);
                ma.setDefaultUrl(value);
                artworks.add(ma);
              }
              reader.endArray();
            }
            else if (name.equals("poster")) {
              reader.beginArray();
              while (reader.hasNext()) {
                String value = reader.nextString();
                LOGGER.trace("SYNO: found poster: " + value);
                MediaArtwork ma = new MediaArtwork("com.synology", MediaArtworkType.POSTER);
                ma.setDefaultUrl(value);
                artworks.add(ma);
              }
              reader.endArray();
            }
            else if (name.equals("rating")) {
              // read array
              reader.beginObject();
              while (reader.hasNext()) {
                String key = reader.nextName();
                String value = reader.nextString();
                LOGGER.trace("SYNO: found rating: " + key + " - " + value);
                try {
                  float f = Float.parseFloat(value);
                  rating = f;
                }
                catch (NumberFormatException e) {
                }
              }
              reader.endObject();
            }
            else if (name.equals("reference")) {
              // read array
              reader.beginObject();
              while (reader.hasNext()) {
                String key = reader.nextName();
                String value = reader.nextString();
                LOGGER.trace("SYNO: found ID: " + key + " = " + value);
                switch (key) {
                  case "imdb":
                    ids.put(Constants.IMDB, value);
                    break;
                  case "thetvdb":
                    ids.put(Constants.TVDB, value);
                    break;
                  case "themoviedb":
                    try {
                      int t = Integer.parseInt(value);
                      ids.put(Constants.TMDB, t);
                    }
                    catch (NumberFormatException e) {
                    }
                    break;

                  default:
                    break;
                }
              }
              reader.endObject();
            }
            else if (name.equals("collection_id")) {
              // read array
              reader.beginObject();
              while (reader.hasNext()) {
                String key = reader.nextName();
                String value = reader.nextString();
                LOGGER.trace("SYNO: found SetID: " + key + " = " + value);
                switch (key) {
                  case "themoviedb":
                    try {
                      int t = Integer.parseInt(value);
                      movieSet = new MovieSet();
                      movieSet.setTmdbId(t);
                    }
                    catch (NumberFormatException e) {
                    }
                    break;

                  default:
                    break;
                }
              }
              reader.endObject();
            }
            else {
              reader.skipValue(); // avoid some unhandled events
            }
          }
        }
        else {
          reader.skipValue(); // avoid some unhandled events
        }
        reader.endObject();
      }
      reader.endObject();
      reader.close();
    }
    catch (IOException e) {
      LOGGER.warn("Could not parse Synology VSMETA file: ", e);
    }
  }

  public Movie getMovie() {
    Movie m = new Movie();
    m.setIds(ids);
    m.setTitle(title1);
    m.setTagline(title3);
    m.setPlot(description);
    m.setReleaseDate(date);
    m.setYear(year);
    m.setRating(rating);
    m.setCertification(certification);

    if (movieSet != null) {
      m.setMovieSet(movieSet);
    }

    for (MediaArtwork ma : artworks) {
      m.setArtworkUrl(ma.getDefaultUrl(), MediaFileType.getMediaFileType(ma.getType()));
    }
    for (MediaGenres g : genres) {
      m.addGenre(g);
    }
    for (MediaCastMember mcm : cast) {
      switch (mcm.getType()) {
        case ACTOR:
          MovieActor actor = new MovieActor();
          actor.setName(mcm.getName());
          m.addActor(actor);
          break;

        case DIRECTOR:
          if (!StringUtils.isEmpty(m.getDirector())) {
            m.setDirector(m.getDirector() + ", ");
          }
          m.setDirector(m.getDirector() + mcm.getName());
          break;

        case WRITER:
          if (!StringUtils.isEmpty(m.getWriter())) {
            m.setWriter(m.getWriter() + ", ");
          }
          m.setWriter(m.getWriter() + mcm.getName());
          break;

        default:
          break;
      }
    }

    return m;
  }

  public TvShowEpisode getTvShowEpisode() {
    TvShowEpisode ep = new TvShowEpisode();
    ep.setIds(ids);
    ep.setTitle(title3);

    ep.setPlot(description);
    ep.setFirstAired(date);
    ep.setYear(year);
    ep.setRating(rating);
    // tv.setCertification(certification);

    for (MediaArtwork ma : artworks) {
      ep.setArtworkUrl(ma.getDefaultUrl(), MediaFileType.getMediaFileType(ma.getType()));
    }
    for (MediaCastMember mcm : cast) {
      switch (mcm.getType()) {
        case ACTOR:
          TvShowActor actor = new TvShowActor();
          actor.setName(mcm.getName());
          ep.addActor(actor);
          break;

        case DIRECTOR:
          if (!StringUtils.isEmpty(ep.getDirector())) {
            ep.setDirector(ep.getDirector() + ", ");
          }
          ep.setDirector(ep.getDirector() + mcm.getName());
          break;

        case WRITER:
          if (!StringUtils.isEmpty(ep.getWriter())) {
            ep.setWriter(ep.getWriter() + ", ");
          }
          ep.setWriter(ep.getWriter() + mcm.getName());
          break;

        default:
          break;
      }
    }

    return ep;
  }

  /**
   * gets the length of 1|2 byte sequence in LittleEndianFormat
   * 
   * @param by1
   *          byte1
   * @param by2
   *          byte2 or 0
   * @return
   */
  private int getLength(byte by1, byte by2) {
    int length = 0;
    int b1 = by1 & 0xFF;
    int b2 = by2 & 0xFF;
    if (b2 > 0 && b2 < 20) { // just to check a valid length a bit
      // hopefully 2 bytes length
      length = b1 + (b2 - 1) * 128;
    }
    else {
      // 1 byte length
      length = b1;
    }
    return length;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
