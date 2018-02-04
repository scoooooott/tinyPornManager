/*
 * Copyright 2012 - 2018 Manuel Laggner
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.jmte.NamedDateRenderer;
import org.tinymediamanager.core.jmte.NamedNumberRenderer;
import org.tinymediamanager.core.jmte.TmmModelAdaptor;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.core.tvshow.filenaming.TvShowEpisodeThumbNaming;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.StrgUtils;

import com.floreysoft.jmte.Engine;

/**
 * The TvShowRenamer Works on per MediaFile basis
 * 
 * @author Myron Boyle
 */
public class TvShowRenamer {
  private static final Logger             LOGGER         = LoggerFactory.getLogger(TvShowRenamer.class);
  private static final TvShowSettings     SETTINGS       = TvShowModuleManager.SETTINGS;

  private static final String[]           seasonNumbers  = { "seasonNr", "seasonNr2", "seasonNrDvd", "seasonNrDvd2" };
  private static final String[]           episodeNumbers = { "episodeNr", "episodeNr2", "episodeNrDvd", "episodeNrDvd2" };
  private static final String[]           episodeTitles  = { "title", "titleSortable" };
  private static final String[]           showTitles     = { "showTitle", "showTitleSortable" };

  private static final Pattern            epDelimiter    = Pattern.compile("(\\s?(folge|episode|[epx]+)\\s?)\\$\\{.*?\\}", Pattern.CASE_INSENSITIVE);
  private static final Pattern            seDelimiter    = Pattern.compile("((staffel|season|s)\\s?)\\$\\{.*?\\}", Pattern.CASE_INSENSITIVE);

  public static final Map<String, String> TOKEN_MAP      = createTokenMap();

  /**
   * initialize the token map for the renamer
   *
   * @return the token map
   */
  private static Map<String, String> createTokenMap() {
    Map<String, String> tokenMap = new HashMap<>();
    // TV show tags
    tokenMap.put("showTitle", "tvShow.title");
    tokenMap.put("showTitleSortable", "tvShow.titleSortable");
    tokenMap.put("showYear", "tvShow.year");

    // episode tags
    tokenMap.put("episodeNr", "episode.episode");
    tokenMap.put("episodeNr2", "episode.episode;number(%02d)");
    tokenMap.put("episodeNrDvd", "episode.dvdEpisode");
    tokenMap.put("episodeNrDvd2", "episode.dvdEpisode;number(%02d)");
    tokenMap.put("seasonNr", "episode.season");
    tokenMap.put("seasonNr2", "episode.season;number(%02d)");
    tokenMap.put("seasonNrDvd", "episode.dvdSeason");
    tokenMap.put("seasonNrDvd2", "episode.dvdSeason;number(%02d)");
    tokenMap.put("title", "episode.title");
    tokenMap.put("titleSortable", "episode.titleSortable");
    tokenMap.put("year", "episode.year");
    tokenMap.put("airedDate", "episode.firstAired;date(yyyy-MM-dd)");

    tokenMap.put("videoCodec", "episode.mediaInfoVideoCodec");
    tokenMap.put("videoFormat", "episode.mediaInfoVideoFormat");
    tokenMap.put("videoResolution", "episode.mediaInfoVideoResolution");
    tokenMap.put("audioCodec", "episode.mediaInfoAudioCodec");
    tokenMap.put("audioChannels", "episode.mediaInfoAudioChannels");
    tokenMap.put("3Dformat", "episode.video3DFormat");

    tokenMap.put("mediaSource", "episode.mediaSource");

    return tokenMap;
  }

  /**
   * morph the given template to the JMTE template
   *
   * @param template
   *          the given template
   * @return the JMTE compatible template
   */
  static String morphTemplate(String template) {
    String morphedTemplate = template;
    // replace normal template entries
    for (Map.Entry<String, String> entry : TOKEN_MAP.entrySet()) {
      Pattern pattern = Pattern.compile("\\$\\{" + entry.getKey() + "([^a-zA-Z0-9])", Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(morphedTemplate);
      while (matcher.find()) {
        morphedTemplate = morphedTemplate.replace(matcher.group(), "${" + entry.getValue() + matcher.group(1));
      }
    }

    // replace conditional template entries
    for (Map.Entry<String, String> entry : TOKEN_MAP.entrySet()) {
      Pattern pattern = Pattern.compile("\\$\\{(.*?)," + entry.getKey() + "([^a-zA-Z0-9])", Pattern.CASE_INSENSITIVE);
      Matcher matcher = pattern.matcher(morphedTemplate);
      while (matcher.find()) {
        morphedTemplate = morphedTemplate.replace(matcher.group(), "${" + matcher.group(1) + "," + entry.getValue() + matcher.group(2));
      }
    }

    return morphedTemplate;
  }

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
    LOGGER.debug("TV show path: " + show.getPathNIO());
    String newPathname = getTvShowFoldername(SETTINGS.getRenamerTvShowFoldername(), show);
    String oldPathname = show.getPathNIO().toString();

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
    cleanup.removeAll(Collections.singleton((MediaFile) null)); // remove all NULL ones!

    String seasonFoldername = getSeasonFoldername(episode.getTvShow(), episode.getSeason());
    Path seasonFolder = episode.getTvShow().getPathNIO();
    if (StringUtils.isNotBlank(seasonFoldername)) {
      seasonFolder = episode.getTvShow().getPathNIO().resolve(seasonFoldername);
      if (!Files.exists(seasonFolder)) {
        try {
          Files.createDirectory(seasonFolder);
        }
        catch (IOException ignored) {
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
    mfs.removeAll(Collections.singleton((MediaFile) null)); // remove all NULL ones!
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
    mfs.removeAll(Collections.singleton((MediaFile) null)); // remove all NULL ones!
    for (MediaFile other : mfs) {
      LOGGER.trace("Rename 1:1 " + other.getType() + " " + other.getFileAsPath());

      List<MediaFile> newMFs = generateEpisodeFilenames(episode.getTvShow(), other); // 1:N
      newMFs.removeAll(Collections.singleton((MediaFile) null)); // remove all NULL ones!

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
   *          the episode to be renamed
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
    if (!disc.getFileName().toString().equalsIgnoreCase("BDMV") && !disc.getFileName().toString().equalsIgnoreCase("VIDEO_TS")
        && !disc.getFileName().toString().equalsIgnoreCase("HVDVD_TS")) {
      LOGGER.error("Episode is labeled as 'on BD/DVD', but structure seems not to match. Better exit and do nothing... o_O");
      return;
    }

    // create SeasonDir
    String seasonFoldername = getSeasonFoldername(episode.getTvShow(), episode.getSeason());
    Path seasonFolder = episode.getTvShow().getPathNIO();
    if (StringUtils.isNotBlank(seasonFoldername)) {
      seasonFolder = episode.getTvShow().getPathNIO().resolve(seasonFoldername);
      if (!Files.exists(seasonFolder)) {
        try {
          Files.createDirectory(seasonFolder);
        }
        catch (IOException ignored) {
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
    catch (IOException ignored) {
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

    return createDestination(SETTINGS.getRenamerFilename(), eps);
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
      newFilename = createDestination(SETTINGS.getRenamerFilename(), eps);
    }
    else {
      newFilename = createDestination(template, eps);
    }

    String seasonFoldername = getSeasonFoldername(tvShow, eps.get(0).getSeason());
    Path seasonFolder = tvShow.getPathNIO();
    if (StringUtils.isNotBlank(seasonFoldername)) {
      seasonFolder = tvShow.getPathNIO().resolve(seasonFoldername);
      if (!Files.exists(seasonFolder)) {
        try {
          Files.createDirectory(seasonFolder);
        }
        catch (IOException ignored) {
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
        // String name = mf.getBasename();
        // Pattern p = Pattern.compile("(?i).*([ _.-]extras[ _.-]).*");
        // Matcher m = p.matcher(name);
        // if (m.matches()) {
        // name = name.substring(m.end(1)); // everything behind
        // }
        // // if not, MF must be within /extras/ folder - use name 1:1
        // MediaFile videoExtra = new MediaFile(mf);
        // videoExtra.setFile(seasonFolder.resolve(newFilename + "-extras-" + name + "." + mf.getExtension()));

        // don't mess with extras - keep em 1:1
        newFiles.add(new MediaFile(mf));
        break;

      // missing enums
      case AUDIO:
      case BANNER:
      case CLEARART:
      case CLEARLOGO:
      case DISC:
      case EXTRAFANART:
      case EXTRATHUMB:
      case GRAPHIC:
      case LANDSCAPE:
      case LOGO:
      case POSTER:
      case SAMPLE:
      case SEASON_POSTER:
      case TEXT:
      case UNKNOWN:
      default:
        break;
    }

    return newFiles;
  }

  /**
   * generate the season folder name according to the settings
   *
   * @param show
   *          the TV show to generate the season folder for
   * @param season
   *          the season to generate the folder name for
   * @return the folder name of that season
   */
  public static String getSeasonFoldername(TvShow show, int season) {
    return getSeasonFoldername(SETTINGS.getRenamerSeasonFoldername(), show, season);
  }

  /**
   * generate the season folder name with the given template
   *
   * @param template
   *          the given template
   * @param show
   *          the TV show to generate the season folder for
   * @param season
   *          the season to generate the folder name for
   * @return the folder name of that season
   */
  public static String getSeasonFoldername(String template, TvShow show, int season) {
    String seasonFolderName = template;
    TvShowSeason tvShowSeason = show.getSeason(season);

    // should not happen, but check it
    if (tvShowSeason == null) {
      // return an empty string
      return "";
    }

    // replace all other tokens
    seasonFolderName = createDestination(seasonFolderName, tvShowSeason);

    // only allow empty season dir if the season is in the filename
    if (StringUtils.isBlank(seasonFolderName) && !(SETTINGS.getRenamerFilename().contains("$1") || SETTINGS.getRenamerFilename().contains("$2")
        || SETTINGS.getRenamerFilename().contains("$3") || SETTINGS.getRenamerFilename().contains("$4"))) {
      seasonFolderName = "Season " + String.valueOf(season);
    }
    return seasonFolderName;
  }

  /**
   * generate the TV show folder name according to the settings
   * 
   * @param tvShow
   *          the TV show to generate the folder name for
   * @return the folder name
   */
  public static String getTvShowFoldername(TvShow tvShow) {
    return getTvShowFoldername(SETTINGS.getRenamerTvShowFoldername(), tvShow);
  }

  /**
   * generate the TV show folder name according to the given template
   *
   * @param template
   *          the template to generate the folder name for
   * @param tvShow
   *          the TV show to generate the folder name for
   * @return the folder name
   */
  public static String getTvShowFoldername(String template, TvShow tvShow) {
    String newPathname;

    if (StringUtils.isNotBlank(SETTINGS.getRenamerTvShowFoldername())) {
      newPathname = Paths.get(tvShow.getDataSource(), createDestination(template, tvShow)).toString();
    }
    else {
      newPathname = tvShow.getPathNIO().toString();
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
    try {
      Engine engine = Engine.createEngine();
      engine.registerNamedRenderer(new NamedDateRenderer());
      engine.registerNamedRenderer(new NamedNumberRenderer());
      engine.setModelAdaptor(new TmmModelAdaptor());
      Map<String, Object> root = new HashMap<>();
      root.put("episode", episode);
      root.put("tvShow", show);
      return engine.transform(morphTemplate(token), root);
    }
    catch (Exception e) {
      LOGGER.warn("unable to process token: " + token);
      return token;
    }
  }

  /**
   * Creates the new TV show folder name according to template string
   *
   * @param template
   *          the template string
   * @param show
   *          the TV show to generate the folder name for
   * @return the TV show folder name
   */
  public static String createDestination(String template, TvShow show) {
    if (StringUtils.isBlank(template)) {
      return "";
    }

    return cleanupDestination(getTokenValue(show, null, template));
  }

  /**
   * Creates the new season folder name according to template string
   *
   * @param template
   *          the template string
   * @param season
   *          the season to generate the folder name for
   * @return the season folder name
   */
  public static String createDestination(String template, TvShowSeason season) {
    if (StringUtils.isBlank(template)) {
      return "";
    }

    // create a dummy episode to inject the season number
    TvShowEpisode episode = new TvShowEpisode();
    episode.setSeason(season.getSeason());

    String newDestination = getTokenValue(season.getTvShow(), episode, template);
    // String newDestination = template;
    //
    // // replace all $x parameters
    // Matcher m = token.matcher(template);
    // while (m.find()) {
    // String value = getTokenValue(season.getTvShow(), episode, m.group(1));
    // newDestination = replaceToken(newDestination, m.group(1), value);
    // }

    newDestination = cleanupDestination(newDestination);
    return newDestination;
  }

  /**
   * Creates the new file/folder name according to template string
   * 
   * @param template
   *          the template
   * @param episodes
   *          the TV show episodes; nullable for TV show root foldername
   * @return the string
   */
  public static String createDestination(String template, List<TvShowEpisode> episodes) {
    if (StringUtils.isBlank(template)) {
      return "";
    }

    String newDestination = template;

    if (episodes.size() == 1) {
      // single episode
      TvShowEpisode firstEp = episodes.get(0);

      newDestination = getTokenValue(firstEp.getTvShow(), firstEp, template);
    }
    else {
      // multi episodes
      TvShowEpisode firstEp = episodes.get(0);
      String loopNumbers = "";

      // *******************
      // LOOP 1 - season/episode
      // *******************
      if (StringUtils.isNotBlank(getTokenFromTemplate(newDestination, seasonNumbers))) {
        Matcher matcher = seDelimiter.matcher(newDestination);
        if (matcher.find()) {
          loopNumbers += matcher.group(0);
        }
      }

      if (StringUtils.isNotBlank(getTokenFromTemplate(newDestination, episodeNumbers))) {
        Matcher matcher = epDelimiter.matcher(newDestination);
        if (matcher.find()) {
          loopNumbers += matcher.group(0);
        }
      }
      loopNumbers = loopNumbers.trim();

      // foreach episode, replace and append pattern:
      String episodeParts = "";
      for (TvShowEpisode episode : episodes) {
        String episodePart = getTokenValue(episode.getTvShow(), episode, loopNumbers);
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
      String titleToken = getTokenFromTemplate(template, episodeTitles);
      if (StringUtils.isNotBlank(titleToken)) {
        Pattern pattern = Pattern.compile("\\$\\{" + titleToken + ".*?\\}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(template);
        if (matcher.find()) {
          loopTitles += matcher.group(0);
        }
      }
      loopTitles = loopTitles.trim();

      // foreach episode, replace and append pattern:
      episodeParts = "";
      for (TvShowEpisode episode : episodes) {
        String episodePart = getTokenValue(episode.getTvShow(), episode, loopTitles);

        // separate multiple titles via -
        if (StringUtils.isNotBlank(episodeParts)) {
          episodeParts += " -";
        }
        episodeParts += " " + episodePart;
      }
      // replace original pattern, with our combined
      if (StringUtils.isNotBlank(loopTitles)) {
        newDestination = newDestination.replace(loopTitles, episodeParts);
      }

      newDestination = getTokenValue(firstEp.getTvShow(), firstEp, newDestination);
    } // end multi episodes

    newDestination = cleanupDestination(newDestination);
    return newDestination;
  }

  /**
   * cleanup the destination (remove empty brackets, space substitution, ..)
   * 
   * @param destination
   *          the string to be cleaned up
   * @return the cleaned up string
   */
  private static String cleanupDestination(String destination) {
    // replace empty brackets
    destination = destination.replaceAll("\\(\\)", "");
    destination = destination.replaceAll("\\[\\]", "");

    // if there are multiple file separators in a row - strip them out
    if (SystemUtils.IS_OS_WINDOWS) {
      // we need to mask it in windows
      destination = destination.replaceAll("\\\\{2,}", "\\\\");
      destination = destination.replaceAll("^\\\\", "");
    }
    else {
      destination = destination.replaceAll(File.separator + "{2,}", File.separator);
      destination = destination.replaceAll("^" + File.separator, "");
    }

    // ASCII replacement
    if (SETTINGS.isAsciiReplacement()) {
      destination = StrgUtils.convertToAscii(destination, false);
    }

    // trim out unnecessary whitespaces
    destination = destination.trim();
    destination = destination.replaceAll(" +", " ").trim();

    // any whitespace replacements?
    if (SETTINGS.isRenamerSpaceSubstitution()) {
      destination = destination.replaceAll(" ", SETTINGS.getRenamerSpaceReplacement());
    }

    // replace trailing dots and spaces
    destination = destination.replaceAll("[ \\.]+$", "");

    // replaces all invalid/illegal characters for filenames with "" except the colon, which will be changed to a dash

    destination = destination.replaceAll(": ", " - "); // nicer
    destination = destination.replaceAll(":", "-"); // nicer
    destination = destination.replaceAll("([\"\\\\:<>|/?*])", "");

    return destination.trim();
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
  @Deprecated
  private static int getPatternPos(String pattern, String[] possibleValues) {
    int pos = -1;
    for (String r : possibleValues) {
      if (pattern.contains(r)) {
        pos = pattern.indexOf(r);
      }
    }
    return pos;
  }

  /**
   * returns the first found token from the matched pattern
   *
   * @param template
   *          the template to be searched for
   * @param possibleTokens
   *          the tokens to look for
   * @return the found token or an emtpy string
   */
  private static String getTokenFromTemplate(String template, String[] possibleTokens) {

    for (String token : possibleTokens) {
      if (template.contains("${" + token)) {
        return token;
      }
    }
    return "";
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
