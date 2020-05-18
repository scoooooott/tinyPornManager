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
package org.tinymediamanager.core.tvshow;

import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_BANNER;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_POSTER;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_THUMB;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.jmte.NamedArrayRenderer;
import org.tinymediamanager.core.jmte.NamedDateRenderer;
import org.tinymediamanager.core.jmte.NamedFilesizeRenderer;
import org.tinymediamanager.core.jmte.NamedLowerCaseRenderer;
import org.tinymediamanager.core.jmte.NamedNumberRenderer;
import org.tinymediamanager.core.jmte.NamedTitleCaseRenderer;
import org.tinymediamanager.core.jmte.NamedUpperCaseRenderer;
import org.tinymediamanager.core.jmte.TmmModelAdaptor;
import org.tinymediamanager.core.jmte.ZeroNumberRenderer;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;
import org.tinymediamanager.core.tvshow.filenaming.TvShowEpisodeThumbNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonBannerNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonPosterNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonThumbNaming;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.StrgUtils;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;
import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.token.Token;

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
    tokenMap.put("showOriginalTitle", "tvShow.originalTitle");
    tokenMap.put("showTitleSortable", "tvShow.titleSortable");
    tokenMap.put("showYear", "tvShow.year");
    tokenMap.put("parent", "tvShow.parent");
    tokenMap.put("showNote", "tvShow.note");

    // Season tags
    tokenMap.put("seasonName", "season.title");

    // episode tags
    tokenMap.put("episodeNr", "episode.episode");
    tokenMap.put("episodeNr2", "episode.episode;number(%02d)");
    tokenMap.put("episodeNrDvd", "episode.dvdEpisode");
    tokenMap.put("episodeNrDvd2", "episode.dvdEpisode;number(%02d)");
    tokenMap.put("seasonNr", "episode.season;number(%d)");
    tokenMap.put("seasonNr2", "episode.season;number(%02d)");
    tokenMap.put("seasonNrDvd", "episode.dvdSeason;number(%d)");
    tokenMap.put("seasonNrDvd2", "episode.dvdSeason;number(%02d)");
    tokenMap.put("title", "episode.title");
    tokenMap.put("originalTitle", "episode.originalTitle");
    tokenMap.put("titleSortable", "episode.titleSortable");
    tokenMap.put("year", "episode.year");
    tokenMap.put("airedDate", "episode.firstAired;date(yyyy-MM-dd)");

    tokenMap.put("videoCodec", "episode.mediaInfoVideoCodec");
    tokenMap.put("videoFormat", "episode.mediaInfoVideoFormat");
    tokenMap.put("videoResolution", "episode.mediaInfoVideoResolution");
    tokenMap.put("videoBitDepth", "episode.mediaInfoVideoBitDepth");
    tokenMap.put("audioCodec", "episode.mediaInfoAudioCodec");
    tokenMap.put("audioCodecList", "episode.mediaInfoAudioCodecList");
    tokenMap.put("audioCodecsAsString", "episode.mediaInfoAudioCodecList;array");
    tokenMap.put("audioChannels", "episode.mediaInfoAudioChannels");
    tokenMap.put("audioChannelList", "episode.mediaInfoAudioChannelList");
    tokenMap.put("audioChannelsAsString", "episode.mediaInfoAudioChannelList;array");
    tokenMap.put("audioLanguage", "episode.mediaInfoAudioLanguage");
    tokenMap.put("audioLanguageList", "episode.mediaInfoAudioLanguageList");
    tokenMap.put("audioLanguagesAsString", "episode.mediaInfoAudioLanguageList;array");
    tokenMap.put("subtitleLanguageList", "episode.mediaInfoSubtitleLanguageList");
    tokenMap.put("subtitleLanguagesAsString", "episode.mediaInfoSubtitleLanguageList;array");
    tokenMap.put("3Dformat", "episode.video3DFormat");
    tokenMap.put("hdr", "episode.videoHDRFormat");
    tokenMap.put("filesize", "episode.videoFilesize;filesize");

    tokenMap.put("mediaSource", "episode.mediaSource");
    tokenMap.put("note", "episode.note");

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

    // last but not least escape single backslashes
    morphedTemplate = morphedTemplate.replace("\\", "\\\\");

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
    // skip renamer, if all templates are empty!
    if (SETTINGS.getRenamerFilename().isEmpty() && SETTINGS.getRenamerSeasonFoldername().isEmpty()
        && SETTINGS.getRenamerTvShowFoldername().isEmpty()) {
      LOGGER.info("NOT renaming TvShow '{}' - renaming patterns are empty!", show.getTitle());
      return;
    }

    LOGGER.debug("TV show year: {}", show.getYear());
    LOGGER.debug("TV show path: {}", show.getPathNIO());
    String newPathname = getTvShowFoldername(SETTINGS.getRenamerTvShowFoldername(), show);
    String oldPathname = show.getPathNIO().toString();

    if (!newPathname.isEmpty()) {
      Path srcDir = Paths.get(oldPathname);
      Path destDir = Paths.get(newPathname);
      // move directory if needed
      if (!srcDir.toAbsolutePath().toString().equals(destDir.toAbsolutePath().toString())) {
        try {
          // ######################################################################
          // ## invalidate image cache
          // ######################################################################
          for (MediaFile gfx : show.getMediaFiles()) {
            ImageCache.invalidateCachedImage(gfx);
          }

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

            // ######################################################################
            // ## build up image cache
            // ######################################################################
            if (Settings.getInstance().isImageCache()) {
              for (MediaFile gfx : show.getMediaFiles()) {
                ImageCache.cacheImageSilently(gfx);
              }
            }
          }
        }
        catch (Exception e) {
          LOGGER.error("error moving folder: {}", e.getMessage());
          MessageManager.instance
              .pushMessage(new Message(MessageLevel.ERROR, srcDir, "message.renamer.failedrename", new String[] { ":", e.getLocalizedMessage() }));
        }
      }
    }

    // also rename the season artwork
    renameSeasonArtwork(show);
  }

  /**
   * rename the season artwork for this TV show
   * 
   * @param tvShow
   *          the TV show to rename the season artwork for
   */
  private static void renameSeasonArtwork(TvShow tvShow) {
    // all the good & needed mediafiles
    Set<MediaFile> needed = new LinkedHashSet<>();
    ArrayList<MediaFile> cleanup = new ArrayList<>();

    List<MediaArtworkType> types = Arrays.asList(SEASON_POSTER, SEASON_BANNER, SEASON_THUMB);

    for (MediaArtworkType type : types) {
      Map<Integer, MediaFile> artwork = tvShow.getSeasonArtworks(type);
      for (Map.Entry<Integer, MediaFile> entry : artwork.entrySet()) {
        Integer season = entry.getKey();
        MediaFile mf = entry.getValue();

        cleanup.add(mf);

        String filename;
        switch (type) {
          case SEASON_POSTER:
            for (TvShowSeasonPosterNaming naming : SETTINGS.getSeasonPosterFilenames()) {
              filename = naming.getFilename(tvShow, season, mf.getExtension());
              if (StringUtils.isNotBlank(filename)) {
                MediaFile newMf = new MediaFile(mf);
                newMf.setFile(Paths.get(tvShow.getPath(), filename));
                boolean ok = copyFile(mf.getFileAsPath(), newMf.getFileAsPath());
                if (ok) {
                  needed.add(newMf);
                }
              }
            }
            break;

          case SEASON_BANNER:
            for (TvShowSeasonBannerNaming naming : SETTINGS.getSeasonBannerFilenames()) {
              filename = naming.getFilename(tvShow, season, mf.getExtension());
              if (StringUtils.isNotBlank(filename)) {
                MediaFile newMf = new MediaFile(mf);
                newMf.setFile(Paths.get(tvShow.getPath(), filename));
                boolean ok = copyFile(mf.getFileAsPath(), newMf.getFileAsPath());
                if (ok) {
                  needed.add(newMf);
                }
              }
            }
            break;

          case SEASON_THUMB:
            for (TvShowSeasonThumbNaming naming : SETTINGS.getSeasonThumbFilenames()) {
              filename = naming.getFilename(tvShow, season, mf.getExtension());
              if (StringUtils.isNotBlank(filename)) {
                MediaFile newMf = new MediaFile(mf);
                newMf.setFile(Paths.get(tvShow.getPath(), filename));
                boolean ok = copyFile(mf.getFileAsPath(), newMf.getFileAsPath());
                if (ok) {
                  needed.add(newMf);
                }
              }
            }
            break;

          default:
            continue;
        }
      }
    }

    // ######################################################################
    // ## invalidate image cache
    // ######################################################################
    for (MediaFile gfx : tvShow.getMediaFiles()) {
      ImageCache.invalidateCachedImage(gfx);
    }

    // ######################################################################
    // ## CLEANUP - delete all files marked for cleanup, which are not "needed"
    // ######################################################################
    LOGGER.info("Cleanup...");
    List<Path> existingFiles = Utils.listFilesRecursive(tvShow.getPathNIO());
    for (int i = cleanup.size() - 1; i >= 0; i--) {
      // cleanup files which are not needed
      if (!needed.contains(cleanup.get(i))) {
        MediaFile cl = cleanup.get(i);
        if (existingFiles.contains(cl.getFileAsPath())) {
          LOGGER.debug("Deleting {}", cl.getFileAsPath());
          Utils.deleteFileWithBackup(cl.getFileAsPath(), tvShow.getDataSource());
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(cl.getFileAsPath().getParent())) {
          if (!directoryStream.iterator().hasNext()) {
            // no iterator = empty
            LOGGER.debug("Deleting empty Directory {}", cl.getFileAsPath().getParent());
            Files.delete(cl.getFileAsPath().getParent()); // do not use recursive her
          }
        }
        catch (IOException e) {
          LOGGER.error("cleanup of {} - {}", cl.getFileAsPath(), e.getMessage());
        }
      }
    }

    // delete empty subfolders
    try {
      Utils.deleteEmptyDirectoryRecursive(tvShow.getPathNIO());
    }
    catch (Exception e) {
      LOGGER.warn("could not delete empty subfolders: {}", e.getMessage());
    }

    // ######################################################################
    // ## build up image cache
    // ######################################################################
    if (Settings.getInstance().isImageCache()) {
      for (MediaFile gfx : tvShow.getMediaFiles()) {
        ImageCache.cacheImageSilently(gfx);
      }
    }

    tvShow.addToMediaFiles(new ArrayList<>(needed));
    tvShow.saveToDb();
  }

  /**
   * Rename Episode (PLUS all Episodes having the same MediaFile!!!).
   * 
   * @param episode
   *          the Episode
   */
  public static void renameEpisode(TvShowEpisode episode) {
    // skip renamer, if all episode related templates are empty!
    if (SETTINGS.getRenamerFilename().isEmpty() && SETTINGS.getRenamerSeasonFoldername().isEmpty()) {
      LOGGER.info("NOT renaming TvShow '{}' Episode {} - renaming patterns are empty!", episode.getTvShow().getTitle(), episode.getEpisode());
      return;
    }

    MediaFile originalVideoMediaFile = new MediaFile(episode.getMainVideoFile());
    // test for valid season/episode number
    if (episode.getSeason() < 0 || episode.getEpisode() < 0) {
      LOGGER.warn("failed to rename episode {} (TV show {}) - invalid season/episode number", episode.getTitle(), episode.getTvShow().getTitle());
      MessageManager.instance.pushMessage(
          new Message(MessageLevel.ERROR, episode.getTvShow().getTitle(), "tvshow.renamer.failedrename", new String[] { episode.getTitle() }));
      return;
    }

    LOGGER.info("Renaming TvShow '{}', Episode {}", episode.getTvShow().getTitle(), episode.getEpisode());

    if (episode.isDisc()) {
      renameEpisodeAsDisc(episode);
      return;
    }

    // all the good & needed mediafiles
    ArrayList<MediaFile> needed = new ArrayList<>();
    ArrayList<MediaFile> cleanup = new ArrayList<>(episode.getMediaFiles());
    cleanup.removeAll(Collections.singleton((MediaFile) null)); // remove all NULL ones!

    String seasonFoldername = getSeasonFoldername(episode.getTvShow(), episode);
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
      LOGGER.trace("Rename 1:1 {} {}", vid.getType(), vid.getFileAsPath());
      MediaFile newMF = generateEpisodeFilenames(episode.getTvShow(), vid).get(0); // there can be only one
      boolean ok = moveFile(vid.getFileAsPath(), newMF.getFileAsPath());
      if (ok) {
        vid.setFile(newMF.getFileAsPath()); // update
        // if we move the episode in its own folder, we might need to upgrade the path as well!
        episode.setPath(newMF.getPath());
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
    mfs.add(episode.getNewestMediaFilesOfType(MediaFileType.CHARACTERART));
    mfs.add(episode.getNewestMediaFilesOfType(MediaFileType.KEYART));
    mfs.removeAll(Collections.singleton((MediaFile) null)); // remove all NULL ones!
    for (MediaFile mf : mfs) {
      LOGGER.trace("Rename 1:N {} {}", mf.getType(), mf.getFileAsPath());
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
      if (!newNFOs.isEmpty()) {
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
      LOGGER.trace("Rename 1:1 {} {}", subtitle.getType(), subtitle.getFileAsPath());
      MediaFile sub = generateEpisodeFilenames(episode.getTvShow(), subtitle).get(0); // there can be only one
      boolean ok = moveFile(subtitle.getFileAsPath(), sub.getFileAsPath());
      if (ok) {
        if (sub.getFilename().endsWith(".sub")) {
          // when having a .sub, also rename .idx (don't care if error)
          try {
            Path oldidx = subtitle.getFileAsPath().resolveSibling(subtitle.getFilename().replaceFirst("sub$", "idx"));
            Path newidx = sub.getFileAsPath().resolveSibling(sub.getFilename().replaceFirst("sub$", "idx"));
            Utils.moveFileSafe(oldidx, newidx);
          }
          catch (Exception e) {
            // no idx found or error - ignore
          }
        }
        subtitle.setFile(sub.getFileAsPath()); // update
      }
      needed.add(subtitle); // add vid, since we're updating existing MF object
    }

    // ######################################################################
    // ## rename all other types (copy 1:1)
    // ######################################################################
    mfs = new ArrayList<>(episode.getMediaFilesExceptType(MediaFileType.VIDEO, MediaFileType.NFO, MediaFileType.POSTER, MediaFileType.FANART,
        MediaFileType.BANNER, MediaFileType.CLEARART, MediaFileType.THUMB, MediaFileType.LOGO, MediaFileType.CLEARLOGO, MediaFileType.DISC,
        MediaFileType.CHARACTERART, MediaFileType.KEYART, MediaFileType.SUBTITLE));
    mfs.removeAll(Collections.singleton((MediaFile) null)); // remove all NULL ones!
    for (MediaFile other : mfs) {
      LOGGER.trace("Rename 1:1 {} - {}", other.getType(), other.getFileAsPath());

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
    // ## invalidate image cache
    // ######################################################################
    for (MediaFile gfx : episode.getMediaFiles()) {
      ImageCache.invalidateCachedImage(gfx);
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
          LOGGER.debug("Deleting {}", cl.getFileAsPath());
          Utils.deleteFileWithBackup(cl.getFileAsPath(), episode.getTvShow().getDataSource());
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(cl.getFileAsPath().getParent())) {
          if (!directoryStream.iterator().hasNext()) {
            // no iterator = empty
            LOGGER.debug("Deleting empty Directory {}", cl.getFileAsPath().getParent());
            Files.delete(cl.getFileAsPath().getParent()); // do not use recursive her
          }
        }
        catch (IOException e) {
          LOGGER.error("cleanup of {} - {}", cl.getFileAsPath(), e.getMessage());
        }
      }
    }

    // update paths/mfs for the episode
    List<TvShowEpisode> eps = new ArrayList<>();
    eps.add(episode);

    // if the files are multi EP files, change all other episodes too
    eps.addAll(TvShowList.getTvEpisodesByFile(episode.getTvShow(), originalVideoMediaFile.getFile()));
    for (TvShowEpisode e : eps) {
      e.removeAllMediaFiles();
      e.addToMediaFiles(needed);
      e.setPath(episode.getPath());
      e.gatherMediaFileInformation(false);
      e.saveToDb();

      // ######################################################################
      // ## build up image cache
      // ######################################################################
      if (Settings.getInstance().isImageCache()) {
        for (MediaFile gfx : e.getMediaFiles()) {
          ImageCache.cacheImageSilently(gfx);
        }
      }
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
    MediaFile mf = episode.getMainVideoFile();
    List<TvShowEpisode> eps = TvShowList.getTvEpisodesByFile(episode.getTvShow(), mf.getFile());

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
    String seasonFoldername = getSeasonFoldername(episode.getTvShow(), episode);
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
          LOGGER.debug("updating *all* MFs for new path -> {}", newEpFolder);
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
        LOGGER.debug("Deleting empty Directory - {}", dir);
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
    List<TvShowEpisode> eps = TvShowList.getTvEpisodesByFile(tvShow, mf.getFile());
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

    List<TvShowEpisode> eps = TvShowList.getTvEpisodesByFile(tvShow, mf.getFile());
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

    String seasonFoldername = getSeasonFoldername(tvShow, eps.get(0));
    Path seasonFolder = tvShow.getPathNIO();
    if (StringUtils.isNotBlank(seasonFoldername)) {
      seasonFolder = tvShow.getPathNIO().resolve(seasonFoldername);
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
        if (subtitles != null && !subtitles.isEmpty()) {
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

        if (StringUtils.isBlank(subtitleFilename)) {
          // detect from filename, if we don't have a MediaFileSubtitle entry or could not create a file name out of it!
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

          try {
            for (String s : LanguageUtils.KEY_TO_LOCALE_MAP.keySet()) {
              if (LanguageUtils.doesStringEndWithLanguage(shortname, s)) {
                originalLang = s;
                // lang = Utils.getIso3LanguageFromLocalizedString(s);
                // LOGGER.debug("found language '" + s + "' in subtitle; displaying it as '" + lang + "'");
                break;
              }
            }
          }
          catch (Exception e) {
            e.printStackTrace();
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

        // still no subtitle filename? take at least the whole filename
        if (StringUtils.isBlank(subtitleFilename)) {
          subtitleFilename = newFilename;
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
        String videoExt = FilenameUtils.getExtension(FilenameUtils.getBaseName(mf.getFilename()));
        newFilename += "." + videoExt + ".vsmeta";
        vsmeta.setFile(seasonFolder.resolve(newFilename));
        newFiles.add(vsmeta);
        break;

      ////////////////////////////////////////////////////////////////////////
      // VIDEO_EXTRA
      ////////////////////////////////////////////////////////////////////////
      case EXTRA:
      case VIDEO_EXTRA:
        // this extra is for an episode -> move it at least to the season folder and try to replace the episode tokens
        MediaFile extra = new MediaFile(mf);
        // try to detect the title of the extra file
        TvShowEpisodeAndSeasonParser.EpisodeMatchingResult result = TvShowEpisodeAndSeasonParser
            .detectEpisodeFromFilenameAlternative(mf.getFilename(), tvShow.getTitle());
        extra.setFile(seasonFolder.resolve("extras/" + newFilename + "-" + result.cleanedName + "." + mf.getExtension()));
        newFiles.add(extra);
        break;

      ////////////////////////////////////////////////////////////////////////
      // SAMPLE
      ////////////////////////////////////////////////////////////////////////
      case SAMPLE:
        MediaFile sample = new MediaFile(mf);
        sample.setFile(seasonFolder.resolve(newFilename + "-sample." + mf.getExtension()));
        newFiles.add(sample);
        break;

      ////////////////////////////////////////////////////////////////////////
      // AUDIO / TEXT / UNKNOWN / VIDEO_EXTRA
      // take the unknown part of the file name and attach it to the new file name
      ////////////////////////////////////////////////////////////////////////
      case AUDIO:
      case TEXT:
      case UNKNOWN:
        // this is something extra for an episode -> try to replace the episode tokens and preserve the extra in the filename
        // try to detect the title of the extra file
        result = TvShowEpisodeAndSeasonParser.detectEpisodeFromFilenameAlternative(mf.getFilename(), tvShow.getTitle());

        MediaFile other = new MediaFile(mf);
        boolean spaceSubstitution = SETTINGS.isRenamerFilenameSpaceSubstitution();
        String spaceReplacement = SETTINGS.getRenamerFilenameSpaceReplacement();
        String destination = cleanupDestination(newFilename + "-" + result.cleanedName, spaceSubstitution, spaceReplacement);
        other.setFile(seasonFolder.resolve(destination + "." + mf.getExtension()));
        newFiles.add(other);
        break;

      // missing enums
      case BANNER:
      case CLEARART:
      case CLEARLOGO:
      case DISC:
      case EXTRAFANART:
      case EXTRATHUMB:
      case GRAPHIC:
      case LOGO:
      case POSTER:
      case CHARACTERART:
      case KEYART:
      case SEASON_POSTER:
      case SEASON_BANNER:
      case SEASON_THUMB:
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
   * @param episode
   *          the episode to generate the season folder name for
   * @return the folder name of that season
   */
  public static String getSeasonFoldername(TvShow show, TvShowEpisode episode) {
    return getSeasonFoldername(SETTINGS.getRenamerSeasonFoldername(), show, episode);
  }

  /**
   * generate the season folder name with the given template
   *
   * @param template
   *          the given template
   * @param show
   *          the TV show to generate the season folder for
   * @param episode
   *          the episode to generate the season folder name for
   * @return the folder name of that season
   */
  public static String getSeasonFoldername(String template, TvShow show, TvShowEpisode episode) {
    String seasonFolderName = template;
    TvShowSeason tvShowSeason = show.getSeason(episode.getSeason());

    // should not happen, but check it
    if (tvShowSeason == null) {
      // return an empty string
      return "";
    }

    // season 0 = Specials
    if (tvShowSeason.getSeason() == 0 && TvShowModuleManager.SETTINGS.isSpecialSeason()) {
      seasonFolderName = "Specials";
    }
    else {
      // replace all other tokens
      seasonFolderName = createDestination(seasonFolderName, tvShowSeason, episode);
    }

    // only allow empty season dir if the season is in the filename (aka recommended)
    if (StringUtils.isBlank(seasonFolderName) && !TvShowRenamer.isRecommended(template, TvShowModuleManager.SETTINGS.getRenamerFilename())) {
      seasonFolderName = "Season " + tvShowSeason.getSeason();
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
      engine.registerRenderer(Number.class, new ZeroNumberRenderer());
      engine.registerNamedRenderer(new NamedDateRenderer());
      engine.registerNamedRenderer(new NamedNumberRenderer());
      engine.registerNamedRenderer(new NamedUpperCaseRenderer());
      engine.registerNamedRenderer(new NamedLowerCaseRenderer());
      engine.registerNamedRenderer(new NamedTitleCaseRenderer());
      engine.registerNamedRenderer(new TvShowNamedFirstCharacterRenderer());
      engine.registerNamedRenderer(new NamedArrayRenderer());
      engine.registerNamedRenderer(new NamedFilesizeRenderer());
      engine.setModelAdaptor(new TvShowRenamerModelAdaptor());
      Map<String, Object> root = new HashMap<>();
      if (episode != null) {
        root.put("episode", episode);
        root.put("season", episode.getTvShowSeason());
      }
      root.put("tvShow", show);
      return engine.transform(morphTemplate(token), root);
    }
    catch (Exception e) {
      LOGGER.warn("unable to process token: {}", token);
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

    boolean spaceSubstitution = SETTINGS.isRenamerShowPathnameSpaceSubstitution();
    String spaceReplacement = SETTINGS.getRenamerShowPathnameSpaceReplacement();

    return cleanupDestination(getTokenValue(show, null, template), spaceSubstitution, spaceReplacement);
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
  public static String createDestination(String template, TvShowSeason season, TvShowEpisode episode) {
    if (StringUtils.isBlank(template)) {
      return "";
    }

    String newDestination = getTokenValue(season.getTvShow(), episode, template);
    boolean spaceSubstitution = SETTINGS.isRenamerSeasonPathnameSpaceSubstitution();
    String spaceReplacement = SETTINGS.getRenamerSeasonPathnameSpaceReplacement();

    newDestination = cleanupDestination(newDestination, spaceSubstitution, spaceReplacement);
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
      String seasonToken = getTokenFromTemplate(newDestination, seasonNumbers);
      if (StringUtils.isNotBlank(seasonToken)) {
        String seasonPart = "";
        Matcher matcher = seDelimiter.matcher(newDestination);
        if (matcher.find()) {
          seasonPart = matcher.group(0);
        }
        else {
          // no season info found? search for the token itself
          Pattern pattern = Pattern.compile("\\$\\{" + seasonToken + ".*?\\}");
          matcher = pattern.matcher(newDestination);
          if (matcher.find()) {
            seasonPart = matcher.group(0);
          }
        }
        loopNumbers += seasonPart;
      }

      String episodeToken = getTokenFromTemplate(newDestination, episodeNumbers);
      if (StringUtils.isNotBlank(episodeToken)) {
        String episodePart = "";
        Matcher matcher = epDelimiter.matcher(newDestination);
        if (matcher.find()) {
          episodePart += matcher.group(0);
        }
        else {
          // no episode info found? search for the token itself
          Pattern pattern = Pattern.compile("\\$\\{" + episodeToken + ".*?\\}");
          matcher = pattern.matcher(newDestination);
          if (matcher.find()) {
            episodePart = matcher.group(0);
          }
        }

        loopNumbers += episodePart;
      }
      loopNumbers = loopNumbers.trim();

      // foreach episode, replace and append pattern:
      StringBuilder episodeParts = new StringBuilder();
      for (TvShowEpisode episode : episodes) {
        String episodePart = getTokenValue(episode.getTvShow(), episode, loopNumbers);
        episodeParts.append(" ").append(episodePart);
      }

      // replace original pattern, with our combined
      if (!loopNumbers.isEmpty()) {
        newDestination = newDestination.replace(loopNumbers, episodeParts.toString());
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
      episodeParts = new StringBuilder();
      for (TvShowEpisode episode : episodes) {
        String episodePart = getTokenValue(episode.getTvShow(), episode, loopTitles);

        // separate multiple titles via -
        if (StringUtils.isNotBlank(episodeParts.toString())) {
          episodeParts.append(" -");
        }
        episodeParts.append(" ").append(episodePart);
      }
      // replace original pattern, with our combined
      if (StringUtils.isNotBlank(loopTitles)) {
        newDestination = newDestination.replace(loopTitles, episodeParts.toString());
      }

      newDestination = getTokenValue(firstEp.getTvShow(), firstEp, newDestination);
    } // end multi episodes

    boolean spaceSubstitution = SETTINGS.isRenamerFilenameSpaceSubstitution();
    String spaceReplacement = SETTINGS.getRenamerFilenameSpaceReplacement();

    newDestination = cleanupDestination(newDestination, spaceSubstitution, spaceReplacement);

    return newDestination;
  }

  /**
   * cleanup the destination (remove empty brackets, space substitution, ..)
   * 
   * @param destination
   *          the string to be cleaned up
   * @param spaceSubstitution
   *          replace spaces (=true)? or not (=false)
   * @param spaceReplacement
   *          the replacement string for spaces
   * @return the cleaned up string
   */
  private static String cleanupDestination(String destination, Boolean spaceSubstitution, String spaceReplacement) {
    // replace empty brackets
    destination = destination.replaceAll("\\([ ]?\\)", "");
    destination = destination.replaceAll("\\[[ ]?\\]", "");
    destination = destination.replaceAll("\\{[ ]?\\}", "");

    // if there are multiple file separators in a row - strip them out
    if (SystemUtils.IS_OS_WINDOWS) {
      // we need to mask it in windows
      destination = destination.replaceAll("\\\\{2,}", "\\\\");
      destination = destination.replaceAll("^\\\\", "");
      // trim whitespace around directory sep
      destination = destination.replaceAll("\\s+\\\\", "\\\\");
      destination = destination.replaceAll("\\\\\\s+", "\\\\");
      // remove separators in front of path separators
      destination = destination.replaceAll("[ \\.\\-_]+\\\\", "\\\\");
    }
    else {
      destination = destination.replaceAll(File.separator + "{2,}", File.separator);
      destination = destination.replaceAll("^" + File.separator, "");
      // trim whitespace around directory sep
      destination = destination.replaceAll("\\s+/", "/");
      destination = destination.replaceAll("/\\s+", "/");
      // remove separators in front of path separators
      destination = destination.replaceAll("[ \\.\\-_]+/", "/");
    }

    // replace spaces with underscores if needed (filename only)
    if (spaceSubstitution) {
      destination = destination.replace(" ", spaceReplacement);

      // also replace now multiple replacements with one to avoid strange looking results
      // example:
      // Abraham Lincoln - Vampire Hunter -> Abraham-Lincoln---Vampire-Hunter
      destination = destination.replaceAll(Pattern.quote(spaceReplacement) + "+", spaceReplacement);
    }

    // ASCII replacement
    if (SETTINGS.isAsciiReplacement()) {
      destination = StrgUtils.convertToAscii(destination, false);
    }

    // trim out unnecessary whitespaces
    destination = destination.trim();
    destination = destination.replaceAll(" +", " ").trim();

    // replace all leading/trailing separators
    destination = destination.replaceAll("^[ \\.\\-_]+", "");
    destination = destination.replaceAll("[ \\.\\-_]+$", "");

    // the colon is handled by JMTE but it looks like some users are stupid enough to add this to the pattern itself
    destination = destination.replace(": ", " - "); // nicer
    destination = destination.replace(":", "-"); // nicer

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
      if (containsToken(pattern, r)) {
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
      if (containsToken(pattern, r)) {
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
      if (containsToken(template, token)) {
        return token;
      }
    }
    return "";
  }

  private static boolean containsToken(String template, String token) {
    Pattern pattern = Pattern.compile("\\$\\{" + token + "[\\};]");
    Matcher matcher = pattern.matcher(template);
    return matcher.find();
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
    if (TvShowModuleManager.SETTINGS.isRenamerFilenameSpaceSubstitution()) {
      delimiter = TvShowModuleManager.SETTINGS.getRenamerFilenameSpaceReplacement();
    }
    if (!mf.getStackingMarker().isEmpty()) {
      return delimiter + mf.getStackingMarker();
    }
    else if (mf.getStacking() != 0) {
      return delimiter + "CD" + mf.getStacking();
    }
    return "";
  }

  public static String replaceInvalidCharacters(String source) {
    String result = source;

    if ("-".equals(TvShowModuleManager.SETTINGS.getRenamerColonReplacement())) {
      result = result.replaceAll(": ", " - "); // nicer
      result = result.replaceAll(":", "-"); // nicer
    }
    else {
      result = result.replaceAll(":", TvShowModuleManager.SETTINGS.getRenamerColonReplacement());
    }

    return result.replaceAll("([\":<>|?*])", "");
  }

  /**
   * checks supplied renamer pattern against our tokenmap, if everything could be found
   * 
   * @param pattern
   * @return error string, what token(s) are wrong
   */
  public static String isPatternValid(String pattern) {
    String err = "";
    Pattern p = Pattern.compile("\\$\\{(.*?)\\}");
    Matcher matcher = p.matcher(pattern);
    while (matcher.find()) {
      String fulltoken = matcher.group(1);
      String token = "";
      if (fulltoken.contains(",")) {
        // split additional like ${-,token,replace}
        String[] split = fulltoken.split(",");
        token = split[1];
      }
      else if (fulltoken.contains("[")) {
        // strip all after parenthesis
        token = fulltoken.substring(0, fulltoken.indexOf('['));
      }
      else if (fulltoken.contains(";")) {
        // strip all after semicolon like ${title;first}
        token = fulltoken.substring(0, fulltoken.indexOf(';'));
        String first = fulltoken.substring(fulltoken.indexOf(';') + 1);
        if (!first.equals("first")) {
          err += "  " + matcher.group(); // "first" is missing
        }
      }
      else {
        token = fulltoken;
      }
      String tok = TOKEN_MAP.get(token.trim());
      if (tok == null) {
        err += "  " + matcher.group(); // complete token with ${}
      }
    }
    return err;
  }

  public static class TvShowRenamerModelAdaptor extends TmmModelAdaptor {
    @Override
    public Object getValue(Map<String, Object> model, String expression) {
      Object value = super.getValue(model, expression);

      if (value instanceof String) {
        value = replaceInvalidCharacters((String) value);
      }

      return value;
    }

    @Override
    public Object getValue(TemplateContext context, Token token, List<String> segments, String expression) {
      Object value = super.getValue(context, token, segments, expression);

      if (value instanceof String) {
        value = replaceInvalidCharacters((String) value);

        // do not replace path separators on the .parent token
        if (!token.getText().contains("parent")) {
          value = ((String) value).replaceAll("\\/", " ");
          value = ((String) value).replaceAll("\\\\", " ");

        }
      }

      return value;
    }
  }

  public static class TvShowNamedFirstCharacterRenderer implements NamedRenderer {

    @Override
    public String render(Object o, String s, Locale locale, Map<String, Object> map) {
      if (o instanceof String && StringUtils.isNotBlank((String) o)) {
        String source = (String) o;
        if (TvShowModuleManager.SETTINGS.isAsciiReplacement()) {
          source = StrgUtils.convertToAscii(source, false);
        }
        String first = source.trim().substring(0, 1);
        if (first.matches("[\\p{L}]")) {
          return first.toUpperCase(Locale.ROOT);
        }
        return "#";
      }
      if (o instanceof Number) {
        return "#";
      }
      if (o instanceof Date) {
        return "#";
      }
      return "";
    }

    @Override
    public String getName() {
      return "first";
    }

    @Override
    public RenderFormatInfo getFormatInfo() {
      return null;
    }

    @Override
    public Class<?>[] getSupportedClasses() {
      return new Class[] { Date.class, String.class, Integer.class, Long.class };
    }
  }
}
