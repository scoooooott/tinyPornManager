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
package org.tinymediamanager.core.movie;

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
import org.tinymediamanager.core.IFileNaming;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.LanguageStyle;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.jmte.NamedArrayRenderer;
import org.tinymediamanager.core.jmte.NamedDateRenderer;
import org.tinymediamanager.core.jmte.NamedFilesizeRenderer;
import org.tinymediamanager.core.jmte.NamedLowerCaseRenderer;
import org.tinymediamanager.core.jmte.NamedTitleCaseRenderer;
import org.tinymediamanager.core.jmte.NamedUpperCaseRenderer;
import org.tinymediamanager.core.jmte.TmmModelAdaptor;
import org.tinymediamanager.core.jmte.ZeroNumberRenderer;
import org.tinymediamanager.core.movie.connector.MovieConnectors;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.filenaming.MovieBannerNaming;
import org.tinymediamanager.core.movie.filenaming.MovieClearartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieClearlogoNaming;
import org.tinymediamanager.core.movie.filenaming.MovieDiscartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieFanartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieKeyartNaming;
import org.tinymediamanager.core.movie.filenaming.MovieLogoNaming;
import org.tinymediamanager.core.movie.filenaming.MovieNfoNaming;
import org.tinymediamanager.core.movie.filenaming.MoviePosterNaming;
import org.tinymediamanager.core.movie.filenaming.MovieThumbNaming;
import org.tinymediamanager.core.movie.filenaming.MovieTrailerNaming;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.ListUtils;
import org.tinymediamanager.scraper.util.StrgUtils;

import com.floreysoft.jmte.Engine;
import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.RenderFormatInfo;
import com.floreysoft.jmte.TemplateContext;
import com.floreysoft.jmte.token.Token;

/**
 * The Class MovieRenamer.
 * 
 * @author Manuel Laggner / Myron Boyle
 */
public class MovieRenamer {
  private static final Logger             LOGGER                      = LoggerFactory.getLogger(MovieRenamer.class);
  private static final List<String>       KNOWN_IMAGE_FILE_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "bmp", "tbn", "gif");

  // to not use posix here
  private static final Pattern            ALPHANUM                    = Pattern.compile(".*?([a-zA-Z0-9]{1}).*$");
  private static final Pattern            TITLE_PATTERN               = Pattern.compile("\\$\\{.*?title.*?\\}", Pattern.CASE_INSENSITIVE);
  private static final Pattern            YEAR_ID_PATTERN             = Pattern.compile("\\$\\{.*?(year|imdb|tmdb).*?\\}", Pattern.CASE_INSENSITIVE);

  public static final Map<String, String> TOKEN_MAP                   = createTokenMap();

  private MovieRenamer() {
    // hide public constructor for utility classes
  }

  /**
   * initialize the token map for the renamer
   *
   * @return the token map
   */
  private static Map<String, String> createTokenMap() {
    Map<String, String> tokenMap = new HashMap<>();
    tokenMap.put("title", "movie.title");
    tokenMap.put("originalTitle", "movie.originalTitle");
    tokenMap.put("sorttitle", "movie.sortTitle");
    tokenMap.put("year", "movie.year");
    tokenMap.put("releaseDate", "movie.releaseDate;date(yyyy-MM-dd)");
    tokenMap.put("titleSortable", "movie.titleSortable");
    tokenMap.put("rating", "movie.rating.rating");
    tokenMap.put("imdb", "movie.imdbId");
    tokenMap.put("certification", "movie.certification");
    tokenMap.put("language", "movie.spokenLanguages");

    tokenMap.put("genres", "movie.genres");
    tokenMap.put("genresAsString", "movie.genresAsString");
    tokenMap.put("tags", "movie.tags");
    tokenMap.put("actors", "movie.actors");
    tokenMap.put("producers", "movie.producers");
    tokenMap.put("directors", "movie.directors");
    tokenMap.put("writers", "movie.writers");

    tokenMap.put("videoCodec", "movie.mediaInfoVideoCodec");
    tokenMap.put("videoFormat", "movie.mediaInfoVideoFormat");
    tokenMap.put("videoResolution", "movie.mediaInfoVideoResolution");
    tokenMap.put("videoBitDepth", "movie.mediaInfoVideoBitDepth");
    tokenMap.put("audioCodec", "movie.mediaInfoAudioCodec");
    tokenMap.put("audioCodecList", "movie.mediaInfoAudioCodecList");
    tokenMap.put("audioCodecsAsString", "movie.mediaInfoAudioCodecList;array");
    tokenMap.put("audioChannels", "movie.mediaInfoAudioChannels");
    tokenMap.put("audioChannelList", "movie.mediaInfoAudioChannelList");
    tokenMap.put("audioChannelsAsString", "movie.mediaInfoAudioChannelList;array");
    tokenMap.put("audioLanguage", "movie.mediaInfoAudioLanguage");
    tokenMap.put("audioLanguageList", "movie.mediaInfoAudioLanguageList");
    tokenMap.put("audioLanguagesAsString", "movie.mediaInfoAudioLanguageList;array");
    tokenMap.put("subtitleLanguageList", "movie.mediaInfoSubtitleLanguageList");
    tokenMap.put("subtitleLanguagesAsString", "movie.mediaInfoSubtitleLanguageList;array");
    tokenMap.put("3Dformat", "movie.video3DFormat");
    tokenMap.put("hdr", "movie.videoHDRFormat");
    tokenMap.put("filesize", "movie.videoFilesize;filesize");

    tokenMap.put("mediaSource", "movie.mediaSource");
    tokenMap.put("edition", "movie.edition");
    tokenMap.put("parent", "movie.parent");
    tokenMap.put("note", "movie.note");

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

  private static void renameSubtitles(Movie m) {
    // build language lists
    Set<String> langArray = LanguageUtils.KEY_TO_LOCALE_MAP.keySet();
    List<MediaFile> subtitleFiles = m.getMediaFiles(MediaFileType.SUBTITLE);

    for (MediaFile sub : subtitleFiles) {
      String originalLang = "";
      String lang = "";
      String forced = "";
      List<MediaFileSubtitle> mfsl = sub.getSubtitles();

      if (mfsl != null && !mfsl.isEmpty()) {
        // use internal values
        MediaFileSubtitle mfs = mfsl.get(0);
        originalLang = mfs.getLanguage();
        if (mfs.isForced()) {
          forced = ".forced";
        }
      }
      else {
        // detect from filename, if we don't have a MediaFileSubtitle entry!
        // remove the filename of movie from subtitle, to ease parsing
        List<MediaFile> mfs = m.getMediaFiles(MediaFileType.VIDEO);
        String shortname = sub.getBasename().toLowerCase(Locale.ROOT);
        if (ListUtils.isNotEmpty(mfs)) {
          shortname = sub.getBasename().toLowerCase(Locale.ROOT).replace(m.getVideoBasenameWithoutStacking(), "");
        }

        if (sub.getFilename().toLowerCase(Locale.ROOT).contains("forced")) {
          // add "forced" prior language
          forced = ".forced";
          shortname = shortname.replaceAll("\\p{Punct}*forced", "");
        }
        // shortname = shortname.replaceAll("\\p{Punct}", "").trim(); // NEVER EVER!!!

        for (String s : langArray) {
          if (LanguageUtils.doesStringEndWithLanguage(shortname, s)) {
            originalLang = s;
            LOGGER.trace("found language '{}' in subtitle", s);
            break;
          }
        }
      }

      // check if there is only one subtitle file and the user wants to write this w/o the language tag
      if (!MovieModuleManager.SETTINGS.isSubtitleWithoutLanguageTag() || subtitleFiles.size() > 1) {
        lang = LanguageStyle.getLanguageCodeForStyle(originalLang, MovieModuleManager.SETTINGS.getSubtitleLanguageStyle());
        if (StringUtils.isBlank(lang)) {
          lang = originalLang;
        }
      }
      else {
        lang = "";
      }

      // rebuild new filename
      String newSubName = "";

      if (sub.getStacking() == 0) {
        // fine, so match to first movie file
        MediaFile mf = m.getMediaFiles(MediaFileType.VIDEO).get(0);
        newSubName = mf.getBasename();
        if (!lang.isEmpty()) {
          newSubName += "." + lang;
        }
        newSubName += forced;
      }
      else {
        // with stacking info; try to match
        for (MediaFile mf : m.getMediaFiles(MediaFileType.VIDEO)) {
          if (mf.getStacking() == sub.getStacking()) {
            newSubName = mf.getBasename();
            if (!lang.isEmpty()) {
              newSubName += "." + lang;
            }
            newSubName += forced;
          }
        }
      }
      newSubName += "." + sub.getExtension();

      Path newFile = m.getPathNIO().resolve(newSubName);
      try {
        boolean ok = Utils.moveFileSafe(sub.getFileAsPath(), newFile);
        if (ok) {
          if (sub.getFilename().endsWith(".sub")) {
            // when having a .sub, also rename .idx (don't care if error)
            try {
              Path oldidx = sub.getFileAsPath().resolveSibling(sub.getFilename().replaceFirst("sub$", "idx"));
              Path newidx = newFile.resolveSibling(newFile.getFileName().toString().replaceFirst("sub$", "idx"));
              Utils.moveFileSafe(oldidx, newidx);
            }
            catch (Exception e) {
              // no idx found or error - ignore
            }
          }
          m.removeFromMediaFiles(sub);
          MediaFile mf = new MediaFile(newFile);
          MediaFileSubtitle mfs = new MediaFileSubtitle();
          if (!lang.isEmpty()) {
            mfs.setLanguage(lang);
          }
          if (!forced.isEmpty()) {
            mfs.setForced(true);
          }
          mfs.setCodec(sub.getExtension());
          mf.setContainerFormat(sub.getExtension()); // set containerformat, so mediainfo deos not overwrite our new array
          mf.addSubtitle(mfs);
          m.addToMediaFiles(mf);
        }
        else {
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, sub.getFilename(), "message.renamer.failedrename"));
        }
      }
      catch (Exception e) {
        LOGGER.error("error moving subtitles", e);
        MessageManager.instance.pushMessage(
            new Message(MessageLevel.ERROR, sub.getFilename(), "message.renamer.failedrename", new String[] { ":", e.getLocalizedMessage() }));
      }
    } // end MF loop
    m.saveToDb();
  }

  /**
   * remove empty subfolders in this folder after renaming; only valid if we're in a single movie folder!
   * 
   * @param movie
   *          the movie to clean
   */
  private static void removeEmptySubfolders(Movie movie) {
    if (movie.isMultiMovieDir()) {
      return;
    }

    // check all subfolders if they're empty (recursively)
    try {
      Utils.deleteEmptyDirectoryRecursive(movie.getPathNIO());
    }
    catch (IOException e) {
      LOGGER.warn("could not delete empty subfolders: {}", e.getMessage());
    }
  }

  /**
   * Rename movie inside the actual datasource.
   * 
   * @param movie
   *          the movie
   */
  public static void renameMovie(Movie movie) {
    // skip renamer, if all templates are empty!
    if (MovieModuleManager.SETTINGS.getRenamerPathname().isEmpty() && MovieModuleManager.SETTINGS.getRenamerFilename().isEmpty()) {
      LOGGER.info("NOT renaming Movie '{}' - renaming patterns are empty!", movie.getTitle());
      return;
    }

    // FIXME: what? when?
    boolean posterRenamed = false;
    boolean fanartRenamed = false;
    boolean downloadMissingArtworks = false;

    // check if a datasource is set
    if (StringUtils.isEmpty(movie.getDataSource())) {
      LOGGER.error("no Datasource set");
      return;
    }

    if (movie.getTitle().isEmpty()) {
      LOGGER.error("won't rename movie '{}' / '{}' not even title is set?", movie.getPathNIO(), movie.getTitle());
      return;
    }

    // all the good & needed mediafiles
    ArrayList<MediaFile> needed = new ArrayList<>();
    ArrayList<MediaFile> cleanup = new ArrayList<>();

    LOGGER.info("Renaming movie: {}", movie.getTitle());
    LOGGER.debug("movie year: {}", movie.getYear());
    LOGGER.debug("movie path: {}", movie.getPathNIO());
    LOGGER.debug("movie isDisc?: {}", movie.isDisc());
    LOGGER.debug("movie isMulti?: {}", movie.isMultiMovieDir());
    if (movie.getMovieSet() != null) {
      LOGGER.debug("movieset: {}", movie.getMovieSet().getTitle());
    }
    LOGGER.debug("path expression: {}", MovieModuleManager.SETTINGS.getRenamerPathname());
    LOGGER.debug("file expression: {}", MovieModuleManager.SETTINGS.getRenamerFilename());

    String newPathname = createDestinationForFoldername(MovieModuleManager.SETTINGS.getRenamerPathname(), movie);
    String oldPathname = movie.getPathNIO().toString();

    if (!newPathname.isEmpty()) {
      newPathname = Paths.get(movie.getDataSource(), newPathname).toString();
      Path srcDir = movie.getPathNIO();
      Path destDir = Paths.get(newPathname);
      if (!srcDir.toAbsolutePath().toString().equals(destDir.toAbsolutePath().toString())) {

        boolean newDestIsMultiMovieDir = false;
        // re-evaluate multiMovieDir based on renamer settings
        // folder MUST BE UNIQUE, we need at least a T/E-Y combo or IMDBid
        // so if renaming just to a fixed pattern (eg "$S"), movie will downgrade to a MMD
        if (!isFolderPatternUnique(MovieModuleManager.SETTINGS.getRenamerPathname())) {
          // FIXME: if we already in a normal dir - keep it?
          newDestIsMultiMovieDir = true;
        }
        // FIXME: add warning to GUI if downgrade!!!!!!
        LOGGER.debug("movie willBeMulti?: {}", newDestIsMultiMovieDir);

        // ######################################################################
        // ## 1) old = separate movie dir, and new too -> move folder
        // ######################################################################
        if (!movie.isMultiMovieDir() && !newDestIsMultiMovieDir) {
          boolean ok = false;
          try {
            ok = Utils.moveDirectorySafe(srcDir, destDir);
            if (ok) {
              movie.setMultiMovieDir(false);
              movie.updateMediaFilePath(srcDir, destDir);
              movie.setPath(newPathname);
              movie.saveToDb(); // since we moved already, save it
            }
          }
          catch (Exception e) {
            LOGGER.error("error moving folder: ", e);
            MessageManager.instance
                .pushMessage(new Message(MessageLevel.ERROR, srcDir, "message.renamer.failedrename", new String[] { ":", e.getLocalizedMessage() }));
          }
          if (!ok) {
            // FIXME: when we were not able to rename folder, display error msg and abort!!!
            LOGGER.error("Could not move to destination '{}' - NOT renaming folder", destDir);
            return;
          }
        }
        else if (movie.isMultiMovieDir() && !newDestIsMultiMovieDir) {
          // ######################################################################
          // ## 2) MMD movie -> normal movie (upgrade)
          // ######################################################################
          LOGGER.trace("Upgrading movie into it's own dir :) - {}", newPathname);
          if (!Files.exists(destDir)) {
            try {
              Files.createDirectories(destDir);
            }
            catch (Exception e) {
              LOGGER.error("Could not create destination '{}' - NOT renaming folder ('upgrade' movie)", destDir);
              // well, better not to rename
              return;
            }
          }
          else {
            LOGGER.error("Directory already exists! '{}' - NOT renaming folder ('upgrade' movie)", destDir);
            // well, better not to rename
            return;
          }
          movie.setMultiMovieDir(false);
          downloadMissingArtworks = true; // yay - we upgraded our movie, so we could try to get additional artworks :)
        }
        else {
          // ######################################################################
          // ## Can be
          // ## 3) MMD movie -> MMD movie (but foldername possible changed)
          // ## 4) normal movie -> MMD movie (downgrade)
          // ## either way - check & create dest folder
          // ######################################################################
          LOGGER.trace("New movie path is a MMD :( - {}", newPathname);
          if (!Files.exists(destDir)) { // if existent, all is good -> MMD (FIXME: kinda, we *might* have another full movie in there)
            try {
              Files.createDirectories(destDir);
            }
            catch (Exception e) {
              LOGGER.error("Could not create destination '{}' - NOT renaming folder ('MMD' movie)", destDir);
              // well, better not to rename
              return;
            }
          }
          movie.setMultiMovieDir(true);
        }
      } // src == dest
    } // folder pattern empty
    else {
      LOGGER.info("Folder rename settings were empty - NOT renaming folder");
      // set it to current for file renaming
      newPathname = movie.getPathNIO().toString();
    }

    // ######################################################################
    // ## mark ALL existing and known files for cleanup (clone!!)
    // ######################################################################
    for (MovieNfoNaming s : MovieNfoNaming.values()) {
      String nfoFilename = movie.getNfoFilename(s);
      if (StringUtils.isBlank(nfoFilename)) {
        continue;
      }
      // mark all known variants for cleanup
      MediaFile del = new MediaFile(movie.getPathNIO().resolve(nfoFilename), MediaFileType.NFO);
      cleanup.add(del);
    }
    List<IFileNaming> fileNamings = new ArrayList<>();
    fileNamings.addAll(Arrays.asList(MoviePosterNaming.values()));
    fileNamings.addAll(Arrays.asList(MovieFanartNaming.values()));
    fileNamings.addAll(Arrays.asList(MovieBannerNaming.values()));
    fileNamings.addAll(Arrays.asList(MovieClearartNaming.values()));
    fileNamings.addAll(Arrays.asList(MovieLogoNaming.values()));
    fileNamings.addAll(Arrays.asList(MovieClearlogoNaming.values()));
    fileNamings.addAll(Arrays.asList(MovieThumbNaming.values()));
    fileNamings.addAll(Arrays.asList(MovieDiscartNaming.values()));
    fileNamings.addAll(Arrays.asList(MovieKeyartNaming.values()));

    for (IFileNaming fileNaming : fileNamings) {
      for (String ext : KNOWN_IMAGE_FILE_EXTENSIONS) {
        MediaFile del = new MediaFile(movie.getPathNIO().resolve(MovieArtworkHelper.getArtworkFilename(movie, fileNaming, ext)));
        cleanup.add(del);
      }
    }

    // cleanup ALL MFs
    for (MediaFile del : movie.getMediaFiles()) {
      cleanup.add(new MediaFile(del));
    }
    cleanup.removeAll(Collections.singleton(null)); // remove all NULL ones!

    // BASENAME
    String newVideoBasename = "";
    if (!isFilePatternValid()) {
      // Template empty or not even title set, so we are NOT renaming any files
      // we keep the same name on renaming ;)
      newVideoBasename = movie.getVideoBasenameWithoutStacking();
      LOGGER.warn("Filepattern is not valid - NOT renaming files!");
    }
    else {
      // since we rename, generate the new basename
      MediaFile ftr = generateFilename(movie, movie.getMediaFiles(MediaFileType.VIDEO).get(0), newVideoBasename).get(0); // there can be only one
      newVideoBasename = FilenameUtils.getBaseName(ftr.getFilenameWithoutStacking());
    }
    LOGGER.debug("Our new basename for renaming: {}", newVideoBasename);

    // ######################################################################
    // ## rename VIDEO (move 1:1)
    // ######################################################################
    for (MediaFile vid : movie.getMediaFiles(MediaFileType.VIDEO)) {
      LOGGER.trace("Rename 1:1 {} - {}", vid.getType(), vid.getFileAsPath());
      MediaFile newMF = generateFilename(movie, vid, newVideoBasename).get(0); // there can be only one
      boolean ok = moveFile(vid.getFileAsPath(), newMF.getFileAsPath());
      if (ok) {
        vid.setFile(newMF.getFileAsPath()); // update
      }
      needed.add(vid); // add vid, since we're updating existing MF object
    }

    // ######################################################################
    // ## rename POSTER, FANART, BANNER, CLEARART, THUMB, LOGO, CLEARLOGO, DISCART, KEYART (copy 1:N)
    // ######################################################################
    // we can have multiple ones, just get the newest one and copy(overwrite) them to all needed
    ArrayList<MediaFile> mfs = new ArrayList<>();
    mfs.add(movie.getNewestMediaFilesOfType(MediaFileType.FANART));
    mfs.add(movie.getNewestMediaFilesOfType(MediaFileType.POSTER));
    mfs.add(movie.getNewestMediaFilesOfType(MediaFileType.BANNER));
    mfs.add(movie.getNewestMediaFilesOfType(MediaFileType.CLEARART));
    mfs.add(movie.getNewestMediaFilesOfType(MediaFileType.THUMB));
    mfs.add(movie.getNewestMediaFilesOfType(MediaFileType.LOGO));
    mfs.add(movie.getNewestMediaFilesOfType(MediaFileType.CLEARLOGO));
    mfs.add(movie.getNewestMediaFilesOfType(MediaFileType.DISC));
    mfs.add(movie.getNewestMediaFilesOfType(MediaFileType.KEYART));
    mfs.removeAll(Collections.singleton(null)); // remove all NULL ones!
    for (MediaFile mf : mfs) {
      LOGGER.trace("Rename 1:N {} - {}", mf.getType(), mf.getFileAsPath());
      List<MediaFile> newMFs = generateFilename(movie, mf, newVideoBasename); // 1:N
      for (MediaFile newMF : newMFs) {
        posterRenamed = true;
        fanartRenamed = true;
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
    for (MediaFile mf : movie.getMediaFiles(MediaFileType.NFO)) {
      if (mf.getFiledate() >= nfo.getFiledate() && MovieConnectors.isValidNFO(mf.getFileAsPath())) {
        nfo = new MediaFile(mf);
      }
    }

    if (nfo.getFiledate() > 0) { // one valid found? copy our NFO to all variants
      List<MediaFile> newNFOs = generateFilename(movie, nfo, newVideoBasename); // 1:N
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
      LOGGER.trace("No valid NFO found for this movie");
    }

    // now iterate over all non-tmm NFOs, and add them for cleanup or not
    for (MediaFile mf : movie.getMediaFiles(MediaFileType.NFO)) {
      if (MovieConnectors.isValidNFO(mf.getFileAsPath())) {
        cleanup.add(mf);
      }
      else {
        if (MovieModuleManager.SETTINGS.isRenamerNfoCleanup()) {
          cleanup.add(mf);
        }
        else {
          needed.add(mf);
        }
      }
    }

    // ######################################################################
    // ## rename all other types (copy 1:1)
    // ######################################################################
    mfs = new ArrayList<>(movie.getMediaFilesExceptType(MediaFileType.VIDEO, MediaFileType.NFO, MediaFileType.POSTER, MediaFileType.FANART,
        MediaFileType.BANNER, MediaFileType.CLEARART, MediaFileType.THUMB, MediaFileType.LOGO, MediaFileType.CLEARLOGO, MediaFileType.DISC,
        MediaFileType.KEYART, MediaFileType.SUBTITLE));
    mfs.removeAll(Collections.singleton(null)); // remove all NULL ones!
    for (MediaFile other : mfs) {
      LOGGER.trace("Rename 1:1 {} - {}", other.getType(), other.getFileAsPath());

      List<MediaFile> newMFs = generateFilename(movie, other, newVideoBasename); // 1:N
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
    // ## rename subtitles later, but ADD it to not clean up
    // ######################################################################
    needed.addAll(movie.getMediaFiles(MediaFileType.SUBTITLE));

    // ######################################################################
    // ## invalidate image cache
    // ######################################################################
    for (MediaFile gfx : movie.getMediaFiles()) {
      if (gfx.isGraphic()) {
        ImageCache.invalidateCachedImage(gfx);
      }
    }

    // remove duplicate MediaFiles
    Set<MediaFile> newMFs = new LinkedHashSet<>(needed);
    needed.clear();
    needed.addAll(newMFs);

    movie.removeAllMediaFiles();
    movie.addToMediaFiles(needed);
    movie.setPath(newPathname);
    movie.saveToDb();

    // cleanup & rename subtitle files
    renameSubtitles(movie);

    movie.gatherMediaFileInformation(false);

    // rewrite NFO if it's a MP NFO and there was a change with poster/fanart
    if (MovieModuleManager.SETTINGS.getMovieConnector() == MovieConnectors.MP && (posterRenamed || fanartRenamed)) {
      movie.writeNFO();
    }

    movie.saveToDb();

    // ######################################################################
    // ## CLEANUP - delete all files marked for cleanup, which are not "needed"
    // ######################################################################
    LOGGER.info("Cleanup...");

    // get all existing files in the movie dir, since Files.exist is not reliable in OSX
    List<Path> existingFiles;
    if (movie.isMultiMovieDir()) {
      // no recursive search in MMD needed
      existingFiles = Utils.listFiles(movie.getPathNIO());
    }
    else {
      // search all files recursive for deeper cleanup
      existingFiles = Utils.listFilesRecursive(movie.getPathNIO());
    }

    // also add all files from the old path (if upgraded from MMD)
    existingFiles.addAll(Utils.listFiles(Paths.get(oldPathname)));

    for (int i = cleanup.size() - 1; i >= 0; i--) {
      MediaFile cl = cleanup.get(i);

      // cleanup files which are not needed
      if (!needed.contains(cl)) {
        if (cl.getFileAsPath().equals(Paths.get(movie.getDataSource())) || cl.getFileAsPath().equals(movie.getPathNIO())
            || cl.getFileAsPath().equals(Paths.get(oldPathname))) {
          LOGGER.warn("Wohoo! We tried to remove complete datasource / movie folder. Nooo way...! {}: {}", cl.getType(), cl.getFileAsPath());
          // happens when iterating eg over the getNFONaming and we return a "" string.
          // then the path+filename = movie path and we want to delete :/
          continue;
        }

        if (existingFiles.contains(cl.getFileAsPath())) {
          LOGGER.debug("Deleting {}", cl.getFileAsPath());
          Utils.deleteFileWithBackup(cl.getFileAsPath(), movie.getDataSource());
          // also cleanup the cache for deleted mfs
          if (cl.isGraphic()) {
            ImageCache.invalidateCachedImage(cl);
          }
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(cl.getFileAsPath().getParent())) {
          if (!directoryStream.iterator().hasNext()) {
            // no iterator = empty
            LOGGER.debug("Deleting empty Directory {}", cl.getFileAsPath().getParent());
            Files.delete(cl.getFileAsPath().getParent()); // do not use recursive her
          }
        }
        catch (IOException e) {
          LOGGER.warn("could not search for empty dir: {}", e.getMessage());
        }
      }
    }

    removeEmptySubfolders(movie);

    if (downloadMissingArtworks) {
      LOGGER.debug("Yay - movie upgrade :) download missing artworks");
      MovieArtworkHelper.downloadMissingArtwork(movie);
      // also trigger a download of actor images
      movie.writeActorImages();
    }
  }

  /**
   * generates renamed filename(s) per MF
   * 
   * @param movie
   *          the movie (for datasource, path)
   * @param mf
   *          the MF
   * @param videoFileName
   *          the basename of the renamed videoFileName (saved earlier)
   * @return list of renamed filename
   */
  public static List<MediaFile> generateFilename(Movie movie, MediaFile mf, String videoFileName) {
    // return list of all generated MFs
    ArrayList<MediaFile> newFiles = new ArrayList<>();
    boolean newDestIsMultiMovieDir = movie.isMultiMovieDir();
    String newPathname = "";

    String pattern = MovieModuleManager.SETTINGS.getRenamerPathname();
    // keep MMD setting unless renamer pattern is not empty
    if (!pattern.isEmpty()) {
      // re-evaluate multiMovieDir based on renamer settings
      // folder MUST BE UNIQUE, so we need at least a T/E-Y combo or IMDBid
      // If renaming just to a fixed pattern (eg "$S"), movie will downgrade to a MMD
      newDestIsMultiMovieDir = !MovieRenamer.isFolderPatternUnique(pattern);
      newPathname = MovieRenamer.createDestinationForFoldername(pattern, movie);
    }
    else {
      // keep same dir
      // Path relativize(Path other)
      newPathname = Utils.relPath(Paths.get(movie.getDataSource()), movie.getPathNIO());
    }
    Path newMovieDir = Paths.get(movie.getDataSource(), newPathname);

    String newFilename = videoFileName;
    if (newFilename == null || newFilename.isEmpty()) {
      // empty only when first generating basename, so generation here is OK
      newFilename = MovieRenamer.createDestinationForFilename(MovieModuleManager.SETTINGS.getRenamerFilename(), movie);
    }

    // extra clone, just for easy adding the "default" ones ;)
    MediaFile defaultMF = new MediaFile(mf);
    defaultMF.replacePathForRenamedFolder(movie.getPathNIO(), newMovieDir);

    if (!isFilePatternValid() && !movie.isDisc()) {
      // not renaming files, but IF we have a folder pattern, we need to move around! (but NOT disc movies!)
      newFiles.add(defaultMF);
      return newFiles;
    }

    switch (mf.getType()) {
      case VIDEO:
        MediaFile vid = new MediaFile(mf);
        if (movie.isDisc() || mf.isDiscFile()) {
          // just replace new path and return file (do not change names!)
          vid.replacePathForRenamedFolder(movie.getPathNIO(), newMovieDir);
        }
        else {
          newFilename += getStackingString(mf);
          newFilename += "." + mf.getExtension();
          vid.setFile(newMovieDir.resolve(newFilename));
        }
        newFiles.add(vid);
        break;

      case TRAILER:
        if (MovieModuleManager.SETTINGS.getTrailerFilenames().isEmpty()) {
          // we do not want trailers to be renamed? so they will be removed....
          break;
        }

        List<MovieTrailerNaming> trailernames = new ArrayList<>();
        if (newDestIsMultiMovieDir) {
          // Fixate the name regardless of setting
          trailernames.add(MovieTrailerNaming.FILENAME_TRAILER);
        }
        else if (movie.isDisc()) {
          trailernames.add(MovieTrailerNaming.FILENAME_TRAILER);
        }
        else {
          trailernames = MovieModuleManager.SETTINGS.getTrailerFilenames();
        }
        for (MovieTrailerNaming name : trailernames) {
          String newTrailerName = movie.getTrailerFilename(name, newFilename + ".avi"); // basename used, so add fake extension
          if (newTrailerName.isEmpty()) {
            continue;
          }
          MediaFile trail = new MediaFile(mf);
          trail.setFile(newMovieDir.resolve(newTrailerName + "." + mf.getExtension())); // get w/o extension to add same
          newFiles.add(trail);
        }
        break;

      case SAMPLE:
        MediaFile sample = new MediaFile(mf);
        newFilename += "-sample." + mf.getExtension();
        sample.setFile(newMovieDir.resolve(newFilename));
        newFiles.add(sample);
        break;

      case MEDIAINFO:
        MediaFile mi = new MediaFile(mf);
        if (movie.isDisc()) {
          // hmm.. dunno, keep at least 1:1
          mi.replacePathForRenamedFolder(movie.getPathNIO(), newMovieDir);
          newFiles.add(mi);
        }
        else {
          newFilename += getStackingString(mf);
          newFilename += "-mediainfo." + mf.getExtension();
          mi.setFile(newMovieDir.resolve(newFilename));
          newFiles.add(mi);
        }
        break;

      case VSMETA:
        MediaFile meta = new MediaFile(mf);
        if (movie.isDisc()) {
          // hmm.. no vsmeta created? keep 1:1 (although this will be never called)
          meta.setFile(newMovieDir.resolve(meta.getFilename()));
          newFiles.add(meta);
        }
        else {
          newFilename += getStackingString(mf);
          // HACK: get video extension from "old" name, eg video.avi.vsmeta
          String videoExt = FilenameUtils.getExtension(FilenameUtils.getBaseName(mf.getFilename()));
          newFilename += "." + videoExt + ".vsmeta";
          meta.setFile(newMovieDir.resolve(newFilename));
          newFiles.add(meta);
        }
        break;

      case SUBTITLE:
        List<MediaFileSubtitle> mfsl = mf.getSubtitles();
        List<MediaFile> subtitleFiles = movie.getMediaFiles(MediaFileType.SUBTITLE);

        // check if there is only one subtitle file and the user wants to write this w/o the language tag
        if (!MovieModuleManager.SETTINGS.isSubtitleWithoutLanguageTag() || subtitleFiles.size() > 1) {
          newFilename += getStackingString(mf);
          if (mfsl != null && !mfsl.isEmpty()) {
            // internal values
            MediaFileSubtitle mfs = mfsl.get(0);
            if (!mfs.getLanguage().isEmpty()) {
              String lang = LanguageStyle.getLanguageCodeForStyle(mfs.getLanguage(), MovieModuleManager.SETTINGS.getSubtitleLanguageStyle());
              if (StringUtils.isBlank(lang)) {
                lang = mfs.getLanguage();
              }
              newFilename += "." + lang;
            }
            if (mfs.isForced()) {
              newFilename += ".forced";
            }
          }
        }
        newFilename += "." + mf.getExtension();

        MediaFile sub = new MediaFile(mf);
        sub.setFile(newMovieDir.resolve(newFilename));
        newFiles.add(sub);
        break;

      case NFO:
        if (MovieModuleManager.SETTINGS.getNfoFilenames().isEmpty()) {
          // we do not want NFO to be renamed? so they will be removed....
          break;
        }

        if (MovieConnectors.isValidNFO(mf.getFileAsPath())) {
          List<MovieNfoNaming> nfonames = new ArrayList<>();
          if (newDestIsMultiMovieDir) {
            // Fixate the name regardless of setting
            nfonames.add(MovieNfoNaming.FILENAME_NFO);
          }
          else if (movie.isDisc()) {
            nfonames.add(MovieNfoNaming.FILENAME_NFO);
            nfonames.add(MovieNfoNaming.MOVIE_NFO); // unneeded, but "TMM style"
          }
          else {
            nfonames = MovieModuleManager.SETTINGS.getNfoFilenames();
          }
          for (MovieNfoNaming name : nfonames) {
            String newNfoName = movie.getNfoFilename(name, newFilename + ".avi"); // basename used, so add fake extension
            if (newNfoName.isEmpty()) {
              continue;
            }
            MediaFile nfo = new MediaFile(mf);
            nfo.setFile(newMovieDir.resolve(newNfoName));
            newFiles.add(nfo);
          }
        }
        else {
          // not a TMM NFO
          if (!MovieModuleManager.SETTINGS.isRenamerNfoCleanup()) {
            newFiles.add(new MediaFile(mf));
          }

        }
        break;

      case POSTER:
        for (MoviePosterNaming name : MovieArtworkHelper.getPosterNamesForMovie(movie)) {
          String newPosterName = name.getFilename(newFilename, getArtworkExtension(mf));
          if (StringUtils.isNotBlank(newPosterName)) {
            MediaFile pos = new MediaFile(mf);
            pos.setFile(newMovieDir.resolve(newPosterName));
            newFiles.add(pos);
          }
        }
        break;

      case FANART:
        for (MovieFanartNaming name : MovieArtworkHelper.getFanartNamesForMovie(movie)) {
          String newFanartName = name.getFilename(newFilename, getArtworkExtension(mf));
          if (StringUtils.isNotBlank(newFanartName)) {
            MediaFile fan = new MediaFile(mf);
            fan.setFile(newMovieDir.resolve(newFanartName));
            newFiles.add(fan);
          }
        }
        break;

      case BANNER:
        for (MovieBannerNaming name : MovieArtworkHelper.getBannerNamesForMovie(movie)) {
          String newBannerName = name.getFilename(newFilename, getArtworkExtension(mf));
          if (StringUtils.isNotBlank(newBannerName)) {
            MediaFile banner = new MediaFile(mf);
            banner.setFile(newMovieDir.resolve(newBannerName));
            newFiles.add(banner);
          }
        }
        break;

      case CLEARART:
        for (MovieClearartNaming name : MovieArtworkHelper.getClearartNamesForMovie(movie)) {
          String newClearartName = name.getFilename(newFilename, getArtworkExtension(mf));
          if (StringUtils.isNotBlank(newClearartName)) {
            MediaFile clearart = new MediaFile(mf);
            clearart.setFile(newMovieDir.resolve(newClearartName));
            newFiles.add(clearart);
          }
        }
        break;

      case DISC:
        for (MovieDiscartNaming name : MovieArtworkHelper.getDiscartNamesForMovie(movie)) {
          String newDiscartName = name.getFilename(newFilename, getArtworkExtension(mf));
          if (StringUtils.isNotBlank(newDiscartName)) {
            MediaFile discart = new MediaFile(mf);
            discart.setFile(newMovieDir.resolve(newDiscartName));
            newFiles.add(discart);
          }
        }
        break;

      case LOGO:
        for (MovieLogoNaming name : MovieArtworkHelper.getLogoNamesForMovie(movie)) {
          String newLogoName = name.getFilename(newFilename, getArtworkExtension(mf));
          if (StringUtils.isNotBlank(newLogoName)) {
            MediaFile logo = new MediaFile(mf);
            logo.setFile(newMovieDir.resolve(newLogoName));
            newFiles.add(logo);
          }
        }
        break;

      case CLEARLOGO:
        for (MovieClearlogoNaming name : MovieArtworkHelper.getClearlogoNamesForMovie(movie)) {
          String newClearlogoName = name.getFilename(newFilename, getArtworkExtension(mf));
          if (StringUtils.isNotBlank(newClearlogoName)) {
            MediaFile clearlogo = new MediaFile(mf);
            clearlogo.setFile(newMovieDir.resolve(newClearlogoName));
            newFiles.add(clearlogo);
          }
        }
        break;

      case THUMB:
        for (MovieThumbNaming name : MovieArtworkHelper.getThumbNamesForMovie(movie)) {
          String newThumbName = name.getFilename(newFilename, getArtworkExtension(mf));
          if (StringUtils.isNotBlank(newThumbName)) {
            MediaFile thumb = new MediaFile(mf);
            thumb.setFile(newMovieDir.resolve(newThumbName));
            newFiles.add(thumb);
          }
        }
        break;

      case KEYART:
        for (MovieKeyartNaming name : MovieArtworkHelper.getKeyartNamesForMovie(movie)) {
          String newKeyartName = name.getFilename(newFilename, getArtworkExtension(mf));
          if (StringUtils.isNotBlank(newKeyartName)) {
            MediaFile key = new MediaFile(mf);
            key.setFile(newMovieDir.resolve(newKeyartName));
            newFiles.add(key);
          }
        }
        break;

      // *************
      // OK, from here we check only the settings
      // *************
      case EXTRAFANART:
      case EXTRATHUMB:
        // pass the file regardless of the settings (they're her so we just rename them)
        if (!newDestIsMultiMovieDir) {
          newFiles.add(defaultMF);
        }
        break;

      // *************
      // here we add all others
      // *************
      case AUDIO:
      case GRAPHIC:
      case SEASON_POSTER:
      case TEXT:
      case UNKNOWN:
      case EXTRA:
      default:
        newFiles.add(defaultMF);
        break;
    }

    return newFiles;
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
   * returns "delimiter + stackingString" for use in filename
   * 
   * @param mf
   *          a mediaFile
   * @return eg ".CD1" dependent of settings
   */
  private static String getStackingString(MediaFile mf) {
    String delimiter = " ";
    if (MovieModuleManager.SETTINGS.isRenamerFilenameSpaceSubstitution()) {
      delimiter = MovieModuleManager.SETTINGS.getRenamerFilenameSpaceReplacement();
    }
    if (!mf.getStackingMarker().isEmpty()) {
      return delimiter + mf.getStackingMarker();
    }
    else if (mf.getStacking() != 0) {
      return delimiter + "CD" + mf.getStacking();
    }
    return "";
  }

  /**
   * Creates the new filename according to template string
   * 
   * @param template
   *          the template
   * @param movie
   *          the movie
   * @return the string
   */
  public static String createDestinationForFilename(String template, Movie movie) {
    return createDestination(template, movie, true);
  }

  /**
   * Creates the new filename according to template string
   * 
   * @param template
   *          the template
   * @param movie
   *          the movie
   * @return the string
   */
  public static String createDestinationForFoldername(String template, Movie movie) {
    return createDestination(template, movie, false);
  }

  /**
   * replaces an optional variable, eg "{ Year $Y }"<br>
   * if we have a year, "Year 2013" will be returned<br>
   * if $Y replacement was empty, the complete optional tag will be empty.
   * 
   * @param s
   *          the string to replace the optional variable for
   * @param movie
   *          the movie holding all needed meta data
   * @param forFilename
   *          do the logic for file or for folder names?
   * @return the resulting string
   */
  private static String replaceOptionalVariable(String s, Movie movie, boolean forFilename) {
    Pattern regex = Pattern.compile("\\$.{1}");
    Matcher mat = regex.matcher(s);
    if (mat.find()) {
      String rep = createDestination(mat.group(), movie, forFilename);
      if (rep.isEmpty()) {
        return "";
      }
      else {
        return s.replace(mat.group(), rep);
      }
    }
    else {
      return "";
    }
  }

  /**
   * gets the token value (${x}) from specified movie object
   * 
   * @param movie
   *          our movie
   * @param token
   *          the ${x} token
   * @return value or empty string
   */
  public static String getTokenValue(Movie movie, String token) {
    try {
      Engine engine = Engine.createEngine();
      engine.registerRenderer(Number.class, new ZeroNumberRenderer());
      engine.registerNamedRenderer(new NamedDateRenderer());
      engine.registerNamedRenderer(new NamedUpperCaseRenderer());
      engine.registerNamedRenderer(new NamedLowerCaseRenderer());
      engine.registerNamedRenderer(new NamedTitleCaseRenderer());
      engine.registerNamedRenderer(new MovieNamedFirstCharacterRenderer());
      engine.registerNamedRenderer(new NamedArrayRenderer());
      engine.registerNamedRenderer(new NamedFilesizeRenderer());
      engine.setModelAdaptor(new MovieRenamerModelAdaptor());
      Map<String, Object> root = new HashMap<>();
      root.put("movie", movie);

      // only offer movie set for movies with more than 1 movies or if setting is set
      if (movie.getMovieSet() != null
          && (movie.getMovieSet().getMovies().size() > 1 || MovieModuleManager.SETTINGS.isRenamerCreateMoviesetForSingleMovie())) {
        root.put("movieSet", movie.getMovieSet());
      }

      return engine.transform(morphTemplate(token), root);
    }
    catch (Exception e) {
      LOGGER.warn("unable to process token: {}", token);
      return token;
    }
  }

  /**
   * gets the first alpha-numeric character
   *
   * @param text
   * @return A-Z0-9 or empty
   */
  protected static String getFirstAlphaNum(String text) {
    if (StringUtils.isNotBlank(text)) {
      Matcher m = ALPHANUM.matcher(text);
      if (m.find()) {
        return m.group(1).toUpperCase(Locale.ROOT);
      }
    }
    return ""; // text empty/null/no alphanum
  }

  /**
   * Creates the new file/folder name according to template string
   * 
   * @param template
   *          the template
   * @param movie
   *          the movie
   * @param forFilename
   *          replace for filename (=true)? or for a foldername (=false)<br>
   *          Former does replace ALL directory separators
   * @return the string
   */
  public static String createDestination(String template, Movie movie, boolean forFilename) {

    String newDestination = getTokenValue(movie, template);

    // replace empty brackets
    newDestination = newDestination.replaceAll("\\([ ]?\\)", "");
    newDestination = newDestination.replaceAll("\\[[ ]?\\]", "");
    newDestination = newDestination.replaceAll("\\{[ ]?\\}", "");

    // if there are multiple file separators in a row - strip them out
    if (SystemUtils.IS_OS_WINDOWS) {
      if (!forFilename) {
        // trim whitespace around directory sep
        newDestination = newDestination.replaceAll("\\s+\\\\", "\\\\");
        newDestination = newDestination.replaceAll("\\\\\\s+", "\\\\");
        // remove separators in front of path separators
        newDestination = newDestination.replaceAll("[ \\.\\-_]+\\\\", "\\\\");
      }
      // we need to mask it in windows
      newDestination = newDestination.replaceAll("\\\\{2,}", "\\\\");
      newDestination = newDestination.replaceAll("^\\\\", "");
    }
    else {
      if (!forFilename) {
        // trim whitespace around directory sep
        newDestination = newDestination.replaceAll("\\s+/", "/");
        newDestination = newDestination.replaceAll("/\\s+", "/");
        // remove separators in front of path separators
        newDestination = newDestination.replaceAll("[ \\.\\-_]+/", "/");
      }
      newDestination = newDestination.replaceAll("/{2,}", "/");
      newDestination = newDestination.replaceAll("^/", "");
    }

    // replace ALL directory separators, if we generate this for filenames!
    if (forFilename) {
      newDestination = replacePathSeparators(newDestination);
    }

    // replace multiple spaces with a single one
    newDestination = newDestination.replaceAll(" +", " ").trim();

    // replace spaces with underscores if needed (filename only)
    if (forFilename && MovieModuleManager.SETTINGS.isRenamerFilenameSpaceSubstitution()) {
      String replacement = MovieModuleManager.SETTINGS.getRenamerFilenameSpaceReplacement();
      newDestination = newDestination.replace(" ", replacement);

      // also replace now multiple replacements with one to avoid strange looking results
      // example:
      // Abraham Lincoln - Vapire Hunter -> Abraham-Lincoln---Vampire-Hunter
      newDestination = newDestination.replaceAll(Pattern.quote(replacement) + "+", replacement);
    }
    else if (!forFilename && MovieModuleManager.SETTINGS.isRenamerPathnameSpaceSubstitution()) {
      String replacement = MovieModuleManager.SETTINGS.getRenamerPathnameSpaceReplacement();
      newDestination = newDestination.replace(" ", replacement);

      // also replace now multiple replacements with one to avoid strange looking results
      // example:
      // Abraham Lincoln - Vapire Hunter -> Abraham-Lincoln---Vampire-Hunter
      newDestination = newDestination.replaceAll(Pattern.quote(replacement) + "+", replacement);
    }

    // replace all leading/trailing separators (except the underscore which could be valid in the front)
    newDestination = newDestination.replaceAll("^[ \\.\\-]+", "");
    newDestination = newDestination.replaceAll("[ \\.\\-_]+$", "");

    // ASCII replacement
    if (MovieModuleManager.SETTINGS.isAsciiReplacement()) {
      newDestination = StrgUtils.convertToAscii(newDestination, false);
    }

    // the colon is handled by JMTE but it looks like some users are stupid enough to add this to the pattern itself
    newDestination = newDestination.replace(": ", " - "); // nicer
    newDestination = newDestination.replace(":", "-"); // nicer

    return newDestination.trim();
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
  static boolean moveFile(Path oldFilename, Path newFilename) {
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
        LOGGER.error("Could not move MF '{}' to '{}'", oldFilename, newFilename);
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
  static boolean copyFile(Path oldFilename, Path newFilename) {
    if (!oldFilename.toAbsolutePath().toString().equals(newFilename.toAbsolutePath().toString())) {
      LOGGER.info("copy file {} to {}", oldFilename, newFilename);
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
   * Check if the folder rename pattern is unique<br>
   * Unique true, when having at least a $T/$E-$Y combo or $I imdbId<br>
   * 
   * @param pattern
   *          the pattern to check the uniqueness for
   * @return true/false
   */
  public static boolean isFolderPatternUnique(String pattern) {
    return TITLE_PATTERN.matcher(pattern).find() && YEAR_ID_PATTERN.matcher(pattern).find();
  }

  /**
   * Check if the FILE rename pattern is valid<br>
   * What means, pattern has at least title set (${title}|${originalTitle}|${titleSortable})<br>
   * "empty" is considered as invalid - so not renaming files
   * 
   * @return true/false
   */
  public static boolean isFilePatternValid() {
    return isFilePatternValid(MovieModuleManager.SETTINGS.getRenamerFilename());
  }

  /**
   * Check if the FILE rename pattern is valid<br>
   * What means, pattern has at least title set (${title}|${originalTitle}|${titleSortable})<br>
   * "empty" is considered as invalid - so not renaming files
   *
   * @return true/false
   */
  public static boolean isFilePatternValid(String pattern) {
    return TITLE_PATTERN.matcher(pattern).find();
  }

  /**
   * replaces all invalid/illegal characters for filenames/foldernames with ""<br>
   * except the colon, which will be changed to a dash
   *
   * @param source
   *          string to clean
   * @return cleaned string
   */
  public static String replaceInvalidCharacters(String source) {
    String result = source;

    if ("-".equals(MovieModuleManager.SETTINGS.getRenamerColonReplacement())) {
      result = result.replace(": ", " - "); // nicer
      result = result.replace(":", "-"); // nicer
    }
    else {
      result = result.replace(":", MovieModuleManager.SETTINGS.getRenamerColonReplacement());
    }

    return result.replaceAll("([\":<>|?*])", "");
  }

  /**
   * replace all path separators in the given {@link String} with a space
   * 
   * @param source
   *          the the original {@link String}
   * @return the cleaned {@link String}
   */
  public static String replacePathSeparators(String source) {
    String result = source.replaceAll("\\/", " ");
    return result.replaceAll("\\\\", " ");
  }

  public static class MovieRenamerModelAdaptor extends TmmModelAdaptor {
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
          value = replacePathSeparators((String) value);

        }
      }

      return value;
    }
  }

  private static class MovieNamedFirstCharacterRenderer implements NamedRenderer {

    @Override
    public String render(Object o, String s, Locale locale, Map<String, Object> map) {
      if (o instanceof String && StringUtils.isNotBlank((String) o)) {
        String source = (String) o;
        if (MovieModuleManager.SETTINGS.isAsciiReplacement()) {
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
