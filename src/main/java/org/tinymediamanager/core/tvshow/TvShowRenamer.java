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
package org.tinymediamanager.core.tvshow;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.LanguageStyle;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.MediaSource;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * The TvShow renamer Works on per MediaFile basis
 * 
 * @author Myron Boyle
 */
public class TvShowRenamer {
  private static final Logger         LOGGER         = LoggerFactory.getLogger(TvShowRenamer.class);
  private static final TvShowSettings SETTINGS       = TvShowModuleManager.SETTINGS;

  private static final String[]       seasonNumbers  = { "$1", "$2", "$3", "$4" };
  private static final String[]       episodeNumbers = { "$E", "$D" };
  private static final String[]       episodeTitles  = { "$T" };
  private static final String[]       showTitles     = { "$N", "$M" };

  private static final Pattern        epDelimiter    = Pattern.compile("(\\s?(folge|episode|[epx]+)\\s?)?\\$[ED]", Pattern.CASE_INSENSITIVE);
  private static final Pattern        seDelimiter    = Pattern.compile("((staffel|season|s)\\s?)?[\\$][1234]", Pattern.CASE_INSENSITIVE);
  private static final Pattern        token          = Pattern.compile("(\\$[\\w#])");

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

  /**
   * renames the TvSHow root folder and updates all mediaFiles
   * 
   * @param show
   *          the show
   */
  public static void renameTvShowRoot(TvShow show) {
    LOGGER.debug("TV show year: " + show.getYear());
    LOGGER.debug("TV show path: " + show.getPath());
    String newPathname = generateTvShowDir(SETTINGS.getRenamerTvShowFoldername(), show);
    String oldPathname = show.getPath();

    if (!newPathname.isEmpty()) {
      // newPathname = show.getDataSource() + File.separator + newPathname;
      Path srcDir = Paths.get(oldPathname);
      Path destDir = Paths.get(newPathname);
      // move directory if needed
      // if (!srcDir.equals(destDir)) {
      if (!srcDir.toAbsolutePath().toString().equals(destDir.toAbsolutePath().toString())) {
        try {
          // FileUtils.moveDirectory(srcDir, destDir);
          // create parent if needed
          if (!Files.exists(destDir.getParent())) {
            Files.createDirectory(destDir.getParent());
          }
          boolean ok = Utils.moveDirectorySafe(srcDir, destDir);
          if (ok) {
            show.updateMediaFilePath(srcDir, destDir); // TvShow MFs
            show.setPath(newPathname);
            for (TvShowEpisode episode : new ArrayList<>(show.getEpisodes())) {
              episode.replacePathForRenamedFolder(srcDir, destDir);
              episode.updateMediaFilePath(srcDir, destDir);
            }
            show.saveToDb();
          }
        }
        catch (Exception e) {
          LOGGER.error("error moving folder: ", e.getMessage());
          MessageManager.instance
              .pushMessage(new Message(MessageLevel.ERROR, srcDir, "message.renamer.failedrename", new String[] { ":", e.getLocalizedMessage() }));
        }
      }
    }
  }

  /**
   * Rename Episode (PLUS all Episodes having the same MediaFile!!!).
   * 
   * @param episode
   *          the Episode
   */
  public static void renameEpisode(TvShowEpisode episode) {
    // test for valid season/episode number
    if (episode.getSeason() < 0 || episode.getEpisode() < 0) {
      LOGGER.warn(
          "failed to rename episode " + episode.getTitle() + " (TV show " + episode.getTvShow().getTitle() + ") - invalid season/episode number");
      MessageManager.instance.pushMessage(
          new Message(MessageLevel.ERROR, episode.getTvShow().getTitle(), "tvshow.renamer.failedrename", new String[] { episode.getTitle() }));
      return;
    }

    if (episode.isDisc()) {
      renameDiscEpisode(episode);
    }
    else {
      LOGGER.info("Renaming TvShow '" + episode.getTvShow().getTitle() + "' Episode " + episode.getEpisode());
      for (MediaFile mf : new ArrayList<>(episode.getMediaFiles())) {
        renameMediaFile(mf, episode.getTvShow());
      }
    }
  }

  private static void renameDiscEpisode(TvShowEpisode episode) {
    // \Season 1\S01E02E03\VIDEO_TS\VIDEO_TS.VOB
    // seasonDir \epFolder \disc... \ file
    Path epFolder = episode.getPathNIO();
    TvShow show = episode.getTvShow();

    // gen season dir
    String seasonName = generateSeasonDir(SETTINGS.getRenamerSeasonFoldername(), episode);
    Path seasonDir = show.getPathNIO();
    if (StringUtils.isNotBlank(seasonName)) {
      seasonDir = show.getPathNIO().resolve(seasonName);
      if (!Files.exists(seasonDir)) {
        try {
          Files.createDirectory(seasonDir);
        }
        catch (IOException e) {
        }
      }
    }

    // sanity checks
    // DVD files in show root - NO, keep everything
    if (show.getPathNIO().toString().equals(episode.getPathNIO().toString())) {
      LOGGER.error("Episode is labeled as 'on BD/DVD', but files are in show root. Cannot rename episode o_O");
      return;
    }

    // find DISC root folder
    MediaFile mf = episode.getMediaFiles(MediaFileType.VIDEO).get(0);
    Path disc = mf.getFileAsPath().getParent().toAbsolutePath(); // folder
    String folder = show.getPathNIO().relativize(disc).toString().toUpperCase(Locale.ROOT); // relative
    while (folder.contains("BDMV") || folder.contains("VIDEO_TS")) {
      disc = disc.getParent();
      folder = show.getPathNIO().relativize(disc).toString().toUpperCase(Locale.ROOT); // reevaluate
    }

    if (!disc.getFileName().toString().equalsIgnoreCase("BDMV") && !disc.getFileName().toString().equalsIgnoreCase("VIDEO_TS")) {
      LOGGER.error("Episode is labeled as 'on BD/DVD', but structure seems not to match. Better exit and do nothing... o_O");
      // return;
    }

    String newFoldername = FilenameUtils.getBaseName(generateFolderename(show, mf)); // w/o extension
    if (newFoldername != null && !newFoldername.isEmpty()) {
      Path newEpFolder = seasonDir.resolve(newFoldername);

      Path newDisc = newEpFolder.resolve(disc.getFileName()); // old disc name
      // disc root files same as episode root - no DISC folder
      if (disc.toAbsolutePath().toString().equals(episode.getPathNIO().toString())) {
        newDisc = newEpFolder;
      }

      try {
        // if (!epFolder.equals(newEpFolder)) {
        if (!epFolder.toAbsolutePath().toString().equals(newEpFolder.toAbsolutePath().toString())) {
          boolean ok = false;
          try {
            // create parent if needed
            if (!Files.exists(newEpFolder.getParent())) {
              Files.createDirectory(newEpFolder.getParent());
            }
            ok = Utils.moveDirectorySafe(epFolder, newEpFolder);
          }
          catch (Exception e) {
            LOGGER.error(e.getMessage());
            MessageManager.instance.pushMessage(
                new Message(MessageLevel.ERROR, epFolder, "message.renamer.failedrename", new String[] { ":", e.getLocalizedMessage() }));
          }
          if (ok) {
            // iterate over all EPs & MFs and fix new path
            LOGGER.debug("updating *all* MFs for new path -> " + newEpFolder);
            episode.updateMediaFilePath(disc, newDisc); // update path for files in disc root
            episode.updateMediaFilePath(epFolder, newEpFolder);// update path for files in episode root
            episode.setPath(newEpFolder.toAbsolutePath().toString());
            episode.saveToDb();
          }
          // and cleanup
          cleanEmptyDir(epFolder);
        }
        else {
          // old and new folder are equal, do nothing
        }

        // rename thumb files (we wrote them wrong / old style)
        List<MediaFile> thumbs = episode.getMediaFiles(MediaFileType.THUMB);
        if (thumbs != null && !thumbs.isEmpty()) {
          MediaFile th = thumbs.get(0);
          MediaFile th2 = new MediaFile(thumbs.get(0)); // clone
          th2.setFilename("thumb." + th2.getExtension());
          Utils.moveFileSafe(th.getFileAsPath(), th2.getFileAsPath());
        }
      }
      catch (Exception e) {
        LOGGER.error("error moving video file " + disc + " to " + newFoldername, e);
        MessageManager.instance.pushMessage(
            new Message(MessageLevel.ERROR, mf.getFilename(), "message.renamer.failedrename", new String[] { ":", e.getLocalizedMessage() }));
      }
    }
  }

  /**
   * Renames a MediaFiles<br>
   * gets all episodes of it, creates season folder, updates MFs & DB
   * 
   * @param mf
   *          the MediaFile
   * @param show
   *          the tvshow (only needed for path)
   */
  public static void renameMediaFile(MediaFile mf, TvShow show) {
    // #######################################################
    // Assumption: all multi-episodes share the same season!!!
    // #######################################################

    List<TvShowEpisode> eps = TvShowList.getInstance().getTvEpisodesByFile(show, mf.getFile());
    if (eps == null || eps.size() == 0) {
      // FIXME: workaround for r1972
      // when moving video file, all NFOs get deleted and a new gets created.
      // so this OLD NFO is not found anylonger - just delete it
      if (mf.getType() == MediaFileType.NFO) {
        Utils.deleteFileSafely(mf.getFileAsPath());
        return;
      }

      LOGGER.warn("No episodes found for file '" + mf.getFilename() + "' - skipping");
      return;
    }

    // get first, for isDisc and season
    TvShowEpisode ep = eps.get(0);

    // test access rights or return
    LOGGER.debug("testing file S:" + ep.getSeason() + " E:" + ep.getEpisode() + " MF:" + mf.getFile().getAbsolutePath());
    File f = mf.getFile();
    boolean testRenameOk = false;
    for (int i = 0; i < 5; i++) {
      testRenameOk = f.renameTo(f); // haahaa, try to rename to itself :P
      if (testRenameOk) {
        break; // ok it worked, step out
      }
      try {
        if (!f.exists()) {
          LOGGER.debug("Hmmm... file " + f + " does not even exists; delete from DB");
          // delete from MF
          for (TvShowEpisode e : eps) {
            e.removeFromMediaFiles(mf);
            e.saveToDb();
          }
          return;
        }
        LOGGER.debug("rename did not work - sleep a while and try again...");
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {
        LOGGER.warn("I'm so excited - could not sleep");
      }
    }
    if (!testRenameOk) {
      LOGGER.warn("File " + mf.getFileAsPath() + " is not accessible!");
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, mf.getFilename(), "message.renamer.failedrename"));
      return;
    }

    // create SeasonDir
    // String seasonName = "Season " + String.valueOf(ep.getSeason());
    String seasonName = generateSeasonDir(SETTINGS.getRenamerSeasonFoldername(), ep);
    Path seasonDir = show.getPathNIO();
    if (StringUtils.isNotBlank(seasonName)) {
      seasonDir = show.getPathNIO().resolve(seasonName);
      if (!Files.exists(seasonDir)) {
        try {
          Files.createDirectory(seasonDir);
        }
        catch (IOException e) {
        }
      }
    }

    MediaFile newMF = new MediaFile(mf); // clone MF
    if (mf.getType().equals(MediaFileType.TRAILER)) {
      // move trailer into separate dir - not supported by XBMC
      Path sample = seasonDir.resolve("sample");
      if (!Files.exists(sample)) {
        try {
          Files.createDirectory(sample);
        }
        catch (IOException e) {
        }
      }
      seasonDir = sample; // change directory storage
    }
    String filename = generateFilename(show, mf);
    LOGGER.debug("new filename should be " + filename);
    if (StringUtils.isNotBlank(filename)) {
      Path newFile = seasonDir.resolve(filename);

      try {
        // if (!mf.getFile().equals(newFile)) {
        if (!mf.getFileAsPath().toString().equals(newFile.toString())) {
          Path oldMfFile = mf.getFileAsPath();
          boolean ok = false;
          try {
            // create parent if needed
            if (!Files.exists(newFile.getParent())) {
              Files.createDirectory(newFile.getParent());
            }
            ok = Utils.moveFileSafe(oldMfFile, newFile);
          }
          catch (Exception e) {
            LOGGER.error(e.getMessage());
            MessageManager.instance.pushMessage(
                new Message(MessageLevel.ERROR, oldMfFile, "message.renamer.failedrename", new String[] { ":", e.getLocalizedMessage() }));
          }
          if (ok) {
            if (mf.getFilename().endsWith(".sub")) {
              // when having a .sub, also rename .idx (don't care if error)
              try {
                Path oldidx = mf.getFileAsPath().resolveSibling(mf.getFilename().toString().replaceFirst("sub$", "idx"));
                Path newidx = newFile.resolveSibling(newFile.getFileName().toString().replaceFirst("sub$", "idx"));
                Utils.moveFileSafe(oldidx, newidx);
              }
              catch (Exception e) {
                // no idx found or error - ignore
              }
            }
            newMF.setPath(seasonDir.toString());
            newMF.setFilename(filename);
            // iterate over all EPs and delete old / set new MF
            for (TvShowEpisode e : eps) {
              e.removeFromMediaFiles(mf);
              e.addToMediaFiles(newMF);
              e.setPath(seasonDir.toString());
              e.saveToDb();
            }
          }
          // and cleanup
          cleanEmptyDir(oldMfFile.getParent());
        }
        else {
          // old and new file are equal, keep MF
        }
      }
      catch (Exception e) {
        LOGGER.error("error moving video file " + mf.getFilename() + " to " + newFile, e);
        MessageManager.instance.pushMessage(
            new Message(MessageLevel.ERROR, mf.getFilename(), "message.renamer.failedrename", new String[] { ":", e.getLocalizedMessage() }));
      }
    }
  }

  private static void cleanEmptyDir(Path dir) {
    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir)) {
      if (!directoryStream.iterator().hasNext()) {
        // no iterator = empty
        LOGGER.debug("Deleting empty Directory " + dir);
        Files.delete(dir); // do not use recursive her
        return;
      }
    }
    catch (IOException ex) {
    }

    // FIXME: recursive backward delete?! why?!
    // if (Files.isDirectory(dir)) {
    // cleanEmptyDir(dir.getParent());
    // }
  }

  /**
   * generates the filename of a TvShow MediaFile according to settings <b>(without path)</b>
   * 
   * @param tvShow
   *          the tvShow
   * @param mf
   *          the MF for multiepisode
   * @return the file name for the media file
   */
  public static String generateFilename(TvShow tvShow, MediaFile mf) {
    return generateName("", tvShow, mf, true);
  }

  /**
   * generates the filename of a TvShow MediaFile according to settings <b>(without path)</b>
   * 
   * @param template
   *          the renaming template
   * @param tvShow
   *          the tvShow
   * @param mf
   *          the MF for multiepisode
   * @return the file name for the media file
   */
  public static String generateFilename(String template, TvShow tvShow, MediaFile mf) {
    return generateName(template, tvShow, mf, true);
  }

  /**
   * generates the foldername of a TvShow MediaFile according to settings <b>(without path)</b><br>
   * Mainly for DISC files
   * 
   * @param tvShow
   *          the tvShow
   * @param mf
   *          the MF for multiepisode
   * @return the file name for media file
   */
  public static String generateFolderename(TvShow tvShow, MediaFile mf) {
    return generateName("", tvShow, mf, false);
  }

  private static String generateName(String template, TvShow tvShow, MediaFile mf, boolean forFile) {
    String forcedExtension = "";

    String filename = "";
    List<TvShowEpisode> eps = TvShowList.getInstance().getTvEpisodesByFile(tvShow, mf.getFile());
    if (eps == null || eps.size() == 0) {
      return "";
    }

    if (StringUtils.isBlank(template)) {
      filename = createDestination(SETTINGS.getRenamerFilename(), tvShow, eps);
    }
    else {
      filename = createDestination(template, tvShow, eps);
    }

    if (StringUtils.isBlank(filename) && forFile) {
      return mf.getFilename();
    }

    // since we can use this method for folders too, use the next options solely for files
    if (forFile) {
      if (mf.getType().equals(MediaFileType.THUMB)) {
        switch (TvShowModuleManager.SETTINGS.getTvShowEpisodeThumbFilename()) {
          case FILENAME_THUMB_POSTFIX:
            filename = filename + "-thumb";
            break;

          case FILENAME_THUMB_TBN:
            forcedExtension = "tbn";
            break;

          case FILENAME_THUMB: // filename as is
          default:
            break;
        }
        TvShowEpisode ep = eps.get(0);
        if (ep.isDisc()) {
          filename = "thumb";
        }
      }
      if (mf.getType().equals(MediaFileType.FANART)) {
        filename = filename + "-fanart";
      }
      if (mf.getType().equals(MediaFileType.TRAILER)) {
        filename = filename + "-trailer";
      }
      if (mf.getType().equals(MediaFileType.MEDIAINFO)) {
        filename = filename + "-mediainfo";
      }
      if (mf.getType().equals(MediaFileType.VSMETA)) {
        // HACK: get video extension from "old" name, eg video.avi.vsmeta
        String ext = FilenameUtils.getExtension(FilenameUtils.getBaseName(mf.getFilename()));
        filename = filename + "." + ext;
      }
      if (mf.getType().equals(MediaFileType.VIDEO_EXTRA)) {
        String name = mf.getBasename();
        Pattern p = Pattern.compile("(?i).*([ _.-]extras[ _.-]).*");
        Matcher m = p.matcher(name);
        if (m.matches()) {
          name = name.substring(m.end(1)); // everything behind
        }
        // if not, MF must be within /extras/ folder - use name 1:1
        filename = filename + "-extras-" + name;
      }
      if (mf.getType().equals(MediaFileType.SUBTITLE)) {
        List<MediaFileSubtitle> subtitles = mf.getSubtitles();
        if (subtitles != null && subtitles.size() > 0) {
          MediaFileSubtitle mfs = mf.getSubtitles().get(0);
          if (mfs != null) {
            if (!mfs.getLanguage().isEmpty()) {
              String lang = LanguageStyle.getLanguageCodeForStyle(mfs.getLanguage(), TvShowModuleManager.SETTINGS.getTvShowRenamerLanguageStyle());
              if (StringUtils.isBlank(lang)) {
                lang = mfs.getLanguage();
              }
              filename = filename + "." + lang;
            }
            if (mfs.isForced()) {
              filename = filename + ".forced";
            }
          }
        }
        else {
          // detect from filename, if we don't have a MediaFileSubtitle entry!
          // remove the filename of episode from subtitle, to ease parsing
          String shortname = mf.getBasename().toLowerCase(Locale.ROOT).replace(eps.get(0).getVideoBasenameWithoutStacking(), "");
          String originalLang = "";
          String lang = "";
          String forced = "";

          if (mf.getFilename().toLowerCase(Locale.ROOT).contains("forced")) {
            // add "forced" prior language
            forced = ".forced";
            shortname = shortname.replaceAll("\\p{Punct}*forced", "");
          }
          // shortname = shortname.replaceAll("\\p{Punct}", "").trim(); // NEVER EVER!!!

          for (String s : LanguageUtils.KEY_TO_LOCALE_MAP.keySet()) {
            if (shortname.equalsIgnoreCase(s) || shortname.matches("(?i).*[ _.-]+" + s + "$")) {
              originalLang = s;
              // lang = Utils.getIso3LanguageFromLocalizedString(s);
              // LOGGER.debug("found language '" + s + "' in subtitle; displaying it as '" + lang + "'");
              break;
            }
          }
          lang = LanguageStyle.getLanguageCodeForStyle(originalLang, TvShowModuleManager.SETTINGS.getTvShowRenamerLanguageStyle());
          if (StringUtils.isBlank(lang)) {
            lang = originalLang;
          }
          if (StringUtils.isNotBlank(lang)) {
            filename = filename + "." + lang;
          }
          if (StringUtils.isNotBlank(forced)) {
            filename += forced;
          }
        }
      }
    } // end forFile

    // ASCII replacement
    if (SETTINGS.isAsciiReplacement()) {
      filename = StrgUtils.convertToAscii(filename, false);
    }

    // don't write jpeg -> write jpg
    if (mf.getExtension().equalsIgnoreCase("JPEG")) {
      forcedExtension = "jpg";
    }

    if (StringUtils.isNotBlank(forcedExtension)) {
      filename = filename + "." + forcedExtension; // add forced extension
    }
    else {
      filename = filename + "." + mf.getExtension(); // readd original extension
    }

    return filename;
  }

  public static String generateSeasonDir(String template, TvShowEpisode episode) {
    String seasonDir = template;

    // replace all other tokens
    seasonDir = createDestination(seasonDir, episode.getTvShow(), Arrays.asList(episode));

    // only allow empty season dir if the season is in the filename
    if (StringUtils.isBlank(seasonDir) && !(SETTINGS.getRenamerFilename().contains("$1") || SETTINGS.getRenamerFilename().contains("$2")
        || SETTINGS.getRenamerFilename().contains("$3") || SETTINGS.getRenamerFilename().contains("$4"))) {
      seasonDir = "Season " + String.valueOf(episode.getSeason());
    }
    return seasonDir;
  }

  public static String generateTvShowDir(TvShow tvShow) {
    return generateTvShowDir(SETTINGS.getRenamerTvShowFoldername(), tvShow);
  }

  public static String generateTvShowDir(String template, TvShow tvShow) {
    String newPathname;

    if (StringUtils.isNotBlank(SETTINGS.getRenamerTvShowFoldername())) {
      newPathname = tvShow.getDataSource() + File.separator + createDestination(template, tvShow, null);
    }
    else {
      newPathname = tvShow.getPath();
    }

    return newPathname;
  }

  /**
   * gets the token value ($x) from specified object
   * 
   * @param show
   *          our show
   * @param episode
   *          our episode
   * @param token
   *          the $x token
   * @return value or empty string
   */
  public static String getTokenValue(TvShow show, TvShowEpisode episode, String token) {
    String ret = "";
    if (show == null) {
      show = new TvShow();
    }
    if (episode == null) {
      episode = new TvShowEpisode();
    }
    MediaFile mf = new MediaFile();
    if (episode.getMediaFiles(MediaFileType.VIDEO).size() > 0) {
      mf = episode.getMediaFiles(MediaFileType.VIDEO).get(0);
    }
    switch (token.toUpperCase(Locale.ROOT)) {
      // SHOW
      case "$N":
        ret = show.getTitle();
        break;
      case "$M":
        ret = show.getTitleSortable();
        break;
      case "$Y":
        ret = show.getYear().equals("0") ? "" : show.getYear();
        break;

      // EPISODE
      case "$1":
        ret = String.valueOf(episode.getSeason());
        break;
      case "$2":
        ret = lz(episode.getSeason());
        break;
      case "$3":
        ret = String.valueOf(episode.getDvdSeason());
        break;
      case "$4":
        ret = lz(episode.getDvdSeason());
        break;
      case "$E":
        ret = lz(episode.getEpisode());
        break;
      case "$D":
        ret = lz(episode.getDvdEpisode());
        break;
      case "$T":
        ret = episode.getTitle();
        break;
      case "$S":
        if (episode.getMediaSource() != MediaSource.UNKNOWN) {
          ret = episode.getMediaSource().toString();
        }
        break;

      // MEDIAFILE
      case "$R":
        ret = mf.getVideoResolution();
        break;
      case "$A":
        ret = mf.getAudioCodec() + (mf.getAudioCodec().isEmpty() ? "" : "-") + mf.getAudioChannels();
        break;
      case "$V":
        ret = mf.getVideoCodec() + (mf.getVideoCodec().isEmpty() ? "" : "-") + mf.getVideoFormat();
        break;
      case "$F":
        ret = mf.getVideoFormat();
        break;
      default:
        break;
    }
    return ret;
  }

  /**
   * Creates the new file/folder name according to template string
   * 
   * @param template
   *          the template
   * @param show
   *          the TV show
   * @param episodes
   *          the TV show episodes; nullable for TV show root foldername
   * @return the string
   */
  public static String createDestination(String template, TvShow show, List<TvShowEpisode> episodes) {
    String newDestination = template;
    TvShowEpisode firstEp = null;

    if (StringUtils.isBlank(template)) {
      return "";
    }

    if (episodes == null || episodes.isEmpty()) {
      // TV show root folder

      // replace all $x parameters
      Matcher m = token.matcher(template);
      while (m.find()) {
        String value = getTokenValue(show, null, m.group(1));
        newDestination = replaceToken(newDestination, m.group(1), value);
      }
    }
    else if (episodes.size() == 1) {
      // single episode

      firstEp = episodes.get(0);
      // replace all $x parameters
      Matcher m = token.matcher(template);
      while (m.find()) {
        String value = getTokenValue(show, firstEp, m.group(1));
        newDestination = replaceToken(newDestination, m.group(1), value);
      }
    }
    else {
      // multi episodes
      firstEp = episodes.get(0);
      String loopNumbers = "";

      // *******************
      // LOOP 1 - season/episode
      // *******************
      if (getPatternPos(newDestination, seasonNumbers) > -1) {
        Matcher m = seDelimiter.matcher(newDestination);
        if (m.find()) {
          if (m.group(1) != null) {
            loopNumbers += m.group(1); // "delimiter"
          }
          loopNumbers += newDestination.substring(m.end() - 2, m.end()); // add token
        }
      }

      if (getPatternPos(newDestination, episodeNumbers) > -1) {
        Matcher m = epDelimiter.matcher(newDestination);
        if (m.find()) {
          if (m.group(1) != null) {
            loopNumbers += m.group(1); // "delimiter"
          }
          loopNumbers += newDestination.substring(m.end() - 2, m.end()); // add token
        }
      }
      loopNumbers = loopNumbers.trim();

      // foreach episode, replace and append pattern:
      String episodeParts = "";
      for (TvShowEpisode episode : episodes) {
        String episodePart = loopNumbers;
        // replace all $x parameters
        Matcher m = token.matcher(episodePart);
        while (m.find()) {
          String value = getTokenValue(show, episode, m.group(1));
          episodePart = replaceToken(episodePart, m.group(1), value);
        }
        episodeParts += " " + episodePart;
      }

      // replace original pattern, with our combined
      if (!loopNumbers.isEmpty()) {
        newDestination = newDestination.replace(loopNumbers, episodeParts);
      }

      // *******************
      // LOOP 2 - title
      // *******************
      String loopTitles = "";
      int titlePos = getPatternPos(template, episodeTitles);
      if (titlePos > -1) {
        loopTitles += template.substring(titlePos, titlePos + 2); // add replacer
      }
      loopTitles = loopTitles.trim();

      // foreach episode, replace and append pattern:
      episodeParts = "";
      for (TvShowEpisode episode : episodes) {
        String episodePart = loopTitles;

        // replace all $x parameters
        Matcher m = token.matcher(episodePart);
        while (m.find()) {
          String value = getTokenValue(show, episode, m.group(1));
          episodePart = replaceToken(episodePart, m.group(1), value);
        }

        // separate multiple titles via -
        if (StringUtils.isNotBlank(episodeParts)) {
          episodeParts += " -";
        }
        episodeParts += " " + episodePart;
      }
      // replace original pattern, with our combined
      if (!loopTitles.isEmpty()) {
        newDestination = newDestination.replace(loopTitles, episodeParts);
      }

      // replace all other $x parameters
      Matcher m = token.matcher(newDestination);
      while (m.find()) {
        String value = getTokenValue(show, firstEp, m.group(1));
        newDestination = replaceToken(newDestination, m.group(1), value);
      }

    } // end multi episodes

    // DEFAULT CLEANUP
    // replace empty brackets
    newDestination = newDestination.replaceAll("\\(\\)", "");
    newDestination = newDestination.replaceAll("\\[\\]", "");

    // if there are multiple file separators in a row - strip them out
    if (SystemUtils.IS_OS_WINDOWS) {
      // we need to mask it in windows
      newDestination = newDestination.replaceAll("\\\\{2,}", "\\\\");
      newDestination = newDestination.replaceAll("^\\\\", "");
    }
    else {
      newDestination = newDestination.replaceAll(File.separator + "{2,}", File.separator);
      newDestination = newDestination.replaceAll("^" + File.separator, "");
    }

    // ASCII replacement
    if (SETTINGS.isAsciiReplacement()) {
      newDestination = StrgUtils.convertToAscii(newDestination, false);
    }

    // trim out unnecessary whitespaces
    newDestination = newDestination.trim();
    newDestination = newDestination.replaceAll(" +", " ").trim();

    // any whitespace replacements?
    if (SETTINGS.isRenamerSpaceSubstitution()) {
      newDestination = newDestination.replaceAll(" ", SETTINGS.getRenamerSpaceReplacement());
    }

    // replace trailing dots and spaces
    newDestination = newDestination.replaceAll("[ \\.]+$", "");

    return newDestination.trim();
  }

  /**
   * checks, if the pattern has a recommended structure (S/E numbers, title filled)<br>
   * when false, it might lead to some unpredictable renamings...
   * 
   * @param seasonPattern
   *          the season pattern
   * @param filePattern
   *          the file pattern
   * @return true/false
   */
  public static boolean isRecommended(String seasonPattern, String filePattern) {
    // count em
    int epCnt = count(filePattern, episodeNumbers);
    int titleCnt = count(filePattern, episodeTitles);
    int seCnt = count(filePattern, seasonNumbers);
    int seFolderCnt = count(seasonPattern, seasonNumbers);// check season folder pattern

    // check rules
    if (epCnt != 1 || titleCnt != 1 || seCnt > 1 || seFolderCnt > 1 || (seCnt + seFolderCnt) == 0) {
      LOGGER.debug("Too many/less episode/season/title replacer patterns");
      return false;
    }

    int epPos = getPatternPos(filePattern, episodeNumbers);
    int sePos = getPatternPos(filePattern, seasonNumbers);
    int titlePos = getPatternPos(filePattern, episodeTitles);

    if (sePos > epPos) {
      LOGGER.debug("Season pattern should be before episode pattern!");
      return false;
    }

    // check if title not in-between season/episode pattern in file
    if (titleCnt == 1 && seCnt == 1) {
      if (titlePos < epPos && titlePos > sePos) {
        LOGGER.debug("Title should not be between season/episode pattern");
        return false;
      }
    }

    return true;
  }

  /**
   * Count the amount of renamer tokens per group
   * 
   * @param pattern
   *          the pattern to analyze
   * @param possibleValues
   *          an array of possible values
   * @return 0, or amount
   */
  private static int count(String pattern, String[] possibleValues) {
    int count = 0;
    for (String r : possibleValues) {
      if (pattern.contains(r)) {
        count++;
      }
    }
    return count;
  }

  /**
   * Returns first position of any matched patterns
   * 
   * @param pattern
   *          the pattern to get the position for
   * @param possibleValues
   *          an array of all possible values
   * @return the position of the first occurrence
   */
  private static int getPatternPos(String pattern, String[] possibleValues) {
    int pos = -1;
    for (String r : possibleValues) {
      if (pattern.contains(r)) {
        pos = pattern.indexOf(r);
      }
    }
    return pos;
  }

  private static String replaceToken(String destination, String token, String replacement) {
    String replacingCleaned = "";
    if (StringUtils.isNotBlank(replacement)) {
      // replace illegal characters
      // http://msdn.microsoft.com/en-us/library/windows/desktop/aa365247%28v=vs.85%29.aspx
      replacingCleaned = replacement.replaceAll("([\"\\:<>|/?*])", "");
    }
    return destination.replace(token, replacingCleaned);
  }

  /**
   * replaces all invalid/illegal characters for filenames with ""<br>
   * except the colon, which will be changed to a dash
   * 
   * @param source
   *          string to clean
   * @return cleaned string
   */
  public static String replaceInvalidCharacters(String source) {
    source = source.replaceAll(": ", " - "); // nicer
    source = source.replaceAll(":", "-"); // nicer
    return source.replaceAll("([\"\\\\:<>|/?*])", "");
  }

}
