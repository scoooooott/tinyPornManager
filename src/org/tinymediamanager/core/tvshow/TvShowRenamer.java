/*
 * Copyright 2012 - 2015 Manuel Laggner
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * The TvShow renamer Works on per MediaFile basis
 * 
 * @author Myron Boyle
 */
public class TvShowRenamer {
  private static final Logger         LOGGER                   = LoggerFactory.getLogger(TvShowRenamer.class);
  private static final TvShowSettings SETTINGS                 = Globals.settings.getTvShowSettings();

  // the regexp to find the episode relevant tokens which have to be repeated on multi ep files
  private static final Pattern        multiEpisodeTokenPattern = Pattern
                                                                   .compile("(S|Season|Staffel)?(\\$1|\\$2|\\$E|\\$T)+?.*(\\$1|\\$2|\\$E|\\$T)?");

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
      File srcDir = new File(oldPathname);
      File destDir = new File(newPathname);
      // move directory if needed
      // if (!srcDir.equals(destDir)) {
      if (!srcDir.getAbsolutePath().equals(destDir.getAbsolutePath())) {
        try {
          // FileUtils.moveDirectory(srcDir, destDir);
          boolean ok = Utils.moveDirectorySafe(srcDir, destDir);
          if (ok) {
            show.updateMediaFilePath(srcDir, destDir); // TvShow MFs
            show.setPath(newPathname);
            for (TvShowEpisode episode : new ArrayList<TvShowEpisode>(show.getEpisodes())) {
              episode.replacePathForRenamedFolder(srcDir, destDir);
              episode.updateMediaFilePath(srcDir, destDir);
            }
            show.saveToDb();
          }
        }
        catch (Exception e) {
          LOGGER.error("error moving folder: ", e.getMessage());
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, srcDir.getPath(), "message.renamer.failedrename", new String[] { ":",
              e.getLocalizedMessage() }));
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
      LOGGER.warn("failed to rename episode " + episode.getTitle() + " (TV show " + episode.getTvShow().getTitle()
          + ") - invalid season/episode number");
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, episode.getTvShow().getTitle(), "tvshow.renamer.failedrename",
          new String[] { episode.getTitle() }));
      return;
    }

    LOGGER.info("Renaming TvShow '" + episode.getTvShow().getTitle() + "' Episode " + episode.getEpisode());
    for (MediaFile mf : new ArrayList<MediaFile>(episode.getMediaFiles())) {
      renameMediaFile(mf, episode.getTvShow());
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
      // this should not happen, but unluckily ODB does it sometimes; try a second time to get the episode
      try {
        Thread.sleep(250);
      }
      catch (Exception e) {
      }
      eps = TvShowList.getInstance().getTvEpisodesByFile(show, mf.getFile());
    }
    if (eps == null || eps.size() == 0) {
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
      LOGGER.warn("File " + mf.getFile().getAbsolutePath() + " is not accessible!");
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, mf.getFilename(), "message.renamer.failedrename"));
      return;
    }

    // create SeasonDir
    // String seasonName = "Season " + String.valueOf(ep.getSeason());
    String seasonName = generateSeasonDir(SETTINGS.getRenamerSeasonFoldername(), ep);
    File seasonDir = null;
    if (StringUtils.isNotBlank(seasonName)) {
      seasonDir = new File(show.getPath(), seasonName);
      if (!seasonDir.exists()) {
        seasonDir.mkdir();
      }
    }
    else {
      seasonDir = new File(show.getPath());
    }

    // rename epFolder accordingly
    if (ep.isDisc() || mf.isDiscFile()) {
      // \Season 1\S01E02E03\VIDEO_TS\VIDEO_TS.VOB
      // ........ \epFolder \disc... \ file
      File disc = mf.getFile().getParentFile();
      File epFolder = disc.getParentFile();

      // sanity check
      if (!disc.getName().equalsIgnoreCase("BDMV") && !disc.getName().equalsIgnoreCase("VIDEO_TS")) {
        LOGGER.error("Episode is labeled as 'on BD/DVD', but structure seems not to match. Better exit and do nothing... o_O");
        return;
      }

      String newFoldername = FilenameUtils.getBaseName(generateFolderename(show, mf)); // w/o extension
      if (newFoldername != null && !newFoldername.isEmpty()) {
        File newEpFolder = new File(seasonDir + File.separator + newFoldername);
        File newDisc = new File(newEpFolder + File.separator + disc.getName()); // old disc name

        try {
          // if (!epFolder.equals(newEpFolder)) {
          if (!epFolder.getAbsolutePath().equals(newEpFolder.getAbsolutePath())) {
            boolean ok = false;
            try {
              ok = Utils.moveDirectorySafe(epFolder, newEpFolder);
            }
            catch (Exception e) {
              LOGGER.error(e.getMessage());
              MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, epFolder.getName(), "message.renamer.failedrename", new String[] {
                  ":", e.getLocalizedMessage() }));
            }
            if (ok) {
              // iterate over all EPs & MFs and fix new path
              LOGGER.debug("updating *all* MFs for new path -> " + newEpFolder);
              for (TvShowEpisode e : eps) {
                e.updateMediaFilePath(disc, newDisc);
                e.setPath(newEpFolder.getPath());
                e.saveToDb();
              }
            }
            // and cleanup
            cleanEmptyDir(epFolder);
          }
          else {
            // old and new folder are equal, do nothing
          }
        }
        catch (Exception e) {
          LOGGER.error("error moving video file " + disc.getName() + " to " + newFoldername, e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, mf.getFilename(), "message.renamer.failedrename", new String[] { ":",
              e.getLocalizedMessage() }));
        }
      }
    } // end isDisc
    else {
      MediaFile newMF = new MediaFile(mf); // clone MF
      if (mf.getType().equals(MediaFileType.TRAILER)) {
        // move trailer into separate dir - not supported by XBMC
        File sample = new File(seasonDir, "sample");
        if (!sample.exists()) {
          sample.mkdir();
        }
        seasonDir = sample; // change directory storage
      }
      String filename = generateFilename(show, mf);
      LOGGER.debug("new filename should be " + filename);
      if (filename != null && !filename.isEmpty()) {
        File newFile = new File(seasonDir, filename);

        try {
          // if (!mf.getFile().equals(newFile)) {
          if (!mf.getFile().getAbsolutePath().equals(newFile.getAbsolutePath())) {
            File oldMfFile = mf.getFile();
            boolean ok = false;
            try {
              ok = Utils.moveFileSafe(oldMfFile, newFile);
            }
            catch (Exception e) {
              LOGGER.error(e.getMessage());
              MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, oldMfFile.getPath(), "message.renamer.failedrename", new String[] {
                  ":", e.getLocalizedMessage() }));
            }
            if (ok) {
              newMF.setPath(seasonDir.getAbsolutePath());
              newMF.setFilename(filename);
              // iterate over all EPs and delete old / set new MF
              for (TvShowEpisode e : eps) {
                e.removeFromMediaFiles(mf);
                e.addToMediaFiles(newMF);
                e.setPath(seasonDir.getAbsolutePath());
                e.saveToDb();
              }
            }
            // and cleanup
            cleanEmptyDir(oldMfFile.getParentFile());
          }
          else {
            // old and new file are equal, keep MF
          }
        }
        catch (Exception e) {
          LOGGER.error("error moving video file " + mf.getFilename() + " to " + newFile.getPath(), e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, mf.getFilename(), "message.renamer.failedrename", new String[] { ":",
              e.getLocalizedMessage() }));
        }
      }
    }
  }

  private static void cleanEmptyDir(File dir) {
    if (dir.isDirectory() && dir.listFiles().length == 0) {
      dir.delete();
      cleanEmptyDir(dir.getParentFile());
    }
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
    String filename = "";
    List<TvShowEpisode> eps = TvShowList.getInstance().getTvEpisodesByFile(tvShow, mf.getFile());
    if (eps == null || eps.size() == 0) {
      // this should not happen, but unluckily ODB does it sometimes; try a second time to get the episode
      try {
        Thread.sleep(250);
      }
      catch (Exception ex) {
      }
      eps = TvShowList.getInstance().getTvEpisodesByFile(tvShow, mf.getFile());
    }
    if (eps == null || eps.size() == 0) {
      return "";
    }

    if (StringUtils.isBlank(template)) {
      filename = createDestination(SETTINGS.getRenamerFilename(), tvShow, eps);
    }
    else {
      filename = createDestination(template, tvShow, eps);
    }

    // since we can use this method for folders too, use the next options solely for files
    if (forFile) {
      if (mf.getType().equals(MediaFileType.THUMB)) {
        if (SETTINGS.isUseRenamerThumbPostfix()) {
          filename = filename + "-thumb";
        }
        // else let the filename as is
      }
      if (mf.getType().equals(MediaFileType.FANART)) {
        filename = filename + "-fanart";
      }
      if (mf.getType().equals(MediaFileType.TRAILER)) {
        filename = filename + "-trailer";
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
              filename = filename + "." + mfs.getLanguage();
            }
            if (mfs.isForced()) {
              filename = filename + ".forced";
            }
          }
          else {
            // TODO: meh, we didn't have an actual MF yet - need to parse filename ourselves (like movie). But with a recent scan of files/DB this
            // should not occur.
          }
        }
      }
    } // end forFile

    // ASCII replacement
    if (SETTINGS.isAsciiReplacement()) {
      filename = StrgUtils.convertToAscii(filename, false);
    }

    filename = filename + "." + mf.getExtension(); // readd original extension

    return filename;
  }

  public static String generateSeasonDir(String template, TvShowEpisode episode) {
    String seasonDir = template;

    // replace $1 and $2 as the only episode specific tokens
    seasonDir = seasonDir.replace("$1", String.valueOf(episode.getSeason()));
    seasonDir = seasonDir.replace("$2", lz(episode.getSeason()));

    // replace all other tokens
    seasonDir = createDestination(seasonDir, episode.getTvShow(), new ArrayList<TvShowEpisode>());

    // only allow empty season dir if the season is in the filename
    if (StringUtils.isBlank(seasonDir) && !(SETTINGS.getRenamerFilename().contains("$1") || SETTINGS.getRenamerFilename().contains("$2"))) {
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
      newPathname = tvShow.getDataSource() + File.separator + createDestination(template, tvShow, new ArrayList<TvShowEpisode>());
    }
    else {
      newPathname = tvShow.getPath();
    }

    return newPathname;
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

    // replace token show title ($N)
    if (newDestination.contains("$N")) {
      newDestination = replaceToken(newDestination, "$N", show.getTitle());
    }

    // parse out episode depended tokens - for multi EP naming
    if (!episodes.isEmpty()) {
      Matcher matcher = multiEpisodeTokenPattern.matcher(template);
      String episodeTokens = "";

      if (matcher.find()) {
        episodeTokens = matcher.group(0);
      }

      String combinedEpisodeParts = "";
      for (TvShowEpisode episode : episodes) {
        String episodePart = episodeTokens;

        // remember first episode for media file tokens
        if (firstEp == null) {
          firstEp = episode;
        }

        // Season w/o leading zeros ($1)
        if (episodePart.contains("$1")) {
          episodePart = replaceToken(episodePart, "$1", String.valueOf(episode.getSeason()));
        }

        // Season leading zeros ($2)
        if (episodePart.contains("$2")) {
          episodePart = replaceToken(episodePart, "$2", lz(episode.getSeason()));
        }

        // episode number
        if (episodePart.contains("$E")) {
          episodePart = replaceToken(episodePart, "$E", lz(episode.getEpisode()));
        }

        // episode title
        if (episodePart.contains("$T")) {
          episodePart = replaceToken(episodePart, "$T", episode.getTitle());
        }

        combinedEpisodeParts += episodePart + " ";
      }

      // and now fill in the (multiple) episode parts
      if (StringUtils.isNotBlank(episodeTokens)) {
        newDestination = newDestination.replace(episodeTokens, combinedEpisodeParts);
      }
    }
    else {
      // we're in either TV show folder or season folder generation;
      // strip out episode tokens
      newDestination = newDestination.replace("$E", "");
      newDestination = newDestination.replace("$T", "");
    }

    // replace token year ($Y)
    if (newDestination.contains("$Y")) {
      if (show.getYear().equals("0")) {
        newDestination = newDestination.replace("$Y", "");
      }
      else {
        newDestination = replaceToken(newDestination, "$Y", show.getYear());
      }
    }

    if (firstEp != null && firstEp.getMediaFiles(MediaFileType.VIDEO).size() > 0) {
      MediaFile mf = firstEp.getMediaFiles(MediaFileType.VIDEO).get(0);
      // replace token resolution ($R)
      if (newDestination.contains("$R")) {
        newDestination = replaceToken(newDestination, "$R", mf.getVideoResolution());
      }

      // replace token audio codec + channels ($A)
      if (newDestination.contains("$A")) {
        newDestination = replaceToken(newDestination, "$A", mf.getAudioCodec() + (mf.getAudioCodec().isEmpty() ? "" : "-") + mf.getAudioChannels());
      }

      // replace token video codec + format ($V)
      if (newDestination.contains("$V")) {
        newDestination = replaceToken(newDestination, "$V", mf.getVideoCodec() + (mf.getVideoCodec().isEmpty() ? "" : "-") + mf.getVideoFormat());
      }

      // replace token video format ($F)
      if (newDestination.contains("$F")) {
        newDestination = replaceToken(newDestination, "$F", mf.getVideoFormat());
      }
    }
    else {
      // no mediafiles; remove at least token (if available)
      newDestination = newDestination.replace("$R", "");
      newDestination = newDestination.replace("$A", "");
      newDestination = newDestination.replace("$V", "");
      newDestination = newDestination.replace("$F", "");
    }

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

    // any whitespace replacements?
    if (SETTINGS.isRenamerSpaceSubstitution()) {
      newDestination = newDestination.replaceAll(" ", SETTINGS.getRenamerSpaceReplacement());
    }

    // replace trailing dots and spaces
    newDestination = newDestination.replaceAll("[ \\.]+$", "");

    return newDestination.trim();
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
