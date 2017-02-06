/*
 * Copyright 2012 - 2016 Manuel Laggner
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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ImageCache;
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
import org.tinymediamanager.core.tvshow.filenaming.TvShowEpisodeThumbNaming;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.ListUtils;
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

    LOGGER.info("Renaming TvShow '" + episode.getTvShow().getTitle() + "' Episode " + episode.getEpisode());

    if (episode.isDisc()) {
      renameEpisodeAsDisc(episode);
      return;
    }

    // all the good & needed mediafiles
    ArrayList<MediaFile> needed = new ArrayList<>();
    ArrayList<MediaFile> cleanup = new ArrayList<>(episode.getMediaFiles());
    cleanup.removeAll(Collections.singleton(null)); // remove all NULL ones!

    String seasonFoldername = getSeasonFoldername(SETTINGS.getRenamerSeasonFoldername(), episode);
    Path seasonFolder = episode.getTvShow().getPathNIO();
    if (StringUtils.isNotBlank(seasonFoldername)) {
      seasonFolder = episode.getTvShow().getPathNIO().resolve(seasonFoldername);
      if (!Files.exists(seasonFolder)) {
        try {
          Files.createDirectory(seasonFolder);
        }
        catch (IOException e) {
        }
      }
    }
    // ######################################################################
    // ## rename VIDEO (move 1:1)
    // ######################################################################
    for (MediaFile vid : episode.getMediaFiles(MediaFileType.VIDEO)) {
      LOGGER.trace("Rename 1:1 " + vid.getType() + " " + vid.getFileAsPath());
      MediaFile newMF = generateEpisodeFilenames(episode.getTvShow(), vid).get(0); // there can be only one
      boolean ok = moveFile(vid.getFileAsPath(), newMF.getFileAsPath());
      if (ok) {
        vid.setFile(newMF.getFileAsPath()); // update
      }
      needed.add(vid); // add vid, since we're updating existing MF object
    }

    // ######################################################################
    // ## rename POSTER, FANART, BANNER, CLEARART, THUMB, LOGO, CLEARLOGO, DISCART (copy 1:N)
    // ######################################################################
    // we can have multiple ones, just get the newest one and copy(overwrite) them to all needed
    ArrayList<MediaFile> mfs = new ArrayList<>();
    mfs.add(episode.getNewestMediaFilesOfType(MediaFileType.FANART));
    mfs.add(episode.getNewestMediaFilesOfType(MediaFileType.POSTER));
    mfs.add(episode.getNewestMediaFilesOfType(MediaFileType.BANNER));
    mfs.add(episode.getNewestMediaFilesOfType(MediaFileType.CLEARART));
    mfs.add(episode.getNewestMediaFilesOfType(MediaFileType.THUMB));
    mfs.add(episode.getNewestMediaFilesOfType(MediaFileType.LOGO));
    mfs.add(episode.getNewestMediaFilesOfType(MediaFileType.CLEARLOGO));
    mfs.add(episode.getNewestMediaFilesOfType(MediaFileType.DISC));
    mfs.removeAll(Collections.singleton(null)); // remove all NULL ones!
    for (MediaFile mf : mfs) {
      LOGGER.trace("Rename 1:N " + mf.getType() + " " + mf.getFileAsPath());
      List<MediaFile> newMFs = generateEpisodeFilenames(episode.getTvShow(), mf); // 1:N
      for (MediaFile newMF : newMFs) {
        boolean ok = copyFile(mf.getFileAsPath(), newMF.getFileAsPath());
        if (ok) {
          needed.add(newMF);
        }
      }
    }

    // ######################################################################
    // ## rename NFO (copy 1:N) - only TMM NFOs
    // ######################################################################
    // we need to find the newest, valid TMM NFO
    MediaFile nfo = new MediaFile();
    for (MediaFile mf : episode.getMediaFiles(MediaFileType.NFO)) {
      if (mf.getFiledate() >= nfo.getFiledate()) {// && TvShowEpisodeConnectors.isValidNFO(mf.getFileAsPath())) { //FIXME
        nfo = new MediaFile(mf);
      }
    }

    if (nfo.getFiledate() > 0) { // one valid found? copy our NFO to all variants
      List<MediaFile> newNFOs = generateEpisodeFilenames(episode.getTvShow(), nfo); // 1:N
      if (newNFOs.size() > 0) {
        // ok, at least one has been set up
        for (MediaFile newNFO : newNFOs) {
          boolean ok = copyFile(nfo.getFileAsPath(), newNFO.getFileAsPath());
          if (ok) {
            needed.add(newNFO);
          }
        }
      }
      else {
        // list was empty, so even remove this NFO
        cleanup.add(nfo);
      }
    }
    else {
      LOGGER.trace("No valid NFO found for this episode");
    }

    // ######################################################################
    // ## rename subtitles (copy 1:1)
    // ######################################################################
    for (MediaFile subtitle : episode.getMediaFiles(MediaFileType.SUBTITLE)) {
      LOGGER.trace("Rename 1:1 " + subtitle.getType() + " " + subtitle.getFileAsPath());
      MediaFile newMF = generateEpisodeFilenames(episode.getTvShow(), subtitle).get(0); // there can be only one
      boolean ok = moveFile(subtitle.getFileAsPath(), newMF.getFileAsPath());
      if (ok) {
        subtitle.setFile(newMF.getFileAsPath()); // update
      }
      needed.add(subtitle); // add vid, since we're updating existing MF object
    }

    // ######################################################################
    // ## rename all other types (copy 1:1)
    // ######################################################################
    mfs = new ArrayList<>();
    mfs.addAll(
        episode.getMediaFilesExceptType(MediaFileType.VIDEO, MediaFileType.NFO, MediaFileType.POSTER, MediaFileType.FANART, MediaFileType.BANNER,
            MediaFileType.CLEARART, MediaFileType.THUMB, MediaFileType.LOGO, MediaFileType.CLEARLOGO, MediaFileType.DISC, MediaFileType.SUBTITLE));
    mfs.removeAll(Collections.singleton(null)); // remove all NULL ones!
    for (MediaFile other : mfs) {
      LOGGER.trace("Rename 1:1 " + other.getType() + " " + other.getFileAsPath());

      List<MediaFile> newMFs = generateEpisodeFilenames(episode.getTvShow(), other); // 1:N
      newMFs.removeAll(Collections.singleton(null)); // remove all NULL ones!

      for (MediaFile newMF : newMFs) {
        boolean ok = copyFile(other.getFileAsPath(), newMF.getFileAsPath());
        if (ok) {
          needed.add(newMF);
        }
        else {
          // FIXME: what to do? not copied/exception... keep it for now...
          needed.add(other);
        }
      }
    }

    // ######################################################################
    // ## invalidade image cache
    // ######################################################################
    for (MediaFile gfx : episode.getMediaFiles()) {
      if (gfx.isGraphic()) {
        ImageCache.invalidateCachedImage(gfx.getFileAsPath());
      }
    }

    // remove duplicate MediaFiles
    Set<MediaFile> newMFs = new LinkedHashSet<>(needed);
    needed.clear();
    needed.addAll(newMFs);

    // ######################################################################
    // ## CLEANUP - delete all files marked for cleanup, which are not "needed"
    // ######################################################################
    LOGGER.info("Cleanup...");
    for (int i = cleanup.size() - 1; i >= 0; i--) {
      // cleanup files which are not needed
      if (!needed.contains(cleanup.get(i))) {
        MediaFile cl = cleanup.get(i);
        if (Files.exists(cl.getFileAsPath())) { // unneeded, but for not displaying wrong deletes in logger...
          LOGGER.debug("Deleting " + cl.getFileAsPath());
          Utils.deleteFileWithBackup(cl.getFileAsPath(), episode.getTvShow().getDataSource());
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(cl.getFileAsPath().getParent())) {
          if (!directoryStream.iterator().hasNext()) {
            // no iterator = empty
            LOGGER.debug("Deleting empty Directory " + cl.getFileAsPath().getParent());
            Files.delete(cl.getFileAsPath().getParent()); // do not use recursive her
          }
        }
        catch (IOException e) {
          LOGGER.error("cleanup of " + cl.getFileAsPath().toString() + " : " + e.getMessage());
        }
      }
    }

    // get the first MF of this episode
    MediaFile mf = episode.getMediaFiles(MediaFileType.VIDEO).get(0);
    List<TvShowEpisode> eps = TvShowList.getInstance().getTvEpisodesByFile(episode.getTvShow(), mf.getFile());
    for (TvShowEpisode e : eps) {
      e.removeAllMediaFiles();
      e.addToMediaFiles(needed);
      e.setPath(seasonFolder.toString());
      e.gatherMediaFileInformation(false);
      e.saveToDb();
    }
  }

  /**
   * renames the episode as disc
   * 
   * @param episode
   */
  private static void renameEpisodeAsDisc(TvShowEpisode episode) {
    // get the first MF of this episode
    MediaFile mf = episode.getMediaFiles(MediaFileType.VIDEO).get(0);
    List<TvShowEpisode> eps = TvShowList.getInstance().getTvEpisodesByFile(episode.getTvShow(), mf.getFile());

    // and do some checks
    if (!episode.isDisc() || !mf.isDiscFile()) {
      return;
    }

    // \Season 1\S01E02E03\VIDEO_TS\VIDEO_TS.VOB
    // ........ \epFolder \disc... \mf
    Path disc = mf.getFileAsPath().getParent();
    Path epFolder = disc.getParent();
    if (!disc.getFileName().toString().equalsIgnoreCase("BDMV") && !disc.getFileName().toString().equalsIgnoreCase("VIDEO_TS")) {
      LOGGER.error("Episode is labeled as 'on BD/DVD', but structure seems not to match. Better exit and do nothing... o_O");
      return;
    }

    // create SeasonDir
    String seasonFoldername = getSeasonFoldername(SETTINGS.getRenamerSeasonFoldername(), episode);
    Path seasonFolder = episode.getTvShow().getPathNIO();
    if (StringUtils.isNotBlank(seasonFoldername)) {
      seasonFolder = episode.getTvShow().getPathNIO().resolve(seasonFoldername);
      if (!Files.exists(seasonFolder)) {
        try {
          Files.createDirectory(seasonFolder);
        }
        catch (IOException e) {
        }
      }
    }

    // rename epFolder accordingly

    String newFoldername = FilenameUtils.getBaseName(generateFoldername(episode.getTvShow(), mf)); // w/o extension
    if (StringUtils.isBlank(newFoldername)) {
      LOGGER.warn("empty disc folder name - exiting");
      return;
    }

    Path newEpFolder = seasonFolder.resolve(newFoldername);
    Path newDisc = newEpFolder.resolve(disc.getFileName()); // old disc name

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
          MessageManager.instance
              .pushMessage(new Message(MessageLevel.ERROR, epFolder, "message.renamer.failedrename", new String[] { ":", e.getLocalizedMessage() }));
        }
        if (ok) {
          // iterate over all EPs & MFs and fix new path
          LOGGER.debug("updating *all* MFs for new path -> " + newEpFolder);
          for (TvShowEpisode e : eps) {
            e.updateMediaFilePath(disc, newDisc);
            e.setPath(newEpFolder.toAbsolutePath().toString());
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
      LOGGER.error("error moving video file " + disc + " to " + newFoldername, e);
      MessageManager.instance.pushMessage(
          new Message(MessageLevel.ERROR, mf.getFilename(), "message.renamer.failedrename", new String[] { ":", e.getLocalizedMessage() }));
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
   * generates the foldername of a TvShow MediaFile according to settings <b>(without path)</b><br>
   * Mainly for DISC files
   * 
   * @param tvShow
   *          the tvShow
   * @param mf
   *          the MF for multiepisode
   * @return the file name for media file
   */
  public static String generateFoldername(TvShow tvShow, MediaFile mf) {
    List<TvShowEpisode> eps = TvShowList.getInstance().getTvEpisodesByFile(tvShow, mf.getFile());
    if (ListUtils.isEmpty(eps)) {
      return "";
    }

    return createDestination(SETTINGS.getRenamerFilename(), tvShow, eps);
  }

  /**
   * generates a list of filenames of a TvShow MediaFile according to settings
   *
   * @param tvShow
   *          the tvShow
   * @param mf
   *          the MF for multiepisode
   * @return the file name for the media file
   */
  public static List<MediaFile> generateEpisodeFilenames(TvShow tvShow, MediaFile mf) {
    return generateEpisodeFilenames("", tvShow, mf);
  }

  /**
   * generates a list of filenames of a TvShow MediaFile according to settings
   *
   * @param template
   *          the renaming template
   * @param tvShow
   *          the tvShow
   * @param mf
   *          the MF for multiepisode
   * @return the file name for the media file
   */
  public static List<MediaFile> generateEpisodeFilenames(String template, TvShow tvShow, MediaFile mf) {
    // return list of all generated MFs
    ArrayList<MediaFile> newFiles = new ArrayList<>();

    List<TvShowEpisode> eps = TvShowList.getInstance().getTvEpisodesByFile(tvShow, mf.getFile());
    if (ListUtils.isEmpty(eps)) {
      return newFiles;
    }

    String newFilename;
    if (StringUtils.isBlank(template)) {
      newFilename = createDestination(SETTINGS.getRenamerFilename(), tvShow, eps);
    }
    else {
      newFilename = createDestination(template, tvShow, eps);
    }

    String seasonFoldername = getSeasonFoldername(SETTINGS.getRenamerSeasonFoldername(), eps.get(0));
    Path seasonFolder = tvShow.getPathNIO();
    if (StringUtils.isNotBlank(seasonFoldername)) {
      seasonFolder = tvShow.getPathNIO().resolve(seasonFoldername);
      if (!Files.exists(seasonFolder)) {
        try {
          Files.createDirectory(seasonFolder);
        }
        catch (IOException e) {
        }
      }
    }

    // no new filename? just move the file
    if (StringUtils.isBlank(newFilename)) {
      MediaFile mediaFile = new MediaFile(mf);
      mediaFile.setFile(seasonFolder.resolve(mf.getFilename()));
      newFiles.add(mediaFile);
      return newFiles;
    }

    switch (mf.getType()) {
      ////////////////////////////////////////////////////////////////////////
      // VIDEO
      ////////////////////////////////////////////////////////////////////////
      case VIDEO:
        MediaFile video = new MediaFile(mf);
        newFilename += getStackingString(mf); // ToDo
        newFilename += "." + mf.getExtension();
        video.setFile(seasonFolder.resolve(newFilename));
        newFiles.add(video);
        break;

      ////////////////////////////////////////////////////////////////////////
      // NFO
      ////////////////////////////////////////////////////////////////////////
      case NFO:
        MediaFile nfo = new MediaFile(mf);
        newFilename += "." + mf.getExtension();
        nfo.setFile(seasonFolder.resolve(newFilename));
        newFiles.add(nfo);
        break;

      ////////////////////////////////////////////////////////////////////////
      // THUMB
      ////////////////////////////////////////////////////////////////////////
      case THUMB:
        for (TvShowEpisodeThumbNaming thumbNaming : SETTINGS.getEpisodeThumbFilenames()) {
          String thumbFilename = thumbNaming.getFilename(newFilename, getArtworkExtension(mf));
          MediaFile thumb = new MediaFile(mf);
          thumb.setFile(seasonFolder.resolve(thumbFilename));
          newFiles.add(thumb);
        }
        break;

      ////////////////////////////////////////////////////////////////////////
      // SUBTITLE
      ////////////////////////////////////////////////////////////////////////
      case SUBTITLE:
        List<MediaFileSubtitle> subtitles = mf.getSubtitles();
        String subtitleFilename = "";
        if (subtitles != null && subtitles.size() > 0) {
          MediaFileSubtitle mfs = mf.getSubtitles().get(0);
          if (mfs != null) {
            if (!mfs.getLanguage().isEmpty()) {
              String lang = LanguageStyle.getLanguageCodeForStyle(mfs.getLanguage(), TvShowModuleManager.SETTINGS.getSubtitleLanguageStyle());
              if (StringUtils.isBlank(lang)) {
                lang = mfs.getLanguage();
              }
              subtitleFilename = newFilename + "." + lang;
            }
            if (mfs.isForced()) {
              subtitleFilename = newFilename + ".forced";
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
          lang = LanguageStyle.getLanguageCodeForStyle(originalLang, TvShowModuleManager.SETTINGS.getSubtitleLanguageStyle());
          if (StringUtils.isBlank(lang)) {
            lang = originalLang;
          }
          if (StringUtils.isNotBlank(lang)) {
            subtitleFilename = newFilename + "." + lang;
          }
          if (StringUtils.isNotBlank(forced)) {
            subtitleFilename += forced;
          }
        }
        if (StringUtils.isNotBlank(subtitleFilename)) {
          MediaFile subtitle = new MediaFile(mf);
          subtitle.setFile(seasonFolder.resolve(subtitleFilename + "." + mf.getExtension()));
          newFiles.add(subtitle);
        }
        break;

      ////////////////////////////////////////////////////////////////////////
      // FANART
      ////////////////////////////////////////////////////////////////////////
      case FANART:
        MediaFile fanart = new MediaFile(mf);
        fanart.setFile(seasonFolder.resolve(newFilename + "-fanart." + getArtworkExtension(mf)));
        newFiles.add(fanart);
        break;

      ////////////////////////////////////////////////////////////////////////
      // TRAILER
      ////////////////////////////////////////////////////////////////////////
      case TRAILER:
        MediaFile trailer = new MediaFile(mf);
        trailer.setFile(seasonFolder.resolve(newFilename + "-trailer." + mf.getExtension()));
        newFiles.add(trailer);
        break;

      ////////////////////////////////////////////////////////////////////////
      // MEDIAINFO
      ////////////////////////////////////////////////////////////////////////
      case MEDIAINFO:
        MediaFile mediainfo = new MediaFile(mf);
        mediainfo.setFile(seasonFolder.resolve(newFilename + "-mediainfo." + mf.getExtension()));
        newFiles.add(mediainfo);
        break;

      ////////////////////////////////////////////////////////////////////////
      // VSMETA
      ////////////////////////////////////////////////////////////////////////
      case VSMETA:
        MediaFile vsmeta = new MediaFile(mf);
        // HACK: get video extension from "old" name, eg video.avi.vsmeta
        String ext = FilenameUtils.getExtension(FilenameUtils.getBaseName(mf.getFilename()));
        vsmeta.setFile(seasonFolder.resolve(newFilename + "." + ext));
        newFiles.add(vsmeta);
        break;

      ////////////////////////////////////////////////////////////////////////
      // VIDEO_EXTRA
      ////////////////////////////////////////////////////////////////////////
      case VIDEO_EXTRA:
        String name = mf.getBasename();
        Pattern p = Pattern.compile("(?i).*([ _.-]extras[ _.-]).*");
        Matcher m = p.matcher(name);
        if (m.matches()) {
          name = name.substring(m.end(1)); // everything behind
        }
        // if not, MF must be within /extras/ folder - use name 1:1
        MediaFile videoExtra = new MediaFile(mf);
        videoExtra.setFile(seasonFolder.resolve(newFilename + "-extras-" + name + "." + mf.getExtension()));
        newFiles.add(videoExtra);
        break;
    }

    return newFiles;
  }

  public static String getSeasonFoldername(String template, TvShowEpisode episode) {
    String seasonFolderName = template;

    // replace all other tokens
    seasonFolderName = createDestination(seasonFolderName, episode.getTvShow(), Arrays.asList(episode));

    // only allow empty season dir if the season is in the filename
    if (StringUtils.isBlank(seasonFolderName) && !(SETTINGS.getRenamerFilename().contains("$1") || SETTINGS.getRenamerFilename().contains("$2")
        || SETTINGS.getRenamerFilename().contains("$3") || SETTINGS.getRenamerFilename().contains("$4"))) {
      seasonFolderName = "Season " + String.valueOf(episode.getSeason());
    }
    return seasonFolderName;
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

  private static String getArtworkExtension(MediaFile mf) {
    String ext = mf.getExtension().replaceAll("jpeg", "jpg"); // we only have one constant and only write jpg
    if (ext.equalsIgnoreCase("tbn")) {
      String cont = mf.getContainerFormat();
      if (cont.equalsIgnoreCase("PNG")) {
        ext = "png";
      }
      else if (cont.equalsIgnoreCase("JPEG")) {
        ext = "jpg";
      }
    }
    return ext;
  }

  /**
   * moves a file.
   *
   * @param oldFilename
   *          the old filename
   * @param newFilename
   *          the new filename
   * @return true, when we moved file
   */
  private static boolean moveFile(Path oldFilename, Path newFilename) {
    try {
      // create parent if needed
      if (!Files.exists(newFilename.getParent())) {
        Files.createDirectory(newFilename.getParent());
      }
      boolean ok = Utils.moveFileSafe(oldFilename, newFilename);
      if (ok) {
        return true;
      }
      else {
        LOGGER.error("Could not move MF '" + oldFilename + "' to '" + newFilename + "'");
        return false; // rename failed
      }
    }
    catch (Exception e) {
      LOGGER.error("error moving file", e);
      MessageManager.instance
          .pushMessage(new Message(MessageLevel.ERROR, oldFilename, "message.renamer.failedrename", new String[] { ":", e.getLocalizedMessage() }));
      return false; // rename failed
    }
  }

  /**
   * copies a file.
   *
   * @param oldFilename
   *          the old filename
   * @param newFilename
   *          the new filename
   * @return true, when we copied file OR DEST IS EXISTING
   */
  private static boolean copyFile(Path oldFilename, Path newFilename) {
    if (!oldFilename.toAbsolutePath().toString().equals(newFilename.toAbsolutePath().toString())) {
      LOGGER.info("copy file " + oldFilename + " to " + newFilename);
      if (oldFilename.equals(newFilename)) {
        // windows: name differs, but File() is the same!!!
        // use move in this case, which handles this
        return moveFile(oldFilename, newFilename);
      }
      try {
        // create parent if needed
        if (!Files.exists(newFilename.getParent())) {
          Files.createDirectory(newFilename.getParent());
        }
        Utils.copyFileSafe(oldFilename, newFilename, true);
        return true;
      }
      catch (Exception e) {
        return false;
      }
    }
    else { // file is the same, return true to keep file
      return true;
    }
  }

  /**
   * returns "delimiter + stackingString" for use in filename
   *
   * @param mf
   *          a mediaFile
   * @return eg ".CD1" dependent of settings
   */
  private static String getStackingString(MediaFile mf) {
    String delimiter = ".";
    if (!mf.getStackingMarker().isEmpty()) {
      return delimiter + mf.getStackingMarker();
    }
    else if (mf.getStacking() != 0) {
      return delimiter + "CD" + mf.getStacking();
    }
    return "";
  }
}
