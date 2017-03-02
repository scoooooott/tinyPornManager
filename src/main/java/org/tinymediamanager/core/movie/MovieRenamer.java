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
package org.tinymediamanager.core.movie;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import org.tinymediamanager.core.movie.connector.MovieConnectors;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieActor;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.util.LanguageUtils;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * The Class MovieRenamer.
 * 
 * @author Manuel Laggner / Myron Boyle
 */
public class MovieRenamer {
  private static final Logger  LOGGER   = LoggerFactory.getLogger(MovieRenamer.class);
  private static final Pattern ALPHANUM = Pattern.compile(".*?([a-zA-Z0-9]{1}).*$");  // to not use posix

  private static void renameSubtitles(Movie m) {
    // build language lists
    Set<String> langArray = LanguageUtils.KEY_TO_LOCALE_MAP.keySet();

    for (MediaFile sub : m.getMediaFiles(MediaFileType.SUBTITLE)) {
      String originalLang = "";
      String lang = "";
      String forced = "";
      List<MediaFileSubtitle> mfsl = sub.getSubtitles();

      if (mfsl != null && mfsl.size() > 0) {
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
        if (mfs != null && mfs.size() > 0) {
          shortname = sub.getBasename().toLowerCase(Locale.ROOT).replace(m.getVideoBasenameWithoutStacking(), "");
        }

        if (sub.getFilename().toLowerCase(Locale.ROOT).contains("forced")) {
          // add "forced" prior language
          forced = ".forced";
          shortname = shortname.replaceAll("\\p{Punct}*forced", "");
        }
        // shortname = shortname.replaceAll("\\p{Punct}", "").trim(); // NEVER EVER!!!

        for (String s : langArray) {
          if (shortname.equalsIgnoreCase(s) || shortname.matches("(?i).*[ _.-]+" + s + "$")) {
            originalLang = s;
            // lang = Utils.getIso3LanguageFromLocalizedString(s);
            // LOGGER.debug("found language '" + s + "' in subtitle; displaying it as '" + lang + "'");
            break;
          }
        }
      }

      lang = LanguageStyle.getLanguageCodeForStyle(originalLang, MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerLanguageStyle());
      if (StringUtils.isBlank(lang)) {
        lang = originalLang;
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
              Path oldidx = sub.getFileAsPath().resolveSibling(sub.getFilename().toString().replaceFirst("sub$", "idx"));
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
   * Rename movie.
   * 
   * @param movie
   *          the movie
   */
  public static void renameMovie(Movie movie) {
    // FIXME: what? when?
    boolean posterRenamed = false;
    boolean fanartRenamed = false;
    boolean downloadMissingArtworks = false;

    // check if a datasource is set
    if (StringUtils.isEmpty(movie.getDataSource())) {
      LOGGER.error("no Datasource set");
      return;
    }

    // if (!movie.isScraped()) {
    if (movie.getTitle().isEmpty()) {
      LOGGER.error("won't rename movie '" + movie.getPathNIO() + "' / '" + movie.getTitle() + "' not even title is set?");
      return;
    }

    // all the good & needed mediafiles
    ArrayList<MediaFile> needed = new ArrayList<>();
    ArrayList<MediaFile> cleanup = new ArrayList<>();

    LOGGER.info("Renaming movie: " + movie.getTitle());
    LOGGER.debug("movie year: " + movie.getYear());
    LOGGER.debug("movie path: " + movie.getPathNIO());
    LOGGER.debug("movie isDisc?: " + movie.isDisc());
    LOGGER.debug("movie isMulti?: " + movie.isMultiMovieDir());
    if (movie.getMovieSet() != null) {
      LOGGER.debug("movieset: " + movie.getMovieSet().getTitle());
    }
    LOGGER.debug("path expression: " + MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerPathname());
    LOGGER.debug("file expression: " + MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerFilename());

    String newPathname = createDestinationForFoldername(MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerPathname(), movie);
    String oldPathname = movie.getPathNIO().toString();

    if (!newPathname.isEmpty()) {
      newPathname = movie.getDataSource() + File.separator + newPathname;
      Path srcDir = movie.getPathNIO();
      Path destDir = Paths.get(newPathname);
      if (!srcDir.toAbsolutePath().equals(destDir.toAbsolutePath())) {

        boolean newDestIsMultiMovieDir = false;
        // re-evaluate multiMovieDir based on renamer settings
        // folder MUST BE UNIQUE, we need at least a T/E-Y combo or IMDBid
        // so if renaming just to a fixed pattern (eg "$S"), movie will downgrade to a MMD
        if (!isFolderPatternUnique(MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerPathname())) {
          // FIXME: if we already in a normal dir - keep it?
          newDestIsMultiMovieDir = true;
        }
        // FIXME: add warning to GUI if downgrade!!!!!!
        LOGGER.debug("movie willBeMulti?: " + newDestIsMultiMovieDir);

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
            LOGGER.error("Could not move to destination '" + destDir + "' - NOT renaming folder");
            return;
          }
        }
        else if (movie.isMultiMovieDir() && !newDestIsMultiMovieDir) {
          // ######################################################################
          // ## 2) MMD movie -> normal movie (upgrade)
          // ######################################################################
          LOGGER.trace("Upgrading movie into it's own dir :) " + newPathname);
          if (!Files.exists(destDir)) {
            try {
              Files.createDirectories(destDir);
            }
            catch (Exception e) {
              LOGGER.error("Could not create destination '" + destDir + "' - NOT renaming folder ('upgrade' movie)");
              // well, better not to rename
              return;
            }
          }
          else {
            LOGGER.error("Directory already exists! '" + destDir + "' - NOT renaming folder ('upgrade' movie)");
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
          LOGGER.trace("New movie path is a MMD :( " + newPathname);
          if (!Files.exists(destDir)) { // if existent, all is good -> MMD (FIXME: kinda, we *might* have another full movie in there)
            try {
              Files.createDirectories(destDir);
            }
            catch (Exception e) {
              LOGGER.error("Could not create destination '" + destDir + "' - NOT renaming folder ('MMD' movie)");
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
    for (MoviePosterNaming s : MoviePosterNaming.values()) {
      MediaFile del = new MediaFile(movie.getPathNIO().resolve(replaceInvalidCharacters(MovieArtworkHelper.getPosterFilename(s, movie))),
          MediaFileType.POSTER);
      cleanup.add(del);
    }
    for (MovieFanartNaming s : MovieFanartNaming.values()) {
      MediaFile del = new MediaFile(movie.getPathNIO().resolve(replaceInvalidCharacters(MovieArtworkHelper.getFanartFilename(s, movie))),
          MediaFileType.FANART);
      cleanup.add(del);
    }
    // cleanup ALL MFs
    for (MediaFile del : movie.getMediaFiles()) {
      cleanup.add(new MediaFile(del));
    }
    cleanup.removeAll(Collections.singleton(null)); // remove all NULL ones!

    // update movie path at end of renaming - we need the old one here!!
    // movie.setPath(newPathname);
    // movie.saveToDb();

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
    LOGGER.debug("Our new basename for renaming: " + newVideoBasename);

    // unneeded / more reliable with with java 7?
    // // ######################################################################
    // // ## test VIDEO rename
    // // ######################################################################
    // for (MediaFile vid : movie.getMediaFiles(MediaFileType.VIDEO)) {
    // LOGGER.debug("testing file " + vid.getFileAsPath());
    // Path f = vid.getFileAsPath();
    // boolean testRenameOk = false;
    // for (int i = 0; i < 5; i++) {
    // testRenameOk = f.renameTo(f); // haahaa, try to rename to itself :P
    // if (testRenameOk) {
    // break; // ok it worked, step out
    // }
    // // we had the case, that the renaemoTo didn't work,
    // // and even the exists did not work!
    // // so we skip this additional check, which results in not removing the movie file
    // // if (!f.exists()) {
    // // LOGGER.debug("Hmmm... file " + f + " does not even exists; delete from DB");
    // // // delete from MF or ignore for later cleanup (but better now!)
    // // movie.removeFromMediaFiles(vid);
    // // testRenameOk = true; // we "tested" this ok
    // // break;
    // // }
    // try {
    // LOGGER.debug("rename did not work - sleep a while and try again...");
    // Thread.sleep(1000);
    // }
    // catch (InterruptedException e) {
    // LOGGER.warn("I'm so excited - could not sleep");
    // }
    // }
    // if (!testRenameOk) {
    // LOGGER.warn("File " + vid.getFileAsPath() + " is not accessible!");
    // MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, vid.getFilename(), "message.renamer.failedrename"));
    // return;
    // }
    // }

    // ######################################################################
    // ## rename VIDEO (move 1:1)
    // ######################################################################
    for (MediaFile vid : movie.getMediaFiles(MediaFileType.VIDEO)) {
      LOGGER.trace("Rename 1:1 " + vid.getType() + " " + vid.getFileAsPath());
      MediaFile newMF = generateFilename(movie, vid, newVideoBasename).get(0); // there can be only one
      boolean ok = moveFile(vid.getFileAsPath(), newMF.getFileAsPath());
      if (ok) {
        vid.setFile(newMF.getFileAsPath()); // update
      }
      needed.add(vid); // add vid, since we're updating existing MF object
    }

    // ######################################################################
    // ## rename POSTER, FANART (copy 1:N)
    // ######################################################################
    // we can have multiple ones, just get the newest one and copy(overwrite) them to all needed
    ArrayList<MediaFile> mfs = new ArrayList<>();
    mfs.add(movie.getNewestMediaFilesOfType(MediaFileType.FANART));
    mfs.add(movie.getNewestMediaFilesOfType(MediaFileType.POSTER));
    mfs.removeAll(Collections.singleton(null)); // remove all NULL ones!
    for (MediaFile mf : mfs) {
      LOGGER.trace("Rename 1:N " + mf.getType() + " " + mf.getFileAsPath());
      ArrayList<MediaFile> newMFs = generateFilename(movie, mf, newVideoBasename); // 1:N
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
      ArrayList<MediaFile> newNFOs = generateFilename(movie, nfo, newVideoBasename); // 1:N
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
      LOGGER.trace("No valid NFO found for this movie");
    }

    // now iterate over all non-tmm NFOs, and add them for cleanup or not
    for (MediaFile mf : movie.getMediaFiles(MediaFileType.NFO)) {
      if (MovieConnectors.isValidNFO(mf.getFileAsPath())) {
        cleanup.add(mf);
      }
      else {
        if (MovieModuleManager.MOVIE_SETTINGS.isMovieRenamerNfoCleanup()) {
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
    mfs = new ArrayList<>();
    mfs.addAll(
        movie.getMediaFilesExceptType(MediaFileType.VIDEO, MediaFileType.NFO, MediaFileType.POSTER, MediaFileType.FANART, MediaFileType.SUBTITLE));
    mfs.removeAll(Collections.singleton(null)); // remove all NULL ones!
    for (MediaFile other : mfs) {
      LOGGER.trace("Rename 1:1 " + other.getType() + " " + other.getFileAsPath());

      ArrayList<MediaFile> newMFs = generateFilename(movie, other, newVideoBasename); // 1:N
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
    // ## invalidade image cache
    // ######################################################################
    for (MediaFile gfx : movie.getMediaFiles()) {
      if (gfx.isGraphic()) {
        ImageCache.invalidateCachedImage(gfx.getFileAsPath());
      }
    }

    // remove duplicate MediaFiles
    Set<MediaFile> newMFs = new LinkedHashSet<>(needed);
    needed.clear();
    needed.addAll(newMFs);

    movie.removeAllMediaFiles();
    movie.addToMediaFiles(needed);
    movie.setPath(newPathname);

    // update .actors
    for (MovieActor actor : movie.getActors()) {
      actor.setEntityRoot(newPathname);
    }

    movie.saveToDb();

    // cleanup & rename subtitle files
    renameSubtitles(movie);

    movie.gatherMediaFileInformation(false);

    // rewrite NFO if it's a MP NFO and there was a change with poster/fanart
    if (MovieModuleManager.MOVIE_SETTINGS.getMovieConnector() == MovieConnectors.MP && (posterRenamed || fanartRenamed)) {
      movie.writeNFO();
    }

    movie.saveToDb();

    // ######################################################################
    // ## CLEANUP - delete all files marked for cleanup, which are not "needed"
    // ######################################################################
    LOGGER.info("Cleanup...");
    for (int i = cleanup.size() - 1; i >= 0; i--) {
      // cleanup files which are not needed
      if (!needed.contains(cleanup.get(i))) {
        MediaFile cl = cleanup.get(i);
        if (cl.getFileAsPath().equals(Paths.get(movie.getDataSource())) || cl.getFileAsPath().equals(movie.getPathNIO())
            || cl.getFileAsPath().equals(Paths.get(oldPathname))) {
          LOGGER.warn("Wohoo! We tried to remove complete datasource / movie folder. Nooo way...! " + cl.getType() + ": " + cl.getFileAsPath());
          // happens when iterating eg over the getNFONaming and we return a "" string.
          // then the path+filename = movie path and we want to delete :/
          // do not show an error anylonger, just silently ignore...
          // MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, cl.getFile(), "message.renamer.failedrename"));
          // return; // rename failed
          continue;
        }

        if (Files.exists(cl.getFileAsPath())) { // unneeded, but for not displaying wrong deletes in logger...
          LOGGER.debug("Deleting " + cl.getFileAsPath());
          Utils.deleteFileWithBackup(cl.getFileAsPath(), movie.getDataSource());
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(cl.getFileAsPath().getParent())) {
          if (!directoryStream.iterator().hasNext()) {
            // no iterator = empty
            LOGGER.debug("Deleting empty Directory " + cl.getFileAsPath().getParent());
            Files.delete(cl.getFileAsPath().getParent()); // do not use recursive her
          }
        }
        catch (IOException ex) {
        }
      }
    }

    if (downloadMissingArtworks) {
      LOGGER.debug("Yay - movie upgrade :) download missing artworks");
      MovieArtworkHelper.downloadMissingArtwork(movie);
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
  public static ArrayList<MediaFile> generateFilename(Movie movie, MediaFile mf, String videoFileName) {
    // return list of all generated MFs
    ArrayList<MediaFile> newFiles = new ArrayList<>();
    boolean newDestIsMultiMovieDir = movie.isMultiMovieDir();
    String newPathname = "";

    String pattern = MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerPathname();
    // keep MMD setting unless renamer pattern is not empty
    if (!pattern.isEmpty()) {
      // re-evaluate multiMovieDir based on renamer settings
      // folder MUST BE UNIQUE, so we need at least a T/E-Y combo or IMDBid
      // If renaming just to a fixed pattern (eg "$S"), movie will downgrade to a MMD
      if (MovieRenamer.isFolderPatternUnique(pattern)) {
        newDestIsMultiMovieDir = false;
      }
      else {
        newDestIsMultiMovieDir = true;
      }
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
      newFilename = MovieRenamer.createDestinationForFilename(MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerFilename(), movie);
    }

    if (!isFilePatternValid() && !movie.isDisc()) {
      // not renaming files, but IF we have a folder pattern, we need to move around! (but NOT disc movies!)
      MediaFile newMF = new MediaFile(mf);
      newMF.setPath(newMovieDir.toString());
      newFiles.add(newMF);
      return newFiles;
    }

    // extra clone, just for easy adding the "default" ones ;)
    MediaFile defaultMF = null;
    String defaultMFext = "";
    // if (!newDestIsMultiMovieDir) {
    defaultMF = new MediaFile(mf);
    defaultMF.replacePathForRenamedFolder(movie.getPathNIO(), newMovieDir);
    defaultMFext = "." + FilenameUtils.getExtension(defaultMF.getFilename());
    // }

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
        MediaFile trail = new MediaFile(mf);
        newFilename += "-trailer." + mf.getExtension();
        trail.setFile(newMovieDir.resolve(newFilename));
        newFiles.add(trail);
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

        newFilename += getStackingString(mf);
        if (mfsl != null && mfsl.size() > 0) {
          // internal values
          MediaFileSubtitle mfs = mfsl.get(0);
          if (!mfs.getLanguage().isEmpty()) {
            String lang = LanguageStyle.getLanguageCodeForStyle(mfs.getLanguage(), MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerLanguageStyle());
            if (StringUtils.isBlank(lang)) {
              lang = mfs.getLanguage();
            }
            newFilename += "." + lang;
          }
          if (mfs.isForced()) {
            newFilename += ".forced";
          }
        }
        newFilename += "." + mf.getExtension();

        MediaFile sub = new MediaFile(mf);
        sub.setFile(newMovieDir.resolve(newFilename));
        newFiles.add(sub);
        break;

      case NFO:
        if (MovieConnectors.isValidNFO(mf.getFileAsPath())) {
          List<MovieNfoNaming> nfonames = new ArrayList<>();
          if (newDestIsMultiMovieDir) {
            // Fixate the name regardless of setting
            nfonames.add(MovieNfoNaming.FILENAME_NFO);
          }
          else {
            nfonames = MovieModuleManager.MOVIE_SETTINGS.getMovieNfoFilenames();
          }
          for (MovieNfoNaming name : nfonames) {
            String newNfoName = movie.getNfoFilename(name, newFilename + ".avi");// dirty hack, but full filename needed
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
          if (!MovieModuleManager.MOVIE_SETTINGS.isMovieRenamerNfoCleanup()) {
            newFiles.add(new MediaFile(mf));
          }

        }
        break;

      case POSTER:
        for (MoviePosterNaming name : MovieArtworkHelper.getPosterNamesForMovie(movie)) {
          String newPosterName = MovieArtworkHelper.getPosterFilename(name, movie, newFilename);
          if (newPosterName != null && !newPosterName.isEmpty()) {
            String curExt = mf.getExtension().replaceAll("jpeg", "jpg"); // we only have one constant and only write jpg
            if (curExt.equalsIgnoreCase("tbn")) {
              String cont = mf.getContainerFormat();
              if (cont.equalsIgnoreCase("PNG")) {
                curExt = "png";
              }
              else if (cont.equalsIgnoreCase("JPEG")) {
                curExt = "jpg";
              }
            }
            if (!curExt.equals(FilenameUtils.getExtension(newPosterName))) {
              // match extension to not rename PNG to JPG and vice versa
              continue;
            }
          }
          if (StringUtils.isNotBlank(newPosterName)) {
            MediaFile pos = new MediaFile(mf);
            pos.setFile(newMovieDir.resolve(newPosterName));
            newFiles.add(pos);
          }
        }
        break;

      case FANART:
        for (MovieFanartNaming name : MovieArtworkHelper.getFanartNamesForMovie(movie)) {
          String newFanartName = MovieArtworkHelper.getFanartFilename(name, movie, newFilename);
          if (newFanartName != null && !newFanartName.isEmpty()) {
            String curExt = mf.getExtension().replaceAll("jpeg", "jpg"); // we only have one constant and only write jpg
            if (curExt.equalsIgnoreCase("tbn")) {
              String cont = mf.getContainerFormat();
              if (cont.equalsIgnoreCase("PNG")) {
                curExt = "png";
              }
              else if (cont.equalsIgnoreCase("JPEG")) {
                curExt = "jpg";
              }
            }
            if (!curExt.equals(FilenameUtils.getExtension(newFanartName))) {
              // match extension to not rename PNG to JPG and vice versa
              continue;
            }
          }
          if (StringUtils.isNotBlank(newFanartName)) {
            MediaFile fan = new MediaFile(mf);
            fan.setFile(newMovieDir.resolve(newFanartName));
            newFiles.add(fan);
          }
        }
        break;
      // *************
      // OK, from here we check only the settings
      // *************
      case BANNER:
        if (MovieModuleManager.MOVIE_SETTINGS.isImageBanner()) {
          defaultMFext = defaultMFext.toLowerCase(Locale.ROOT).replaceAll("jpeg", "jpg"); // don't write jpeg -> write jpg
          // reset filename: type.ext on single, <filename>-type.ext on MMD
          if (newDestIsMultiMovieDir) {
            defaultMF.setFilename(newFilename + "-" + mf.getType().name().toLowerCase(Locale.ROOT) + defaultMFext);
          }
          else {
            defaultMF.setFilename(mf.getType().name().toLowerCase(Locale.ROOT) + defaultMFext);
          }
          newFiles.add(defaultMF);
        }
        break;
      case CLEARART:
        if (MovieModuleManager.MOVIE_SETTINGS.isImageClearart()) {
          defaultMFext = defaultMFext.toLowerCase(Locale.ROOT).replaceAll("jpeg", "jpg"); // don't write jpeg -> write jpg
          // reset filename: type.ext on single, <filename>-type.ext on MMD
          if (newDestIsMultiMovieDir) {
            defaultMF.setFilename(newFilename + "-" + mf.getType().name().toLowerCase(Locale.ROOT) + defaultMFext);
          }
          else {
            defaultMF.setFilename(mf.getType().name().toLowerCase(Locale.ROOT) + defaultMFext);
          }
          newFiles.add(defaultMF);
        }
        break;
      case DISCART:
        if (MovieModuleManager.MOVIE_SETTINGS.isImageDiscart()) {
          defaultMFext = defaultMFext.toLowerCase(Locale.ROOT).replaceAll("jpeg", "jpg"); // don't write jpeg -> write jpg
          // reset filename: type.ext on single, <filename>-type.ext on MMD
          if (newDestIsMultiMovieDir) {
            defaultMF.setFilename(newFilename + "-disc" + defaultMFext);
          }
          else {
            defaultMF.setFilename("disc" + defaultMFext);
          }
          newFiles.add(defaultMF);
        }
        break;
      case LOGO:
      case CLEARLOGO:
        if (MovieModuleManager.MOVIE_SETTINGS.isImageLogo()) {
          defaultMFext = defaultMFext.toLowerCase(Locale.ROOT).replaceAll("jpeg", "jpg"); // don't write jpeg -> write jpg
          // reset filename: type.ext on single, <filename>-type.ext on MMD
          if (newDestIsMultiMovieDir) {
            defaultMF.setFilename(newFilename + "-" + mf.getType().name().toLowerCase(Locale.ROOT) + defaultMFext);
          }
          else {
            defaultMF.setFilename(mf.getType().name().toLowerCase(Locale.ROOT) + defaultMFext);
          }
          newFiles.add(defaultMF);
        }
        break;
      case THUMB:
        if (MovieModuleManager.MOVIE_SETTINGS.isImageThumb()) {
          defaultMFext = defaultMFext.toLowerCase(Locale.ROOT).replaceAll("jpeg", "jpg"); // don't write jpeg -> write jpg
          // reset filename: type.ext on single, <filename>-type.ext on MMD
          if (newDestIsMultiMovieDir) {
            defaultMF.setFilename(newFilename + "-" + mf.getType().name().toLowerCase(Locale.ROOT) + defaultMFext);
          }
          else {
            defaultMF.setFilename(mf.getType().name().toLowerCase(Locale.ROOT) + defaultMFext);
          }
          newFiles.add(defaultMF);
        }
        break;
      case EXTRAFANART:
        if (MovieModuleManager.MOVIE_SETTINGS.isImageExtraFanart() && !newDestIsMultiMovieDir) {
          newFiles.add(defaultMF);
        }
        break;
      case EXTRATHUMB:
        if (MovieModuleManager.MOVIE_SETTINGS.isImageExtraThumbs() && !newDestIsMultiMovieDir) {
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
      case VIDEO_EXTRA:
      default:
        newFiles.add(defaultMF);
        break;
    }

    return newFiles;
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
    if (MovieModuleManager.MOVIE_SETTINGS.isMovieRenamerSpaceSubstitution()) {
      delimiter = MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerSpaceReplacement();
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
    // replace optional group first
    Pattern regex = Pattern.compile("\\{(.*?)\\}");
    Matcher mat = regex.matcher(template);
    while (mat.find()) {
      template = template.replace(mat.group(0), replaceOptionalVariable(mat.group(1), movie, true));
    }
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
    // replace optional group first
    Pattern regex = Pattern.compile("\\{(.*?)\\}");
    Matcher mat = regex.matcher(template);
    while (mat.find()) {
      template = template.replace(mat.group(0), replaceOptionalVariable(mat.group(1), movie, false));
    }
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
   * gets the token value ($x) from specified movie object
   * 
   * @param movie
   *          our movie
   * @param token
   *          the $x token
   * @return value or empty string
   */
  public static String getTokenValue(Movie movie, String token) {
    String ret = "";
    MediaFile mf = new MediaFile();
    if (movie.getMediaFiles(MediaFileType.VIDEO).size() > 0) {
      mf = movie.getMediaFiles(MediaFileType.VIDEO).get(0);
    }

    switch (token.toUpperCase(Locale.ROOT)) {
      case "$T":
        ret = movie.getTitle();
        break;
      case "$1":
        ret = getFirstAlphaNum(movie.getTitle());
        break;
      case "$2":
        ret = getFirstAlphaNum(movie.getTitleSortable());
        break;
      case "$Y":
        ret = movie.getYear().equals("0") ? "" : movie.getYear();
        break;
      case "$O":
        ret = movie.getOriginalTitle();
        break;
      case "$M":
        if (movie.getMovieSet() != null
            && (movie.getMovieSet().getMovies().size() > 1 || MovieModuleManager.MOVIE_SETTINGS.isMovieRenamerCreateMoviesetForSingleMovie())) {
          ret = movie.getMovieSet().getTitleSortable();
        }
        break;
      case "$N":
        if (movie.getMovieSet() != null
            && (movie.getMovieSet().getMovies().size() > 1 || MovieModuleManager.MOVIE_SETTINGS.isMovieRenamerCreateMoviesetForSingleMovie())) {
          ret = movie.getMovieSet().getTitle();
        }
        break;
      case "$I":
        ret = movie.getImdbId();
        break;
      case "$E":
        ret = movie.getTitleSortable();
        break;
      case "$L":
        ret = movie.getSpokenLanguages();
        break;
      case "$C":
        if (movie.getCertification() != Certification.NOT_RATED) {
          ret = movie.getCertification().getName();
        }
        break;
      case "$U":
        if (movie.getEdition() != MovieEdition.NONE) {
          ret = movie.getEditionAsString();
        }
        break;
      case "$G":
        if (!movie.getGenres().isEmpty()) {
          MediaGenres genre = movie.getGenres().get(0);
          ret = genre.getLocalizedName();
        }
        break;
      case "$D":
        ret = movie.getDirector();
        break;
      case "$R":
        ret = mf.getVideoResolution();
        break;
      case "$3":
        if (StringUtils.isNotBlank(mf.getVideo3DFormat())) {
          ret = mf.getVideo3DFormat();
        }
        else if (movie.isVideoIn3D()) { // no MI info, but flag set from user
          ret = "3D";
        }
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
      case "$S":
        if (movie.getMediaSource() != MediaSource.UNKNOWN) {
          ret = movie.getMediaSource().toString();
        }
        break;
      case "$#":
        if (movie.getRating() > 0) {
          ret = String.valueOf(movie.getRating());
        }
        break;
      case "$K":
        if (!movie.getTags().isEmpty()) {
          ret = movie.getTags().get(0);
        }
      default:
        break;
    }

    return ret;
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
    String newDestination = template;

    // replace all $x parameters
    Pattern p = Pattern.compile("(\\$[\\w#])"); // # is for rating
    Matcher m = p.matcher(template);
    while (m.find()) {
      String value = getTokenValue(movie, m.group(1));
      newDestination = replaceToken(newDestination, m.group(1), value);
    }

    // replace empty brackets
    newDestination = newDestination.replaceAll("\\(\\)", "");
    newDestination = newDestination.replaceAll("\\[\\]", "");
    newDestination = newDestination.replaceAll("\\{\\}", "");

    // if there are multiple file separators in a row - strip them out
    if (SystemUtils.IS_OS_WINDOWS) {
      if (!forFilename) {
        // trim whitespace around directory sep
        newDestination = newDestination.replaceAll("\\s+\\\\", "\\\\");
        newDestination = newDestination.replaceAll("\\\\\\s+", "\\\\");
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
      }
      newDestination = newDestination.replaceAll("/{2,}", "/");
      newDestination = newDestination.replaceAll("^/", "");
    }

    // replace ALL directory separators, if we generate this for filenames!
    if (forFilename) {
      newDestination = newDestination.replaceAll("\\/", " ");
      newDestination = newDestination.replaceAll("\\\\", " ");
    }

    // replace multiple spaces with a single one
    newDestination = newDestination.replaceAll(" +", " ").trim();

    // replace spaces with underscores if needed
    if (MovieModuleManager.MOVIE_SETTINGS.isMovieRenamerSpaceSubstitution()) {
      String replacement = MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerSpaceReplacement();
      newDestination = newDestination.replace(" ", replacement);

      // also replace now multiple replacements with one to avoid strange looking results;
      // example:
      // Abraham Lincoln - Vapire Hunter -> Abraham-Lincoln---Vampire-Hunter
      newDestination = newDestination.replaceAll(Pattern.quote(replacement) + "+", replacement);
    }

    // ASCII replacement
    if (MovieModuleManager.MOVIE_SETTINGS.isAsciiReplacement()) {
      newDestination = StrgUtils.convertToAscii(newDestination, false);
    }

    // replace trailing dots and spaces (filename only!)
    if (forFilename) {
      newDestination = newDestination.replaceAll("[ \\.]+$", "");
    }

    return newDestination.trim();
  }

  private static String replaceToken(String destination, String token, String replacement) {
    String replacingCleaned = "";
    if (StringUtils.isNotBlank(replacement)) {
      // replace illegal characters
      // http://msdn.microsoft.com/en-us/library/windows/desktop/aa365247%28v=vs.85%29.aspx
      replacingCleaned = replaceInvalidCharacters(replacement);
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

  /**
   * MOVIES file.
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
   * copies file.
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
   * Check if the folder rename pattern is unique<br>
   * Unique true, when having at least a $T/$E-$Y combo or $I imdbId<br>
   * 
   * @param pattern
   *          the pattern to check the uniqueness for
   * @return true/false
   */
  public static boolean isFolderPatternUnique(String pattern) {
    if (((pattern.contains("$T") || pattern.contains("$E") || pattern.contains("$O")) && pattern.contains("$Y")) || pattern.contains("$I")) {
      return true;
    }
    return false;
  }

  /**
   * Check if the FILE rename pattern is valid<br>
   * What means, pattern has at least title set ($T|$E|$O)<br>
   * "empty" is considered as invalid - so not renaming files
   * 
   * @return true/false
   */
  public static boolean isFilePatternValid() {
    String pattern = MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerFilename().toUpperCase(Locale.ROOT).trim();

    if (pattern.contains("$T") || pattern.contains("$E") || pattern.contains("$O")) {
      return true;
    }
    return false;
  }
}
