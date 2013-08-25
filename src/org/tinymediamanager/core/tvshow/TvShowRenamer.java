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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFile;
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
   * Rename TvShow.
   * 
   * @param show
   *          the TvShow
   */
  public static void renameTvShow(TvShow show) {

    // check if a datasource is set
    if (StringUtils.isEmpty(show.getDataSource())) {
      LOGGER.error("no Datasource set");
      return;
    }

    LOGGER.info("Renaming TvShow: " + show.getTitle());
    LOGGER.debug("TvShow year: " + show.getYear());
    LOGGER.debug("TvShow path: " + show.getPath());

    // this are the TV show MFs like poster/banner/...
    // for (MediaFile mf : show.getMediaFiles()) {
    // renameMediaFile(mf, show);
    // }

    for (MediaFile mf : show.getEpisodesMediaFiles()) {
      renameMediaFile(mf, show);
    }
  }

  /**
   * Rename Season.
   * 
   * @param season
   *          the Season
   */
  public static void renameSeason(TvShowSeason season) {
    LOGGER.info("Renaming TvShow '" + season.getTvShow().getTitle() + "' Season " + season.getSeason());
    for (MediaFile mf : season.getMediaFiles()) {
      renameMediaFile(mf, season.getTvShow());
    }
  }

  /**
   * Rename Episode (PLUS all Episodes having the same MediaFile!!!).
   * 
   * @param episode
   *          the Episode
   */
  public static void renameEpisode(TvShowEpisode episode) {
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

    List<TvShowEpisode> eps = TvShowList.getInstance().getTvEpisodesByFile(mf.getFile());
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

    if (ep.isDisc()) {
      // \Season 1\S01E02E03\VIDEO_TS\VIDEO_TS.VOB
      // ......... \epFolder \disc... \ file
      File disc = mf.getFile().getParentFile();
      File epFolder = disc.getParentFile();

      // sanity check
      if (!disc.getName().equalsIgnoreCase("BDMV") && !disc.getName().equalsIgnoreCase("VIDEO_TS")) {
        LOGGER.error("Episode is labeled as 'on BD/DVD', but structure seems not to match. Better exit and do nothing... o_O");
        return;
      }

      String newFoldername = FilenameUtils.getBaseName(generateFilename(mf)); // w/o extension
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
        }
        else {
          // old and new folder are equal, do nothing
        }
      }
      catch (Exception e) {
        LOGGER.error("error moving video file", e);
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, mf.getFilename(), "message.renamer.failedrename", new String[] { ":",
            e.getLocalizedMessage() }));
      }
    } // end isDisc
    else {
      MediaFile newMF = new MediaFile(mf); // clone MF
      String filename = generateFilename(mf);
      File newFile = new File(seasonDir, filename);

      try {
        if (!mf.getFile().equals(newFile)) {
          boolean ok = Utils.moveFileSafe(mf.getFile(), newFile);
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
        }
        else {
          // old and new file are equal, keep MF
        }
      }
      catch (Exception e) {
        LOGGER.error("error moving video file", e);
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, mf.getFilename(), "message.renamer.failedrename", new String[] { ":",
            e.getLocalizedMessage() }));
      }
    }
  }

  /**
   * generates the filename of a TvShow MediaFile according to settings <b>(without path)</b>
   * 
   * @param mf
   *          the MediaFile
   */
  public static String generateFilename(MediaFile mf) {
    String filename = "";
    String s = "";
    String e = "";
    String delim = "";

    TvShowEpisodeNaming form = Globals.settings.getTvShowSettings().getRenamerFormat();
    String separator = Globals.settings.getTvShowSettings().getRenamerSeparator();
    if (separator.isEmpty()) {
      separator = "_";
    }

    List<TvShowEpisode> eps = TvShowList.getInstance().getTvEpisodesByFile(mf.getFile());

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

      if (Globals.settings.getTvShowSettings().getRenamerAddTitle()) {
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
    filename = filename + "." + mf.getExtension(); // readd original extension

    return filename;
  }

  public static String generateSeasonDir(String template, TvShowEpisode episode) {
    String seasonDir = template;

    seasonDir = seasonDir.replace("$1", String.valueOf(episode.getSeason()));
    seasonDir = seasonDir.replace("$2", lz(episode.getSeason()));

    return seasonDir;
  }
}
