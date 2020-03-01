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

package org.tinymediamanager.thirdparty;

import static org.tinymediamanager.core.entities.Person.Type.ACTOR;
import static org.tinymediamanager.core.entities.Person.Type.DIRECTOR;
import static org.tinymediamanager.core.entities.Person.Type.WRITER;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.entities.MediaEntity;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.movie.MovieList;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieSet;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;

import com.google.gson.stream.JsonReader;

/**
 * Class to parse Synology .VSMETA files as additional nfo source
 * 
 * @author Myron Boyle
 *
 */
public class VSMeta {

  private static final Logger           LOGGER                    = LoggerFactory.getLogger(VSMeta.class);

  // https://gist.github.com/soywiz/2c10feb1231e70aca19a58aca9d6c16a
  private static final byte             TAG_TITLE1                = 0x12;
  private static final byte             TAG_TITLE2                = 0x1A;
  private static final byte             TAG_TITLE3                = 0x22;
  private static final byte             TAG_YEAR                  = 0x28;
  private static final byte             TAG_RELEASE_DATE          = 0x32;
  private static final byte             TAG_LOCKED                = 0x38;
  private static final byte             TAG_SUMMARY               = 0x42;
  private static final byte             TAG_META_JSON             = 0x4A;                                 // movie & episode
  private static final byte             TAG_CLASSIFICATION        = 0x5A;
  private static final byte             TAG_RATING                = 0x60;
  private static final int              TAG_POSTER_DATA           = 0x8a;
  private static final int              TAG_POSTER_MD5            = 0x92;
  private static final int              TAG_GROUP3                = 0xaa;                                 // on v1, the backdrop is in its own group

  private static final byte             TAG_GROUP1                = 0x52;
  private static final byte             TAG1_CAST                 = 0x0A;
  private static final byte             TAG1_DIRECTOR             = 0x12;
  private static final byte             TAG1_GENRE                = 0x1A;
  private static final byte             TAG1_WRITER               = 0x22;

  private static final int              TAG_GROUP2                = 0x9a;
  private static final byte             TAG2_SEASON               = 0x08;
  private static final byte             TAG2_EPISODE              = 0x10;
  private static final byte             TAG2_RELEASE_DATE_TV_SHOW = 0x22;
  private static final byte             TAG2_LOCKED               = 0x28;

  private static final byte             TAG2_TVSHOW_YEAR          = 0x18;
  private static final byte             TAG2_TVSHOW_SUMMARY       = 0x32;
  private static final byte             TAG2_TVSHOW_POSTER_DATA   = 0x3A;
  private static final byte             TAG2_TVSHOW_POSTER_MD5    = 0x42;
  private static final byte             TAG2_TVSHOW_META_JSON     = 0x4A;

  private static final byte             TAG2_GROUP3               = 0x52;
  private static final byte             TAG3_BACKDROP_DATA        = 0x0a;
  private static final byte             TAG3_BACKDROP_MD5         = 0x12;
  private static final byte             TAG3_TIMESTAMP            = 0x18;

  private static final SimpleDateFormat FORMAT_DATE               = new SimpleDateFormat("yyyy-MM-dd");

  // global array & position
  private MemorySyncStream              data;
  private Info                          info                      = new Info();
  private HashMap<String, Object>       ids                       = new HashMap<>(0);

  private Path                          vsMetaFile                = null;
  private String                        basename                  = "";
  private MovieSet                      movieSet                  = null;
  private float                         rating                    = 0.0f;
  private List<MediaArtwork>            artworks                  = new ArrayList<>(0);

  static class Info {
    public String    title1            = "";             // movie/show title
    public String    title2            = "";             // movie/show title?
    public String    title3            = "";             // movie tagline / episode title
    public int       year              = 0;
    public Date      releaseDate       = null;
    public String    summary           = "";
    public String    classification    = "";
    public int       season            = 1;
    public int       episode           = 1;
    public Double    rating            = null;
    public ListInfo  list              = new ListInfo();
    public ImageInfo images            = new ImageInfo();
    public String    entityJson        = "";             // movie & episode
    public Date      timestamp         = new Date();
    public boolean   locked            = true;

    public String    tvShowJson        = "";
    public Date      tvshowReleaseDate = null;
    public int       tvshowYear        = 0;
    public String    tvshowSummary     = "";
    public boolean   tvshowLocked      = true;
  }

  static class ListInfo {
    public Set<String> cast     = new HashSet<>();
    public Set<String> genre    = new HashSet<>();
    public Set<String> director = new HashSet<>();
    public Set<String> writer   = new HashSet<>();
  }

  static class ImageInfo {
    public byte[] poster    = null; // movie/episode
    public byte[] showImage = null; // show
    public byte[] backdrop  = null;
  }

  static SyncStream openSync(byte[] data) {
    return new MemorySyncStream(data);
  }

  public VSMeta(Path file) {
    this.vsMetaFile = file;
    this.basename = FilenameUtils.getBaseName(FilenameUtils.getBaseName(file.getFileName().toString())); // remove .ext.vsmeta
  }

  private void writeImage(Path file, byte[] bytes) {
    try {
      LOGGER.debug("SYNO: write image to filesystem: {}", file);
      Files.write(file, bytes);
    }
    catch (IOException e) {
      LOGGER.warn("Could not write image {}", file, e);
    }
  }

  /**
   * Extracts the images out of the .vsmeta, and returns MFs you want to add
   * 
   * @param entity
   * @return
   */
  public List<MediaFile> generateMediaFile(MediaEntity entity) {
    List<MediaFile> mfs = new ArrayList<>();

    if (info.images.poster != null) {
      MediaFile mf = null;
      if (entity instanceof Movie) {
        mf = new MediaFile(vsMetaFile.getParent().resolve(basename + "-poster.jpg"), MediaFileType.POSTER);
      }
      else if (entity instanceof TvShowEpisode) {
        mf = new MediaFile(vsMetaFile.getParent().resolve(basename + "-thumb.jpg"), MediaFileType.THUMB);
      }
      if (mf != null && !mf.exists() && !Files.exists(vsMetaFile.getParent().resolve("poster.jpg"))) {
        writeImage(mf.getFileAsPath(), info.images.poster);
        mfs.add(mf);
      }
    }

    if (info.images.backdrop != null) {
      MediaFile mf = null;
      if (entity instanceof Movie) { // || entity instanceof TvShowEpisode) { // no episode fanart - treat it as show fanart!!
        mf = new MediaFile(vsMetaFile.getParent().resolve(basename + "-fanart.jpg"), MediaFileType.FANART);
      }
      if (entity instanceof TvShow) {
        mf = new MediaFile(entity.getPathNIO().resolve("fanart.jpg"), MediaFileType.FANART);
      }
      if (mf != null && !mf.exists() && !Files.exists(vsMetaFile.getParent().resolve("fanart.jpg"))) {
        writeImage(mf.getFileAsPath(), info.images.backdrop);
        mfs.add(mf);
      }
    }

    if (info.images.showImage != null) {
      MediaFile mf = null;
      if (entity instanceof TvShow) {
        mf = new MediaFile(entity.getPathNIO().resolve("poster.jpg"), MediaFileType.POSTER);
      }
      if (mf != null && !mf.exists()) {
        writeImage(mf.getFileAsPath(), info.images.showImage);
        mfs.add(mf);
      }
    }

    return mfs;
  }

  /**
   * tries to parse a .VSMETA file
   * 
   * @param vsMetaFile
   */
  public void parseFile() {
    try {
      data = new MemorySyncStream(Files.readAllBytes(vsMetaFile));

      int magic = data.readU8();
      int version = data.readU8();
      if (magic != 0x08) {
        throw new Exception("Not a vsmeta archive");
      }
      if (version != 0x02) {
        // throw new Exception("Only supported vsmeta version 2");
      }
      LOGGER.debug("SYNO: found valid .vsmeta (Version " + version + ") - try to parse metadata...");

      while (!data.eof()) {
        long pos = data.position();
        int kind = data.readU_VL_Int();
        switch (kind) {
          case TAG_TITLE1:
            info.title1 = data.readStringVL();
            break;
          case TAG_TITLE2:
            info.title2 = data.readStringVL();
            break;
          case TAG_TITLE3:
            info.title3 = data.readStringVL();
            break;
          case TAG_YEAR:
            info.year = data.readU_VL_Int();
            break;
          case TAG_RELEASE_DATE:
            try {
              info.releaseDate = FORMAT_DATE.parse(data.readStringVL());
            }
            catch (ParseException e) {
              LOGGER.warn("Could not parse date...");
            }
            break;
          case TAG_LOCKED:
            info.locked = data.readU_VL_Int() != 0;
            break;
          case TAG_SUMMARY:
            info.summary = data.readStringVL();
            break;
          case TAG_META_JSON:
            info.entityJson = data.readStringVL();
            parseJSON(); // movie & episode
            break;
          case TAG_GROUP1:
            byte[] groupSize = data.readBytesVL();
            parseGroup(openSync(groupSize), info);
            break;
          case TAG_CLASSIFICATION:
            info.classification = data.readStringVL();
            break;
          case TAG_RATING:
            final int it = data.readU_VL_Int();
            if (it < 0) {
              info.rating = null;
            }
            else {
              info.rating = ((double) it) / 10;
            }
            break;
          case TAG_POSTER_DATA:
            info.images.poster = fromBase64IgnoreSpaces(data.readStringVL());
            break;
          case TAG_GROUP3:
            // TODO: avatar (v1) fails, check with actual movie
            // info.images.backdrop = fromBase64IgnoreSpaces(data.readStringVL());
            int data3Size = data.readU_VL_Int();
            long pos3 = (int) data.position();
            byte[] meta3 = data.readBytes(data3Size);
            parseGroup3(openSync(meta3), info, (int) pos3);
            break;
          case TAG_POSTER_MD5:
            // assert (hex(md5(info.imagedata.episodeImage)).equals(data.readStringVL()));
            data.readStringVL();
            break;
          case TAG_GROUP2: {
            int dataSize = data.readU_VL_Int();
            long pos2 = data.position();
            byte[] meta = data.readBytes(dataSize);
            parseGroup2(openSync(meta), info, (int) pos2);
            break;
          }
          default: {
            LOGGER.warn("[MAIN] Unexpected kind={} at {} - try skipping", Integer.toHexString(kind), pos);
            data.readStringVL(); // interpret next value as string.....
          }
        }
      }
    }
    catch (Exception e) {
      LOGGER.warn("SYNO: Error parsing file ({})", vsMetaFile, e);
    }
  }

  private void parseGroup(SyncStream s, Info info) {
    while (!s.eof()) {
      long pos = s.position();
      int kind = s.readU_VL_Int();
      switch (kind) {
        case TAG1_CAST:
          info.list.cast.add(s.readStringVL());
          break;
        case TAG1_DIRECTOR:
          info.list.director.add(s.readStringVL());
          break;
        case TAG1_GENRE:
          info.list.genre.add(s.readStringVL());
          break;
        case TAG1_WRITER:
          info.list.writer.add(s.readStringVL());
          break;
        default:
          LOGGER.warn("[GROUP1] Unexpected kind={} at {}", kind, pos);
      }
    }
  }

  private void parseGroup2(SyncStream s, Info info, int start) {
    while (!s.eof()) {
      long pos = s.position();
      int kind = s.readU_VL_Int();
      switch (kind) {
        case TAG2_SEASON:
          info.season = s.readU_VL_Int();
          break;
        case TAG2_EPISODE:
          info.episode = s.readU_VL_Int();
          break;
        case TAG2_TVSHOW_YEAR:
          info.tvshowYear = s.readU_VL_Int();
          break;
        case TAG2_RELEASE_DATE_TV_SHOW:
          try {
            info.tvshowReleaseDate = FORMAT_DATE.parse(s.readStringVL());
          }
          catch (ParseException e) {
            LOGGER.warn("Could not parse date...");
          }
          break;
        case TAG2_LOCKED:
          info.tvshowLocked = s.readU_VL_Int() != 0;
          break;
        case TAG2_TVSHOW_SUMMARY:
          info.tvshowSummary = s.readStringVL();
          break;
        case TAG2_TVSHOW_POSTER_DATA:
          info.images.showImage = fromBase64IgnoreSpaces(s.readStringVL());
          // writeImage("-tvshow.jpg", info.images.showImage); // TODO: nah, we do not keep TvShow posters on episode basis
          break;
        case TAG2_TVSHOW_POSTER_MD5:
          String md5 = s.readStringVL();
          // if (!md5.equalsIgnoreCase(StrgUtils.bytesToHex(md5(info.images.tvshowPoster)))) {
          // LOGGER.warn("embedded MD5 does not match embedded image...?");
          // }
          break;
        case TAG2_TVSHOW_META_JSON:
          info.tvShowJson = s.readStringVL();
          break;
        case TAG2_GROUP3: { // GROUP3
          int dataSize = s.readU_VL_Int();
          int start2 = (int) s.position();
          byte[] data = s.readBytes(dataSize);
          parseGroup3(openSync(data), info, start2 + start);
          break;
        }
        default:
          LOGGER.warn("[GROUP2] Unexpected kind={} at {}", kind, pos);
      }
    }
  }

  private void parseGroup3(SyncStream s, Info info, int start) {
    while (!s.eof()) {
      long pos = s.position();
      int kind = s.readU_VL_Int();
      switch (kind) {
        case TAG3_BACKDROP_DATA:
          info.images.backdrop = fromBase64IgnoreSpaces(s.readStringVL());
          break;
        case TAG3_BACKDROP_MD5:
          String md5 = s.readStringVL();
          // if (!md5.equalsIgnoreCase(StrgUtils.bytesToHex(md5(info.images.tvshowBackdrop)))) {
          // LOGGER.warn("embedded MD5 does not match embedded image...?");
          // }
          break;
        case TAG3_TIMESTAMP:
          info.timestamp = new Date(s.readU_VL_Long() * 1000L);
          break;
        default:
          LOGGER.warn("[GROUP3] Unexpected kind={} at {}", kind, pos);
      }
    }
  }

  private byte[] fromBase64IgnoreSpaces(String str) {
    byte[] ret = null;
    try {
      ret = Base64.getDecoder().decode(str.replaceAll("\\s+", ""));
    }
    catch (Exception e) {
      LOGGER.warn("Could not decode image: {}", e.getMessage());
    }
    return ret;
  }

  private void parseJSON() {
    if (StringUtils.isBlank(info.entityJson) || "null".equals(info.entityJson)) {
      // "null" being written inside vsmeta
      return;
    }
    try {
      // parse JSON
      LOGGER.trace("SYNO: try to parse additional JSON info...");
      JsonReader reader = new JsonReader(new StringReader(info.entityJson));
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
                  // FIXME: add MediaRatingS
                  float f = Float.parseFloat(value);
                  rating = f;
                }
                catch (NumberFormatException ignored) {
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
                  case "synovideodb":
                    ids.put(key, value);
                    break;
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
                    catch (NumberFormatException ignored) {
                    }
                    break;

                  default:
                    LOGGER.trace("SYNO: yet unknown key '{}' - please add!", key);
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
                      movieSet = MovieList.getInstance().getMovieSet(info.title1 + "_col", t);
                    }
                    catch (NumberFormatException ignored) {
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
      LOGGER.warn("Could not parse Synology VSMETA JSON part!", e);
    }
  }

  public byte[] getPosterBytes() {
    // return an empty byte array if anything is null
    if (info == null || info.images == null || info.images.poster == null) {
      return new byte[0];
    }

    return info.images.poster;
  }

  public byte[] getBackdropBytes() {
    // return an empty byte array if anything is null
    if (info == null || info.images == null || info.images.backdrop == null) {
      return new byte[0];
    }

    return info.images.backdrop;
  }

  public byte[] getShowImageBytes() {
    // return an empty byte array if anything is null
    if (info == null || info.images == null || info.images.showImage == null) {
      return new byte[0];
    }

    return info.images.showImage;
  }

  public Movie getMovie() {
    Movie m = new Movie();
    m.setIds(ids);
    m.setTitle(info.title1);
    m.setTagline(info.title3);
    m.setPlot(info.summary);
    m.setReleaseDate(info.releaseDate);
    try {
      m.setYear(info.year);
    }
    catch (Exception e) {
      m.setYear(0);
    }

    if (rating > 0) {
      MediaRating r = new MediaRating(MediaRating.NFO, rating);
      m.setRating(r);
    }

    m.setCertification(MediaCertification.findCertification(info.classification));

    if (movieSet != null) {
      m.setMovieSet(movieSet);
    }

    for (MediaArtwork ma : artworks) {
      m.setArtworkUrl(ma.getDefaultUrl(), MediaFileType.getMediaFileType(ma.getType()));
    }
    for (String g : info.list.genre) {
      m.addGenre(MediaGenres.getGenre(g));
    }
    for (String cast : info.list.cast) {
      Person mcm = new Person(ACTOR);
      mcm.setName(cast);
      m.addActor(new Person(mcm));
    }
    for (String dir : info.list.director) {
      Person mcm = new Person(DIRECTOR);
      mcm.setName(dir);
      m.addDirector(new Person(mcm));
    }
    for (String writ : info.list.writer) {
      Person mcm = new Person(WRITER);
      mcm.setName(writ);
      m.addWriter(new Person(mcm));
    }

    return m;
  }

  public TvShowEpisode getTvShowEpisode() {
    TvShowEpisode ep = new TvShowEpisode();
    ep.setIds(ids);
    ep.setTitle(info.title3);
    ep.setSeason(info.season);
    ep.setEpisode(info.episode);

    ep.setPlot(info.summary);
    ep.setFirstAired(info.releaseDate);
    try {
      ep.setYear(info.year);
    }
    catch (Exception e) {
      ep.setYear(0);
    }

    if (rating > 0) {
      MediaRating r = new MediaRating(MediaRating.NFO, rating);
      ep.setRating(r);
    }
    // tv.setCertification(certification);

    for (MediaArtwork ma : artworks) {
      ep.setArtworkUrl(ma.getDefaultUrl(), MediaFileType.getMediaFileType(ma.getType()));
    }
    for (String cast : info.list.cast) {
      Person mcm = new Person(ACTOR);
      mcm.setName(cast);
      ep.addActor(new Person(mcm));
    }
    for (String dir : info.list.director) {
      Person mcm = new Person(DIRECTOR);
      mcm.setName(dir);
      ep.addDirector(new Person(mcm));
    }
    for (String writ : info.list.writer) {
      Person mcm = new Person(WRITER);
      mcm.setName(writ);
      ep.addWriter(new Person(mcm));
    }

    return ep;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  static public class MemorySyncStream extends SyncStream {
    int    position;
    int    length;
    byte[] data;

    public MemorySyncStream(byte[] data) {
      this(data, 0);
    }

    public MemorySyncStream(byte[] data, int position) {
      this.data = data;
      this.position = position;
      this.length = data.length;
    }

    @Override
    long position() {
      return position;
    }

    @Override
    long length() {
      return length;
    }

    @Override
    public int readU8() {
      if (eof())
        return -1;
      return this.data[position++];
    }

    @Override
    public void write8(int value) {
      if (position >= length) {
        length++;
        if (length > data.length) {
          data = Arrays.copyOf(data, 7 + data.length * 3);
        }
      }
      this.data[position++] = (byte) value;
    }

    public byte[] toByteArray() {
      return Arrays.copyOf(this.data, this.length);
    }

  }

  static public abstract class SyncStream {
    abstract long position();

    abstract long length();

    boolean eof() {
      return position() >= length();
    }

    boolean hasMore() {
      return !eof();
    }

    long available() {
      return length() - position();
    }

    abstract public int readU8();

    abstract public void write8(int value);

    public void writeBytes(byte[] data) {
      for (int n = 0; n < data.length; n++)
        write8(data[n]);
    }

    public void readExact(byte[] data, int offset, int length) {
      for (int n = 0; n < length; n++) {
        int v = readU8();
        data[offset + n] = (byte) v;
      }
    }

    public byte[] readBytes(int count) {
      byte[] out = new byte[Math.min(count, (int) available())];
      readExact(out, 0, out.length);
      return out;
    }

    public void writeU_VL_Int(int value) {
      writeU_VL_Long((long) value);
    }

    public void writeU_VL_Long(long value) {
      long v = value;
      do {
        int data = (int) (v & 0x7F);
        v = v >>> 7;
        boolean hasMore = v != 0L;
        int data2 = (hasMore) ? 0x80 : 0x00;
        write8(data | data2);
      } while (hasMore());
    }

    public int readU_VL_Int() {
      return (int) readU_VL_Long();
    }

    public long readU_VL_Long() {
      long out = 0L;
      int offset = 0;
      int v;
      do {
        v = readU8();
        out = out | ((long) (v & 0x7F) << offset);
        offset += 7;
      } while ((v & 0x80) != 0 && hasMore());
      LOGGER.trace("SYNO long  dec: {}  hex: {}", out, String.format("%02X", out));
      return out;
    }

    public void writeBytesVL(byte[] data) {
      writeU_VL_Int(data.length);
      writeBytes(data);
    }

    public void writeStringVL(String str, Charset charset) {
      writeBytesVL(str.getBytes(charset));
    }

    public byte[] readBytesVL() {
      byte[] bytes = new byte[readU_VL_Int()];
      readExact(bytes, 0, bytes.length);
      return bytes;
    }

    public String readStringVL() {
      return readStringVL(StandardCharsets.UTF_8);
    }

    public String readStringVL(Charset charset) {
      String str = new java.lang.String(readBytesVL(), charset);
      if (str.length() > 1000) {
        LOGGER.trace("SYNO str: {}... (truncated)", StringUtils.left(str, 1000)); // cut-off pictures
      }
      else {
        LOGGER.trace("SYNO str: {}", str);
      }
      return str;
    }
  }

}
