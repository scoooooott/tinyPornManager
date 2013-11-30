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
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileSubtitle;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;

/**
 * The TvShow renamer Works on per MediaFile basis
 * 
 * @author Myron Boyle
 */
public class TvShowRenamer {

  /** The Constant LOGGER. */
  private static final Logger LOGGER = LoggerFactory.getLogger(TvShowRenamer.class);

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
   * replaces all the invalid filename character from a string
   * 
   * @param name
   *          the string to clean
   * @return the cleaned string
   */
  private static String cleanForFilename(String name) {
    return name.replaceAll("([\"\\:<>|/?*])", "");
  }

  /**
   * renames the TvSHow root folder and updates all mediaFiles
   * 
   * @param show
   *          the show
   */
  public static void renameTvShowRoot(TvShow show) {
    LOGGER.debug("movie year: " + show.getYear());
    LOGGER.debug("movie path: " + show.getPath());
    String newPathname = createDestination("$T ($Y)", show); // hardcode for now
    String oldPathname = show.getPath();

    if (!newPathname.isEmpty()) {
      newPathname = show.getDataSource() + File.separator + newPathname;
      File srcDir = new File(oldPathname);
      File destDir = new File(newPathname);
      // move directory if needed
      if (!srcDir.equals(destDir)) {
        boolean ok = false;
        try {
          // FileUtils.moveDirectory(srcDir, destDir);
          ok = Utils.moveDirectorySafe(srcDir, destDir);
          if (ok) {
            show.updateMediaFilePath(srcDir, destDir); // TvShow MFs
            show.setPath(newPathname);
            for (TvShowEpisode episode : new ArrayList<TvShowEpisode>(show.getEpisodes())) {
              episode.updateMediaFilePath(srcDir, destDir);
            }
            show.saveToDb();
          }
        }
        catch (Exception e) {
          LOGGER.error("error moving folder: ", e);
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
      LOGGER.warn("No episodes found for file '" + mf.getFilename() + "' - skipping");
      return;
    }

    // get first, for isDisc and season
    TvShowEpisode ep = eps.get(0);
    File episodePath = new File(ep.getPath());

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
    String seasonName = generateSeasonDir(Globals.settings.getTvShowSettings().getRenamerSeasonFolder(), ep);
    File seasonDir = new File(show.getPath(), seasonName);
    if (!seasonDir.exists()) {
      seasonDir.mkdir();
    }

    if (ep.isDisc() || mf.isDiscFile()) {
      // \Season 1\S01E02E03\VIDEO_TS\VIDEO_TS.VOB
      // ......... \epFolder \disc... \ file
      File disc = mf.getFile().getParentFile();
      File epFolder = disc.getParentFile();

      // sanity check
      if (!disc.getName().equalsIgnoreCase("BDMV") && !disc.getName().equalsIgnoreCase("VIDEO_TS")) {
        LOGGER.error("Episode is labeled as 'on BD/DVD', but structure seems not to match. Better exit and do nothing... o_O");
        return;
      }

      String newFoldername = FilenameUtils.getBaseName(generateFilename(show, mf)); // w/o extension
      if (newFoldername != null && !newFoldername.isEmpty()) {
        File newEpFolder = new File(seasonDir + File.separator + newFoldername);
        File newDisc = new File(newEpFolder + File.separator + disc.getName()); // old disc name

        try {
          if (!epFolder.equals(newEpFolder)) {
            boolean ok = Utils.moveDirectorySafe(epFolder, newEpFolder);
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
      if (filename != null && !filename.isEmpty()) {
        File newFile = new File(seasonDir, filename);

        try {
          if (!mf.getFile().equals(newFile)) {
            File oldMfFile = mf.getFile();
            boolean ok = Utils.moveFileSafe(oldMfFile, newFile);
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
   * @param mf
   *          the MediaFile
   */
  public static String generateFilename(TvShow tvShow, MediaFile mf) {
    String filename = "";
    String s = "";
    String e = "";
    String delim = "";

    TvShowEpisodeNaming form = Globals.settings.getTvShowSettings().getRenamerFormat();
    String separator = Globals.settings.getTvShowSettings().getRenamerSeparator();
    if (separator.isEmpty()) {
      separator = "_";
    }

    List<TvShowEpisode> eps = TvShowList.getInstance().getTvEpisodesByFile(tvShow, mf.getFile());
    if (eps == null || eps.size() == 0) {
      return "";
    }

    String show = cleanForFilename(eps.get(0).getTvShow().getTitle());
    if (Globals.settings.getTvShowSettings().getRenamerAddShow()) {
      filename = filename + show;
    }

    // generate SEE-title string appended
    for (int i = 0; i < eps.size(); i++) {
      TvShowEpisode ep = eps.get(i);

      filename = filename + separator;
      switch (form) {
        case WITH_SE:
          s = "S" + lz(ep.getSeason());
          e = "E" + lz(ep.getEpisode());
          break;
        case WITH_X:
          s = String.valueOf(ep.getSeason());
          e = lz(ep.getEpisode());
          delim = "x";
          break;
        case WITH_0X:
          s = lz(ep.getSeason());
          e = lz(ep.getEpisode());
          delim = "x";
          break;
        case NUMBER:
          s = String.valueOf(ep.getSeason());
          e = lz(ep.getEpisode());
          break;
        default:
          break;
      }
      if (Globals.settings.getTvShowSettings().getRenamerAddSeason()) {
        filename = filename + s;
      }
      filename = filename + delim;
      filename = filename + e;

      if (Globals.settings.getTvShowSettings().getRenamerAddTitle() && eps.size() < 3) {
        String epTitle = cleanForFilename(ep.getTitle());
        if (epTitle.matches("[0-9]+.*") && separator.equals(".")) {
          // EP title starts with a number, so "S01E01.1 Day in..." could be misleading parsed
          // as sub-episode E01.1 - override separator for that hardcoded!
          filename = filename + '_';
        }
        else {
          filename = filename + separator;
        }
        filename = filename + epTitle;
      }
    }
    if (filename.startsWith(separator)) {
      filename = filename.substring(separator.length());
    }
    if (mf.getType().equals(MediaFileType.THUMB)) {
      filename = filename + "-thumb";
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
        // TODO: meh, we didn't have an actual MF yet - need to parse filename ourselves (like movie). But with a recent scan of files/DB this should
        // not occur.
      }
    }

    filename = filename + "." + mf.getExtension(); // readd original extension

    return filename;
  }

  public static String generateSeasonDir(String template, TvShowEpisode episode) {
    String seasonDir = template;

    seasonDir = seasonDir.replace("$1", String.valueOf(episode.getSeason()));
    seasonDir = seasonDir.replace("$2", lz(episode.getSeason()));
    if (seasonDir.isEmpty()) {
      seasonDir = "Season " + String.valueOf(episode.getSeason());
    }
    return seasonDir;
  }

  /**
   * Creates the new file/folder name according to template string
   * 
   * @param template
   *          the template
   * @param show
   *          the movie
   * @return the string
   */
  public static String createDestination(String template, TvShow show) {
    String newDestination = template;

    // replace token title ($T)
    if (newDestination.contains("$T")) {
      newDestination = replaceToken(newDestination, "$T", show.getTitle());
    }

    // replace token first letter of title ($1)
    if (newDestination.contains("$1")) {
      newDestination = replaceToken(newDestination, "$1", StringUtils.isNotBlank(show.getTitle()) ? show.getTitle().substring(0, 1).toUpperCase()
          : "");
    }

    // replace token first letter of sort title ($2)
    if (newDestination.contains("$2")) {
      newDestination = replaceToken(newDestination, "$2", StringUtils.isNotBlank(show.getTitleSortable()) ? show.getTitleSortable().substring(0, 1)
          .toUpperCase() : "");
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

    // replace token orignal title ($O)
    if (newDestination.contains("$O")) {
      newDestination = replaceToken(newDestination, "$O", show.getOriginalTitle());
    }

    // replace token IMDBid ($I)
    if (newDestination.contains("$I")) {
      newDestination = replaceToken(newDestination, "$I", show.getImdbId());
    }

    // replace token sort title ($E)
    if (newDestination.contains("$E")) {
      newDestination = replaceToken(newDestination, "$E", show.getTitleSortable());
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

    // trim out unnecessary whitespaces
    newDestination = newDestination.trim();

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

}
