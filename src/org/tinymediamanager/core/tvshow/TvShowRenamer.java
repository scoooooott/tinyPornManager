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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFile;

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
   * @param TvShow
   *          the TvShow
   */
  public static void renameTvShow(TvShow show) {

    // check if a datasource is set
    if (StringUtils.isEmpty(show.getDataSource())) {
      LOGGER.error("no Datasource set");
      return;
    }

    // all the good & needed mediafiles
    ArrayList<MediaFile> needed = new ArrayList<MediaFile>();
    ArrayList<MediaFile> cleanup = new ArrayList<MediaFile>();

    LOGGER.info("Renaming TvShow: " + show.getTitle());
    LOGGER.debug("TvShow year: " + show.getYear());
    LOGGER.debug("TvShow path: " + show.getPath());

    for (TvShowSeason season : show.getSeasons()) {
      renameSeason(season, show);
    }
  }

  /**
   * Rename all MediaFiles of a single season<br>
   * create a "Season X" folder if non existent.
   * 
   * @param season
   *          the season
   * @param show
   *          the tvshow (only needed for path)
   */
  public static void renameSeason(TvShowSeason season, TvShow show) {

    if (season.getSeason() < 0) {
      LOGGER.warn("Season was -1 - skipping");
      return;
    }

    String s = "Season " + String.valueOf(season.getSeason());
    File sdir = new File(show.getPath(), s);
    if (!sdir.exists()) {
      sdir.mkdir();
    }

    List<MediaFile> mfs = season.getMediaFiles();
    for (MediaFile mf : mfs) {
      renameFile(season, mf);
    }
  }

  /**
   * renames a MediaFile to its intended name/path
   * 
   * @param season
   *          the season
   * @param mf
   *          the mediafile
   */
  private static void renameFile(TvShowSeason season, MediaFile mf) {
    String filename = generateFilename(season, mf);
    // TODO: rename file

  }

  /**
   * generates the filename of a TvShow MediaFile according to settings
   * 
   * @param mf
   *          MediaFile
   */
  public static String generateFilename(TvShowSeason season, MediaFile mf) {
    String filename = "";
    String s = "";
    String e = "";
    String delim = "";

    TvShowEpisodeNaming form = Globals.settings.getTvShowSettings().getRenamerFormat();
    String separator = Globals.settings.getTvShowSettings().getRenamerSeparator();

    String show = cleanForFilename(season.getTvShow().getTitle());
    if (Globals.settings.getTvShowSettings().getRenamerAddShow()) {
      filename = filename + show;
    }

    List<TvShowEpisode> eps = TvShowList.getInstance().getTvEpisodesByFile(mf.getFile());
    // generate SEE-title string appended
    for (int i = 0; i < eps.size(); i++) {
      TvShowEpisode ep = eps.get(i);

      filename = filename + separator;
      // TODO: handle upper/lower case and leadingZero or not
      switch (form) {
        case WITH_SE:
          s = "S" + lz(season.getSeason());
          e = "E" + lz(ep.getEpisode());
          break;
        case WITH_X:
          s = String.valueOf(season.getSeason());
          e = lz(ep.getEpisode());
          delim = "x";
          break;
        case NUMBER:
          s = String.valueOf(season.getSeason());
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

    return filename;
  }
}
