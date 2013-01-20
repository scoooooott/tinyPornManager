/*
 * Copyright 2012 Manuel Laggner
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

import static java.util.Arrays.asList;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.tinymediamanager.thirdparty.MediaInfo;
import org.tinymediamanager.thirdparty.MediaInfo.StreamKind;

/**
 * The Class MediaFile.
 * 
 * @author manuel
 */
@Entity
public class MediaFile extends AbstractModelObject {

  private static final Logger LOGGER         = Logger.getLogger(MediaFile.class);

  /** The Constant PATH. */
  private static final String PATH           = "path";

  /** The Constant FILENAME. */
  private static final String FILENAME       = "filename";

  /** The Constant FILESIZE. */
  private static final String FILESIZE       = "filesize";

  private static final String FILESIZE_IN_MB = "filesizeInMegabytes";

  /** The path. */
  private String              path;

  /** The filename. */
  private String              filename;

  /** The filesize. */
  private long                filesize;

  /** the mediainfo object */
  @Transient
  private MediaInfo           mediaInfo;

  public MediaFile() {
    this.path = "";
    this.filename = "";
  }

  /**
   * Instantiates a new media file.
   * 
   * @param path
   *          the path
   * @param filename
   *          the filename
   */
  public MediaFile(String path, String filename) {
    this.path = path;
    this.filename = filename;

    File file = new File(this.path, this.filename);
    if (file.exists()) {
      setFilesize(FileUtils.sizeOf(file));
    }
  }

  /**
   * Instantiates a new media file.
   * 
   * @param pathAndFilename
   *          the path and filename
   */
  public MediaFile(String pathAndFilename) {
    this.path = FilenameUtils.getPath(pathAndFilename);
    this.filename = FilenameUtils.getName(pathAndFilename);

    File file = new File(this.path, this.filename);
    if (file.exists()) {
      setFilesize(FileUtils.sizeOf(file));
    }
  }

  /**
   * gets the file handle
   * 
   * @return the file handle or NULL if file does not exits
   */
  public File getFile() {
    File f = new File(this.path, this.filename);
    if (f.exists()) {
      return f;
    }
    else {
      return null;
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
    firePropertyChange(PATH, oldValue, newValue);
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
    firePropertyChange(FILENAME, oldValue, newValue);
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

  public String getFilesizeInMegabytes() {
    DecimalFormat df = new DecimalFormat("#0.00");
    return df.format(filesize / (1024.0 * 1024.0)) + " M";
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a
   * <code>toString</code> for the specified object.
   * </p>
   * 
   * @param object
   *          the Object to be output
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  /**
   * instantiates and gets new mediainfo object
   * 
   * @return MediaInfo object
   */
  private synchronized MediaInfo getMediaInfo() {
    if (mediaInfo == null) {
      mediaInfo = new MediaInfo();
      if (!mediaInfo.open(this.getFile())) {
        LOGGER.error("Mediainfo could not open file: " + this.getFilename());
      }
    }
    return mediaInfo;
  }

  /**
   * Gets the real mediainfo values
   * 
   * @param streamKind
   *          MediaInfo.StreamKind.(General|Video|Audio|Text|Chapters|Image|Menu
   *          )
   * @param streamNumber
   *          the stream number (0 for first)
   * @param keys
   *          the information you want to fetch
   * @return the media information you asked<br>
   *         <b>OR AN EMPTY STRING IF MEDIAINFO COULD NOT BE LOADED</b>
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

  public static boolean isEmptyValue(Object object) {
    return object == null || object.toString().length() == 0;
  }

  /**
   * Joins an iterateable object with specified delimeter
   * 
   * @param values
   *          the iterable object
   * @param delimiter
   *          the delimeter
   * @return the joined string
   */
  public static String join(Iterable<?> values, CharSequence delimiter) {
    StringBuilder sb = new StringBuilder();

    for (Iterator<?> iterator = values.iterator(); iterator.hasNext();) {
      Object value = iterator.next();
      if (!isEmptyValue(value)) {
        if (sb.length() > 0) {
          sb.append(delimiter);
        }
        sb.append(value);
      }
    }
    return sb.toString();
  }

  /**
   * gets the first token of video codec<br>
   * (e.g. DivX 5 => DivX)
   * 
   * @return the video codec
   */
  public String getVideoCodec() {
    // e.g. XviD, x264, DivX 5, MPEG-4 Visual, AVC, etc.
    String codec = getMediaInfo(StreamKind.Video, 0, "Encoded_Library/Name", "CodecID/Hint", "Format");
    // get first token (e.g. DivX 5 => DivX)
    return StringUtils.isEmpty(codec) ? "" : new Scanner(codec).next();
  }

  /**
   * gets the audio codec<br>
   * (w/o punctuation; eg AC-3 => AC3)
   * 
   * @return the audio codec
   */
  public String getAudioCodec() {
    // e.g. AC-3, DTS, AAC, Vorbis, MP3, etc.
    String codec = getMediaInfo(StreamKind.Audio, 0, "CodecID/Hint", "Format");
    // remove punctuation (e.g. AC-3 => AC3)
    return codec.replaceAll("\\p{Punct}", "");
  }

  /**
   * returns the container format extensions (e.g. avi, mkv mka mks, OGG, etc.)
   * 
   * @return the container format
   */
  public String getContainerFormat() {
    String extensions = getMediaInfo(StreamKind.General, 0, "Codec/Extensions", "Format");
    // get first extension
    return StringUtils.isEmpty(extensions) ? "" : new Scanner(extensions).next().toLowerCase();
  }

  /**
   * gets the common video format<br>
   * 
   * @return 1080p 720p 480p... or null if too small
   */
  public String getVideoFormat() {
    String v = getMediaInfo(StreamKind.Video, 0, "Height");
    if (v.isEmpty()) {
      return "";
    }
    int height = Integer.parseInt(v);

    int ns = 0;
    int[] hs = new int[] { 1080, 720, 576, 540, 480, 360, 240, 120 };
    for (int i = 0; i < hs.length - 1; i++) {
      if (height > hs[i + 1]) {
        ns = hs[i];
        break;
      }
    }

    if (ns > 0) {
      // e.g. 720p, nobody actually wants files to be tagged as interlaced, e.g.
      // 720i
      return String.format("%dp", ns);
    }

    return null; // video too small
  }

  /**
   * gets the exact video format (height + scantype p/i)
   * 
   * @return
   */
  public String getExactVideoFormat() {
    String height = getMediaInfo(StreamKind.Video, 0, "Height");
    String scanType = getMediaInfo(StreamKind.Video, 0, "ScanType");

    if (height == null || height.isEmpty() || scanType == null || scanType.isEmpty()) {
      return "";
    }
    return height + Character.toLowerCase(scanType.charAt(0));
  }

  /**
   * returns the amount of audio channels
   * 
   * @return the amount of audio channels (eg. 6ch)
   */
  public String getAudioChannels() {
    String channels = getMediaInfo(StreamKind.Audio, 0, "Channel(s)");
    if (channels.isEmpty()) {
      return "";
    }
    return channels + "ch";
  }

  /**
   * returns the exact video resolution
   * 
   * @return eg 1280x720
   */
  public String getVideoResolution() {
    List<Integer> dim = getDimension();
    if (dim.contains(null)) {
      return "";
    }
    return join(dim, "x");
  }

  /**
   * if width-to-height aspect ratio greater than 1.37:1
   * 
   * @return true/false if widescreen
   */
  public Boolean isWidescreen() {
    List<Integer> dim = getDimension();
    if (dim.contains(null)) {
      return false;
    }
    return (float) dim.get(0) / dim.get(1) > 1.37f ? true : false;
  }

  /**
   * returns the aspect ratio
   * 
   * @return the aspect ratio
   */
  public Float getAspectRatio() {
    List<Integer> dim = getDimension();
    if (dim.contains(null)) {
      return 0F;
    }
    return (float) (Math.round(dim.get(0).floatValue() / dim.get(1).floatValue() * 100) / 100.0);
  }

  /**
   * SD (less than 720 lines) or HD (more than 720 lines)
   * 
   * @return SD or HD
   */
  public String getVideoDefinitionCategory() {
    List<Integer> dim = getDimension();
    if (dim.contains(null)) {
      return "";
    }
    return dim.get(0) >= 1280 || dim.get(1) >= 720 ? "HD" : "SD";
  }

  /**
   * gets the dimension of video stream
   * 
   * @return
   */
  public List<Integer> getDimension() {
    String width = getMediaInfo(StreamKind.Video, 0, "Width");
    String height = getMediaInfo(StreamKind.Video, 0, "Height");

    return asList(!width.isEmpty() ? Integer.parseInt(width) : null, !height.isEmpty() ? Integer.parseInt(height) : null);
  }

}
