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
package org.tinymediamanager.core.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.thirdparty.MediaInfo;
import org.tinymediamanager.thirdparty.MediaInfo.StreamKind;
import org.tinymediamanager.thirdparty.MediaInfoXMLParser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileEntry;
import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileSystem;

/**
 * The Class MediaFile.
 * 
 * @author Manuel Laggner
 */
public class MediaFile extends AbstractModelObject implements Comparable<MediaFile> {
  private static final Logger                        LOGGER             = LoggerFactory.getLogger(MediaFile.class);

  private static final String                        PATH               = "path";
  private static final String                        FILENAME           = "filename";
  private static final String                        FILESIZE           = "filesize";
  private static final String                        FILESIZE_IN_MB     = "filesizeInMegabytes";
  private static final List<String>                  PLEX_EXTRA_FOLDERS = Arrays.asList("behind the scenes", "behindthescenes", "deleted scenes",
      "deletedscenes", "featurettes", "interviews", "scenes", "shorts");

  private static Pattern                             moviesetPattern    = Pattern
      .compile("(?i)movieset-(poster|fanart|banner|disc|discart|logo|clearlogo|clearart|thumb)\\..{2,4}");
  private static Pattern                             posterPattern      = Pattern
      .compile("(?i)(.*-poster|poster|folder|movie|.*-cover|cover)\\..{2,4}");
  private static Pattern                             fanartPattern      = Pattern.compile("(?i)(.*-fanart|.*\\.fanart|fanart)[0-9]{0,2}\\..{2,4}");
  private static Pattern                             bannerPattern      = Pattern.compile("(?i)(.*-banner|banner)\\..{2,4}");
  private static Pattern                             thumbPattern       = Pattern.compile("(?i)(.*-thumb|thumb)[0-9]{0,2}\\..{2,4}");
  private static Pattern                             seasonPattern      = Pattern.compile("(?i)season([0-9]{0,2}|-specials)-poster\\..{2,4}");
  private static Pattern                             logoPattern        = Pattern.compile("(?i)(.*-logo|logo)\\..{2,4}");
  private static Pattern                             clearlogoPattern   = Pattern.compile("(?i)(.*-clearlogo|clearlogo)\\..{2,4}");
  // be careful: disc.avi would be valid!
  private static Pattern                             discartPattern     = Pattern
      .compile("(?i)(.*-discart|discart|.*-disc|disc)\\.(jpg|jpeg|png|tbn)");
  private static Pattern                             clearartPattern    = Pattern.compile("(?i)(.*-clearart|clearart)\\..{2,4}");

  public static final String                         VIDEO_FORMAT_480P  = "480p";
  public static final String                         VIDEO_FORMAT_576P  = "576p";
  public static final String                         VIDEO_FORMAT_540P  = "540p";
  public static final String                         VIDEO_FORMAT_720P  = "720p";
  public static final String                         VIDEO_FORMAT_1080P = "1080p";
  public static final String                         VIDEO_FORMAT_4K    = "4k";
  public static final String                         VIDEO_FORMAT_8K    = "8k";

  // meta formats
  public static final String                         VIDEO_FORMAT_SD    = "SD";
  public static final String                         VIDEO_FORMAT_HD    = "HD";

  // 3D / side-by-side / top-and-bottom / H=half - http://wiki.xbmc.org/index.php?title=3D#Video_filenames_flags
  public static final String                         VIDEO_3D           = "3D";
  public static final String                         VIDEO_3D_SBS       = "3D SBS";
  public static final String                         VIDEO_3D_TAB       = "3D TAB";
  public static final String                         VIDEO_3D_HSBS      = "3D HSBS";
  public static final String                         VIDEO_3D_HTAB      = "3D HTAB";

  @JsonProperty
  private MediaFileType                              type               = MediaFileType.UNKNOWN;
  @JsonProperty
  private String                                     path               = "";
  @JsonProperty
  private String                                     filename           = "";
  @JsonProperty
  private long                                       filesize           = 0;
  @JsonProperty
  private long                                       filedate           = 0;
  @JsonProperty
  private String                                     videoCodec         = "";
  @JsonProperty
  private String                                     containerFormat    = "";
  @JsonProperty
  private String                                     exactVideoFormat   = "";
  @JsonProperty
  private String                                     video3DFormat      = "";
  @JsonProperty
  private int                                        videoWidth         = 0;
  @JsonProperty
  private int                                        videoHeight        = 0;
  @JsonProperty
  private int                                        overallBitRate     = 0;
  @JsonProperty
  private int                                        durationInSecs     = 0;
  @JsonProperty
  private int                                        stacking           = 0;
  @JsonProperty
  private String                                     stackingMarker     = "";

  @JsonProperty
  private List<MediaFileAudioStream>                 audioStreams       = new CopyOnWriteArrayList<>();
  @JsonProperty
  private List<MediaFileSubtitle>                    subtitles          = new CopyOnWriteArrayList<>();

  private MediaInfo                                  mediaInfo;
  private Map<StreamKind, List<Map<String, String>>> miSnapshot         = null;
  private Path                                       file               = null;
  private boolean                                    isISO              = false;

  /**
   * "clones" a new media file.
   */
  public MediaFile(MediaFile clone) {
    this.path = new String(clone.path);
    this.filename = new String(clone.filename);
    this.filesize = clone.filesize;
    this.filedate = clone.filedate;
    this.videoCodec = new String(clone.videoCodec);
    this.containerFormat = new String(clone.containerFormat);
    this.exactVideoFormat = new String(clone.exactVideoFormat);
    this.video3DFormat = new String(clone.video3DFormat);
    this.videoHeight = clone.videoHeight;
    this.videoWidth = clone.videoWidth;
    this.overallBitRate = clone.overallBitRate;
    this.durationInSecs = clone.durationInSecs;
    this.stacking = clone.stacking;
    this.stackingMarker = clone.stackingMarker;
    this.type = clone.type;
    this.audioStreams.addAll(clone.audioStreams);
    this.subtitles.addAll(clone.subtitles);
  }

  /**
   * Instantiates a new media file.
   */
  public MediaFile() {
    this.path = "";
    this.filename = "";
  }

  /**
   * Instantiates a new media file.
   * 
   * @deprecated use Java 7 Path() instead of File()
   * @param f
   *          the file
   */
  @Deprecated
  public MediaFile(File f) {
    this(f.toPath(), null);
  }

  /**
   * Instantiates a new media file.
   * 
   * @param f
   *          the file
   */
  public MediaFile(Path f) {
    this(f, null);
  }

  /**
   * Instantiates a new media file.
   * 
   * @deprecated use Java 7 Path() instead of File()
   * @param f
   *          the file
   * @param type
   *          the MediaFileType
   */
  @Deprecated
  public MediaFile(File f, MediaFileType type) {
    this(f.toPath(), type);
  }

  /**
   * Instantiates a new media file.
   * 
   * @param f
   *          the file
   * @param type
   *          the MediaFileType
   */
  public MediaFile(Path f, MediaFileType type) {
    this.path = f.getParent().toString(); // just path w/o filename
    this.filename = f.getFileName().toString();
    this.file = f.toAbsolutePath();
    if (type == null) {
      this.type = parseType();
    }
    else {
      this.type = type;
    }

    // set containerformat for non MI files
    if (!isValidMediainfoFormat() && StringUtils.isBlank(getContainerFormat())) {
      setContainerFormat(getExtension());
    }
  }

  private void gatherSubtitleInformation() {
    MediaFileSubtitle sub = new MediaFileSubtitle();
    String shortname = getBasename().toLowerCase(Locale.ROOT);
    if (shortname.contains("forced")) {
      sub.setForced(true);
      shortname = shortname.replaceAll("\\p{Punct}*forced", "");
    }
    sub.setLanguage(parseLanguageFromString(shortname));

    if (sub.getLanguage().isEmpty() && this.filename.endsWith(".sub")) {
      // not found in name, try to parse from idx
      BufferedReader br;
      try {
        File idx = new File(this.path, this.filename.replaceFirst("sub$", "idx"));
        br = new BufferedReader(new FileReader(idx));
        String line;
        while ((line = br.readLine()) != null) {
          String lang = "";
          // System.out.println("line: " + line);
          if (line.startsWith("id:")) {
            lang = StrgUtils.substr(line, "id: (.*?),");
          }
          if (line.startsWith("# alt:")) {
            lang = StrgUtils.substr(line, "^# alt: (.*?)$");
          }
          if (!lang.isEmpty()) {
            sub.setLanguage(LanguageUtils.getIso3LanguageFromLocalizedString(lang));
            break;
          }
        }
        br.close();
      }
      catch (IOException e) {
        // ignore
      }
    }
    sub.setCodec(getExtension());
    subtitles.clear();
    subtitles.add(sub);
  }

  /**
   * tries to get the MediaFileType out of filename.
   * 
   * @return the MediaFileType
   */
  public MediaFileType parseType() {
    String ext = getExtension().toLowerCase(Locale.ROOT);
    String basename = FilenameUtils.getBaseName(getFilename());
    String foldername = FilenameUtils.getBaseName(getPath()).toLowerCase(Locale.ROOT);

    if (ext.equals("nfo")) {
      return MediaFileType.NFO;
    }

    if (ext.equals("vsmeta")) {
      return MediaFileType.VSMETA;
    }

    if (basename.endsWith("-mediainfo") && "xml".equals(ext)) {
      return MediaFileType.MEDIAINFO;
    }

    if (ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("tbn") || ext.equals("gif")) {
      return parseImageType();
    }

    if (Globals.settings.getAudioFileType().contains("." + ext)) {
      return MediaFileType.AUDIO;
    }

    if (Globals.settings.getSubtitleFileType().contains("." + ext)) {
      return MediaFileType.SUBTITLE;
    }

    if (Globals.settings.getVideoFileType().contains("." + ext)) {
      // has to fit TV & Movie naming...
      // String cleanName = ParserUtils.detectCleanMoviename(name); // tbc if useful...
      // old impl: https://github.com/brentosmith/xbmc-dvdextras
      // Official: http://wiki.xbmc.org/index.php?title=Add-on:VideoExtras#File_Naming_Convention
      if (getFilename().contains(".EXTRAS.") // scene file naming (need to check first! upper case!)
          || basename.matches("(?i).*[_.-]+extra[s]?$") // end with "-extra[s]"
          || basename.matches("(?i).*[-]+extra[s]?[-].*") // extra[s] just with surrounding dash (other delims problem)
          || foldername.equalsIgnoreCase("extras") // preferred folder name
          || foldername.equalsIgnoreCase("extra") // preferred folder name
          || basename.matches("(?i).*[-](behindthescenes|deleted|featurette|interview|scene|short)$") // Plex (w/o trailer)
          || PLEX_EXTRA_FOLDERS.contains(foldername)) // Plex Extra folders
      {
        return MediaFileType.VIDEO_EXTRA;
      }

      if (basename.matches("(?i).*[_.-]*trailer?$") || foldername.equalsIgnoreCase("trailer") || foldername.equalsIgnoreCase("trailers")) {
        return MediaFileType.TRAILER;
      }

      // we have some false positives too - make a more precise check
      if (basename.matches("(?i).*[_.-]*sample$") // end with sample
          || foldername.equalsIgnoreCase("sample")) { // sample folder name
        return MediaFileType.SAMPLE;
      }

      return MediaFileType.VIDEO;
    }

    if (ext.equals("txt")) {
      return MediaFileType.TEXT;
    }

    return MediaFileType.UNKNOWN;
  }

  /**
   * Parses the image type.
   * 
   * @return the media file type
   */
  private MediaFileType parseImageType() {
    String name = getFilename();

    // movieset artwork
    Matcher matcher = moviesetPattern.matcher(name);
    if (matcher.matches()) {
      return MediaFileType.GRAPHIC;
    }

    // *season(XX|-specials)-poster.*
    matcher = seasonPattern.matcher(name);
    if (matcher.matches()) {
      return MediaFileType.SEASON_POSTER;
    }

    // *-poster.* or poster.* or folder.* or movie.*
    matcher = posterPattern.matcher(name);
    if (matcher.matches()) {
      return MediaFileType.POSTER;
    }

    // *-fanart.* or fanart.* or *-fanartXX.* or fanartXX.*
    matcher = fanartPattern.matcher(name);
    if (matcher.matches()) {
      // decide between fanart and extrafanart
      if (getPath().endsWith("extrafanart")) {
        return MediaFileType.EXTRAFANART;
      }
      return MediaFileType.FANART;
    }

    // *-banner.* or banner.*
    matcher = bannerPattern.matcher(name);
    if (matcher.matches()) {
      return MediaFileType.BANNER;
    }

    // *-thumb.* or thumb.* or *-thumbXX.* or thumbXX.*
    matcher = thumbPattern.matcher(name);
    if (matcher.matches()) {
      // decide between thumb and extrathumb
      if (getPath().endsWith("extrathumbs")) {
        return MediaFileType.EXTRATHUMB;
      }
      return MediaFileType.THUMB;
    }

    // clearart.*
    matcher = clearartPattern.matcher(name);
    if (matcher.matches()) {
      return MediaFileType.CLEARART;
    }

    // logo.*
    matcher = logoPattern.matcher(name);
    if (matcher.matches()) {
      return MediaFileType.LOGO;
    }

    // clearlogo.*
    matcher = clearlogoPattern.matcher(name);
    if (matcher.matches()) {
      return MediaFileType.CLEARLOGO;
    }

    // discart.*
    matcher = discartPattern.matcher(name);
    if (matcher.matches()) {
      return MediaFileType.DISCART;
    }

    return MediaFileType.GRAPHIC;
  }

  /**
   * is this a "packed" file? (zip, rar, whatsoever).
   * 
   * @return true/false
   */
  public boolean isPacked() {
    String ext = getExtension().toLowerCase(Locale.ROOT);
    return (ext.equals("zip") || ext.equals("rar") || ext.equals("7z") || ext.matches("r\\d+"));
  }

  /**
   * Is this a graphic file?.
   * 
   * @return true/false
   */
  public boolean isGraphic() {
    switch (type) {
      case GRAPHIC:
      case BANNER:
      case FANART:
      case POSTER:
      case THUMB:
      case LOGO:
      case CLEARLOGO:
      case CLEARART:
      case SEASON_POSTER:
      case EXTRAFANART:
      case EXTRATHUMB:
      case DISCART:
        return true;

      default:
        return false;
    }
  }

  /**
   * Is this a playable video file?.
   * 
   * @return true/false
   */
  public boolean isVideo() {
    return (type.equals(MediaFileType.VIDEO) || type.equals(MediaFileType.VIDEO_EXTRA) || type.equals(MediaFileType.TRAILER)
        || type.equals(MediaFileType.SAMPLE));
  }

  /**
   * is this a "disc file"? (video_ts, vts, bdmv, ...) for movierenamer
   * 
   * @return true/false
   */
  public boolean isDiscFile() {
    return isBlurayFile() || isDVDFile();
  }

  /**
   * is this a BLURAY "disc file"? (video_ts, vts...) for movierenamer
   * 
   * @return true/false
   */
  public boolean isDVDFile() {
    String name = getFilename().toLowerCase(Locale.ROOT);
    return name.matches("(video_ts|vts_\\d\\d_\\d)\\.(vob|bup|ifo)");
  }

  /**
   * is this a DVD "disc file"? (index, movieobject, bdmv, ...) for movierenamer
   * 
   * @return true/false
   */
  public boolean isBlurayFile() {
    String name = getFilename().toLowerCase(Locale.ROOT);
    return name.matches("(index\\.bdmv|movieobject\\.bdmv|\\d{5}\\.m2ts)");
  }

  @Deprecated
  public File getFile() {
    if (file == null) {
      file = getFileAsPath();
    }
    return file.toFile();
  }

  public Path getFileAsPath() {
    if (file == null) {
      Path f = Paths.get(this.path, this.filename);
      file = f.toAbsolutePath();
    }
    return file;
  }

  public void setFile(Path file) {
    setFilename(file.getFileName().toString());
    setPath(file.toAbsolutePath().getParent().toString());
    this.file = file.toAbsolutePath();
  }

  /**
   * if name/path changes, invalidate the file handle (if not null).
   */
  private void invalidateFileHandle() {
    if (file != null) {
      file = null;
    }
  }

  /**
   * Gets the path.
   * 
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets the path.
   * 
   * @param newValue
   *          the new path
   */
  public void setPath(String newValue) {
    String oldValue = this.path;
    this.path = newValue;
    invalidateFileHandle();
    firePropertyChange(PATH, oldValue, newValue);
  }

  /**
   * (re)sets the path (when renaming MediaEntity folder).<br>
   * Exchanges the beginning MF path from oldPath with newPath<br>
   * <br>
   * eg: <br>
   * Params: /movie/alien1/ & /movie/Alien 1/<br>
   * File: /movie/alien1/asdf/jklo/file.avi -> /movie/Alien 1/asdf/jklo/file.avi<br>
   * <br>
   * this id done by a simple string.replace()
   * 
   * @param oldPath
   *          the old path
   * @param newPath
   *          the new path
   */
  public void replacePathForRenamedFolder(Path oldPath, Path newPath) {
    String p = getPath();
    p = p.replace(oldPath.toAbsolutePath().toString(), newPath.toAbsolutePath().toString());
    setPath(p);
  }

  public String getFilename() {
    return filename;
  }

  /**
   * it might be, that there "seems" to be a stacking marker in filename,<br>
   * but the file is not stacked itself. Just return correct string.
   * 
   * @return the file name without the stacking info
   */
  public String getFilenameWithoutStacking() {
    if (stackingMarker.isEmpty()) {
      // no stacking, remove all occurrences
      // fname = Utils.cleanStackingMarkers(filename);
      // NOO, keep em!!! "mockingjay part1" might be unstacked, so keep the part1 !!!
      return filename;
    }
    else {
      // stacking, so just remove known marker
      return filename.replaceAll("[ _.-]*" + stackingMarker, ""); // optional delimiter
    }
  }

  public void setFilename(String newValue) {
    String oldValue = this.filename;
    this.filename = newValue;
    invalidateFileHandle();
    firePropertyChange(FILENAME, oldValue, newValue);
  }

  public String getExtension() {
    return FilenameUtils.getExtension(filename);
  }

  /**
   * Gets the "basename" (filename without extension).
   * 
   * @return the basename
   */
  public String getBasename() {
    return FilenameUtils.getBaseName(filename);
  }

  public long getFiledate() {
    return filedate;
  }

  public long getFilesize() {
    return filesize;
  }

  public void setFilesize(long newValue) {
    long oldValue = this.filesize;
    this.filesize = newValue;
    firePropertyChange(FILESIZE, oldValue, newValue);
    firePropertyChange(FILESIZE_IN_MB, oldValue, newValue);
  }

  public String getFilesizeInMegabytes() {
    DecimalFormat df = new DecimalFormat("#0.00");
    return df.format(filesize / (1024.0 * 1024.0)) + " M";
  }

  public MediaFileType getType() {
    return type;
  }

  public void setType(MediaFileType type) {
    this.type = type;
  }

  public int getStacking() {
    return stacking;
  }

  public void setStacking(int stacking) {
    this.stacking = stacking;
  }

  public String getStackingMarker() {
    return stackingMarker;
  }

  public void setStackingMarker(String stackingMarker) {
    this.stackingMarker = stackingMarker;
  }

  /**
   * this might be needed in case of "Harry Potter 7 - Part 1" - this is no stacking!
   */
  public void removeStackingInformation() {
    setStacking(0);
    setStackingMarker("");
  }

  /**
   * detect stacking information for this media file
   */
  public void detectStackingInformation() {
    this.stacking = Utils.getStackingNumber(this.filename);
    if (this.stacking == 0) {
      // try to parse from parent directory
      this.stacking = Utils.getStackingNumber(FilenameUtils.getBaseName(getPath()));
    }
    this.stackingMarker = Utils.getStackingMarker(this.filename);
    if (this.stackingMarker.isEmpty()) {
      // try to parse from parent directory
      this.stackingMarker = Utils.getFolderStackingMarker(FilenameUtils.getBaseName(getPath()));
    }
  }

  public List<MediaFileSubtitle> getSubtitles() {
    return subtitles;
  }

  public String getSubtitlesAsString() {
    StringBuilder sb = new StringBuilder();
    Set<MediaFileSubtitle> cleansub = new LinkedHashSet<>(subtitles);

    for (MediaFileSubtitle sub : cleansub) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(sub.getLanguage());
    }

    return sb.toString();
  }

  public void setSubtitles(List<MediaFileSubtitle> subtitles) {
    this.subtitles = subtitles;
  }

  public void addSubtitle(MediaFileSubtitle subtitle) {
    if (!this.subtitles.contains(subtitle)) {
      this.subtitles.add(subtitle);
    }
  }

  /**
   * clears all subtitles
   */
  public void clearAllSubtitles() {
    this.subtitles.clear();
  }

  public boolean hasSubtitles() {
    return (subtitles != null && subtitles.size() > 0);
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a <code>toString</code> for the specified object.
   * </p>
   * 
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  /**
   * instantiates and gets new mediainfo object.
   */
  private void getMediaInfoSnapshot() {
    if (mediaInfo == null) {
      mediaInfo = new MediaInfo();
    }

    if (miSnapshot == null) {
      try {
        if (!mediaInfo.open(this.getFileAsPath())) {
          LOGGER.error("Mediainfo could not open file: " + getFileAsPath());

          // clear references
          closeMediaInfo();
        }
        else {
          miSnapshot = mediaInfo.snapshot();
        }
      }
      // sometimes also an error is thrown
      catch (Exception | Error e) {
        LOGGER.error("Mediainfo could not open file: " + getFileAsPath() + "; " + e.getMessage());

        // clear references
        closeMediaInfo();
      }
    }
  }

  /**
   * Closes the connection to the mediainfo lib.
   */
  private void closeMediaInfo() {
    if (mediaInfo != null) {
      mediaInfo.close();
      mediaInfo = null;
    }
    miSnapshot = null;
  }

  /**
   * Gets the real mediainfo values.
   * 
   * @param streamKind
   *          MediaInfo.StreamKind.(General|Video|Audio|Text|Chapters|Image|Menu )
   * @param streamNumber
   *          the stream number (0 for first)
   * @param keys
   *          the information you want to fetch
   * @return the media information you asked<br>
   *         <b>OR AN EMPTY STRING IF MEDIAINFO COULD NOT BE LOADED</b> (never NULL)
   */
  private String getMediaInfo(StreamKind streamKind, int streamNumber, String... keys) {
    if (miSnapshot == null) {
      getMediaInfoSnapshot(); // load snapshot
    }
    if (miSnapshot != null) {
      for (String key : keys) {
        List<Map<String, String>> stream = miSnapshot.get(streamKind);
        if (stream != null) {
          LinkedHashMap<String, String> info = (LinkedHashMap<String, String>) stream.get(streamNumber);
          if (info != null) {
            String value = info.get(key);
            // System.out.println(" " + streamKind + " " + key + " = " + value);
            if (value != null && value.length() > 0) {
              return value;
            }
          }
        }
      }
    }

    return "";
  }

  /**
   * Checks if is empty value.
   * 
   * @param object
   *          the object
   * @return true, if is empty value
   */
  public static boolean isEmptyValue(Object object) {
    return object == null || object.toString().length() == 0;
  }

  /**
   * gets the first token of video codec<br>
   * (e.g. DivX 5 => DivX)
   * 
   * @return the video codec
   */
  public String getVideoCodec() {
    return videoCodec;
  }

  /**
   * Sets the video codec.
   * 
   * @param newValue
   *          the new video codec
   */
  public void setVideoCodec(String newValue) {
    // AVC = h264 = x264; display as h264
    if ("avc".equalsIgnoreCase(newValue) || "x264".equalsIgnoreCase(newValue)) {
      newValue = "h264";
    }

    String oldValue = this.videoCodec;
    this.videoCodec = newValue;
    firePropertyChange("videoCodec", oldValue, newValue);
  }

  /**
   * gets the audio codec<br>
   * (w/o punctuation; eg AC-3 => AC3).
   * 
   * @return the audio codec
   */
  public String getAudioCodec() {
    String codec = "";

    // get audio stream with highest channel count
    MediaFileAudioStream highestStream = getBestAudioStream();
    if (highestStream != null) {
      codec = highestStream.getCodec();
    }

    return codec;
  }

  private MediaFileAudioStream getBestAudioStream() {
    MediaFileAudioStream highestStream = null;
    for (MediaFileAudioStream stream : audioStreams) {
      if (highestStream == null) {
        highestStream = stream;
      }
      else if (highestStream.getChannelsAsInt() < stream.getChannelsAsInt()) {
        highestStream = stream;
      }
    }
    return highestStream;
  }

  public String getAudioLanguage() {
    String language = "";

    // get audio stream with highest channel count
    MediaFileAudioStream highestStream = getBestAudioStream();
    if (highestStream != null) {
      language = highestStream.getLanguage();
    }

    return language;
  }

  /**
   * returns the container format extensions (e.g. avi, mkv mka mks, OGG, etc.)
   * 
   * @return the container format
   */
  public String getContainerFormat() {
    return this.containerFormat;
  }

  /**
   * sets the container format (e.g. avi, mkv mka mks, OGG, etc.)<br>
   * <b>DOES A DIRECT CALL TO MEDIAINFO</b>
   */
  public void setContainerFormatDirect() {
    String extensions = getMediaInfo(StreamKind.General, 0, "Codec/Extensions", "Format");
    setContainerFormat(StringUtils.isEmpty(extensions) ? "" : new Scanner(extensions).next().toLowerCase(Locale.ROOT));
  }

  /**
   * Sets the container format.
   * 
   * @param newValue
   *          the new container format
   */
  public void setContainerFormat(String newValue) {
    String oldValue = this.containerFormat;
    this.containerFormat = newValue;
    firePropertyChange("containerFormat", oldValue, newValue);
  }

  /**
   * gets the "common" video format.
   * 
   * @return 1080p 720p 480p... or SD if too small
   */
  public String getVideoFormat() {
    int w = getVideoWidth();
    int h = getVideoHeight();

    // use XBMC implementation https://github.com/xbmc/xbmc/blob/master/xbmc/utils/StreamDetails.cpp#L559
    if (w == 0 || h == 0) {
      return "";
    }
    else if (w <= blur(720) && h <= blur(480)) {
      return VIDEO_FORMAT_480P;
    }
    // else if (w <= 768 && h <= 576) {
    else if (w <= blur(776) && h <= blur(592)) {
      // 720x576 (PAL) (handbrake sometimes encode it to a max of 776 x 592)
      return VIDEO_FORMAT_576P;
    }
    else if (w <= blur(960) && h <= blur(544)) {
      // 960x540 (sometimes 544 which is multiple of 16)
      return VIDEO_FORMAT_540P;
    }
    else if (w <= blur(1280) && h <= blur(720)) {
      return VIDEO_FORMAT_720P;
    }
    else if (w <= blur(1920) && h <= blur(1080)) {
      return VIDEO_FORMAT_1080P;
    }
    else if (w <= blur(3840) && h <= blur(2160)) {
      return VIDEO_FORMAT_4K;
    }

    return VIDEO_FORMAT_8K;
  }

  // add 1%
  private int blur(int res) {
    return res + (res / 100);
  }

  /**
   * gets the exact video format (height + scantype p/i).
   * 
   * @return the exact video format
   */
  public String getExactVideoFormat() {
    return this.exactVideoFormat;
  }

  /**
   * Sets the exact video format (height + scantype p/i).
   * 
   * @param newValue
   *          the new exact video format
   */
  public void setExactVideoFormat(String newValue) {
    String oldValue = this.exactVideoFormat;
    this.exactVideoFormat = newValue;
    firePropertyChange("exactVideoFormat", oldValue, newValue);
  }

  public String getAudioChannels() {
    String channels = "";

    // get audio stream with highest channel count
    MediaFileAudioStream highestStream = getBestAudioStream();
    if (highestStream != null) {
      channels = highestStream.getChannels();
    }

    return channels;
  }

  /**
   * returns the exact video resolution.
   * 
   * @return eg 1280x720
   */
  public String getVideoResolution() {
    if (this.videoWidth == 0 || this.videoHeight == 0) {
      return "";
    }
    return this.videoWidth + "x" + this.videoHeight;
  }

  public int getVideoWidth() {
    return videoWidth;
  }

  public int getVideoHeight() {
    return videoHeight;
  }

  public void setVideoWidth(int newValue) {
    int oldValue = this.videoWidth;
    this.videoWidth = newValue;
    firePropertyChange("videoWidth", oldValue, newValue);
    firePropertyChange("videoFormat", oldValue, newValue);
  }

  public void setVideoHeight(int newValue) {
    int oldValue = this.videoHeight;
    this.videoHeight = newValue;
    firePropertyChange("videoHeight", oldValue, newValue);
    firePropertyChange("videoFormat", oldValue, newValue);
  }

  /**
   * if width-to-height aspect ratio greater than 1.37:1
   * 
   * @return true/false if widescreen
   */
  public Boolean isWidescreen() {
    if (this.videoWidth == 0 || this.videoHeight == 0) {
      return false;
    }
    return ((float) this.videoWidth) / ((float) this.videoHeight) > 1.37f ? true : false;
  }

  public Float getAspectRatio() {
    Float ret = 0F;
    if (this.videoWidth == 0 || this.videoHeight == 0) {
      return ret;
    }
    Float ar = (float) this.videoWidth / (float) this.videoHeight;

    // https://github.com/xbmc/xbmc/blob/master/xbmc/utils/StreamDetails.cpp#L538
    // Given that we're never going to be able to handle every single possibility in
    // aspect ratios, particularly when cropping prior to video encoding is taken into account
    // the best we can do is take the "common" aspect ratios, and return the closest one available.
    // The cutoffs are the geometric mean of the two aspect ratios either side.
    if (ar < 1.3499f) { // sqrt(1.33*1.37)
      ret = 1.33F;
    }
    else if (ar < 1.5080f) { // sqrt(1.37*1.66)
      ret = 1.37F;
    }
    else if (ar < 1.7190f) { // sqrt(1.66*1.78)
      ret = 1.66F;
    }
    else if (ar < 1.8147f) { // sqrt(1.78*1.85)
      ret = 1.78F;
    }
    else if (ar < 2.0174f) { // sqrt(1.85*2.20)
      ret = 1.85F;
    }
    else if (ar < 2.2738f) { // sqrt(2.20*2.35)
      ret = 2.20F;
    }
    else if (ar < 2.3749f) { // sqrt(2.35*2.40)
      ret = 2.35F;
    }
    else if (ar < 2.4739f) { // sqrt(2.40*2.55)
      ret = 2.40F;
    }
    else if (ar < 2.6529f) { // sqrt(2.55*2.76)
      ret = 2.55F;
    }
    else {
      ret = 2.76F;
    }
    return ret;
  }

  /**
   * SD (less than 720 lines) or HD (more than 720 lines).
   * 
   * @return SD or HD
   */
  public String getVideoDefinitionCategory() {
    if (this.videoWidth == 0 || this.videoHeight == 0) {
      return "";
    }
    return this.videoWidth >= 1280 || this.videoHeight >= 720 ? "HD" : "SD";
  }

  /**
   * returns the overall bit rate for this file.
   * 
   * @return bitrate in kbps
   */
  public int getOverallBitRate() {
    return overallBitRate;
  }

  /**
   * sets the overall bit rate for this file (in kbps).
   * 
   * @param newValue
   *          the new overall bit rate
   */
  public void setOverallBitRate(int newValue) {
    int oldValue = this.overallBitRate;
    this.overallBitRate = newValue;
    firePropertyChange("overallBitRate", oldValue, newValue);
    firePropertyChange("bitRateInKbps", oldValue, newValue);
  }

  /**
   * Gets the bite rate in kbps.
   * 
   * @return the bite rate in kbps
   */
  public String getBiteRateInKbps() {
    return this.overallBitRate + " kbps";
  }

  /**
   * returns the duration / runtime in seconds.
   * 
   * @return the duration
   */
  public int getDuration() {
    return durationInSecs;
  }

  public int getDurationInMinutes() {
    return durationInSecs / 60;
  }

  /**
   * returns the duration / runtime formatted<br>
   * eg 1h 35m.
   * 
   * @return the duration
   */
  public String getDurationHM() {
    if (this.durationInSecs == 0) {
      return "";
    }
    long h = TimeUnit.SECONDS.toHours(this.durationInSecs);
    long m = TimeUnit.SECONDS.toMinutes(this.durationInSecs - TimeUnit.HOURS.toSeconds(h));
    long s = TimeUnit.SECONDS.toSeconds(this.durationInSecs - TimeUnit.HOURS.toSeconds(h) - TimeUnit.MINUTES.toSeconds(m));
    if (s > 30) {
      m += 1; // round seconds
    }
    return h + "h " + String.format("%02d", m) + "m";
  }

  /**
   * returns the duration / runtime formatted<br>
   * eg 01:35:00.
   * 
   * @return the duration
   */
  public String getDurationHHMMSS() {
    if (this.durationInSecs == 0) {
      return "";
    }
    long h = TimeUnit.SECONDS.toHours(this.durationInSecs);
    long m = TimeUnit.SECONDS.toMinutes(this.durationInSecs - TimeUnit.HOURS.toSeconds(h));
    long s = TimeUnit.SECONDS.toSeconds(this.durationInSecs - TimeUnit.HOURS.toSeconds(h) - TimeUnit.MINUTES.toSeconds(m));
    return String.format("%02d:%02d:%02d", h, m, s);
  }

  /**
   * returns the duration / runtime formatted - human readable<br>
   * eg 1:05:12 or 35:12
   *
   * @return the duration
   */
  public String getDurationShort() {
    if (this.durationInSecs == 0) {
      return "";
    }
    long h = TimeUnit.SECONDS.toHours(this.durationInSecs);
    long m = TimeUnit.SECONDS.toMinutes(this.durationInSecs - TimeUnit.HOURS.toSeconds(h));
    long s = TimeUnit.SECONDS.toSeconds(this.durationInSecs - TimeUnit.HOURS.toSeconds(h) - TimeUnit.MINUTES.toSeconds(m));
    if (h > 0) {
      return String.format("%d:%02d:%02d", h, m, s);
    }
    else if (m > 0) {
      return String.format("%d:%02d", m, s);
    }
    else {
      return String.format("%d", s);
    }
  }

  /**
   * sets the duration / runtime in seconds.
   * 
   * @param newValue
   *          the new duration
   */
  public void setDuration(int newValue) {
    int oldValue = this.durationInSecs;
    this.durationInSecs = newValue;
    firePropertyChange("duration", oldValue, newValue);
    firePropertyChange("durationHM", oldValue, newValue);
    firePropertyChange("durationHHMMSS", oldValue, newValue);
  }

  public List<MediaFileAudioStream> getAudioStreams() {
    return audioStreams;
  }

  public void setAudioStreams(List<MediaFileAudioStream> audioStreams) {
    this.audioStreams = audioStreams;
  }

  public String getCombinedCodecs() {
    StringBuilder sb = new StringBuilder(videoCodec);

    for (MediaFileAudioStream audioStream : audioStreams) {
      if (sb.length() > 0) {
        sb.append(" / ");
      }
      sb.append(audioStream.getCodec());
    }

    return sb.toString();
  }

  /**
   * gets the 3D string from former mediainfo<br>
   * can be 3D / 3D SBS / 3D TAB
   * 
   */
  public String getVideo3DFormat() {
    return this.video3DFormat;
  }

  /**
   * explicit set the 3D format
   * 
   * @param video3DFormat
   *          the 3D format
   */
  public void setVideo3DFormat(String video3DFormat) {
    this.video3DFormat = video3DFormat;
  }

  private long getMediaInfoSnapshotFromISO() {
    // check if we have a snapshot xml
    Path xmlFile = Paths.get(this.path, this.filename.replaceFirst("\\.iso$", "-mediainfo.xml"));
    if (Files.exists(xmlFile)) {
      try {
        LOGGER.info("ISO: try to parse " + xmlFile);

        JAXBContext context = JAXBContext.newInstance(MediaInfoXMLParser.class);
        Unmarshaller um = context.createUnmarshaller();
        MediaInfoXMLParser xml = new MediaInfoXMLParser();

        Reader in = Files.newBufferedReader(xmlFile, StandardCharsets.UTF_8);
        xml = (MediaInfoXMLParser) um.unmarshal(in);
        in.close();
        xml.snapshot();

        // get snapshot from biggest file
        setMiSnapshot(xml.getBiggestFile().snapshot);
        setDuration(xml.getDuration()); // accumulated duration
        return xml.getFilesize();
      }
      catch (Exception e) {
        LOGGER.warn("ISO: Unable to parse " + xmlFile, e);
      }
    }

    if (miSnapshot == null) {
      int BUFFER_SIZE = 64 * 1024;
      Iso9660FileSystem image = null;
      try {
        LOGGER.trace("ISO: Open");
        image = new Iso9660FileSystem(getFileAsPath().toFile(), true);
        int dur = 0;
        long siz = 0L; // accumulated filesize
        long biggest = 0L;

        for (Iso9660FileEntry entry : image) {
          LOGGER.trace("ISO: got entry " + entry.getName() + " size:" + entry.getSize());
          siz += entry.getSize();

          if (entry.getSize() <= 5000) { // small files and "." entries
            continue;
          }

          MediaFile mf = new MediaFile(Paths.get(getFileAsPath().toString(), entry.getPath())); // set ISO as MF path
          // mf.setMediaInfo(fileMI); // we need set the inner MI
          if (mf.getType() == MediaFileType.VIDEO && mf.isDiscFile()) { // would not count video_ts.bup for ex (and not .dat files or other types)
            mf.setFilesize(entry.getSize());

            MediaInfo fileMI = new MediaInfo();
            try {
              // mediaInfo.option("File_IsSeekable", "0");
              byte[] From_Buffer = new byte[BUFFER_SIZE];
              int From_Buffer_Size; // The size of the read file buffer

              // Preparing to fill MediaInfo with a buffer
              fileMI.openBufferInit(entry.getSize(), 0);

              long pos = 0L;
              // The parsing loop
              do {
                // Reading data somewhere, do what you want for this.
                From_Buffer_Size = image.readBytes(entry, pos, From_Buffer, 0, BUFFER_SIZE);
                pos += From_Buffer_Size; // add bytes read to file position

                // Sending the buffer to MediaInfo
                int Result = fileMI.openBufferContinue(From_Buffer, From_Buffer_Size);
                if ((Result & 8) == 8) { // Status.Finalized
                  break;
                }

                // Testing if MediaInfo request to go elsewhere
                if (fileMI.openBufferContinueGoToGet() != -1) {
                  pos = fileMI.openBufferContinueGoToGet();
                  LOGGER.trace("ISO: Seek to " + pos);
                  // From_Buffer_Size = image.readBytes(entry, newPos, From_Buffer, 0, BUFFER_SIZE);
                  // pos = newPos + From_Buffer_Size; // add bytes read to file position
                  fileMI.openBufferInit(entry.getSize(), pos); // Informing MediaInfo we have seek
                }

              } while (From_Buffer_Size > 0);

              LOGGER.trace("ISO: finalize");
              // Finalizing
              fileMI.openBufferFinalize(); // This is the end of the stream, MediaInfo must finish some work
              Map<StreamKind, List<Map<String, String>>> tempSnapshot = fileMI.snapshot();
              fileMI.close();

              mf.setMiSnapshot(tempSnapshot); // set ours to MI for standard gathering
              mf.gatherMediaInformation(); // normal gather from snapshots

              // set ISO snapshot ONCE from biggest video file, so we copy all the resolutions & co
              if (entry.getSize() > biggest) {
                biggest = entry.getSize();
                miSnapshot = tempSnapshot;
              }

              // accumulate durations from every MF
              dur += mf.getDuration();
              LOGGER.trace("ISO: file duration:" + mf.getDurationHHMMSS() + "  accumulated min:" + dur / 60);
            }
            // sometimes also an error is thrown
            catch (Exception | Error e) {
              LOGGER.error("Mediainfo could not open file STREAM", e);
              fileMI.close();
            }
          } // end VIDEO
        } // end entry
        setDuration(dur); // set it here, and ignore duration parsing for ISO in gatherMI method...
        LOGGER.trace("ISO: final duration:" + getDurationHHMMSS());
        image.close();
        return siz;
      }
      catch (Exception e) {
        LOGGER.error("Mediainfo could not open STREAM - trying fallback", e);
        try {
          if (image != null) {
            image.close();
            image = null;
          }
        }
        catch (IOException e1) {
          LOGGER.warn("Uh-oh. Cannot close disc image :(", e);
        }
        closeMediaInfo();
        getMediaInfoSnapshot();
      }
    }
    return 0;
  }

  /**
   * DO NOT USE - only for ISO!!!
   */
  @Deprecated
  public void setMediaInfo(MediaInfo mediaInfo) {
    this.mediaInfo = mediaInfo;
  }

  /**
   * DO NOT USE - only for ISO!!!
   */
  @Deprecated
  public void setMiSnapshot(Map<StreamKind, List<Map<String, String>>> miSnapshot) {
    this.miSnapshot = miSnapshot;
  }

  /**
   * Gathers the media information via the native mediainfo lib.<br>
   * If mediafile has already be scanned, it will be skipped.<br>
   * Use gatherMediaInformation(boolean force) to force the execution.
   */
  public void gatherMediaInformation() {
    gatherMediaInformation(false);
  }

  /**
   * Gathers the media information via the native mediainfo lib.
   * 
   * @param force
   *          forces the execution, will not stop on already imported files
   */
  public void gatherMediaInformation(boolean force) {
    // check for supported filetype
    if (!isValidMediainfoFormat()) {
      // okay, we have no valid MI file, be sure it will not be triggered any more
      if (StringUtils.isBlank(getContainerFormat())) {
        setContainerFormat(getExtension());
      }
      return;
    }

    // mediainfo already gathered
    if (!force && !getContainerFormat().isEmpty()) {
      return;
    }

    // gather subtitle infos independent of MI
    if (getType() == MediaFileType.SUBTITLE) {
      gatherSubtitleInformation();
    }

    // file size and last modified
    try {
      BasicFileAttributes attrs = Files.readAttributes(getFileAsPath(), BasicFileAttributes.class);
      filedate = attrs.lastModifiedTime().toMillis();
      setFilesize(attrs.size());
    }
    catch (IOException e) {
      if (miSnapshot == null) { // maybe we set it already (from ISO) so only display message when empty
        LOGGER.warn("could not get file information (size/date): " + e.getMessage());
      }
      // do not set/return here - we might have set it already... and the next check does check for a 0-byte file
      // setContainerFormat(getExtension());
      // return;
    }

    // do not work further on 0 byte files
    if (getFilesize() == 0) {
      LOGGER.warn("0 Byte file detected: " + this.filename);
      // set container format to do not trigger it again
      setContainerFormat(getExtension());
      return;
    }

    // do not work further on subtitles/NFO files
    if (type == MediaFileType.SUBTITLE || type == MediaFileType.NFO) {
      // set container format to do not trigger it again
      setContainerFormat(getExtension());
      return;
    }

    // get media info
    LOGGER.debug("start MediaInfo for " + this.getFileAsPath());
    long discFilesSizes = 0L;
    if (isISO) {
      discFilesSizes = getMediaInfoSnapshotFromISO();
    }
    else {
      getMediaInfoSnapshot();
    }

    if (miSnapshot == null) {
      // MI could not be opened
      LOGGER.error("error getting MediaInfo for " + this.filename);
      // set container format to do not trigger it again
      setContainerFormat(getExtension());
      closeMediaInfo();
      return;
    }
    LOGGER.trace("got MI");

    String height = "";
    String scanType = "";
    String width = "";
    String videoCodec = "";

    switch (type) {
      case VIDEO:
      case VIDEO_EXTRA:
      case SAMPLE:
      case TRAILER:
        height = getMediaInfo(StreamKind.Video, 0, "Height");
        scanType = getMediaInfo(StreamKind.Video, 0, "ScanType");
        width = getMediaInfo(StreamKind.Video, 0, "Width");
        videoCodec = getMediaInfo(StreamKind.Video, 0, "CodecID/Hint", "Format");

        // fix for Microsoft VC-1
        if (StringUtils.containsIgnoreCase(videoCodec, "Microsoft")) {
          videoCodec = getMediaInfo(StreamKind.Video, 0, "Format");
        }

        // *****************
        // get audio streams
        // *****************
        // int streams = getMediaInfo().streamCount(StreamKind.Audio);
        int streams = 0;
        if (streams == 0) {
          // fallback 1
          String cnt = getMediaInfo(StreamKind.General, 0, "AudioCount");
          try {
            streams = Integer.parseInt(cnt);
          }
          catch (Exception e) {
            streams = 0;
          }
        }
        if (streams == 0) {
          // fallback 2
          String cnt = getMediaInfo(StreamKind.Audio, 0, "StreamCount");
          try {
            streams = Integer.parseInt(cnt);
          }
          catch (Exception e) {
            streams = 0;
          }
        }
        audioStreams.clear();
        for (int i = 0; i < streams; i++) {
          MediaFileAudioStream stream = new MediaFileAudioStream();
          String audioCodec = getMediaInfo(StreamKind.Audio, i, "CodecID/Hint", "Format");
          audioCodec = audioCodec.replaceAll("\\p{Punct}", "");

          String audioAddition = getMediaInfo(StreamKind.Audio, i, "Format_Profile");
          if ("dts".equalsIgnoreCase(audioCodec) && StringUtils.isNotBlank(audioAddition)) {
            if (audioAddition.contains("ES")) {
              audioCodec = "DTSHD-ES";
            }
            if (audioAddition.contains("HRA")) {
              audioCodec = "DTSHD-HRA";
            }
            if (audioAddition.contains("MA")) {
              audioCodec = "DTSHD-MA";
            }
          }
          stream.setCodec(audioCodec);

          // AAC sometimes codes channels into Channel(s)_Original
          String channels = getMediaInfo(StreamKind.Audio, i, "Channel(s)_Original", "Channel(s)");
          stream.setChannels(StringUtils.isEmpty(channels) ? "" : channels + "ch");
          try {
            String br = getMediaInfo(StreamKind.Audio, i, "BitRate");
            stream.setBitrate(Integer.parseInt(br) / 1024);
          }
          catch (Exception ignored) {
          }
          String language = getMediaInfo(StreamKind.Audio, i, "Language/String", "Language");
          if (language.isEmpty()) {
            if (!isDiscFile()) { // video_ts parsed 'ts' as Tsonga
              // try to parse from filename
              String shortname = getBasename().toLowerCase(Locale.ROOT);
              stream.setLanguage(parseLanguageFromString(shortname));
            }
          }
          else {
            stream.setLanguage(parseLanguageFromString(language));
          }
          audioStreams.add(stream);
        }

        // ********************
        // get subtitle streams
        // ********************
        // streams = getMediaInfo().streamCount(StreamKind.Text);
        streams = 0;
        if (streams == 0) {
          // fallback 1
          String cnt = getMediaInfo(StreamKind.General, 0, "TextCount");
          try {
            streams = Integer.parseInt(cnt);
          }
          catch (Exception e) {
            streams = 0;
          }
        }
        if (streams == 0) {
          // fallback 2
          String cnt = getMediaInfo(StreamKind.Text, 0, "StreamCount");
          try {
            streams = Integer.parseInt(cnt);
          }
          catch (Exception e) {
            streams = 0;
          }
        }

        subtitles.clear();
        for (int i = 0; i < streams; i++) {
          MediaFileSubtitle stream = new MediaFileSubtitle();

          String codec = getMediaInfo(StreamKind.Text, i, "CodecID/Hint", "Format");
          stream.setCodec(codec.replaceAll("\\p{Punct}", ""));
          String lang = getMediaInfo(StreamKind.Text, i, "Language/String", "Language");
          stream.setLanguage(parseLanguageFromString(lang));

          String forced = getMediaInfo(StreamKind.Text, i, "Forced");
          boolean b = forced.equalsIgnoreCase("true") || forced.equalsIgnoreCase("yes");
          stream.setForced(b);

          subtitles.add(stream);
        }

        // detect 3D video (mainly from MKV files)
        // sample Mediainfo output:
        // MultiView_Count : 2
        // MultiView_Layout : Top-Bottom (left eye first)
        // MultiView_Layout : Side by Side (left eye first)
        String mvc = getMediaInfo(StreamKind.Video, 0, "MultiView_Count");
        if (!StringUtils.isEmpty(mvc) && mvc.equals("2")) {
          video3DFormat = VIDEO_3D;
          String mvl = getMediaInfo(StreamKind.Video, 0, "MultiView_Layout").toLowerCase(Locale.ROOT);
          LOGGER.debug("3D detected :) " + mvl);
          if (!StringUtils.isEmpty(mvl) && mvl.contains("top") && mvl.contains("bottom")) {
            video3DFormat = VIDEO_3D_TAB;
          }
          if (!StringUtils.isEmpty(mvl) && mvl.contains("side")) {
            video3DFormat = VIDEO_3D_SBS;
          }
        }

        break;

      case AUDIO:
        MediaFileAudioStream stream = new MediaFileAudioStream();
        String audioCodec = getMediaInfo(StreamKind.Audio, 0, "CodecID/Hint", "Format");
        stream.setCodec(audioCodec.replaceAll("\\p{Punct}", ""));
        String channels = getMediaInfo(StreamKind.Audio, 0, "Channel(s)");
        stream.setChannels(StringUtils.isEmpty(channels) ? "" : channels + "ch");
        try {
          String br = getMediaInfo(StreamKind.Audio, 0, "BitRate");
          stream.setBitrate(Integer.parseInt(br) / 1024);
        }
        catch (Exception e) {
        }
        String language = getMediaInfo(StreamKind.Audio, 0, "Language/String", "Language");
        if (language.isEmpty()) {
          // try to parse from filename
          String shortname = getBasename().toLowerCase(Locale.ROOT);
          stream.setLanguage(parseLanguageFromString(shortname));
        }
        else {
          stream.setLanguage(parseLanguageFromString(language));
        }
        audioStreams.clear();
        audioStreams.add(stream);
        break;

      case POSTER:
      case BANNER:
      case FANART:
      case THUMB:
      case EXTRAFANART:
      case GRAPHIC:
      case SEASON_POSTER:
      case LOGO:
      case CLEARLOGO:
      case CLEARART:
      case DISCART:
      case EXTRATHUMB:
        height = getMediaInfo(StreamKind.Image, 0, "Height");
        // scanType = getMediaInfo(StreamKind.Image, 0, "ScanType"); // no scantype on graphics
        width = getMediaInfo(StreamKind.Image, 0, "Width");
        videoCodec = getMediaInfo(StreamKind.Image, 0, "CodecID/Hint", "Format");
        // System.out.println(height + "-" + width + "-" + videoCodec);
        break;

      case NFO: // do nothing here, but do not display default warning (since we got the filedate)
        break;

      default:
        LOGGER.warn("no mediainformation handling for MediaFile type " + getType() + " yet.");
        break;
    }

    // video codec
    // e.g. XviD, x264, DivX 5, MPEG-4 Visual, AVC, etc.
    // get first token (e.g. DivX 5 => DivX)
    setVideoCodec(StringUtils.isEmpty(videoCodec) ? "" : new Scanner(videoCodec).next());

    // container format for all except subtitles (subtitle container format is handled another way)
    if (type == MediaFileType.SUBTITLE) {
      setContainerFormat(getExtension());
    }
    else {
      String extensions = getMediaInfo(StreamKind.General, 0, "Codec/Extensions", "Format");
      // get first extension
      setContainerFormat(StringUtils.isBlank(extensions) ? "" : new Scanner(extensions).next().toLowerCase(Locale.ROOT));

      // if container format is still empty -> insert the extension
      if (StringUtils.isBlank(containerFormat)) {
        setContainerFormat(getExtension());
      }
    }

    if (height.isEmpty() || scanType.isEmpty()) {
      setExactVideoFormat("");
    }
    else {
      setExactVideoFormat(height + Character.toLowerCase(scanType.charAt(0)));
    }

    // video dimension
    if (!width.isEmpty()) {
      try {
        setVideoWidth(Integer.parseInt(width));
      }
      catch (NumberFormatException e) {
        setVideoWidth(0);
      }
    }
    if (!height.isEmpty()) {
      try {
        setVideoHeight(Integer.parseInt(height));
      }
      catch (NumberFormatException e) {
        setVideoHeight(0);
      }
    }

    switch (type) {
      case VIDEO:
      case VIDEO_EXTRA:
      case SAMPLE:
      case TRAILER:
      case AUDIO:
        // overall bitrate (OverallBitRate/String)
        String br = getMediaInfo(StreamKind.General, 0, "OverallBitRate");
        if (!br.isEmpty()) {
          try {
            setOverallBitRate(Integer.parseInt(br) / 1024); // in kbps
          }
          catch (NumberFormatException e) {
            setOverallBitRate(0);
          }
        }

        // Duration;Play time of the stream in ms
        // Duration/String;Play time in format : XXx YYy only, YYy omited if zero
        // Duration/String1;Play time in format : HHh MMmn SSs MMMms, XX om.if.z.
        // Duration/String2;Play time in format : XXx YYy only, YYy omited if zero
        // Duration/String3;Play time in format : HH:MM:SS.MMM
        if (!isISO) {
          // ISO files get duration accumulated with snapshot
          String dur = getMediaInfo(StreamKind.General, 0, "Duration");
          if (!dur.isEmpty()) {
            try {
              Double d = Double.parseDouble(dur);
              setDuration(d.intValue() / 1000);
            }
            catch (NumberFormatException e) {
              setDuration(0);
            }
          }
        }
        else {
          if (discFilesSizes > 0 && filesize > 0) {
            // do some sanity check, to see, if we have an invalid DVD structure
            // eg when the sum(filesize) way higher than ISO size
            long diff = Math.abs(filesize - discFilesSizes);
            Double ratio = diff * 100.0 / filesize;
            LOGGER.debug("ISO size:" + filesize + "  reportedDataSize:" + discFilesSizes + "  = diff:" + diff + " ~" + ratio.intValue() + "%");
            if (ratio > 10) {
              LOGGER.error("ISO file seems to have an invalid structure - ignore duration");
              // we set the ISO duration to zero,
              // so the standard getDuration() will always get the scraped duration
              setDuration(0);
            }
          }
        }
      default:
        break;
    }

    LOGGER.trace("extracted MI");
    // close mediainfo lib
    closeMediaInfo();
    LOGGER.trace("closed MI");
  }

  private String parseLanguageFromString(String shortname) {
    if (StringUtils.isBlank(shortname)) {
      return "";
    }
    Set<String> langArray = LanguageUtils.KEY_TO_LOCALE_MAP.keySet();
    shortname = shortname.replaceAll("(?i)Part [Ii]+", ""); // hardcoded; remove Part II which is no stacking marker; b/c II is a valid iso code :p
    shortname = StringUtils.split(shortname, '/')[0].trim(); // possibly "de / de" - just take first
    for (String s : langArray) {
      try {
        if (shortname.equalsIgnoreCase(s) || shortname.matches("(?i).*[ _.-]+" + s + "$")) {// ends with lang + delimiter prefix
          LOGGER.debug("found language '" + s + "' in '" + this.getFilename());
          return LanguageUtils.getIso3LanguageFromLocalizedString(s);
        }
      }
      catch (Exception e) {
        LOGGER.warn("Error parsing subtitle language from locale keyset: " + s, e);
      }
    }
    return "";
  }

  /**
   * Checks if is valid mediainfo format.
   * 
   * @return true, if is valid mediainfo format
   */
  private boolean isValidMediainfoFormat() {
    String extension = FilenameUtils.getExtension(filename).toLowerCase(Locale.ROOT);

    // check unsupported extensions
    if ("bin".equals(extension) || "dat".equals(extension) || "img".equals(extension) || "nrg".equals(extension) || "disc".equals(extension)) {
      return false;
    }
    else if ("iso".equals(extension)) {
      isISO = true;
      return true;
    }

    // parse audio, video and graphic files (NFO only for getting the filedate)
    if (type.equals(MediaFileType.VIDEO) || type.equals(MediaFileType.VIDEO_EXTRA) || type.equals(MediaFileType.TRAILER)
        || type.equals(MediaFileType.SAMPLE) || type.equals(MediaFileType.SUBTITLE) || type.equals(MediaFileType.AUDIO)
        || type.equals(MediaFileType.NFO) || isGraphic()) {
      return true;
    }

    return false;
  }

  /**
   * does MediaFile exists?
   * 
   * @return true/false
   */
  public boolean exists() {
    return Files.exists(getFileAsPath());
  }

  /**
   * <b>PHYSICALLY</b> deletes a MF by moving it to datasource backup folder<br>
   * DS\.backup\&lt;filename&gt;<br>
   * maintaining its orginating directory
   * 
   * @param datasource
   *          the data source (for the location of the backup folder)
   * @return true/false if successful
   */
  public boolean deleteSafely(String datasource) {
    return Utils.deleteFileWithBackup(getFileAsPath(), datasource);
  }

  @Override
  public boolean equals(Object mf2) {
    if ((mf2 != null) && (mf2 instanceof MediaFile)) {
      return compareTo((MediaFile) mf2) == 0;
    }
    return false;
  }

  @Override
  public int compareTo(MediaFile mf2) {
    return this.getFileAsPath().compareTo(mf2.getFileAsPath());
  }

  @Override
  public int hashCode() {
    return this.getFileAsPath().hashCode();
  }
}
