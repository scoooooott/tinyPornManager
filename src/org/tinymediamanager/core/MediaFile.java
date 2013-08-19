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
package org.tinymediamanager.core;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Transient;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.thirdparty.MediaInfo;
import org.tinymediamanager.thirdparty.MediaInfo.StreamKind;

/**
 * The Class MediaFile.
 * 
 * @author Manuel Laggner
 */
@Embeddable
public class MediaFile extends AbstractModelObject implements Comparable<MediaFile> {

  private static final Logger        LOGGER           = LoggerFactory.getLogger(MediaFile.class);

  private static final String        PATH             = "path";
  private static final String        FILENAME         = "filename";
  private static final String        FILESIZE         = "filesize";
  private static final String        FILESIZE_IN_MB   = "filesizeInMegabytes";

  private static Pattern             posterPattern    = Pattern.compile("(?i)(.*-poster|poster|folder|movie|.*-cover|cover)\\..{2,4}");
  private static Pattern             fanartPattern    = Pattern.compile("(?i)(.*-fanart|.*\\.fanart|fanart)[0-9]{0,2}\\..{2,4}");
  private static Pattern             bannerPattern    = Pattern.compile("(?i)(.*-banner|banner)\\..{2,4}");
  private static Pattern             thumbPattern     = Pattern.compile("(?i)(.*-thumb|thumb)[0-9]{0,2}\\..{2,4}");

  private String                     path             = "";
  private String                     filename         = "";
  private long                       filesize         = 0;
  private String                     videoCodec       = "";
  private String                     containerFormat  = "";
  private String                     videoFormat      = "";
  private String                     exactVideoFormat = "";
  private int                        videoWidth       = 0;
  private int                        videoHeight      = 0;
  private int                        overallBitRate   = 0;
  private int                        durationInSecs   = 0;
  private int                        stacking         = 0;

  @Enumerated(EnumType.STRING)
  private MediaFileType              type             = MediaFileType.UNKNOWN;

  private List<MediaFileAudioStream> audioStreams     = new ArrayList<MediaFileAudioStream>();
  private List<MediaFileSubtitle>    subtitles        = new ArrayList<MediaFileSubtitle>();

  @Transient
  private MediaInfo                  mediaInfo;
  @Transient
  private File                       file             = null;

  /**
   * "clones" a new media file.
   */
  public MediaFile(MediaFile clone) {
    this.path = clone.path;
    this.filename = clone.filename;
    this.filesize = clone.filesize;
    this.videoCodec = clone.videoCodec;
    this.containerFormat = clone.containerFormat;
    this.videoFormat = clone.videoFormat;
    this.exactVideoFormat = clone.exactVideoFormat;
    this.videoHeight = clone.videoHeight;
    this.videoWidth = clone.videoWidth;
    this.overallBitRate = clone.overallBitRate;
    this.durationInSecs = clone.durationInSecs;
    this.stacking = clone.stacking;
    this.type = clone.type;
    this.audioStreams = clone.audioStreams;
    this.subtitles = clone.subtitles;
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
   *          the f
   */
  public MediaFile(File f) {
    this.path = f.getParent(); // just path w/o filename
    this.filename = f.getName();
    this.file = f;
    this.type = parseType();
    this.stacking = Utils.getStackingNumber(f.getName());
    if (this.stacking == 0) {
      // try to parse from parent directory
      this.stacking = Utils.getStackingNumber(FilenameUtils.getBaseName(getPath()));
    }
    if (file.exists()) {
      setFilesize(FileUtils.sizeOf(file));
    }
  }

  /**
   * Instantiates a new media file.
   * 
   * @param f
   *          the f
   * @param type
   *          the type
   */
  public MediaFile(File f, MediaFileType type) {
    this.path = f.getParent(); // just path w/o filename
    this.filename = f.getName();
    this.file = f;
    this.type = type;
    this.stacking = Utils.getStackingNumber(f.getName());
    if (this.stacking == 0) {
      // try to parse from parent directory
      this.stacking = Utils.getStackingNumber(FilenameUtils.getBaseName(getPath()));
    }
    if (file.exists()) {
      setFilesize(FileUtils.sizeOf(file));
    }
  }

  /**
   * tries to get the MediaFileType out of filename.
   * 
   * @return the MediaFileType
   */
  public MediaFileType parseType() {
    String ext = getExtension().toLowerCase();
    String name = getFilename().toLowerCase();
    String foldername = FilenameUtils.getBaseName(getPath()).toLowerCase();

    if ((name.contains("sample") || name.contains("trailer") || foldername.contains("sample")
        && Globals.settings.getVideoFileType().contains("." + ext))) {
      return MediaFileType.TRAILER;
    }

    if (ext.equals("nfo")) {
      return MediaFileType.NFO;
    }

    if (ext.equals("jpg") || ext.equals("png") || ext.equals("tbn")) {
      return parseImageType();
    }

    if (Globals.settings.getAudioFileType().contains("." + ext)) {
      return MediaFileType.AUDIO;
    }

    if (Globals.settings.getSubtitleFileType().contains("." + ext)) {
      return MediaFileType.SUBTITLE;
    }

    if (Globals.settings.getVideoFileType().contains("." + ext)) {
      return MediaFileType.VIDEO;
    }

    // if (name.contains("subs") || name.contains("subtitle") || foldername.contains("subs")) {
    // return MediaFileType.SUBTITLE;
    // }

    return MediaFileType.UNKNOWN;
  }

  /**
   * Parses the image type.
   * 
   * @return the media file type
   */
  private MediaFileType parseImageType() {
    String name = getFilename();

    // *-poster.* or poster.* or folder.* or movie.*
    Matcher matcher = posterPattern.matcher(name);
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
      return MediaFileType.THUMB;
    }

    return MediaFileType.GRAPHIC;
  }

  /**
   * is this a "packed" file? (zip, rar, whatsoever).
   * 
   * @return true/false
   */
  public boolean isPacked() {
    String ext = getExtension().toLowerCase();
    return (ext.equals("zip") || ext.equals("rar") || ext.equals("7z") || ext.matches("r\\d+"));
  }

  /**
   * Is this a graphic file?.
   * 
   * @return true/false
   */
  public boolean isGraphic() {
    return (type.equals(MediaFileType.GRAPHIC) || type.equals(MediaFileType.BANNER) || type.equals(MediaFileType.FANART)
        || type.equals(MediaFileType.POSTER) || type.equals(MediaFileType.THUMB) || type.equals(MediaFileType.EXTRAFANART));
  }

  /**
   * gets the file handle.
   * 
   * @return the file handle or NULL if file does not exits
   */
  public File getFile() {
    if (file == null) {
      File f = new File(this.path, this.filename);
      // if (f.exists()) {
      file = f;
      // }
      // else {
      // return null;
      // }
    }
    return file;
  }

  /**
   * Sets the file.
   * 
   * @param file
   *          the new file
   */
  public void setFile(File file) {
    setFilename(file.getName());
    setPath(file.getParent());
    this.file = file;
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
   * calculates relative path from old and exchanges new path (should be on same level)
   * 
   * @param oldPath
   *          the old path
   * @param newPath
   *          the new path
   */
  public void fixPathForRenamedFolder(File oldPath, File newPath) {
    String rel = Utils.relPath(oldPath, this.path); // relative from old
    String newPathS = newPath.getPath();
    if (!rel.isEmpty()) {
      newPathS += File.separator + rel;
    }
    setPath(newPathS);
  }

  /**
   * Gets the filename.
   * 
   * @return the filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Sets the filename.
   * 
   * @param newValue
   *          the new filename
   */
  public void setFilename(String newValue) {
    String oldValue = this.filename;
    this.filename = newValue;
    invalidateFileHandle();
    firePropertyChange(FILENAME, oldValue, newValue);
  }

  /**
   * Gets the extension.
   * 
   * @return the extension
   */
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

  /**
   * Gets the filesize.
   * 
   * @return the filesize
   */
  public long getFilesize() {
    return filesize;
  }

  /**
   * Sets the filesize.
   * 
   * @param newValue
   *          the new filesize
   */
  public void setFilesize(long newValue) {
    long oldValue = this.filesize;
    this.filesize = newValue;
    firePropertyChange(FILESIZE, oldValue, newValue);
    firePropertyChange(FILESIZE_IN_MB, oldValue, newValue);
  }

  /**
   * Gets the filesize in megabytes.
   * 
   * @return the filesize in megabytes
   */
  public String getFilesizeInMegabytes() {
    DecimalFormat df = new DecimalFormat("#0.00");
    return df.format(filesize / (1024.0 * 1024.0)) + " M";
  }

  /**
   * gets the MediaFile type.
   * 
   * @return the type
   */
  public MediaFileType getType() {
    return type;
  }

  /**
   * sets the MediaFile type.
   * 
   * @param type
   *          the new type
   */
  public void setType(MediaFileType type) {
    this.type = type;
  }

  /**
   * gets the stacking information.
   * 
   * @return the stacking
   */
  public int getStacking() {
    return stacking;
  }

  /**
   * gets the stacking information.
   * 
   * @param stacking
   *          the new stacking
   */
  public void setStacking(int stacking) {
    this.stacking = stacking;
  }

  /**
   * get parsed subtitles.
   * 
   * @return the subtitles
   */
  public List<MediaFileSubtitle> getSubtitles() {
    return subtitles;
  }

  public String getSubtitlesAsString() {
    StringBuilder sb = new StringBuilder();
    Set<MediaFileSubtitle> cleansub = new LinkedHashSet<MediaFileSubtitle>(subtitles);

    for (MediaFileSubtitle sub : cleansub) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(sub.getLanguage());
    }

    return sb.toString();
  }

  /**
   * sets the subtitle object
   * 
   * @param subtitles
   */
  public void setSubtitles(List<MediaFileSubtitle> subtitles) {
    this.subtitles = subtitles;
  }

  /**
   * sets ONE subtitle
   * 
   * @param subtitle
   */
  public void addSubtitle(MediaFileSubtitle subtitle) {
    this.subtitles.add(subtitle);
  }

  /**
   * clears all subtitles
   */
  public void clearAllSubtitles() {
    this.subtitles.clear();
  }

  /**
   * dies his mediafile has subtitles.
   * 
   * @return true, if successful
   */
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
   * 
   * @return MediaInfo object
   */
  private synchronized MediaInfo getMediaInfo() {
    if (mediaInfo == null) {
      mediaInfo = new MediaInfo();
      if (!mediaInfo.open(this.getFile())) {
        LOGGER.error("Mediainfo could not open file: " + this.getPath() + File.separator + this.getFilename());
      }
    }
    return mediaInfo;
  }

  /**
   * Closes the connection to the mediainfo lib.
   */
  private synchronized void closeMediaInfo() {
    if (mediaInfo != null) {
      mediaInfo.close();
      mediaInfo = null;
    }
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
    for (String key : keys) {
      String value = getMediaInfo().get(streamKind, streamNumber, key);
      if (value.length() > 0) {
        return value;
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
    if (audioStreams.size() > 0) {
      return audioStreams.get(0).getCodec();
    }

    return "";
  }

  // /**
  // * Sets the audio codec.
  // *
  // * @param newValue
  // * the new audio codec
  // */
  // public void setAudioCodec(String newValue) {
  // String oldValue = this.audioCodec;
  // this.audioCodec = newValue;
  // firePropertyChange("audioCodec", oldValue, newValue);
  // }

  /**
   * returns the container format extensions (e.g. avi, mkv mka mks, OGG, etc.)
   * 
   * @return the container format
   */
  public String getContainerFormat() {
    return this.containerFormat;
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
   * gets the common video format<br>
   * .
   * 
   * @return 1080p 720p 480p... or null if too small
   */
  public String getVideoFormat() {
    return this.videoFormat;
  }

  /**
   * Sets the video format.
   * 
   * @param newValue
   *          the new video format
   */
  public void setVideoFormat(String newValue) {
    String oldValue = this.videoFormat;
    this.videoFormat = newValue;
    firePropertyChange("videoFormat", oldValue, newValue);
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
   * Sets the exact video format.
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
   * returns the amount of audio channels.
   * 
   * @return the amount of audio channels (eg. 6ch)
   */
  public String getAudioChannels() {
    if (audioStreams.size() > 0) {
      return audioStreams.get(0).getChannels();
    }

    return "";
  }

  // /**
  // * Sets the audio channels.
  // *
  // * @param newValue
  // * the new audio channels
  // */
  // public void setAudioChannels(String newValue) {
  // String oldValue = this.audioChannels;
  // this.audioChannels = newValue;
  // firePropertyChange("audioChannels", oldValue, newValue);
  // }

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

  /**
   * Gets the video width.
   * 
   * @return the video width
   */
  public int getVideoWidth() {
    return videoWidth;
  }

  /**
   * Gets the video height.
   * 
   * @return the video height
   */
  public int getVideoHeight() {
    return videoHeight;
  }

  /**
   * Sets the video width.
   * 
   * @param newValue
   *          the new video width
   */
  public void setVideoWidth(int newValue) {
    int oldValue = this.videoWidth;
    this.videoWidth = newValue;
    firePropertyChange("videoWidth", oldValue, newValue);
  }

  /**
   * Sets the video height.
   * 
   * @param newValue
   *          the new video height
   */
  public void setVideoHeight(int newValue) {
    int oldValue = this.videoHeight;
    this.videoHeight = newValue;
    firePropertyChange("videoHeight", oldValue, newValue);
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

  /**
   * returns the aspect ratio.
   * 
   * @return the aspect ratio
   */
  public Float getAspectRatio() {
    if (this.videoWidth == 0 || this.videoHeight == 0) {
      return 0F;
    }
    return (float) (Math.round((float) this.videoWidth / (float) this.videoHeight * 100) / 100.0);
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

    int minutes = (int) (this.durationInSecs / 60) % 60;
    int hours = (int) (this.durationInSecs / (60 * 60)) % 24;
    return hours + "h " + String.format("%02d", minutes) + "m";
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

    int seconds = (int) this.durationInSecs % 60;
    int minutes = (int) (this.durationInSecs / 60) % 60;
    int hours = (int) (this.durationInSecs / (60 * 60)) % 24;
    return String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
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
      return;
    }

    // mediainfo already gathered
    if (!force && !getContainerFormat().isEmpty()) {
      return;
    }

    LOGGER.debug("start MediaInfo for " + this.filename);

    String height = "";
    String scanType = "";
    String width = "";
    String videoCodec = "";

    switch (type) {
      case VIDEO:
      case TRAILER:
        height = getMediaInfo(StreamKind.Video, 0, "Height");
        scanType = getMediaInfo(StreamKind.Video, 0, "ScanType");
        width = getMediaInfo(StreamKind.Video, 0, "Width");
        videoCodec = getMediaInfo(StreamKind.Video, 0, "Encoded_Library/Name", "CodecID/Hint", "Format");

        // get audio streams
        int streams = getMediaInfo().streamCount(StreamKind.Audio);
        audioStreams.clear();
        for (int i = 0; i < streams; i++) {
          MediaFileAudioStream stream = new MediaFileAudioStream();
          String audioCodec = getMediaInfo(StreamKind.Audio, i, "CodecID/Hint", "Format");
          stream.setCodec(audioCodec.replaceAll("\\p{Punct}", ""));
          String channels = getMediaInfo(StreamKind.Audio, i, "Channel(s)");
          stream.setChannels(StringUtils.isEmpty(channels) ? "" : channels + "ch");
          try {
            String br = getMediaInfo(StreamKind.Audio, i, "BitRate");
            stream.setBitrate(Integer.valueOf(br) / 1024);
          }
          catch (Exception e) {
          }
          String language = getMediaInfo(StreamKind.Audio, i, "Language");
          if (language.isEmpty()) {
            // try to parse from filename
            String shortname = getBasename().toLowerCase();
            Set<String> langArray = Utils.KEY_TO_LOCALE_MAP.keySet();
            for (String l : langArray) {
              if (shortname.equalsIgnoreCase(l) || shortname.matches("(?i).*[_ .-]+" + l + "$")) {// ends with lang + delimiter prefix
                String lang = Utils.getDisplayLanguage(l);
                LOGGER.debug("found language '" + l + "' in audiofile; displaying it as '" + lang + "'");
                stream.setLanguage(lang);
                break;
              }
            }
          }
          else {
            // map locale
            String l = Utils.getDisplayLanguage(language);
            if (l.isEmpty()) {
              // could not map locale, use detected
              stream.setLanguage(language);
            }
            else {
              // set our localized name
              stream.setLanguage(l);
            }
          }
          audioStreams.add(stream);
        }

        // get subtitle streams
        streams = getMediaInfo().streamCount(StreamKind.Text);
        subtitles.clear();
        for (int i = 0; i < streams; i++) {
          MediaFileSubtitle stream = new MediaFileSubtitle();

          String codec = getMediaInfo(StreamKind.Text, i, "CodecID/Hint", "Format");
          stream.setCodec(codec.replaceAll("\\p{Punct}", ""));
          stream.setLanguage(getMediaInfo(StreamKind.Text, i, "Language/String"));

          String forced = getMediaInfo(StreamKind.Text, i, "Forced");
          boolean b = forced.equalsIgnoreCase("true") || forced.equalsIgnoreCase("yes");
          stream.setForced(b);

          subtitles.add(stream);
        }
        break;

      case SUBTITLE:
        if (subtitles == null || subtitles.size() == 0) {
          MediaFileSubtitle sub = new MediaFileSubtitle();
          String shortname = getBasename().toLowerCase();
          if (shortname.contains("forced")) {
            sub.setForced(true);
            shortname = shortname.replaceAll("\\p{Punct}*forced", "");
          }
          Set<String> langArray = Utils.KEY_TO_LOCALE_MAP.keySet();
          for (String l : langArray) {
            if (shortname.equalsIgnoreCase(l) || shortname.matches("(?i).*[_ .-]+" + l + "$")) {// ends with lang + delimiter prefix
              String lang = Utils.getDisplayLanguage(l);
              LOGGER.debug("found language '" + l + "' in subtitle; displaying it as '" + lang + "'");
              sub.setLanguage(lang);
              break;
            }
          }
          sub.setCodec(getExtension());
          subtitles.add(sub);
        }
        setContainerFormat(getExtension());
        break;

      case AUDIO:
        MediaFileAudioStream stream = new MediaFileAudioStream();
        String audioCodec = getMediaInfo(StreamKind.Audio, 0, "CodecID/Hint", "Format");
        stream.setCodec(audioCodec.replaceAll("\\p{Punct}", ""));
        String channels = getMediaInfo(StreamKind.Audio, 0, "Channel(s)");
        stream.setChannels(StringUtils.isEmpty(channels) ? "" : channels + "ch");
        try {
          String br = getMediaInfo(StreamKind.Audio, 0, "BitRate");
          stream.setBitrate(Integer.valueOf(br) / 1024);
        }
        catch (Exception e) {
        }
        String language = getMediaInfo(StreamKind.Audio, 0, "Language");
        if (language.isEmpty()) {
          // try to parse from filename
          String shortname = getBasename().toLowerCase();
          Set<String> langArray = Utils.KEY_TO_LOCALE_MAP.keySet();
          for (String l : langArray) {
            if (shortname.equalsIgnoreCase(l) || shortname.matches("(?i).*[_ .-]+" + l + "$")) {// ends with lang + delimiter prefix
              String lang = Utils.getDisplayLanguage(l);
              LOGGER.debug("found language '" + l + "' in audiofile; displaying it as '" + lang + "'");
              stream.setLanguage(lang);
              break;
            }
          }
        }
        else {
          // map locale
          String l = Utils.getDisplayLanguage(language);
          if (l.isEmpty()) {
            // could not map locale, use detected
            stream.setLanguage(language);
          }
          else {
            // set our localized name
            stream.setLanguage(l);
          }
        }
        audioStreams.add(stream);
        break;

      case POSTER:
      case BANNER:
      case FANART:
      case THUMB:
      case EXTRAFANART:
      case GRAPHIC:
        height = getMediaInfo(StreamKind.Image, 0, "Height");
        scanType = getMediaInfo(StreamKind.Image, 0, "ScanType");
        width = getMediaInfo(StreamKind.Image, 0, "Width");
        videoCodec = getMediaInfo(StreamKind.Image, 0, "Encoded_Library/Name", "CodecID/Hint", "Format");
        break;

      default:
        break;
    }

    // video codec
    // e.g. XviD, x264, DivX 5, MPEG-4 Visual, AVC, etc.
    // get first token (e.g. DivX 5 => DivX)
    setVideoCodec(StringUtils.isEmpty(videoCodec) ? "" : new Scanner(videoCodec).next());

    // container format
    String extensions = getMediaInfo(StreamKind.General, 0, "Codec/Extensions", "Format");
    // get first extension
    setContainerFormat(StringUtils.isEmpty(extensions) ? "" : new Scanner(extensions).next().toLowerCase());

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

    // video format
    String format = "";
    int w = getVideoWidth();
    int h = getVideoHeight();

    if (w >= 1920 || h >= 1080) {
      format = "1080p";
    }
    if (format.isEmpty() && (w >= 1280 || h >= 720)) {
      format = "720p";
    }
    // SD with aspect ratio
    if (format.isEmpty() && w > 0) {
      if (isWidescreen()) {
        format = "SD 16:9";
      }
      else {
        format = "SD 4:3";
      }
    }
    setVideoFormat(format);

    // overall bitrate (OverallBitRate/String)
    String br = getMediaInfo(StreamKind.General, 0, "OverallBitRate");
    if (!br.isEmpty()) {
      try {
        setOverallBitRate(Integer.valueOf(br) / 1024); // in kbps
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
    String dur = getMediaInfo(StreamKind.General, 0, "Duration");
    if (!dur.isEmpty()) {
      try {
        setDuration(Integer.valueOf(dur) / 1000);
      }
      catch (NumberFormatException e) {
        setDuration(0);
      }
    }
    /*
     * String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(millis), TimeUnit.MILLISECONDS.toSeconds(millis) -
     * TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
     */

    // close mediainfo lib
    closeMediaInfo();
  }

  /**
   * Save to db.
   */
  public synchronized void saveToDb() {
    // update DB
    Globals.entityManager.getTransaction().begin();
    Globals.entityManager.persist(this);
    Globals.entityManager.getTransaction().commit();
  }

  /**
   * Checks if is valid mediainfo format.
   * 
   * @return true, if is valid mediainfo format
   */
  private boolean isValidMediainfoFormat() {
    String extension = FilenameUtils.getExtension(filename).toLowerCase();

    // check unsupported extensions
    if ("iso".equals(extension) || "bin".equals(extension) || "dat".equals(extension) || "iso".equals(extension) || "img".equals(extension)
        || "nrg".equals(extension)) {
      return false;
    }

    // parse audio, video and graphic files
    if (type.equals(MediaFileType.VIDEO) || type.equals(MediaFileType.TRAILER) || type.equals(MediaFileType.SUBTITLE)
        || type.equals(MediaFileType.AUDIO) || isGraphic()) {
      return true;
    }

    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object mf2) {
    if ((mf2 != null) && (mf2 instanceof MediaFile)) {
      return compareTo((MediaFile) mf2) == 0;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(MediaFile mf2) {
    return this.getFile().compareTo(mf2.getFile());
  }

  @Override
  public int hashCode() {
    return this.getFile().hashCode();
  }
}
