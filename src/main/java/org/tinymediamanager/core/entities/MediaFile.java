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
package org.tinymediamanager.core.entities;

import static org.tinymediamanager.core.MediaFileHelper.getFirstEntryViaScanner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.MediaFileHelper;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.thirdparty.MediaInfo.StreamKind;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.madgag.gif.fmsware.GifDecoder;

/**
 * The Class MediaFile.
 *
 * @author Manuel Laggner
 */
public class MediaFile extends AbstractModelObject implements Comparable<MediaFile> {
  private static final Logger        LOGGER            = LoggerFactory.getLogger(MediaFile.class);

  @JsonProperty
  private MediaFileType              type              = MediaFileType.UNKNOWN;
  @JsonProperty
  private String                     path              = "";
  @JsonProperty
  private String                     filename          = "";
  @JsonProperty
  private long                       filesize          = 0;
  @JsonProperty
  private long                       filedate          = 0;
  @JsonProperty
  private String                     videoCodec        = "";
  @JsonProperty
  private String                     containerFormat   = "";
  @JsonProperty
  private String                     exactVideoFormat  = "";
  @JsonProperty
  private String                     video3DFormat     = "";
  @JsonProperty
  private int                        videoWidth        = 0;
  @JsonProperty
  private int                        videoHeight       = 0;
  @JsonProperty
  private float                      aspectRatio       = 0f;
  @JsonProperty
  private int                        overallBitRate    = 0;
  @JsonProperty
  private int                        bitDepth          = 0;
  @JsonProperty
  private double                     frameRate         = 0.0d;
  @JsonProperty
  private int                        durationInSecs    = 0;
  @JsonProperty
  private int                        stacking          = 0;
  @JsonProperty
  private String                     stackingMarker    = "";
  @JsonProperty
  private String                     title             = "";
  @JsonProperty
  protected Date                     dateCreated       = null;
  @JsonProperty
  protected Date                     dateLastModified  = null;
  @JsonProperty
  private boolean                    isISO             = false;
  @JsonProperty
  private boolean                    isAnimatedGraphic = false;
  @JsonProperty
  private String                     hdrFormat         = "";

  @JsonProperty
  private List<MediaFileAudioStream> audioStreams      = null;
  @JsonProperty
  private List<MediaFileSubtitle>    subtitles         = null;

  private Path                       file              = null;

  /**
   * "clones" a new media file.
   */
  public MediaFile(MediaFile clone) {
    this.type = clone.type;
    this.path = clone.path;
    this.filename = clone.filename;
    this.filesize = clone.filesize;
    this.filedate = clone.filedate;
    this.videoCodec = clone.videoCodec;
    this.containerFormat = clone.containerFormat;
    this.exactVideoFormat = clone.exactVideoFormat;
    this.video3DFormat = clone.video3DFormat;
    this.videoHeight = clone.videoHeight;
    this.videoWidth = clone.videoWidth;
    this.aspectRatio = clone.aspectRatio;
    this.overallBitRate = clone.overallBitRate;
    this.bitDepth = clone.bitDepth;
    this.frameRate = clone.frameRate;
    this.durationInSecs = clone.durationInSecs;
    this.stacking = clone.stacking;
    this.stackingMarker = clone.stackingMarker;
    this.title = clone.title;
    this.dateCreated = clone.dateCreated;
    this.dateLastModified = clone.dateLastModified;
    this.isISO = clone.isISO;
    this.isAnimatedGraphic = clone.isAnimatedGraphic;
    this.hdrFormat = clone.hdrFormat;

    if (ListUtils.isNotEmpty(clone.audioStreams)) {
      audioStreams = new CopyOnWriteArrayList<>(clone.audioStreams);
    }
    if (ListUtils.isNotEmpty(clone.subtitles)) {
      subtitles = new CopyOnWriteArrayList<>(clone.subtitles);
    }
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
   * @param f
   *          the file
   */
  public MediaFile(Path f) {
    this(f, null);
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
    this.path = f.getParent() == null ? "" : f.getParent().toString(); // just path w/o filename
    this.filename = f.getFileName().toString();
    this.file = f.toAbsolutePath();
    if (type == null) {
      this.type = MediaFileHelper.parseMediaFileType(f);
    }
    else {
      this.type = type;
    }

    // check if that file is an ISO file
    if ("iso".equalsIgnoreCase(FilenameUtils.getExtension(filename))) {
      isISO = true;
    }

    // set containerformat for non MI files
    if (!isValidMediainfoFormat() && StringUtils.isBlank(getContainerFormat())) {
      setContainerFormat(getExtension());
    }
  }

  /**
   * Checks if is valid mediainfo format.
   *
   * @return true, if is valid mediainfo format
   */
  public boolean isValidMediainfoFormat() {
    String extension = getExtension();

    // check unsupported extensions
    if ("bin".equalsIgnoreCase(extension) || "dat".equalsIgnoreCase(extension) || "img".equalsIgnoreCase(extension)
        || "nrg".equalsIgnoreCase(extension) || "disc".equalsIgnoreCase(extension)) {
      return false;
    }
    else if ("iso".equalsIgnoreCase(extension)) {
      return true;
    }

    // parse audio, video and graphic files (NFO only for getting the filedate)
    if (type.equals(MediaFileType.VIDEO) || type.equals(MediaFileType.EXTRA) || type.equals(MediaFileType.TRAILER)
        || type.equals(MediaFileType.SAMPLE) || type.equals(MediaFileType.SUBTITLE) || type.equals(MediaFileType.AUDIO)
        || type.equals(MediaFileType.NFO) || isGraphic()) {
      return true;
    }

    return false;
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
      case CHARACTERART:
      case KEYART:
      case SEASON_POSTER:
      case SEASON_BANNER:
      case SEASON_THUMB:
      case EXTRAFANART:
      case EXTRATHUMB:
      case DISC:
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
    switch (type) {
      case VIDEO:
      case TRAILER:
      case SAMPLE:
      case VIDEO_EXTRA:
        return true;

      case EXTRA:
        // check if that extra is a video file
        if (Globals.settings.getVideoFileType().contains("." + getExtension().toLowerCase(Locale.ROOT))) {
          return true;
        }
        break;

      default:
        break;
    }
    return false;
  }

  /**
   * is this a "disc file"? (video_ts, vts, bdmv, ...) for movierenamer
   *
   * @return true/false
   */
  public boolean isDiscFile() {
    return isBlurayFile() || isDVDFile() || isHdDVDFile();
  }

  /**
   * is this a DVD "disc file"? (video_ts, vts...) for movierenamer
   *
   * @return true/false
   */
  public boolean isDVDFile() {
    String name = getFilename().toLowerCase(Locale.ROOT);
    return name.matches("(video_ts|vts_\\d\\d_\\d)\\.(vob|bup|ifo)");
  }

  /**
   * is this a HD-DVD "disc file"? (hvdvd_ts, hv...) for movierenamer
   *
   * @return true/false
   */
  public boolean isHdDVDFile() {
    String name = getFilename().toLowerCase(Locale.ROOT);
    String foldername = FilenameUtils.getBaseName(getPath()).toLowerCase(Locale.ROOT);
    return "hvdvd_ts".equals(foldername) && name.matches(".*(evo|bup|ifo|map)$");
  }

  /**
   * is this a Bluray "disc file"? (index, movieobject, bdmv, ...) for movierenamer
   *
   * @return true/false
   */
  public boolean isBlurayFile() {
    String name = getFilename().toLowerCase(Locale.ROOT);
    return name.matches("(index\\.bdmv|movieobject\\.bdmv|\\d{5}\\.m2ts)");
  }

  /**
   * Every disc has its own starting point.<br>
   * So this returns true if we found it.
   * 
   * @return
   */
  public boolean isMainDiscIdentifierFile() {
    if (getFilename().equalsIgnoreCase("video_ts.ifo") || getFilename().equalsIgnoreCase("index.bdmv")
        || getFilename().equalsIgnoreCase("hv000i01.ifo")) {
      return true;
    }
    return false;
  }

  public Path getFile() {
    return getFileAsPath();
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
    firePropertyChange("path", oldValue, newValue);
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
    firePropertyChange("filename", oldValue, newValue);
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

  public void setFiledate(long newValue) {
    long oldValue = this.filedate;
    this.filedate = newValue;
    firePropertyChange("filedate", oldValue, newValue);
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
    firePropertyChange("filesize", oldValue, newValue);
    firePropertyChange("filesizeInMegabytes", oldValue, newValue);
  }

  public String getFilesizeInMegabytes() {
    DecimalFormat df = new DecimalFormat("#0.00");
    return df.format(filesize / (1000.0 * 1000.0)) + " M";
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

  public boolean isISO() {
    return isISO;
  }

  public void setIsISO(boolean newValue) {
    this.isISO = newValue;
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
    if (this.subtitles == null) {
      return new ArrayList<>();
    }
    return subtitles;
  }

  /**
   * gets the subtitle language from all streams as List
   *
   * @return the subtitle languages as List
   */
  public List<String> getSubtitleLanguagesList() {
    List<String> subtitleLanguages = new ArrayList<>();
    for (MediaFileSubtitle stream : ListUtils.nullSafe(subtitles)) {
      // just in case we couldn't detect the language name
      if (StringUtils.isBlank(stream.getLanguage())) {
        continue;
      }
      subtitleLanguages.add(stream.getLanguage());
    }
    return subtitleLanguages;
  }

  public String getSubtitlesAsString() {
    if (this.subtitles == null) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    Set<MediaFileSubtitle> cleansub = new LinkedHashSet<>(subtitles);

    for (MediaFileSubtitle sub : cleansub) {
      // just in case we couldn't detect the language name
      if (StringUtils.isBlank(sub.getLanguage())) {
        continue;
      }
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(sub.getLanguage());
    }

    return sb.toString();
  }

  public void setSubtitles(List<MediaFileSubtitle> subtitles) {
    if (this.subtitles == null && subtitles.isEmpty()) {
      // nothing to do
      return;
    }

    if (this.subtitles == null) {
      this.subtitles = new CopyOnWriteArrayList<>();
    }
    else {
      this.subtitles.clear();
    }

    this.subtitles.addAll(subtitles);
    firePropertyChange("subtitles", null, subtitles);
  }

  public void addSubtitle(MediaFileSubtitle subtitle) {
    if (this.subtitles == null) {
      this.subtitles = new CopyOnWriteArrayList<>();
    }

    if (!this.subtitles.contains(subtitle)) {
      this.subtitles.add(subtitle);
      firePropertyChange("subtitles", null, subtitles);
    }
  }

  public boolean hasSubtitles() {
    return (subtitles != null && !subtitles.isEmpty());
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
   * The embedded TITLE
   * 
   * @return
   */
  public String getTitle() {
    return title;
  }

  public void setTitle(String newValue) {
    String oldValue = this.title;
    this.title = newValue;
    firePropertyChange("title", oldValue, newValue);
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
    // HEVC = h265 = x265; display as h265
    if ("hevc".equalsIgnoreCase(newValue) || "x265".equalsIgnoreCase(newValue)) {
      newValue = "h265";
    }

    String oldValue = this.videoCodec;
    this.videoCodec = newValue;
    firePropertyChange("videoCodec", oldValue, newValue);
  }

  /**
   * gets the audio codec from the main<br>
   * (w/o punctuation; eg AC-3 => AC3).
   *
   * @return the audio codec
   */
  public String getAudioCodec() {
    String codec = "";

    // get audio stream with highest channel count
    MediaFileAudioStream highestStream = getDefaultOrBestAudioStream();
    if (highestStream != null) {
      codec = highestStream.getCodec();
    }

    return codec;
  }

  /**
   * gets the audio codecs from all streams as List<br>
   * (w/o punctuation; eg AC-3 => AC3).
   *
   * @return the audio codecs as List
   */
  public List<String> getAudioCodecList() {
    List<String> audioCodecs = new ArrayList<>();
    for (MediaFileAudioStream stream : ListUtils.nullSafe(audioStreams)) {
      audioCodecs.add(stream.getCodec());
    }
    return audioCodecs;
  }

  /**
   * returns the "best" available audio stream (aka the one with most channels)
   * 
   * @return
   */
  private MediaFileAudioStream getBestAudioStream() {
    MediaFileAudioStream highestStream = null;
    for (MediaFileAudioStream stream : ListUtils.nullSafe(audioStreams)) {
      if (highestStream == null) {
        highestStream = stream;
      }
      else if (highestStream.getAudioChannels() < stream.getAudioChannels()) {
        highestStream = stream;
      }
    }
    return highestStream;
  }

  /**
   * gets the audio language from the main stream
   *
   * @return the audio language
   */
  public String getAudioLanguage() {
    String language = "";

    // get audio stream with highest channel count
    MediaFileAudioStream highestStream = getDefaultOrBestAudioStream();
    if (highestStream != null) {
      language = highestStream.getLanguage();
    }

    return language;
  }

  /**
   * gets the audio language from all streams as List
   *
   * @return the audio languages as List
   */
  public List<String> getAudioLanguagesList() {
    List<String> audioLanguages = new ArrayList<>();
    for (MediaFileAudioStream stream : ListUtils.nullSafe(audioStreams)) {
      // just in case we couldn't detect the language name
      if (StringUtils.isBlank(stream.getLanguage())) {
        continue;
      }
      audioLanguages.add(stream.getLanguage());
    }
    return audioLanguages;
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
    String extensions = MediaFileHelper.getMediaInfoDirect(this, StreamKind.General, 0, "FileExtension", "Codec/Extensions", "Format/Extensions",
        "Format");
    // remove some well known false positives
    extensions = extensions.replace("braw", ""); // appears when inspecting .mov files
    extensions = extensions.replace("h3d", ""); // appears when inspecting .jpg files
    setContainerFormat(getFirstEntryViaScanner(extensions).toLowerCase(Locale.ROOT));
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
    return MediaFileHelper.getVideoFormat(this);
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

  /**
   * gets the audio channels (with trailing ch) from the main stream<br>
   *
   * @return the audio channels
   */
  public String getAudioChannels() {
    String channels = "";

    // get audio stream with highest channel count
    MediaFileAudioStream highestStream = getDefaultOrBestAudioStream();
    if (highestStream != null) {
      channels = highestStream.getAudioChannels() + "ch";
    }

    return channels;
  }

  public List<String> getAudioChannelsList() {
    List<String> audioChannels = new ArrayList<>();
    for (MediaFileAudioStream stream : ListUtils.nullSafe(audioStreams)) {
      audioChannels.add(stream.getAudioChannels() + "ch");
    }
    return audioChannels;
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

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Date newValue) {
    Date oldValue = this.dateCreated;
    this.dateCreated = newValue;
    firePropertyChange("dateCreated", oldValue, newValue);
  }

  public Date getDateLastModified() {
    return dateLastModified;
  }

  public void setDateLastModified(Date newValue) {
    Date oldValue = this.dateLastModified;
    this.dateLastModified = newValue;
    firePropertyChange("exactVideoFormat", oldValue, newValue);
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
    return ((float) this.videoWidth) / ((float) this.videoHeight) > 1.37f;
  }

  /**
   * override the calculated aspect ratio with this value (only if the values differ)
   *
   * @param newValue
   *          the aspect ratio to be forced
   */
  public void setAspectRatio(float newValue) {
    if (newValue == this.aspectRatio) {
      return;
    }

    float oldValue = this.aspectRatio;
    this.aspectRatio = newValue;
    firePropertyChange("aspectRatio", oldValue, newValue);
  }

  /**
   * get the aspect ratio.<br />
   * if the aspect ratio has been overridden before - this value will be used. otherwise the calculated one will be used
   *
   * @return the aspect ratio
   */
  public float getAspectRatio() {
    // check whether the aspect ratio has been overridden
    if (aspectRatio > 0) {
      return getCommonAspectRatio(aspectRatio);
    }

    if (this.videoWidth == 0 || this.videoHeight == 0) {
      return 0f;
    }

    float ar = (float) this.videoWidth / (float) this.videoHeight;
    return getCommonAspectRatio(ar);
  }

  /**
   * get the "common" (nearest) aspect ratio
   *
   * @return the common aspect ratio
   */
  private float getCommonAspectRatio(Float ar) {
    float ret;

    // https://github.com/xbmc/xbmc/blob/master/xbmc/utils/StreamDetails.cpp#L538
    // Given that we're never going to be able to handle every single possibility in
    // aspect ratios, particularly when cropping prior to video encoding is taken into account
    // the best we can do is take the "common" aspect ratios, and return the closest one available.
    // The cutoffs are the geometric mean of the two aspect ratios either side.

    // the original list of kodi has been enhanced by some other common resolutions
    if (ar < 1.3499f) { // sqrt(1.33*1.37)
      ret = 1.33F;
    }
    else if (ar < 1.3997f) { // sqrt(1.37*1.43)
      ret = 1.37F;
    }
    else if (ar < 1.4935f) { // sqrt(1.43*1.56)
      ret = 1.43F;
    }
    else if (ar < 1.6092f) { // sqrt(1.56*1.66)
      ret = 1.56F;
    }
    else if (ar < 1.7190f) { // sqrt(1.66*1.78)
      ret = 1.66F;
    }
    else if (ar < 1.8147f) { // sqrt(1.78*1.85)
      ret = 1.78F;
    }
    else if (ar < 1.8748f) { // sqrt(1.85*1.90)
      ret = 1.85F;
    }
    else if (ar < 2.0445f) { // sqrt(1.90*2.20)
      ret = 1.90F;
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
   * LD (<=360 lines), SD (>360 and <720 lines), HD (>720 and <2160 lines) and UHD (>2160 lines).
   *
   * @return LD, SD, HD or UHD
   */
  public String getVideoDefinitionCategory() {
    return MediaFileHelper.getVideoDefinitionCategory(this);
  }

  /**
   * is that file a video in format LD?
   * 
   * @return true/false
   */
  public boolean isVideoDefinitionLD() {
    return MediaFileHelper.isVideoDefinitionLD(this);
  }

  /**
   * is that file a video in format SD?
   * 
   * @return true/false
   */
  public boolean isVideoDefinitionSD() {
    return MediaFileHelper.isVideoDefinitionSD(this);
  }

  /**
   * is that file a video in format HD?
   * 
   * @return true/false
   */
  public boolean isVideoDefinitionHD() {
    return MediaFileHelper.isVideoDefinitionHD(this);
  }

  /**
   * is that file a video in format UHD?
   *
   * @return true/false
   */
  public boolean isVideoDefinitionUHD() {
    return MediaFileHelper.isVideoDefinitionUHD(this);
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

  public double getFrameRate() {
    return frameRate;
  }

  public void setFrameRate(double frameRate) {
    double oldValue = this.frameRate;
    this.frameRate = frameRate;
    firePropertyChange("frameRate", oldValue, frameRate);
  }

  /**
   * returns the overall bit depth for this file.
   *
   * @return 8 / 10 bit
   */
  public int getBitDepth() {
    return bitDepth;
  }

  /**
   * sets the overall bit depth for this file (should be 8 or 10).
   *
   * @param newValue
   *          the new overall bit depth
   */
  public void setBitDepth(int newValue) {
    int oldValue = this.bitDepth;
    this.bitDepth = newValue;
    firePropertyChange("bitDepth", oldValue, newValue);
  }

  /**
   * Gets the bite depth as string
   *
   * @return 8 bit / 10 bit
   */
  public String getBitDepthString() {
    return this.bitDepth + " bit";
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
    return String.format("%dh %02dm", h, m);
  }

  /**
   * returns the duration / runtime formatted<br>
   * eg 1h 35m 12s.
   *
   * @return the duration
   */
  public String getDurationHMS() {
    if (this.durationInSecs == 0) {
      return "";
    }
    long h = TimeUnit.SECONDS.toHours(this.durationInSecs);
    long m = TimeUnit.SECONDS.toMinutes(this.durationInSecs - TimeUnit.HOURS.toSeconds(h));
    long s = TimeUnit.SECONDS.toSeconds(this.durationInSecs - TimeUnit.HOURS.toSeconds(h) - TimeUnit.MINUTES.toSeconds(m));

    return String.format("%dh %02dm %02ds", h, m, s);
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
    firePropertyChange("durationShort", oldValue, newValue);
    firePropertyChange("durationHM", oldValue, newValue);
    firePropertyChange("durationHHMMSS", oldValue, newValue);
  }

  public List<MediaFileAudioStream> getAudioStreams() {
    if (this.audioStreams == null) {
      return new ArrayList<>();
    }
    return audioStreams;
  }

  /**
   * Gets the audio stream marked as "default", or, if none, get the best with highest amount of channels
   * 
   * @return
   */
  public MediaFileAudioStream getDefaultOrBestAudioStream() {
    MediaFileAudioStream ret = null;

    for (MediaFileAudioStream as : ListUtils.nullSafe(audioStreams)) {
      if (as.isDefaultStream()) {
        // first default
        ret = as;
        break;
      }
    }

    if (ret == null) {
      // no default? take the "best"
      ret = getBestAudioStream();
    }
    return ret;
  }

  public void setAudioStreams(List<MediaFileAudioStream> audioStreams) {
    if (this.audioStreams == null && audioStreams.isEmpty()) {
      // nothing to do
      return;
    }

    if (this.audioStreams == null) {
      this.audioStreams = new CopyOnWriteArrayList<>();
    }
    else {
      this.audioStreams.clear();
    }

    this.audioStreams.addAll(audioStreams);
    firePropertyChange("audioStreams", null, audioStreams);
  }

  public String getCombinedCodecs() {
    StringBuilder sb = new StringBuilder(videoCodec);

    for (MediaFileAudioStream audioStream : ListUtils.nullSafe(audioStreams)) {
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

  /**
   * is this an animated graphic?<br>
   * (intended usage for ImageCache, to not scale this...)
   *
   * @return
   */
  public boolean isAnimatedGraphic() {
    return isAnimatedGraphic;
  }

  /**
   * sets the animation flag by hand<br>
   * use {@link #checkForAnimation()} to get from GIF file
   *
   * @param isAnimatedGraphic
   */
  public void setAnimatedGraphic(boolean isAnimatedGraphic) {
    this.isAnimatedGraphic = isAnimatedGraphic;
  }

  /**
   * is this file in HDR format? (checks only if the HdrFormat has been set)
   *
   * @return true/false
   */
  public boolean isHDR() {
    return StringUtils.isNotBlank(hdrFormat);
  }

  /**
   * set the HDR format
   * 
   * @param format
   *          the HDR format
   */
  public void setHdrFormat(String format) {
    this.hdrFormat = format;
  }

  /**
   * get the HDR format (HDR10, HDR10+, ...) what has been reported by libmediainfo
   * 
   * @return the HDR format or an empty String
   */
  public String getHdrFormat() {
    return this.hdrFormat;
  }

  /**
   * checks all graphic file for animation, and sets animated flag<br>
   * currently supported only .GIF<br>
   * Direct file access - should be only used in mediaInfo method!
   */
  public void checkForAnimation() {
    if (isGraphic() && getExtension().equalsIgnoreCase("gif")) {
      try {
        GifDecoder decoder = new GifDecoder();
        decoder.read(getFileAsPath().toString());
        if (decoder.getFrameCount() > 1) {
          setAnimatedGraphic(true);
        }
      }
      catch (Exception e) {
        LOGGER.warn("error checking GIF for animation: {}", e.getMessage());
      }
    }
  }

  /**
   * Gathers the media information via the native libmediainfo.<br>
   * If mediafile has already be scanned, it will be skipped.<br>
   * Use gatherMediaInformation(boolean force) to force the execution.
   */
  public void gatherMediaInformation() {
    gatherMediaInformation(false);
  }

  /**
   * Gathers the media information via the native libmediainfo.<br>
   *
   * @param force
   *          forces the execution, will not stop on already imported files
   */
  public void gatherMediaInformation(boolean force) {
    MediaFileHelper.gatherMediaInformation(this, force);
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
    if ((mf2 instanceof MediaFile)) {
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
