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

package org.tinymediamanager.core;

import static org.tinymediamanager.scraper.util.LanguageUtils.parseLanguageFromString;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.MetadataUtil;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.thirdparty.MediaInfo;
import org.tinymediamanager.thirdparty.MediaInfoFile;
import org.tinymediamanager.thirdparty.MediaInfoUtils;
import org.tinymediamanager.thirdparty.MediaInfoXMLParser;

import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileEntry;
import com.github.stephenc.javaisotools.loopfs.iso9660.Iso9660FileSystem;
import com.github.stephenc.javaisotools.loopfs.udf.UDFFileEntry;
import com.github.stephenc.javaisotools.loopfs.udf.UDFFileSystem;

/**
 * the class {@link MediaFileHelper} is used to extract all unneeded logic/variables from the media file (for lower memory consumption)
 *
 * @author Manuel Laggner
 */
public class MediaFileHelper {
  private static final Logger      LOGGER             = LoggerFactory.getLogger(MediaFileHelper.class);

  public static final List<String> PLEX_EXTRA_FOLDERS = Arrays.asList("behind the scenes", "behindthescenes", "deleted scenes", "deletedscenes",
      "featurettes", "interviews", "scenes", "shorts", "other");

  public static final List<String> SUPPORTED_ARTWORK_FILETYPES;
  public static final List<String> DEFAULT_VIDEO_FILETYPES;
  public static final List<String> DEFAULT_AUDIO_FILETYPES;
  public static final List<String> DEFAULT_SUBTITLE_FILETYPES;

  public static final Pattern      MOVIESET_ARTWORK_PATTERN;
  public static final Pattern      POSTER_PATTERN;
  public static final Pattern      FANART_PATTERN;
  public static final Pattern      BANNER_PATTERN;
  public static final Pattern      THUMB_PATTERN;
  public static final Pattern      SEASON_POSTER_PATTERN;
  public static final Pattern      SEASON_BANNER_PATTERN;
  public static final Pattern      SEASON_THUMB_PATTERN;
  public static final Pattern      LOGO_PATTERN;
  public static final Pattern      CLEARLOGO_PATTERN;
  public static final Pattern      CHARACTERART_PATTERN;
  public static final Pattern      DISCART_PATTERN;
  public static final Pattern      CLEARART_PATTERN;
  public static final Pattern      KEYART_PATTERN;

  public static final String       VIDEO_FORMAT_96P   = "96p";
  public static final String       VIDEO_FORMAT_120P  = "120p";
  public static final String       VIDEO_FORMAT_144P  = "144p";
  public static final String       VIDEO_FORMAT_240P  = "240p";
  public static final String       VIDEO_FORMAT_288P  = "288p";
  public static final String       VIDEO_FORMAT_360P  = "360p";
  public static final String       VIDEO_FORMAT_480P  = "480p";
  public static final String       VIDEO_FORMAT_540P  = "540p";
  public static final String       VIDEO_FORMAT_576P  = "576p";
  public static final String       VIDEO_FORMAT_720P  = "720p";
  public static final String       VIDEO_FORMAT_1080P = "1080p";
  public static final String       VIDEO_FORMAT_1440P = "1440p";
  public static final String       VIDEO_FORMAT_2160P = "2160p";
  public static final String       VIDEO_FORMAT_4320P = "4320p";

  public static final List<String> VIDEO_FORMATS      = Arrays.asList(VIDEO_FORMAT_480P, VIDEO_FORMAT_540P, VIDEO_FORMAT_576P, VIDEO_FORMAT_720P,
      VIDEO_FORMAT_1080P, VIDEO_FORMAT_2160P, VIDEO_FORMAT_4320P);

  // meta formats
  public static final String       VIDEO_FORMAT_LD    = "LD";
  public static final String       VIDEO_FORMAT_SD    = "SD";
  public static final String       VIDEO_FORMAT_HD    = "HD";
  public static final String       VIDEO_FORMAT_UHD   = "UHD";

  // 3D / side-by-side / top-and-bottom / H=half - MVC=Multiview Video Coding-http://wiki.xbmc.org/index.php?title=3D#Video_filenames_flags
  public static final String       VIDEO_3D           = "3D";
  public static final String       VIDEO_3D_SBS       = "3D SBS";
  public static final String       VIDEO_3D_TAB       = "3D TAB";
  public static final String       VIDEO_3D_HSBS      = "3D HSBS";
  public static final String       VIDEO_3D_HTAB      = "3D HTAB";
  public static final String       VIDEO_3D_MVC       = "3D MVC";

  static {
    SUPPORTED_ARTWORK_FILETYPES = Collections.unmodifiableList(Arrays.asList("jpg", "jpeg,", "png", "tbn", "gif", "bmp"));

    // .disc = video stubs
    // .evo = hd-dvd
    // .ifo = DVD; only needed for KodiRPC
    DEFAULT_VIDEO_FILETYPES = Collections.unmodifiableList(Arrays.asList(".3gp", ".asf", ".asx", ".avc", ".avi", ".bdmv", ".bin", ".bivx", ".braw",
        ".dat", ".divx", ".dv", ".dvr-ms", ".disc", ".evo", ".fli", ".flv", ".h264", ".ifo", ".img", ".iso", ".mts", ".mt2s", ".m2ts", ".m2v", ".m4v",
        ".mkv", ".mk3d", ".mov", ".mp4", ".mpeg", ".mpg", ".nrg", ".nsv", ".nuv", ".ogm", ".pva", ".qt", ".rm", ".rmvb", ".strm", ".svq3", ".ts",
        ".ty", ".viv", ".vob", ".vp3", ".wmv", ".webm", ".xvid"));

    DEFAULT_AUDIO_FILETYPES = Collections.unmodifiableList(Arrays.asList(".a52", ".aa3", ".aac", ".ac3", ".adt", ".adts", ".aif", ".aiff", ".alac",
        ".ape", ".at3", ".atrac", ".au", ".dts", ".flac", ".m4a", ".m4b", ".m4p", ".mid", ".midi", ".mka", ".mp3", ".mpa", ".mlp", ".oga", ".ogg",
        ".pcm", ".ra", ".ram", ".rm", ".tta", ".thd", ".wav", ".wave", ".wma"));

    DEFAULT_SUBTITLE_FILETYPES = Collections.unmodifiableList(Arrays.asList(".aqt", ".cvd", ".dks", ".jss", ".sub", ".sup", ".ttxt", ".mpl", ".pjs",
        ".psb", ".rt", ".srt", ".smi", ".ssf", ".ssa", ".svcd", ".usf", ".ass", ".pgs", ".vobsub"));

    String extensions = String.join("|", SUPPORTED_ARTWORK_FILETYPES);

    MOVIESET_ARTWORK_PATTERN = Pattern
        .compile("(?i)movieset-(poster|fanart|banner|disc|discart|logo|clearlogo|clearart|thumb)\\.(" + extensions + ")$");
    POSTER_PATTERN = Pattern.compile("(?i)(.*-poster|poster|folder|movie|.*-cover|cover)\\.(" + extensions + ")$");
    FANART_PATTERN = Pattern.compile("(?i)(.*-fanart|.*\\.fanart|fanart)[0-9]{0,2}\\.(" + extensions + ")$");
    BANNER_PATTERN = Pattern.compile("(?i)(.*-banner|banner)\\.(" + extensions + ")$");
    THUMB_PATTERN = Pattern.compile("(?i)(.*-thumb|thumb|.*-landscape|landscape)[0-9]{0,2}\\.(" + extensions + ")$");
    SEASON_POSTER_PATTERN = Pattern.compile("(?i)season([0-9]{1,4}|-specials|-all)(-poster)?\\.(" + extensions + ")$");
    SEASON_BANNER_PATTERN = Pattern.compile("(?i)season([0-9]{1,4}|-specials|-all)-banner\\.(" + extensions + ")$");
    SEASON_THUMB_PATTERN = Pattern.compile("(?i)season([0-9]{1,4}|-specials|-all)-(thumb|landscape)\\.(" + extensions + ")$");
    LOGO_PATTERN = Pattern.compile("(?i)(.*-logo|logo)\\.(" + extensions + ")$");
    CLEARLOGO_PATTERN = Pattern.compile("(?i)(.*-clearlogo|clearlogo)\\.(" + extensions + ")$");
    CHARACTERART_PATTERN = Pattern.compile("(?i)(.*-characterart|characterart)[0-9]{0,2}\\.(" + extensions + ")$");
    DISCART_PATTERN = Pattern.compile("(?i)(.*-discart|discart|.*-disc|disc)\\.(" + extensions + ")$");
    CLEARART_PATTERN = Pattern.compile("(?i)(.*-clearart|clearart)\\.(" + extensions + ")$");
    KEYART_PATTERN = Pattern.compile("(?i)(.*-keyart|keyart)\\.(" + extensions + ")$");
  }

  private MediaFileHelper() {
    // private constructor for utility classes
  }

  /**
   * get a list of all available video formats
   *
   * @return a list of all available video formats
   */
  public static List<String> getVideoFormats() {
    List<String> videoFormats = new ArrayList<>();

    Field[] declaredFields = MediaFileHelper.class.getDeclaredFields();
    for (Field field : declaredFields) {
      if (Modifier.isStatic(field.getModifiers()) && field.getName().startsWith("VIDEO_FORMAT_") && !field.isAnnotationPresent(Deprecated.class)) {
        try {
          videoFormats.add((String) field.get(null));
        }
        catch (Exception ignored) {
          // no need to log here
        }
      }
    }
    return videoFormats;
  }

  /**
   * Parses the media file type out of the given path
   * 
   * @param pathToFile
   *          the path/file to parse
   * @return the detected media file type or MediaFileType.UNKNOWN
   */
  public static MediaFileType parseMediaFileType(Path pathToFile) {
    String filename = pathToFile.getFileName().toString();

    String ext = FilenameUtils.getExtension(filename).toLowerCase(Locale.ROOT);
    String basename = FilenameUtils.getBaseName(filename);
    // just path w/o filename
    String foldername = FilenameUtils.getBaseName(pathToFile.getParent() == null ? "" : pathToFile.getParent().toString().toLowerCase(Locale.ROOT));

    String pparent = "";
    String ppparent = "";
    String pppparent = "";
    try {
      pparent = FilenameUtils.getBaseName(pathToFile.getParent().getParent().toString()).toLowerCase(Locale.ROOT);
      ppparent = FilenameUtils.getBaseName(pathToFile.getParent().getParent().getParent().toString()).toLowerCase(Locale.ROOT);
      pppparent = FilenameUtils.getBaseName(pathToFile.getParent().getParent().getParent().getParent().toString()).toLowerCase(Locale.ROOT);
    }
    catch (Exception ignored) {
      // could happen if we are no 2 levels deep
    }

    // check EXTRAS first
    if (filename.contains(".EXTRAS.") // scene file naming (need to check first! upper case!)
        || basename.matches("(?i).*[_.-]+extra[s]?$") // end with "-extra[s]"
        || basename.matches("(?i).*[-]+extra[s]?[-].*") // extra[s] just with surrounding dash (other delims problem)
        || foldername.equalsIgnoreCase("extras") // preferred folder name
        || foldername.equalsIgnoreCase("extra") // preferred folder name
        || (!pparent.isEmpty() && pparent.matches("extra[s]?")) // extras folder a level deeper
        || (!ppparent.isEmpty() && ppparent.matches("extra[s]?")) // extras folder a level deeper
        || (!pppparent.isEmpty() && pppparent.matches("extra[s]?")) // extras folder a level deeper
        || basename.matches("(?i).*[-](behindthescenes|deleted|featurette|interview|scene|short|other)$") // Plex (w/o trailer)
        || MediaFileHelper.PLEX_EXTRA_FOLDERS.contains(foldername) // Plex Extra folders
        || MediaFileHelper.PLEX_EXTRA_FOLDERS.contains(pparent) // Plex Extra folders
        || MediaFileHelper.PLEX_EXTRA_FOLDERS.contains(ppparent) // Plex Extra folders
        || MediaFileHelper.PLEX_EXTRA_FOLDERS.contains(pppparent)) // Plex Extra folders
    {
      return MediaFileType.EXTRA;
    }

    if (ext.equals("nfo")) {
      return MediaFileType.NFO;
    }

    if (ext.equals("vsmeta")) {
      return MediaFileType.VSMETA;
    }

    if (basename.endsWith("-mediainfo") && "xml".equals(ext)) {
      return MediaFileType.MEDIAINFO;
    }

    if (SUPPORTED_ARTWORK_FILETYPES.contains(ext)) {
      return parseImageType(pathToFile);
    }

    if (basename.matches("(?i).*[_.-]+theme\\d*$") || basename.matches("(?i)theme\\d*")) {
      return MediaFileType.THEME;
    }

    if (Globals.settings.getAudioFileType().contains("." + ext)) {
      return MediaFileType.AUDIO;
    }

    if (Globals.settings.getSubtitleFileType().contains("." + ext)) {
      return MediaFileType.SUBTITLE;
    }

    if (Globals.settings.getVideoFileType().contains("." + ext)) {
      // is this maybe a trailer?
      if (basename.matches("(?i).*[\\[\\]\\(\\)_.-]+trailer[\\[\\]\\(\\)_.-]?$") || basename.equalsIgnoreCase("movie-trailer")
          || foldername.equalsIgnoreCase("trailer") || foldername.equalsIgnoreCase("trailers")) {
        return MediaFileType.TRAILER;
      }

      // we have some false positives too - make a more precise check
      if (basename.matches("(?i).*[\\[\\]\\(\\)_.-]+sample[\\[\\]\\(\\)_.-]?$") || basename.equalsIgnoreCase("sample")
          || foldername.equalsIgnoreCase("sample")) { // sample folder name
        return MediaFileType.SAMPLE;
      }

      // ok, it's the main video
      return MediaFileType.VIDEO;
    }

    if (ext.equals("txt")) {
      return MediaFileType.TEXT;
    }

    return MediaFileType.UNKNOWN;
  }

  /**
   * Parses the image type out of the given path
   *
   * @param pathToFile
   *          the path/file to parse
   * @return the detected media file type or MediaFileType.UNKNOWN
   */
  public static MediaFileType parseImageType(Path pathToFile) {
    String filename = pathToFile.getFileName().toString();
    String foldername = pathToFile.getParent() == null ? "" : pathToFile.getParent().toString().toLowerCase(Locale.ROOT); // just path w/o filename

    // movieset artwork
    Matcher matcher = MediaFileHelper.MOVIESET_ARTWORK_PATTERN.matcher(filename);
    if (matcher.matches()) {
      return MediaFileType.GRAPHIC;
    }

    // season(XX|-specials)-poster.*
    // seasonXX.*
    matcher = MediaFileHelper.SEASON_POSTER_PATTERN.matcher(filename);
    if (matcher.matches()) {
      return MediaFileType.SEASON_POSTER;
    }

    // season(XX|-specials)-banner.*
    matcher = MediaFileHelper.SEASON_BANNER_PATTERN.matcher(filename);
    if (matcher.matches()) {
      return MediaFileType.SEASON_BANNER;
    }

    // season(XX|-specials)-thumb.*
    matcher = MediaFileHelper.SEASON_THUMB_PATTERN.matcher(filename);
    if (matcher.matches()) {
      return MediaFileType.SEASON_THUMB;
    }

    // *-poster.* or poster.* or folder.* or movie.*
    matcher = MediaFileHelper.POSTER_PATTERN.matcher(filename);
    if (matcher.matches()) {
      return MediaFileType.POSTER;
    }

    // *-fanart.* or fanart.* or *-fanartXX.* or fanartXX.*
    matcher = MediaFileHelper.FANART_PATTERN.matcher(filename);
    if (matcher.matches()) {
      // decide between fanart and extrafanart
      if (foldername.endsWith("extrafanart")) {
        return MediaFileType.EXTRAFANART;
      }
      return MediaFileType.FANART;
    }

    // *-banner.* or banner.*
    matcher = MediaFileHelper.BANNER_PATTERN.matcher(filename);
    if (matcher.matches()) {
      return MediaFileType.BANNER;
    }

    // *-thumb.* or thumb.* or *-thumbXX.* or thumbXX.*
    matcher = MediaFileHelper.THUMB_PATTERN.matcher(filename);
    if (matcher.matches()) {
      // decide between thumb and extrathumb
      if (foldername.endsWith("extrathumbs")) {
        return MediaFileType.EXTRATHUMB;
      }
      return MediaFileType.THUMB;
    }

    // clearart.*
    matcher = MediaFileHelper.CLEARART_PATTERN.matcher(filename);
    if (matcher.matches()) {
      return MediaFileType.CLEARART;
    }

    // logo.*
    matcher = MediaFileHelper.LOGO_PATTERN.matcher(filename);
    if (matcher.matches()) {
      return MediaFileType.LOGO;
    }

    // clearlogo.*
    matcher = MediaFileHelper.CLEARLOGO_PATTERN.matcher(filename);
    if (matcher.matches()) {
      return MediaFileType.CLEARLOGO;
    }

    // discart.* / disc.*
    matcher = MediaFileHelper.DISCART_PATTERN.matcher(filename);
    if (matcher.matches()) {
      return MediaFileType.DISC;
    }

    // characterart.*
    matcher = MediaFileHelper.CHARACTERART_PATTERN.matcher(filename);
    if (matcher.matches()) {
      return MediaFileType.CHARACTERART;
    }
    if (foldername.endsWith("characterart")) {
      // own characterart folder (as seen in some skins/plugins)
      return MediaFileType.CHARACTERART;
    }

    // keyart.*
    matcher = MediaFileHelper.KEYART_PATTERN.matcher(filename);
    if (matcher.matches()) {
      return MediaFileType.KEYART;
    }

    // folder style as last chance
    if (foldername.equalsIgnoreCase("extrafanarts") || foldername.equalsIgnoreCase("extrafanart")) {
      return MediaFileType.EXTRAFANART;
    }
    if (foldername.equalsIgnoreCase("extrathumbs") || foldername.equalsIgnoreCase("extrathumb")) {
      return MediaFileType.EXTRATHUMB;
    }

    return MediaFileType.GRAPHIC;
  }

  /**
   * gets the "common" video format for the given {@link MediaFile}
   * 
   * @param mediaFile
   *          the media file to parse
   * @return 1080p 720p 480p... or SD if too small
   */
  public static String getVideoFormat(MediaFile mediaFile) {
    int w = mediaFile.getVideoWidth();
    int h = mediaFile.getVideoHeight();

    // use XBMC implementation https://github.com/xbmc/xbmc/blob/master/xbmc/utils/StreamDetails.cpp#L559
    if (w == 0 || h == 0) {
      return "";
    }
    // https://en.wikipedia.org/wiki/Low-definition_television
    else if (w <= blur(128) && h <= blur(96)) { // MMS-Small 96p 128×96 4:3
      return VIDEO_FORMAT_96P;
    }
    else if (w <= blur(160) && h <= blur(120)) { // QQVGA 120p 160×120 4:3
      return VIDEO_FORMAT_120P;
    }
    else if (w <= blur(176) && h <= blur(144)) { // QCIF Webcam 144p 176×144 11:9
      return VIDEO_FORMAT_144P;
    }
    else if (w <= blur(256) && h <= blur(144)) { // YouTube 144p 144p 256×144 16:9
      return VIDEO_FORMAT_144P;
    }
    else if (w <= blur(320) && h <= blur(240)) { // NTSC square pixel 240p 320×240 4:3
      return VIDEO_FORMAT_240P;
    }
    else if (w <= blur(352) && h <= blur(240)) { // SIF (525) 240p 352×240 4:3
      return VIDEO_FORMAT_240P;
    }
    else if (w <= blur(426) && h <= blur(240)) { // NTSC widescreen 240p 426×240 16:9
      return VIDEO_FORMAT_240P;
    }
    else if (w <= blur(480) && h <= blur(272)) { // PSP 288p 480×272 30:17
      return VIDEO_FORMAT_288P;
    }
    else if (w <= blur(480) && h <= blur(360)) { // 360p 360p 480×360 4:3
      return VIDEO_FORMAT_360P;
    }
    else if (w <= blur(640) && h <= blur(360)) { // Wide 360p 360p 640×360 16:9
      return VIDEO_FORMAT_360P;
    }
    // https://en.wikipedia.org/wiki/480p
    else if (w <= blur(640) && h <= blur(480)) { // 480p 640×480 4:3
      return VIDEO_FORMAT_480P;
    }
    else if (w <= blur(720) && h <= blur(480)) { // Rec. 601 720×480 3:2
      return VIDEO_FORMAT_480P;
    }
    else if (w <= blur(800) && h <= blur(480)) { // Rec. 601 plus a quarter 800×480 5:3
      return VIDEO_FORMAT_480P;
    }
    else if (w <= blur(853) && h <= blur(480)) { // Wide 480p 853.33×480 16:9 (unscaled)
      return VIDEO_FORMAT_480P;
    }
    else if (w <= blur(776) && h <= blur(592)) {
      // 720x576 (PAL) (handbrake sometimes encode it to a max of 776 x 592)
      return VIDEO_FORMAT_576P;
    }
    else if (w <= blur(960) && h <= blur(544)) {
      // 960x540 (sometimes 544 which is multiple of 16)
      return VIDEO_FORMAT_540P;
    }
    else if (w <= blur(1280) && h <= blur(720)) { // 720p Widescreen 16:9
      return VIDEO_FORMAT_720P;
    }
    else if (w <= blur(960) && h <= blur(720)) { // 720p Widescreen 4:3
      return VIDEO_FORMAT_720P;
    }
    else if (w <= blur(1080) && h <= blur(720)) { // 720p Rec. 601 3:2
      return VIDEO_FORMAT_720P;
    }
    else if (w <= blur(1920) && h <= blur(1080)) { // 1080p HD Widescreen 16:9
      return VIDEO_FORMAT_1080P;
    }
    else if (w <= blur(1440) && h <= blur(1080)) { // 1080p SD 4:3
      return VIDEO_FORMAT_1080P;
    }
    else if (w <= blur(1620) && h <= blur(1080)) { // 1080p Rec. 601 3:2
      return VIDEO_FORMAT_1080P;
    }
    else if (w <= blur(1920) && h <= blur(1440)) { // 1440p HD Widescreen 4:3
      return VIDEO_FORMAT_1440P;
    }
    else if (w <= blur(2160) && h <= blur(1440)) { // 1440p Rec. 601 3:2
      return VIDEO_FORMAT_1440P;
    }
    else if (w <= blur(2560) && h <= blur(1440)) { // 1440p HD Widescreen 16:9
      return VIDEO_FORMAT_1440P;
    }
    else if (w <= blur(3840) && h <= blur(2160)) { // 4K Ultra-high-definition television
      return VIDEO_FORMAT_2160P;
    }
    else if (w <= blur(3840) && h <= blur(1600)) { // 4K Ultra-wide-television
      return VIDEO_FORMAT_2160P;
    }
    else if (w <= blur(4096) && h <= blur(2160)) { // DCI 4K (native resolution)
      return VIDEO_FORMAT_2160P;
    }
    else if (w <= blur(4096) && h <= blur(1716)) { // DCI 4K (CinemaScope cropped)
      return VIDEO_FORMAT_2160P;
    }
    else if (w <= blur(3996) && h <= blur(2160)) { // DCI 4K (flat cropped)
      return VIDEO_FORMAT_2160P;
    }

    return VIDEO_FORMAT_4320P;
  }

  /**
   * get the video definition category for the given {@link MediaFile}<br/>
   * LD (<=360 lines), SD (>360 and <720 lines) or HD (720+ lines).
   * 
   * @param mediaFile
   *          the media file
   * @return LD, SD or HD
   */
  public static String getVideoDefinitionCategory(MediaFile mediaFile) {
    if (!mediaFile.isVideo()) {
      return "";
    }

    if (mediaFile.getVideoWidth() == 0 || mediaFile.getVideoHeight() == 0) {
      return "";
    }

    if (mediaFile.getVideoWidth() <= 640 && mediaFile.getVideoHeight() <= 360) { // 360p and below os LD
      return VIDEO_FORMAT_LD;
    }

    else if (mediaFile.getVideoWidth() < 1280 && mediaFile.getVideoHeight() < 720) { // below 720p is SD
      return VIDEO_FORMAT_SD;
    }

    else if (mediaFile.getVideoWidth() <= 1920 && mediaFile.getVideoHeight() <= 1080) { // below 1080p is HD
      return VIDEO_FORMAT_HD;
    }

    return VIDEO_FORMAT_UHD; // anything above 1080p is considered UHD
  }

  /**
   * is that the given {@link MediaFile} a video in format LD?
   * 
   * @param mediaFile
   *          the media file
   * @return true/false
   */
  public static boolean isVideoDefinitionLD(MediaFile mediaFile) {
    if (!mediaFile.isVideo()) {
      return false;
    }

    return VIDEO_FORMAT_LD.equals(getVideoDefinitionCategory(mediaFile));
  }

  /**
   * is that the given {@link MediaFile} a video in format SD?
   * 
   * @param mediaFile
   *          the media file
   * @return true/false
   */
  public static boolean isVideoDefinitionSD(MediaFile mediaFile) {
    if (!mediaFile.isVideo()) {
      return false;
    }

    return VIDEO_FORMAT_SD.equals(getVideoDefinitionCategory(mediaFile));
  }

  /**
   * is that the given {@link MediaFile} a video in format HD?
   * 
   * @param mediaFile
   *          the media file
   * @return true/false
   */
  public static boolean isVideoDefinitionHD(MediaFile mediaFile) {
    if (!mediaFile.isVideo()) {
      return false;
    }

    return VIDEO_FORMAT_HD.equals(getVideoDefinitionCategory(mediaFile));
  }

  /**
   * is that the given {@link MediaFile} a video in format UHD?
   *
   * @param mediaFile
   *          the media file
   * @return true/false
   */
  public static boolean isVideoDefinitionUHD(MediaFile mediaFile) {
    if (!mediaFile.isVideo()) {
      return false;
    }

    return VIDEO_FORMAT_UHD.equals(getVideoDefinitionCategory(mediaFile));
  }

  /**
   * add 1% to the given value
   * 
   * @param value
   *          the value to blur
   * @return the blurred value
   */
  private static int blur(int value) {
    return value + (value / 100);
  }

  /**
   * gather basic file information like file size, creation date and last modified date
   * 
   * @param mediaFile
   *          the {@link MediaFile} to gather the information for
   */
  public static void gatherFileInformation(MediaFile mediaFile) {
    // get basic infos; file size, creation date and last modified
    try {
      BasicFileAttributes view = Files.readAttributes(mediaFile.getFileAsPath(), BasicFileAttributes.class);
      // sanity check; need something litil bit bigger than 0
      if (view.creationTime().toMillis() > 100000) {
        Date creDat = new Date(view.creationTime().toMillis());
        mediaFile.setDateCreated(creDat);
      }
      if (view.lastModifiedTime().toMillis() > 100000) {
        Date modDat = new Date(view.lastModifiedTime().toMillis());
        mediaFile.setDateLastModified(modDat);
      }

      mediaFile.setFiledate(view.lastModifiedTime().toMillis());
      mediaFile.setFilesize(view.size());
    }
    catch (Exception e) {
      LOGGER.warn("could not get file information (size/date): {}", e.getMessage());
    }
  }

  /**
   * Gathers the media information for the given {@link MediaFile} via libmediainfo
   *
   * @param mediaFile
   *          the media file
   * @param force
   *          forces the execution, will not stop on already imported files
   */
  public static void gatherMediaInformation(MediaFile mediaFile, boolean force) {
    // get basic infos; file size, creation date and last modified
    gatherFileInformation(mediaFile);

    // check for supported filetype
    if (!mediaFile.isValidMediainfoFormat()) {
      // okay, we have no valid MI file, be sure it will not be triggered any more
      if (StringUtils.isBlank(mediaFile.getContainerFormat())) {
        mediaFile.setContainerFormat(mediaFile.getExtension());
      }
      return;
    }

    // mediainfo already gathered
    if (!force && !mediaFile.getContainerFormat().isEmpty()) {
      return;
    }

    // gather subtitle infos independent of MI
    if (mediaFile.getType() == MediaFileType.SUBTITLE) {
      gatherSubtitleInformation(mediaFile);
    }

    // do not work further on 0 byte files
    if (mediaFile.getFilesize() == 0) {
      LOGGER.debug("0 Byte file detected: {}", mediaFile.getFilename());
      // set container format to do not trigger it again
      mediaFile.setContainerFormat(mediaFile.getExtension());
      return;
    }

    // do not work further on subtitles/NFO files
    if (mediaFile.getType() == MediaFileType.SUBTITLE || mediaFile.getType() == MediaFileType.NFO) {
      // set container format to do not trigger it again
      mediaFile.setContainerFormat(mediaFile.getExtension());
      return;
    }

    // get media info
    LOGGER.debug("start MediaInfo for {}", mediaFile.getFileAsPath());
    if (mediaFile.isISO()) {
      getMediaInfoSnapshotFromISO(mediaFile);
    }
    else {
      parseMediainfoSnapshot(mediaFile, getMediaInfoSnapshot(mediaFile));
    }
  }

  /**
   * if you have an MI snapshot prepared, parse it
   * 
   * @param mediaFile
   * @param miSnapshot
   */
  public static void parseMediainfoSnapshot(MediaFile mediaFile, Map<MediaInfo.StreamKind, List<Map<String, String>>> miSnapshot) {
    if (miSnapshot == null) {
      // MI could not be opened
      LOGGER.error("error getting MediaInfo for {}", mediaFile.getFilename());
      // set container format to do not trigger it again
      mediaFile.setContainerFormat(mediaFile.getExtension());
      return;
    }
    LOGGER.trace("got MI");

    switch (mediaFile.getType()) {
      case VIDEO:
      case EXTRA:
      case SAMPLE:
      case TRAILER:
        // *****************
        // get video stream
        // *****************
        gatherVideoInformation(mediaFile, miSnapshot);

        // *****************
        // get audio streams
        // *****************
        gatherAudioInformation(mediaFile, miSnapshot);

        // ********************
        // get subtitle streams
        // ********************
        gatherSubtitleInformation(mediaFile, miSnapshot);

        break;

      case AUDIO:
        gatherAudioInformation(mediaFile, miSnapshot);
        break;

      case POSTER:
      case BANNER:
      case FANART:
      case THUMB:
      case EXTRAFANART:
      case GRAPHIC:
      case SEASON_POSTER:
      case SEASON_BANNER:
      case SEASON_THUMB:
      case LOGO:
      case CLEARLOGO:
      case CLEARART:
      case CHARACTERART:
      case DISC:
      case EXTRATHUMB:
      case KEYART:
        gatherImageInformation(mediaFile, miSnapshot);
        break;

      case NFO: // do nothing here, but do not display default warning (since we got the filedate)
        break;

      default:
        LOGGER.warn("no mediainformation handling for MediaFile type {} yet.", mediaFile.getType());
        break;
    }

    // container format for all except subtitles (subtitle container format is handled another way)
    if (mediaFile.getType() == MediaFileType.SUBTITLE) {
      mediaFile.setContainerFormat(mediaFile.getExtension());
    }
    else {
      String extensions = getMediaInfo(miSnapshot, MediaInfo.StreamKind.General, 0, "Codec/Extensions", "Format");
      // get first extension
      mediaFile.setContainerFormat(getFirstEntryViaScanner(extensions).toLowerCase(Locale.ROOT));

      // if container format is still empty -> insert the extension
      if (StringUtils.isBlank(mediaFile.getContainerFormat())) {
        mediaFile.setContainerFormat(mediaFile.getExtension());
      }
    }

    switch (mediaFile.getType()) {
      case VIDEO:
      case EXTRA:
      case SAMPLE:
      case TRAILER:
      case AUDIO:
        // overall bitrate (OverallBitRate/String)
        String br = getMediaInfo(miSnapshot, MediaInfo.StreamKind.General, 0, "OverallBitRate");
        if (!br.isEmpty()) {
          try {
            mediaFile.setOverallBitRate(Integer.parseInt(br) / 1000); // in kbps
          }
          catch (NumberFormatException e) {
            mediaFile.setOverallBitRate(0);
          }
        }

        // get embedded title from general info
        String miTitle = getMediaInfo(miSnapshot, MediaInfo.StreamKind.General, 0, "Title");
        if (!miTitle.isEmpty()) {
          mediaFile.setTitle(miTitle);
        }

        // try getting some real file dates from MI
        // try {
        // @formatter:off
          //    Released_Date             : The date/year that the item was released.
          //    Original/Released_Date    : The date/year that the item was originaly released.
          //    Recorded_Date             : The time/date/year that the recording began.
          //    Encoded_Date              : The time/date/year that the encoding of this item was completed began.
          //    Tagged_Date               : The time/date/year that the tags were done for this item.
          //    Written_Date              : The time/date/year that the composition of the music/script began.
          //    Mastered_Date             : The time/date/year that the item was tranfered to a digitalmedium.
          //    File_Created_Date         : The time that the file was created on the file system
          //    File_Modified_Date        : The time that the file was modified on the file system
          // @formatter:on
        // String embeddedDate = getMediaInfo(StreamKind.General, 0, "Released_Date", "Original/Released_Date", "Recorded_Date", "Encoded_Date",
        // "Mastered_Date");
        // Date d = StrgUtils.parseDate(embeddedDate);
        // if (d.toInstant().toEpochMilli() < filedate) {
        // // so this is older than our file date - use it :)
        // filedate = d.toInstant().toEpochMilli();
        // }
        // }
        // catch (ParseException e) {
        // // could not parse MI date... ignore
        // }

        // Duration;Play time of the stream in ms
        // Duration/String;Play time in format : XXx YYy only, YYy omited if zero
        // Duration/String1;Play time in format : HHh MMmn SSs MMMms, XX om.if.z.
        // Duration/String2;Play time in format : XXx YYy only, YYy omited if zero
        // Duration/String3;Play time in format : HH:MM:SS.MMM

        // ISO files have duration injected with snapshot
        String dur = getMediaInfo(miSnapshot, MediaInfo.StreamKind.General, 0, "Duration");
        if (!dur.isEmpty()) {
          try {
            double ddur = Double.parseDouble(dur);
            if (ddur > 10000) {
              mediaFile.setDuration((int) ddur / 1000);
            }
            else {
              mediaFile.setDuration((int) ddur);
            }
          }
          catch (NumberFormatException e) {
            mediaFile.setDuration(0);
          }
        }

        long discFilesSizes = 0;
        try {
          discFilesSizes = Long.parseLong(getMediaInfo(miSnapshot, MediaInfo.StreamKind.General, 0, "DiscFileSize"));
        }
        catch (Exception e) {
          LOGGER.trace("could not parse injected disc file size: {}", e.getMessage());
        }
        if (discFilesSizes > 0 && mediaFile.getFilesize() > 0) {
          // do some sanity check, to see, if we have an invalid DVD structure
          // eg when the sum(filesize) way higher than ISO size
          long diff = Math.abs(mediaFile.getFilesize() - discFilesSizes);
          double ratio = diff * 100.0 / mediaFile.getFilesize();
          LOGGER.debug("ISO size: {};  reportedDataSize: {};  = diff: {} ~ {}%", mediaFile.getFilesize(), discFilesSizes, diff, (int) ratio);
          if (ratio > 10) {
            LOGGER.error("ISO file seems to have an invalid structure - ignore duration");
            // we set the ISO duration to zero so the standard getDuration() will always get the scraped duration
            mediaFile.setDuration(0);
          }
        }

        break;

      default:
        break;
    }

    LOGGER.trace("extracted MI");
  }

  /**
   * get the libmediainfo snapshot of all data for the given {@link MediaFile}
   *
   * @param mediaFile
   *          the media file
   * @return a map with all libmediainfo data
   */
  private static synchronized Map<MediaInfo.StreamKind, List<Map<String, String>>> getMediaInfoSnapshot(MediaFile mediaFile) {
    Map<MediaInfo.StreamKind, List<Map<String, String>>> miSnapshot = null;

    // check if we have a snapshot xml
    String xmlFilename = FilenameUtils.getBaseName(mediaFile.getFilename()) + "-mediainfo.xml";
    Path xmlFile = Paths.get(mediaFile.getPath(), xmlFilename);
    if (Files.exists(xmlFile)) {
      try {
        LOGGER.info("Try to parse {}", xmlFile);
        MediaInfoXMLParser xml = new MediaInfoXMLParser(xmlFile);
        List<MediaInfoFile> miFiles = xml.parseXML();
        // XML has now ALL the files, as mediainfo would have read it.

        MediaInfoFile mif = miFiles.get(0);
        if (mif != null) {
          // since we operate on file basis, there should be only one filled...
          miSnapshot = mif.getSnapshot();
          if (!miSnapshot.isEmpty()) {
            return miSnapshot;
          }
        }
      }
      catch (Exception e) {
        LOGGER.warn("Unable to parse " + xmlFile, e);
      }
    }

    if (!MediaInfoUtils.USE_LIBMEDIAINFO) {
      return new HashMap<>();
    }

    // open mediaInfo directly on file
    try (MediaInfo mediaInfo = new MediaInfo()) {
      if (!mediaInfo.open(mediaFile.getFileAsPath())) {
        LOGGER.error("Mediainfo could not open file: {}", mediaFile.getFileAsPath());
      }
      else {
        miSnapshot = mediaInfo.snapshot();
      }
    }
    // sometimes also an error is thrown
    catch (Exception | Error e) {
      LOGGER.error("Mediainfo could not open file: {} - {}", mediaFile.getFileAsPath(), e.getMessage());
    }

    return miSnapshot;
  }

  /**
   * get the libmediainfo snapshot of all data for the given {@link MediaFile} is it is an ISO file
   *
   * @param mediaFile
   *          the media file
   * @return a map with all libmediainfo data
   */
  private static synchronized void getMediaInfoSnapshotFromISO(MediaFile mediaFile) {
    List<MediaInfoFile> miFiles = new ArrayList<>();

    // check if we have a snapshot xml, and load all DVD files from XML
    String xmlFilename = FilenameUtils.getBaseName(mediaFile.getFilename()) + "-mediainfo.xml";
    Path xmlFile = Paths.get(mediaFile.getPath(), xmlFilename);
    if (Files.exists(xmlFile)) {
      try {
        LOGGER.info("ISO: try to parse {}", xmlFile);
        MediaInfoXMLParser xml = new MediaInfoXMLParser(xmlFile);
        miFiles = xml.parseXML();
      }
      catch (Exception e) {
        LOGGER.warn("ISO: Unable to parse " + xmlFile, e);
      }
    }

    if (!MediaInfoUtils.USE_LIBMEDIAINFO) {
      return;
    }

    // No? try parse ISO as DVD directly...
    if (miFiles.isEmpty()) {
      int bufferSize = 64 * 1024;
      try (Iso9660FileSystem image = new Iso9660FileSystem(mediaFile.getFileAsPath().toFile(), true)) {
        LOGGER.trace("ISO: Open");

        for (Iso9660FileEntry entry : image) {
          if (entry.getSize() <= 3000) { // small files and folder entries
            continue;
          }
          LOGGER.trace("ISO: got entry {}, size : {}", entry.getName(), entry.getSize());

          MediaFile mf = new MediaFile(Paths.get(mediaFile.getFileAsPath().toString(), entry.getPath())); // set ISO as MF path
          if (mf.isDiscFile()) { // count all known DVD files!

            try (MediaInfo fileMI = new MediaInfo()) {
              byte[] fromBuffer = new byte[bufferSize];
              int fromBufferSize; // The size of the read file buffer
              long fileSize = entry.getSize();

              // Preparing to fill MediaInfo with a buffer
              fileMI.openBufferInit(fileSize, 0);

              long pos = 0L;
              // The parsing loop
              do {
                // limit read to maxBuffer, or to end of file size (cannot determine file end in stream!!)
                long toread = pos + bufferSize > fileSize ? fileSize - pos : bufferSize;

                // Reading data somewhere, do what you want for this.
                fromBufferSize = image.readBytes(entry, pos, fromBuffer, 0, (int) toread);
                if (fromBufferSize > 0) {
                  pos += fromBufferSize; // add bytes read to file position

                  // Sending the buffer to MediaInfo
                  int result = fileMI.openBufferContinue(fromBuffer, fromBufferSize);
                  if ((result & 8) == 8) { // Status.Finalized
                    break;
                  }

                  // Testing if MediaInfo request to go elsewhere
                  if (fileMI.openBufferContinueGoToGet() != -1) {
                    pos = fileMI.openBufferContinueGoToGet();
                    LOGGER.trace("ISO: Seek to {}", pos);
                    fileMI.openBufferInit(fileSize, pos); // Informing MediaInfo we have seek
                  }
                }
              } while (fromBufferSize > 0);

              // Finalizing
              LOGGER.trace("ISO: finalize entry");
              fileMI.openBufferFinalize(); // This is the end of the stream, MediaInfo must finish some work

              MediaInfoFile mif = new MediaInfoFile(entry.getName(), entry.getSize());
              mif.setSnapshot(fileMI.snapshot());
              miFiles.add(mif);
            }
            // sometimes also an error is thrown
            catch (Exception | Error e) {
              LOGGER.debug("Mediainfo could not open file STREAM for file {}", entry.getName(), e);
            }
          } // end VIDEO
        } // end entry
      }
      catch (Exception e) {
        LOGGER.info("Mediainfo could not open as ISO9660 - {}", e.getMessage());
      }
    }

    // STILL No? try parse ISO as UDF directly, taking the biggest file (for now)...
    if (miFiles.isEmpty()) {
      UDFFileEntry biggest = null;
      UDFFileSystem image = null;

      try {
        image = new UDFFileSystem(mediaFile.getFileAsPath().toFile(), true);
        int bufferSize = 64 * 1024;
        for (UDFFileEntry entry : image) {
          if (biggest == null || (entry.getSize() > biggest.getSize() && entry.getName().toLowerCase(Locale.ROOT).endsWith("m2ts"))) {
            biggest = entry;
          }
        }

        LOGGER.trace("ISO: got entry {}, size : {}", biggest.getPath(), biggest.getSize());

        MediaFile mf = new MediaFile(Paths.get(mediaFile.getFileAsPath().toString(), biggest.getPath())); // set ISO as MF path
        if (mf.isDiscFile()) { // count all known DVD files!

          try (MediaInfo fileMI = new MediaInfo()) {
            byte[] fromBuffer = new byte[bufferSize];
            int fromBufferSize; // The size of the read file buffer
            long fileSize = biggest.getSize();

            // Preparing to fill MediaInfo with a buffer
            fileMI.openBufferInit(fileSize, 0);

            long pos = 0L;
            // The parsing loop
            do {
              // limit read to maxBuffer, or to end of file size (cannot determine file end in stream!!)
              long toread = pos + bufferSize > fileSize ? fileSize - pos : bufferSize;

              // Reading data somewhere, do what you want for this.
              fromBufferSize = image.readFileContent(biggest, pos, fromBuffer, 0, (int) toread);
              if (fromBufferSize > 0) {
                pos += fromBufferSize; // add bytes read to file position

                // Sending the buffer to MediaInfo
                int result = fileMI.openBufferContinue(fromBuffer, fromBufferSize);
                if ((result & 8) == 8) { // Status.Finalized
                  break;
                }

                // Testing if MediaInfo request to go elsewhere
                if (fileMI.openBufferContinueGoToGet() != -1) {
                  pos = fileMI.openBufferContinueGoToGet();
                  LOGGER.trace("ISO: Seek to {}", pos);
                  fileMI.openBufferInit(fileSize, pos); // Informing MediaInfo we have seek
                }
              }
            } while (fromBufferSize > 0);

            // Finalizing
            LOGGER.trace("ISO: finalize entry");
            fileMI.openBufferFinalize(); // This is the end of the stream, MediaInfo must finish some work
            parseMediainfoSnapshot(mediaFile, fileMI.snapshot());
            return;
          }
          // sometimes also an error is thrown
          catch (Exception | Error e) {
            LOGGER.debug("Mediainfo could not open file UDF for file {}", biggest.getPath(), e);
          }
        } // end VIDEO
      }
      catch (Exception e) {
        LOGGER.info("Mediainfo could not open as UDF - {}", e.getMessage());
      }
      finally {
        if (image != null) {
          try {
            image.close();
          }
          catch (IOException e) {
            LOGGER.warn("Could not close image", e);
          }
        }
      }
    }

    // now we have an array with complete MI of all DVD files
    // lets do our workflow:

    // find the IFO/BDMV with longest duration
    int dur = 0;
    MediaInfoFile ifo = null;
    MediaInfoFile bd = null;
    for (MediaInfoFile mif : miFiles) {
      String ext = mif.getFilename().toUpperCase();
      // DVD
      if (ext.endsWith("IFO")) {
        if (mif.getDuration() > dur) {
          ifo = mif;
          dur = mif.getDuration();
        }
      }
      // BD
      if (ext.endsWith("BDMV") || ext.endsWith("MPLS")) {
        if (mif.getDuration() > dur) {
          bd = mif;
          dur = mif.getDuration();
        }
      }
    }

    if (ifo == null && bd == null) {
      LOGGER.info("Could not find a valid MediaFile.");
      return;
    }

    if (bd != null) {
      LOGGER.debug("Considering file: {}", bd.getFilename());
      parseMediainfoSnapshot(mediaFile, bd.getSnapshot());
      mediaFile.setDuration(bd.getDuration());
    }
    else {
      LOGGER.debug("Considering file: {}", ifo.getFilename());
      // now find the associated VOB with same VTS prefix.
      // VTS_01_0.IFO <-- meta, full audio language
      // VTS_01_0.VOB <-- menu
      // VTS_01_1.VOB <-- main video
      MediaInfoFile vob = null;
      // check DVD VOBs
      String prefix = StrgUtils.substr(ifo.getFilename(), "(?i)^(VTS_\\d+).*");
      if (prefix.isEmpty()) {
        // check HD-DVD
        prefix = StrgUtils.substr(ifo.getFilename(), "(?i)^(HV\\d+)I.*");
      }
      for (MediaInfoFile mif : miFiles) {
        // TODO: check HD-DVD
        if (mif.getFilename().startsWith(prefix) && !mif.getFilename().endsWith("IFO")) {
          vob = mif;
          // take last to not get the menu one...
        }
      }
      LOGGER.debug("Considering file: {}", vob.getFilename());

      // now we have the 2 files to consider...
      parseMediainfoSnapshot(mediaFile, vob.getSnapshot());

      // vob resets wrong duration - take from ifo
      mediaFile.setDuration(ifo.getDuration());
      // audio & subtitle streams should be parsed from IFO
      mediaFile.getAudioStreams().clear();
      gatherAudioInformation(mediaFile, ifo.getSnapshot());
      mediaFile.getSubtitles().clear();
      gatherSubtitleInformation(mediaFile, ifo.getSnapshot());
    }

    miFiles.clear();
  }

  /**
   * Gets the real mediainfo values.
   *
   * @param miSnapshot
   *          the mediainfo snapshot to load the data from
   * @param streamKind
   *          MediaInfo.StreamKind.(General|Video|Audio|Text|Chapters|Image|Menu )
   * @param streamNumber
   *          the stream number (0 for first)
   * @param keys
   *          the information you want to fetch
   * @return the media information you asked<br>
   *         <b>OR AN EMPTY STRING IF MEDIAINFO COULD NOT BE LOADED</b> (never NULL)
   */
  public static String getMediaInfo(Map<MediaInfo.StreamKind, List<Map<String, String>>> miSnapshot, MediaInfo.StreamKind streamKind,
      int streamNumber, String... keys) {
    // prevent NPE
    if (miSnapshot == null) {
      return "";
    }

    for (String key : keys) {
      List<Map<String, String>> stream = miSnapshot.get(streamKind);
      if (stream != null) {
        LinkedHashMap<String, String> info = (LinkedHashMap<String, String>) stream.get(streamNumber);
        if (info != null) {
          String value = info.get(key);
          if (StringUtils.isNotBlank(value)) {
            return value;
          }
        }
      }
    }

    return "";
  }

  /**
   * Checks, if a specific string can be found in one or multiple values<br>
   * comes handy for different MI versions, where something changed....
   *
   * @param miSnapshot
   *          the mediainfo snapshot to load the data from
   * @param streamKind
   *          MediaInfo.StreamKind.(General|Video|Audio|Text|Chapters|Image|Menu )
   * @param streamNumber
   *          the stream number (0 for first)
   * @param search
   *          the information to search for
   * @param keys
   *          the information you want to fetch
   * @return the search value you asked for, or empty string
   */
  public static String getMediaInfoContains(Map<MediaInfo.StreamKind, List<Map<String, String>>> miSnapshot, MediaInfo.StreamKind streamKind,
      int streamNumber, String search, String... keys) {
    // prevent NPE
    if (miSnapshot == null) {
      return "";
    }
    for (String key : keys) {
      List<Map<String, String>> stream = miSnapshot.get(streamKind);
      if (stream != null) {
        LinkedHashMap<String, String> info = (LinkedHashMap<String, String>) stream.get(streamNumber);
        if (info != null) {
          String value = info.get(key);
          if (StringUtils.isNotBlank(value) && value.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT))) {
            return search;
          }
        }
      }
    }

    return "";
  }

  /**
   * gets a mediainfo value directly by calling libmediainfo.<br />
   * ATTENTION: this causes libmediainfo to open the file
   * 
   * @param mediaFile
   *          the {@link MediaFile} to analyze
   * @param streamKind
   *          the stream kind
   * @param streamNumber
   *          the stream number
   * @param keys
   *          the key
   * @return the requested value or an empty string
   */
  public static String getMediaInfoDirect(MediaFile mediaFile, MediaInfo.StreamKind streamKind, int streamNumber, String... keys) {
    Map<MediaInfo.StreamKind, List<Map<String, String>>> miSnapshot = getMediaInfoSnapshot(mediaFile);
    return getMediaInfo(miSnapshot, streamKind, streamNumber, keys);
  }

  /**
   * gather the subtitle information for the given {@link MediaFile}, but solely from the file naming.<br />
   * usable for subtitle files
   * 
   * @param mediaFile
   *          the media file
   */
  private static void gatherSubtitleInformation(MediaFile mediaFile) {
    String filename = mediaFile.getFilename();
    String path = mediaFile.getPath();

    MediaFileSubtitle sub = new MediaFileSubtitle();
    String shortname = mediaFile.getBasename().toLowerCase(Locale.ROOT);
    if (shortname.contains("forced")) {
      sub.setForced(true);
      shortname = shortname.replaceAll("\\p{Punct}*forced", "");
    }
    sub.setLanguage(parseLanguageFromString(shortname));

    if (sub.getLanguage().isEmpty() && filename.endsWith(".sub")) {
      // not found in name, try to parse from idx
      Path idx = Paths.get(path, filename.replaceFirst("sub$", "idx"));

      try (FileReader fr = new FileReader(idx.toFile()); BufferedReader br = new BufferedReader(fr)) {
        String line;
        while ((line = br.readLine()) != null) {
          String lang = "";

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
      }
      catch (IOException e) {
        LOGGER.debug("could not read idx file: {}", e.getMessage());
      }
    }

    sub.setCodec(mediaFile.getExtension());
    mediaFile.setSubtitles(Collections.singletonList(sub));
  }

  /**
   * gather the subtitle information for the given {@link MediaFile}, but with libmediainfo<br />
   * usable for video files with embedded subtitles
   *
   * @param mediaFile
   *          the media file
   */
  private static void gatherSubtitleInformation(MediaFile mediaFile, Map<MediaInfo.StreamKind, List<Map<String, String>>> miSnapshot) {
    int streamsTextCount = MetadataUtil.parseInt(getMediaInfo(miSnapshot, MediaInfo.StreamKind.General, 0, "TextCount"), 0);
    int streamsStreamCount = MetadataUtil.parseInt(getMediaInfo(miSnapshot, MediaInfo.StreamKind.Text, 0, "StreamCount"), 0);

    int streams = streamsTextCount > 0 ? streamsTextCount : streamsStreamCount;

    List<MediaFileSubtitle> subtitles = new ArrayList<>();

    for (int i = 0; i < streams; i++) {
      MediaFileSubtitle stream = new MediaFileSubtitle();

      String codec = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Text, i, "CodecID/Hint", "Format");
      stream.setCodec(codec.replaceAll("\\p{Punct}", ""));
      String lang = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Text, i, "Language/String", "Language");
      stream.setLanguage(parseLanguageFromString(lang));

      String forced = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Text, i, "Forced");
      boolean b = forced.equalsIgnoreCase("true") || forced.equalsIgnoreCase("yes");
      stream.setForced(b);

      // "default" subtitle stream?
      String def = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Text, i, "Default");
      if (def.equalsIgnoreCase("yes")) {
        stream.setDefaultStream(true);
      }
      subtitles.add(stream);
    }

    mediaFile.setSubtitles(subtitles);
  }

  /**
   * gather the audio information for the given {@link MediaFile}
   * 
   * @param mediaFile
   *          the media file
   * @param miSnapshot
   *          the mediainfo snapshot to load the data from
   */
  private static void gatherAudioInformation(MediaFile mediaFile, Map<MediaInfo.StreamKind, List<Map<String, String>>> miSnapshot) {
    // https://github.com/MediaArea/MediaInfoLib/tree/master/Source/MediaInfo/Audio
    List<MediaFileAudioStream> audioStreams = new ArrayList<>();

    int streams = 0;
    try {
      streams = MetadataUtil.parseInt(getMediaInfo(miSnapshot, MediaInfo.StreamKind.General, 0, "AudioCount"));
    }
    catch (Exception e) {
      LOGGER.trace("could not parse stream count: {}", e.getMessage());
    }
    if (streams == 0) {
      streams = getStreamCount(miSnapshot, MediaInfo.StreamKind.Audio);
    }

    for (int i = 0; i < streams; i++) {
      // workaround for DTS & TrueHD variant detection
      // search for well known String in defined keys (changes between different MI versions!)
      String[] acSearch = new String[] { "Format", "Format_Profile", "Format_Commercial", "Format_Commercial_IfAny", "CodecID", "Codec" };
      String audioCodec = getMediaInfoContains(miSnapshot, MediaInfo.StreamKind.Audio, i, "TrueHD", acSearch);
      if (audioCodec.isEmpty()) {
        audioCodec = getMediaInfoContains(miSnapshot, MediaInfo.StreamKind.Audio, i, "Atmos", acSearch);
      }
      if (audioCodec.isEmpty()) {
        audioCodec = getMediaInfoContains(miSnapshot, MediaInfo.StreamKind.Audio, i, "DTS", acSearch);
      }

      // else just take format
      if (audioCodec.isEmpty()) {
        audioCodec = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Audio, i, "Format");
        audioCodec = audioCodec.replaceAll("\\p{Punct}", "");
      }

      // https://github.com/Radarr/Radarr/blob/develop/src/NzbDrone.Core/MediaFiles/MediaInfo/MediaInfoFormatter.cs#L35
      String addFeature = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Audio, i, "Format_AdditionalFeatures");
      if (!addFeature.isEmpty()) {
        if ("dts".equalsIgnoreCase(audioCodec)) {
          if (addFeature.startsWith("XLL")) {
            if (addFeature.endsWith("X")) {
              audioCodec = "DTS-X";
            }
            else {
              audioCodec = "DTSHD-MA";
            }
          }
          if (addFeature.equals("ES")) {
            audioCodec = "DTS-ES";
          }
          if (addFeature.equals("XBR")) {
            audioCodec = "DTSHD-HRA";
          }
          // stays DTS
        }
        if ("TrueHD".equalsIgnoreCase(audioCodec)) {
          if (addFeature.equalsIgnoreCase("16-ch")) {
            audioCodec = "Atmos";
          }
        }
      }

      // old 18.05 style
      String audioProfile = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Audio, i, "Format_Profile", "Format_profile"); // different case in XML
      if (!audioProfile.isEmpty()) {
        if ("dts".equalsIgnoreCase(audioCodec)) {
          // <Format_Profile>X / MA / Core</Format_Profile>
          if (audioProfile.contains("ES")) {
            audioCodec = "DTS-ES";
          }
          if (audioProfile.contains("HRA")) {
            audioCodec = "DTSHD-HRA";
          }
          if (audioProfile.contains("MA")) {
            audioCodec = "DTSHD-MA";
          }
          if (audioProfile.contains("X")) {
            audioCodec = "DTS-X";
          }
        }
        if ("TrueHD".equalsIgnoreCase(audioCodec)) {
          if (audioProfile.contains("Atmos")) {
            audioCodec = "Atmos";
          }
        }
      }

      // newer 18.12 style
      if ("ac3".equalsIgnoreCase(audioCodec) || "dts".equalsIgnoreCase(audioCodec) || "TrueHD".equalsIgnoreCase(audioCodec)) {
        String commName = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Audio, i, "Format_Commercial", "Format_Commercial_IfAny")
            .toLowerCase(Locale.ROOT);

        if (!commName.isEmpty()) {
          if (commName.contains("master audio")) {
            audioCodec = "DTSHD-MA";
          }
          if (commName.contains("high resolution audio")) {
            audioCodec = "DTSHD-HRA";
          }
          if (commName.contains("extended") || commName.contains("es matrix") || commName.contains("es discrete")) {
            audioCodec = "DTS-ES";
          }
          if (commName.contains("atmos")) {
            audioCodec = "Atmos";
          }
          // Dolby Digital EX
          if (commName.contains("ex audio")) {
            audioCodec = "AC3EX";
          }
        }
      }

      MediaFileAudioStream stream = new MediaFileAudioStream();
      stream.setCodec(audioCodec);

      // AAC sometimes codes channels into Channel(s)_Original
      // and DTS-ES has an additional core channel
      int ch = parseChannelsAsInt(getMediaInfo(miSnapshot, MediaInfo.StreamKind.Audio, i, "Channel(s)"));
      int ch2 = parseChannelsAsInt(getMediaInfo(miSnapshot, MediaInfo.StreamKind.Audio, i, "Channel(s)_Original"));
      if (ch2 > ch) {
        ch = ch2;
      }
      stream.setAudioChannels(ch);

      String br = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Audio, i, "BitRate", "BitRate_Maximum", "BitRate_Minimum", "BitRate_Nominal");

      try {
        String[] brMode = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Audio, i, "BitRate_Mode").split("/");
        if (brMode.length > 1) {
          String[] brChunks = br.split("/");
          int brMult = 0;
          for (String brChunk : brChunks) {
            brMult += MetadataUtil.parseInt(brChunk.trim(), 0);
          }
          stream.setBitrate(brMult / 1000);
        }
        else {
          br = br.replace("kb/s", "");// 448 / 1000 = 0
          stream.setBitrate(Integer.parseInt(br.trim()) / 1000);
        }
      }
      catch (Exception e) {
        LOGGER.debug("could not parse bitrate: {}", e.getMessage());
      }

      String language = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Audio, i, "Language/String", "Language");
      if (language.isEmpty()) {
        if (!mediaFile.isDiscFile()) { // video_ts parsed 'ts' as Tsonga
          // try to parse from filename
          String shortname = mediaFile.getBasename().toLowerCase(Locale.ROOT);
          stream.setLanguage(parseLanguageFromString(shortname));
        }
      }
      else {
        stream.setLanguage(parseLanguageFromString(language));
      }

      // "default" audio stream?
      String def = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Audio, i, "Default");
      if (def.equalsIgnoreCase("yes")) {
        stream.setDefaultStream(true);
      }

      audioStreams.add(stream);
    }

    mediaFile.setAudioStreams(audioStreams);
  }

  /**
   * how many streams of chosen kind do we have gathered?
   *
   * @param miSnapshot
   *          the media info snapshot
   * @param kind
   *          the stream kind
   * @return the stream count
   */
  private static int getStreamCount(Map<MediaInfo.StreamKind, List<Map<String, String>>> miSnapshot, MediaInfo.StreamKind kind) {
    List<Map<String, String>> map = miSnapshot.get(kind);
    if (map != null) {
      return map.size();
    }
    return 0;
  }

  /**
   * channels usually filled like "5.1ch" or "8 / 6". Take the higher
   *
   * @return channels as int
   */
  public static int parseChannelsAsInt(String channels) {
    int highest = 0;
    if (!channels.isEmpty()) {
      try {
        String[] parts = channels.split("/");
        for (String p : parts) {
          if (p.toLowerCase(Locale.ROOT).contains("object")) {
            // "11 objects / 6 channels" - ignore objects
            continue;
          }
          p = p.replaceAll("[a-zA-Z]", ""); // remove now all characters

          int ch = 0;
          String[] c = p.split("[^0-9]"); // split on not-numbers and count all; so 5.1 -> 6
          for (String s : c) {
            if (s.matches("[0-9]+")) {
              ch += Integer.parseInt(s);
            }
          }
          if (ch > highest) {
            highest = ch;
          }
        }
      }
      catch (NumberFormatException e) {
        highest = 0;
      }
    }
    return highest;
  }

  /**
   * gather the video information for the given {@link MediaFile}
   *
   * @param mediaFile
   *          the media file
   * @param miSnapshot
   *          the mediainfo snapshot to load the data from
   */
  public static void gatherVideoInformation(MediaFile mediaFile, Map<MediaInfo.StreamKind, List<Map<String, String>>> miSnapshot) {
    int height = 0;
    int width = 0;

    try {
      height = MetadataUtil.parseInt(getMediaInfo(miSnapshot, MediaInfo.StreamKind.Video, 0, "Height"));
      width = MetadataUtil.parseInt(getMediaInfo(miSnapshot, MediaInfo.StreamKind.Video, 0, "Width"));
    }
    catch (Exception e) {
      LOGGER.trace("could not paese width/height: {}", e.getMessage());
    }

    mediaFile.setVideoWidth(width);
    mediaFile.setVideoHeight(height);

    String scanType = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Video, 0, "ScanType");

    String codecId = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Video, 0, "CodecID");

    String videoCodec = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Video, 0, "CodecID/Hint", "Format");

    // fix for Microsoft VC-1
    if (StringUtils.containsIgnoreCase(videoCodec, "Microsoft")) {
      videoCodec = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Video, 0, "Format");
    }

    // workaround for XVID
    if (codecId.equalsIgnoreCase("XVID")) {
      // XVID is open source variant MP4, only detectable through codecId
      videoCodec = "XVID";
    }

    // detect the right MPEG version
    if (StringUtils.containsIgnoreCase(videoCodec, "MPEG")) {
      // search for the version
      try {
        int version = Integer.parseInt(getMediaInfo(miSnapshot, MediaInfo.StreamKind.Video, 0, "Format_Version"));
        videoCodec = "MPEG-" + version;
      }
      catch (Exception e) {
        LOGGER.trace("could not parse MPEG version: {}", e.getMessage());
      }
    }
    mediaFile.setVideoCodec(getFirstEntryViaScanner(videoCodec));

    String bd = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Video, 0, "BitDepth");
    mediaFile.setBitDepth(MetadataUtil.parseInt(bd, 0));

    try {
      String fr = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Video, 0, "FrameRate");
      mediaFile.setFrameRate(Double.parseDouble(fr));
    }
    catch (Exception e) {
      LOGGER.trace("could not parse frame rate: {}", e.getMessage());
    }

    if (height == 0 || scanType.isEmpty()) {
      mediaFile.setExactVideoFormat("");
    }
    else {
      mediaFile.setExactVideoFormat(height + "" + Character.toLowerCase(scanType.charAt(0)));
    }

    String extensions = getMediaInfo(miSnapshot, MediaInfo.StreamKind.General, 0, "Codec/Extensions", "Format");
    // get first extension
    mediaFile.setContainerFormat(getFirstEntryViaScanner(extensions).toLowerCase(Locale.ROOT));

    // if container format is still empty -> insert the extension
    if (StringUtils.isBlank(mediaFile.getContainerFormat())) {
      mediaFile.setContainerFormat(mediaFile.getExtension());
    }

    String ar = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Video, 0, "DisplayAspectRatio", "Display_aspect_ratio", "DisplayAspectRatio/String",
        "DisplayAspectRatio_Origin");
    try {
      if (ar.equals("16:9")) {
        ar = "1.78";
      }
      ar = ar.replace(":1", ""); // eg 2.40:1
      float arf = Float.parseFloat(ar);
      mediaFile.setAspectRatio(arf);
    }
    catch (Exception e) {
      LOGGER.warn("Could not parse AspectRatio '{}'", ar);
    }

    String mvc = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Video, 0, "MultiView_Count");
    String video3DFormat = "";
    if (!StringUtils.isEmpty(mvc) && mvc.equals("2")) {
      video3DFormat = MediaFileHelper.VIDEO_3D;
      String mvl = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Video, 0, "MultiView_Layout").toLowerCase(Locale.ROOT);
      LOGGER.debug("3D detected :) - {}", mvl);
      if (!StringUtils.isEmpty(mvl) && mvl.contains("top") && mvl.contains("bottom")) {
        video3DFormat = MediaFileHelper.VIDEO_3D_HTAB; // assume HalfTAB as default
        if (height > width) {
          video3DFormat = MediaFileHelper.VIDEO_3D_TAB;// FullTAB eg 1920x2160
        }
      }
      if (!StringUtils.isEmpty(mvl) && mvl.contains("side")) {
        video3DFormat = MediaFileHelper.VIDEO_3D_HSBS;// assume HalfSBS as default
        if (mediaFile.getAspectRatio() > 3) {
          video3DFormat = MediaFileHelper.VIDEO_3D_SBS;// FullSBS eg 3840x1080
        }
      }
      if (!StringUtils.isEmpty(mvl) && mvl.contains("laced")) { // Both Eyes laced in one block
        video3DFormat = MediaFileHelper.VIDEO_3D_MVC;
      }
    }
    else {
      // not detected as 3D by MI - BUT: if we've got some known resolutions, we can at least find the "full" ones ;)
      if (width == 3840 && height == 1080) {
        video3DFormat = MediaFileHelper.VIDEO_3D_SBS;
      }
      else if (width == 1920 && height == 2160) {
        video3DFormat = MediaFileHelper.VIDEO_3D_TAB;
      }
    }
    mediaFile.setVideo3DFormat(video3DFormat);

    // prefer commercial "hdr10+" naming over technical
    mediaFile
        .setHdrFormat(getMediaInfo(miSnapshot, MediaInfo.StreamKind.Video, 0, "HDR_Format_Commercial", "HDR_Format_Compatibility", "HDR_Format"));

    // TODO: season/episode parsing
    // int season = parseToInt(getMediaInfo(StreamKind.General, 0, "Season"));
    // int part = parseToInt(getMediaInfo(StreamKind.General, 0, "Part"));
    // System.out.println("*** S" + season + " E" + part);
    // #here are the "magic codes" to know where apple tag stores the metadata for each file.
    // $BOXTYPE_TVEN = "tven"; # episode name
    // $BOXTYPE_TVES = "tves"; # episode number
    // $BOXTYPE_TVSH = "tvsh"; # TV Show or series
    // $BOXTYPE_DESC = "desc"; # short description - max is 255 characters
    // $BOXTYPE_TVSN = "tvsn"; # season
    // $BOXTYPE_STIK = "stik"; # "magic" to make it realize it's a TV show

  }

  /**
   * gather image information for the given {@link MediaFile}
   * 
   * @param mediaFile
   *          the media file
   * @param miSnapshot
   *          the mediainfo snapshot to load the data from
   */
  public static void gatherImageInformation(MediaFile mediaFile, Map<MediaInfo.StreamKind, List<Map<String, String>>> miSnapshot) {
    int height = MetadataUtil.parseInt(getMediaInfo(miSnapshot, MediaInfo.StreamKind.Image, 0, "Height"), 0);
    int width = MetadataUtil.parseInt(getMediaInfo(miSnapshot, MediaInfo.StreamKind.Image, 0, "Width"), 0);
    String videoCodec = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Image, 0, "CodecID/Hint", "Format");
    mediaFile.checkForAnimation();

    mediaFile.setVideoHeight(height);
    mediaFile.setVideoWidth(width);
    mediaFile.setVideoCodec(getFirstEntryViaScanner(videoCodec));

    String extensions = getMediaInfo(miSnapshot, MediaInfo.StreamKind.General, 0, "Codec/Extensions", "Format");
    // get first extension
    mediaFile.setContainerFormat(getFirstEntryViaScanner(extensions).toLowerCase(Locale.ROOT));

    String bd = getMediaInfo(miSnapshot, MediaInfo.StreamKind.Image, 0, "BitDepth");
    mediaFile.setBitDepth(MetadataUtil.parseInt(bd, 0));

    // if container format is still empty -> insert the extension
    if (StringUtils.isBlank(mediaFile.getContainerFormat())) {
      mediaFile.setContainerFormat(mediaFile.getExtension());
    }
  }

  /**
   * use a scanner to get the first entry
   *
   * @param string
   *          the string to parse
   * @return the first entry or an empty string
   */
  public static String getFirstEntryViaScanner(String string) {
    if (StringUtils.isBlank(string)) {
      return "";
    }
    try (Scanner scanner = new Scanner(string)) {
      return scanner.next();
    }
    catch (Exception e) {
      LOGGER.error("could not parse string {} with a Scanner: {}", string, e.getMessage());
    }
    return "";
  }
}
