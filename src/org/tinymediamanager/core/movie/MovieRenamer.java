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
package org.tinymediamanager.core.movie;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.movie.connector.MovieConnectors;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.util.StrgUtils;

/**
 * The Class MovieRenamer.
 * 
 * @author Manuel Laggner / Myron Boyle
 */
public class MovieRenamer {
  private final static Logger LOGGER = LoggerFactory.getLogger(MovieRenamer.class);

  private static void renameSubtitles(Movie m) {
    // build language lists
    Set<String> langArray = Utils.KEY_TO_LOCALE_MAP.keySet();

    for (MediaFile sub : m.getMediaFiles(MediaFileType.SUBTITLE)) {
      String lang = "";
      String forced = "";
      List<MediaFileSubtitle> mfsl = sub.getSubtitles();

      if (mfsl != null && mfsl.size() > 0) {
        // use internal values
        MediaFileSubtitle mfs = mfsl.get(0);
        lang = mfs.getLanguage();
        if (mfs.isForced()) {
          forced = ".forced";
        }
      }
      else {
        // detect from filename, if we don't have a MediaFileSubtitle entry!

        // FIXME: DOES NOT WORK, movie already renamed!!! - execute before movie rename?!
        // remove the filename of movie from subtitle, to ease parsing
        List<MediaFile> mfs = m.getMediaFiles(MediaFileType.VIDEO);
        String shortname = sub.getBasename().toLowerCase();
        if (mfs != null && mfs.size() > 0) {
          String vname = Utils.cleanStackingMarkers(mfs.get(0).getBasename()).toLowerCase();
          shortname = sub.getBasename().toLowerCase().replace(vname, "");
        }

        if (sub.getFilename().toLowerCase().contains("forced")) {
          // add "forced" prior language
          forced = ".forced";
          shortname = shortname.replaceAll("\\p{Punct}*forced", "");
        }
        // shortname = shortname.replaceAll("\\p{Punct}", "").trim(); // NEVER EVER!!!

        for (String s : langArray) {
          if (shortname.equalsIgnoreCase(s) || shortname.matches("(?i).*[ _.-]+" + s + "$")) {
            lang = Utils.getIso3LanguageFromLocalizedString(s);
            LOGGER.debug("found language '" + s + "' in subtitle; displaying it as '" + lang + "'");
            break;
          }
        }
      }

      // rebuild new filename
      String newSubName = "";

      if (sub.getStacking() == 0) {
        // fine, so match to first movie file
        MediaFile mf = m.getMediaFiles(MediaFileType.VIDEO).get(0);
        newSubName = mf.getBasename() + forced;
        if (!lang.isEmpty()) {
          newSubName += "." + lang;
        }
      }
      else {
        // with stacking info; try to match
        for (MediaFile mf : m.getMediaFiles(MediaFileType.VIDEO)) {
          if (mf.getStacking() == sub.getStacking()) {
            newSubName = mf.getBasename() + forced;
            if (!lang.isEmpty()) {
              newSubName += "." + lang;
            }
          }
        }
      }
      newSubName += "." + sub.getExtension();

      File newFile = new File(m.getPath(), newSubName);
      try {
        boolean ok = Utils.moveFileSafe(sub.getFile(), newFile);
        if (ok) {
          if (sub.getFilename().endsWith(".sub")) {
            // when having a .sub, also rename .idx (don't care if error)
            try {
              File oldidx = new File(sub.getPath(), sub.getFilename().replaceFirst("sub$", "idx"));
              File newidx = new File(newFile.getParent(), newSubName.replaceFirst("sub$", "idx"));
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
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, sub.getFilename(), "message.renamer.failedrename", new String[] { ":",
            e.getLocalizedMessage() }));
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

    // check if a datasource is set
    if (StringUtils.isEmpty(movie.getDataSource())) {
      LOGGER.error("no Datasource set");
      return;
    }

    // all the good & needed mediafiles
    ArrayList<MediaFile> needed = new ArrayList<MediaFile>();
    ArrayList<MediaFile> cleanup = new ArrayList<MediaFile>();

    LOGGER.info("Renaming movie: " + movie.getTitle());
    LOGGER.debug("movie year: " + movie.getYear());
    LOGGER.debug("movie path: " + movie.getPath());
    LOGGER.debug("movie isDisc?: " + movie.isDisc());
    LOGGER.debug("movie isMulti?: " + movie.isMultiMovieDir());
    if (movie.getMovieSet() != null) {
      LOGGER.debug("movieset: " + movie.getMovieSet().getTitle());
    }
    LOGGER.debug("path expression: " + MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerPathname());
    LOGGER.debug("file expression: " + MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerFilename());

    String newPathname = createDestinationForFoldername(MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerPathname(), movie);
    String oldPathname = movie.getPath();

    if (!newPathname.isEmpty()) {
      newPathname = movie.getDataSource() + File.separator + newPathname;
      File srcDir = new File(oldPathname);
      File destDir = new File(newPathname);
      if (!srcDir.getAbsolutePath().equals(destDir.getAbsolutePath())) {

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
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, srcDir.getPath(), "message.renamer.failedrename", new String[] { ":",
                e.getLocalizedMessage() }));
          }
          if (!ok) {
            // FIXME: when we were not able to rename folder, display error msg
            // and abort!!!
            LOGGER.error("Could not move to destination '" + destDir + "' - NOT renaming folder");
            return;
          }
        }
        else if (movie.isMultiMovieDir() && !newDestIsMultiMovieDir) {
          // ######################################################################
          // ## 2) MMD movie -> normal movie (upgrade)
          // ######################################################################
          LOGGER.trace("Upgrading movie into it's own dir :) " + newPathname);
          boolean ok = destDir.mkdirs();
          if (!ok) {
            LOGGER.error("Could not create destination '" + destDir + "' - NOT renaming folder ('upgrade' movie)");
            // well, better not to rename
            return;
          }
          movie.setMultiMovieDir(false);
        }
        else {
          // ######################################################################
          // ## Can be
          // ## 3) MMD movie -> MMD movie (but foldername possible changed)
          // ## 4) normal movie -> MMD movie (downgrade)
          // ## either way - check & create dest folder
          // ######################################################################
          LOGGER.trace("New movie path is a MMD :( " + newPathname);
          if (!destDir.exists()) { // if existent, all is good -> MMD (FIXME: kinda, we *might* have another full movie in there)
            boolean ok = destDir.mkdirs(); // else create dir, if foldername changes
            if (!ok) {
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
      newPathname = movie.getPath();
    }

    // ######################################################################
    // ## mark ALL existing and known files for cleanup (clone!!)
    // ######################################################################
    for (MovieNfoNaming s : MovieNfoNaming.values()) {
      String nfoFilename = movie.getNfoFilename(s);
      if (nfoFilename.isEmpty()) {
        continue;
      }
      // mark all known variants for cleanup
      MediaFile del = new MediaFile(new File(movie.getPath(), nfoFilename), MediaFileType.NFO);
      cleanup.add(del);
    }
    for (MoviePosterNaming s : MoviePosterNaming.values()) {
      MediaFile del = new MediaFile(new File(movie.getPath(), MovieArtworkHelper.getPosterFilename(s, movie)), MediaFileType.POSTER);
      cleanup.add(del);
    }
    for (MovieFanartNaming s : MovieFanartNaming.values()) {
      MediaFile del = new MediaFile(new File(movie.getPath(), MovieArtworkHelper.getFanartFilename(s, movie)), MediaFileType.FANART);
      cleanup.add(del);
    }
    // cleanup ALL MFs
    for (MediaFile del : movie.getMediaFiles()) {
      cleanup.add(new MediaFile(del));
    }
    cleanup.removeAll(Collections.singleton(null)); // remove all NULL ones!

    // after we cleanup our old MFs, update to new path now
    movie.setPath(newPathname);
    // movie.saveToDb(); // FIXME: TBD?!

    // BASENAME
    String newVideoBasename = "";
    if (MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerFilename().trim().isEmpty()) {
      // we are NOT renaming any files, so we keep the same name on renaming ;)
      newVideoBasename = Utils.cleanStackingMarkers(movie.getMediaFiles(MediaFileType.VIDEO).get(0).getBasename());
    }
    else {
      // since we rename, generate the new basename
      MediaFile ftr = generateFilename(movie, movie.getMediaFiles(MediaFileType.VIDEO).get(0), newVideoBasename).get(0); // there can be only one
      newVideoBasename = Utils.cleanStackingMarkers(ftr.getBasename());
    }
    LOGGER.trace("Our new basename for renaming: " + newVideoBasename);

    // ######################################################################
    // ## test VIDEO rename
    // ######################################################################
    for (MediaFile vid : movie.getMediaFiles(MediaFileType.VIDEO)) {
      LOGGER.debug("testing file " + vid.getFile().getAbsolutePath());
      File f = vid.getFile();
      boolean testRenameOk = false;
      for (int i = 0; i < 5; i++) {
        testRenameOk = f.renameTo(f); // haahaa, try to rename to itself :P
        if (testRenameOk) {
          break; // ok it worked, step out
        }
        if (!f.exists()) {
          LOGGER.debug("Hmmm... file " + f + " does not even exists; delete from DB");
          // delete from MF or ignore for later cleanup (but better now!)
          movie.removeFromMediaFiles(vid);
          testRenameOk = true; // we "tested" this ok
          break;
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
        LOGGER.warn("File " + vid.getFile().getAbsolutePath() + " is not accessible!");
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, vid.getFilename(), "message.renamer.failedrename"));
        return;
      }
    }

    // ######################################################################
    // ## rename VIDEO (move 1:1)
    // ######################################################################
    for (MediaFile vid : movie.getMediaFiles(MediaFileType.VIDEO)) {
      LOGGER.trace("Rename 1:1 " + vid.getType() + " " + vid.getFile().getAbsolutePath());
      MediaFile newMF = generateFilename(movie, vid, newVideoBasename).get(0); // there can be only one
      boolean ok = movieFile(vid.getFile(), newMF.getFile());
      if (ok) {
        vid.setFile(newMF.getFile()); // update
      }
      needed.add(vid); // add vid, since we're updating existing MF object
    }

    // ######################################################################
    // ## rename NFO, POSTER, FANART (copy 1:N)
    // ######################################################################
    // we can have multiple ones, just get the newest one and copy(overwrite) them to all needed
    ArrayList<MediaFile> mfs = new ArrayList<MediaFile>();
    mfs.add(movie.getNewestMediaFilesOfType(MediaFileType.NFO));
    mfs.add(movie.getNewestMediaFilesOfType(MediaFileType.FANART));
    mfs.add(movie.getNewestMediaFilesOfType(MediaFileType.POSTER));
    mfs.removeAll(Collections.singleton(null)); // remove all NULL ones!
    for (MediaFile mf : mfs) {
      LOGGER.trace("Rename 1:N " + mf.getType() + " " + mf.getFile().getAbsolutePath());
      ArrayList<MediaFile> newMFs = generateFilename(movie, mf, newVideoBasename); // 1:N
      for (MediaFile newMF : newMFs) {
        if (newMF.getType() == MediaFileType.FANART || newMF.getType() == MediaFileType.POSTER) {
          posterRenamed = true;
          fanartRenamed = true;
        }
        boolean ok = copyFile(mf.getFile(), newMF.getFile());
        if (ok) {
          needed.add(newMF);
        }
      }
    }

    // ######################################################################
    // ## rename all other types (copy 1:1)
    // ######################################################################
    mfs = new ArrayList<MediaFile>();
    mfs.addAll(movie.getMediaFilesExceptType(MediaFileType.VIDEO, MediaFileType.NFO, MediaFileType.POSTER, MediaFileType.FANART));
    mfs.removeAll(Collections.singleton(null)); // remove all NULL ones!
    for (MediaFile other : mfs) {
      LOGGER.trace("Rename 1:1 " + other.getType() + " " + other.getFile().getAbsolutePath());

      ArrayList<MediaFile> newMFs = generateFilename(movie, other, newVideoBasename); // 1:N
      newMFs.removeAll(Collections.singleton(null)); // remove all NULL ones!
      for (MediaFile newMF : newMFs) {
        boolean ok = copyFile(other.getFile(), newMF.getFile());
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
    for (MediaFile gfx : movie.getMediaFiles()) {
      if (gfx.isGraphic()) {
        // FIXME: use File()
        ImageCache.invalidateCachedImage(gfx.getPath() + File.separator + gfx.getFilename());
      }
    }

    // remove duplicate MediaFiles
    Set<MediaFile> newMFs = new LinkedHashSet<MediaFile>(needed);
    needed.clear();
    needed.addAll(newMFs);

    movie.removeAllMediaFiles();
    movie.addToMediaFiles(needed);
    movie.setPath(newPathname);
    movie.saveToDb();

    // cleanup & rename subtitle files
    renameSubtitles(movie);

    movie.gatherMediaFileInformation(false);
    movie.saveToDb();

    // rewrite NFO if it's a MP NFO and there was a change with poster/fanart
    if (MovieModuleManager.MOVIE_SETTINGS.getMovieConnector() == MovieConnectors.MP && (posterRenamed || fanartRenamed)) {
      movie.writeNFO();
    }

    // ######################################################################
    // ## CLEANUP
    // ######################################################################
    LOGGER.info("Cleanup...");
    for (int i = cleanup.size() - 1; i >= 0; i--) {
      // cleanup files which are not needed
      if (!needed.contains(cleanup.get(i))) {
        MediaFile cl = cleanup.get(i);
        if (cl.getFile().equals(new File(movie.getDataSource())) || cl.getFile().equals(new File(movie.getPath()))
            || cl.getFile().equals(new File(oldPathname))) {
          LOGGER.warn("Wohoo! We tried to remove complete datasource / movie folder. Nooo way...! " + cl.getType() + ": "
              + cl.getFile().getAbsolutePath());
          // happens when iterating eg over the getNFONaming and we return a "" string.
          // then the path+filename = movie path and we want to delete :/
          // do not show an error anylonger, just silently ignore...
          // MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, cl.getFile(), "message.renamer.failedrename"));
          // return; // rename failed
          continue;
        }

        if (cl.getFile().exists()) { // unneeded, but for not displaying wrong deletes in logger...
          LOGGER.debug("Deleting " + cl.getFile());
          Utils.deleteFileSafely(cl.getFile(), movie.getDataSource());
        }
        File[] list = cl.getFile().getParentFile().listFiles();
        if (list != null && list.length == 0) {
          // if directory is empty, delete it as well
          LOGGER.debug("Deleting empty Directory " + cl.getFile().getParentFile().getAbsolutePath());
          FileUtils.deleteQuietly(cl.getFile().getParentFile()); // no need for backup ;)
        }
      }
    }

    // clean all non tmm nfos
    if (MovieModuleManager.MOVIE_SETTINGS.isMovieRenamerNfoCleanup()) {
      File[] content = new File(movie.getPath()).listFiles();
      if (content != null) {
        for (File file : content) {
          if (file.isFile() && file.getName().toLowerCase().endsWith(".nfo")) {
            // check if it's a tmm nfo
            boolean supported = false;
            for (MediaFile nfo : movie.getMediaFiles(MediaFileType.NFO)) {
              if (nfo.getFilename().equals(file.getName())) {
                supported = true;
              }
            }
            if (!supported) {
              LOGGER.debug("Deleting " + file);
              Utils.deleteFileSafely(file, movie.getDataSource());
            }
          }
        }
      }
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
    ArrayList<MediaFile> newFiles = new ArrayList<MediaFile>();
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
      newPathname = Utils.relPath(movie.getDataSource(), movie.getPath());
    }
    String newMovieDir = movie.getDataSource() + File.separatorChar + newPathname + File.separatorChar;

    String newFilename = videoFileName;
    if (newFilename == null || newFilename.isEmpty()) {
      newFilename = MovieRenamer.createDestinationForFilename(MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerFilename(), movie);
    }

    // extra clone, just for easy adding the "default" ones ;)
    MediaFile defaultMF = null;
    if (!newDestIsMultiMovieDir) {
      defaultMF = new MediaFile(mf);
      defaultMF.replacePathForRenamedFolder(new File(movie.getPath()), new File(newMovieDir));
    }

    switch (mf.getType()) {
      case VIDEO:
        MediaFile vid = new MediaFile(mf);
        if (movie.isDisc() || mf.isDiscFile()) {
          // just replace new path and return file (do not change names!)
          vid.replacePathForRenamedFolder(new File(movie.getPath()), new File(newMovieDir));
        }
        else {
          newFilename += getStackingString(mf);
          newFilename += "." + mf.getExtension();
          vid.setFile(new File(newMovieDir, newFilename));
        }
        newFiles.add(vid);
        break;

      case TRAILER:
        MediaFile trail = new MediaFile(mf);
        newFilename += "-trailer." + mf.getExtension();
        trail.setFile(new File(newMovieDir, newFilename));
        newFiles.add(trail);
        break;

      case SAMPLE:
        MediaFile sample = new MediaFile(mf);
        newFilename += "-sample." + mf.getExtension();
        sample.setFile(new File(newMovieDir, newFilename));
        newFiles.add(sample);
        break;

      case SUBTITLE:
        List<MediaFileSubtitle> mfsl = mf.getSubtitles();

        newFilename += getStackingString(mf);
        if (mfsl != null && mfsl.size() > 0) {
          // internal values
          MediaFileSubtitle mfs = mfsl.get(0);
          if (mfs.isForced()) {
            newFilename += ".forced";
          }
          if (!mfs.getLanguage().isEmpty()) {
            newFilename += "." + mfs.getLanguage();
          }
        }
        newFilename += "." + mf.getExtension();

        MediaFile sub = new MediaFile(mf);
        sub.setFile(new File(newMovieDir, newFilename));
        newFiles.add(sub);
        break;

      case NFO:
        List<MovieNfoNaming> nfonames = new ArrayList<MovieNfoNaming>();
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
          nfo.setFile(new File(newMovieDir, newNfoName));
          newFiles.add(nfo);
        }
        break;

      case POSTER:
        List<MoviePosterNaming> posternames = new ArrayList<MoviePosterNaming>();
        if (newDestIsMultiMovieDir) {
          // Fixate the name regardless of setting
          posternames.add(MoviePosterNaming.FILENAME_POSTER_JPG);
          posternames.add(MoviePosterNaming.FILENAME_POSTER_PNG);
        }
        else {
          posternames = MovieModuleManager.MOVIE_SETTINGS.getMoviePosterFilenames();
        }
        for (MoviePosterNaming name : posternames) {
          String newPosterName = MovieArtworkHelper.getPosterFilename(name, movie, newFilename + ".avi"); // dirty hack, but full filename needed
          if (newPosterName != null && !newPosterName.isEmpty()) {
            String curExt = mf.getExtension();
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
          MediaFile pos = new MediaFile(mf);
          pos.setFile(new File(newMovieDir, newPosterName));
          newFiles.add(pos);
        }
        break;

      case FANART:
        List<MovieFanartNaming> fanartnames = new ArrayList<MovieFanartNaming>();
        if (newDestIsMultiMovieDir) {
          // Fixate the name regardless of setting
          fanartnames.add(MovieFanartNaming.FILENAME_FANART_JPG);
          fanartnames.add(MovieFanartNaming.FILENAME_FANART_PNG);
        }
        else {
          fanartnames = MovieModuleManager.MOVIE_SETTINGS.getMovieFanartFilenames();
        }
        for (MovieFanartNaming name : fanartnames) {
          String newFanartName = MovieArtworkHelper.getFanartFilename(name, movie, newFilename + ".avi");// dirty hack, but full filename needed
          if (newFanartName != null && !newFanartName.isEmpty()) {
            String curExt = mf.getExtension();
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
          MediaFile fan = new MediaFile(mf);
          fan.setFile(new File(newMovieDir, newFanartName));
          newFiles.add(fan);
        }
        break;
      // *************
      // OK, from here we check only the settings
      // if we are in a MMD, NULL will be added
      // *************
      case BANNER:
        if (MovieModuleManager.MOVIE_SETTINGS.isImageBanner()) {
          newFiles.add(defaultMF);
        }
        break;
      case CLEARART:
        if (MovieModuleManager.MOVIE_SETTINGS.isImageClearart()) {
          newFiles.add(defaultMF);
        }
        break;
      case DISCART:
        if (MovieModuleManager.MOVIE_SETTINGS.isImageDiscart()) {
          newFiles.add(defaultMF);
        }
        break;
      case EXTRAFANART:
        if (MovieModuleManager.MOVIE_SETTINGS.isImageExtraFanart()) {
          newFiles.add(defaultMF);
        }
        break;
      case EXTRATHUMB:
        if (MovieModuleManager.MOVIE_SETTINGS.isImageExtraThumbs()) {
          newFiles.add(defaultMF);
        }
        break;
      case LOGO:
        if (MovieModuleManager.MOVIE_SETTINGS.isImageLogo()) {
          newFiles.add(defaultMF);
        }
        break;
      case THUMB:
        if (MovieModuleManager.MOVIE_SETTINGS.isImageThumb()) {
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
    String stacking = Utils.getStackingMarker(mf.getFilename());
    String delimiter = " ";
    if (MovieModuleManager.MOVIE_SETTINGS.isMovieRenamerSpaceSubstitution()) {
      delimiter = MovieModuleManager.MOVIE_SETTINGS.getMovieRenamerSpaceReplacement();
    }
    if (!stacking.isEmpty()) {
      return delimiter + stacking;
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
   * @param movie
   * @param forFilename
   * @return
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

    // replace token title ($T)
    if (newDestination.contains("$T")) {
      newDestination = replaceToken(newDestination, "$T", movie.getTitle());
    }

    // replace token first letter of title ($1)
    if (newDestination.contains("$1")) {
      newDestination = replaceToken(newDestination, "$1", StringUtils.isNotBlank(movie.getTitle()) ? movie.getTitle().substring(0, 1).toUpperCase()
          : "");
    }

    // replace token first letter of sort title ($2)
    if (newDestination.contains("$2")) {
      newDestination = replaceToken(newDestination, "$2", StringUtils.isNotBlank(movie.getTitleSortable()) ? movie.getTitleSortable().substring(0, 1)
          .toUpperCase() : "");
    }

    // replace token year ($Y)
    if (newDestination.contains("$Y")) {
      if (movie.getYear().equals("0")) {
        newDestination = newDestination.replace("$Y", "");
      }
      else {
        newDestination = replaceToken(newDestination, "$Y", movie.getYear());
      }
    }

    // replace token orignal title ($O)
    if (newDestination.contains("$O")) {
      newDestination = replaceToken(newDestination, "$O", movie.getOriginalTitle());
    }

    // replace token Movie set title - sorted ($M)
    if (newDestination.contains("$M")) {
      if (movie.getMovieSet() != null
          && (movie.getMovieSet().getMovies().size() > 1 || MovieModuleManager.MOVIE_SETTINGS.isMovieRenamerCreateMoviesetForSingleMovie())) {
        newDestination = replaceToken(newDestination, "$M", movie.getMovieSet().getTitleSortable());
      }
      else {
        newDestination = newDestination.replace("$M", "");
      }
    }

    // replace token Movie set title ($N)
    if (newDestination.contains("$N")) {
      if (movie.getMovieSet() != null
          && (movie.getMovieSet().getMovies().size() > 1 || MovieModuleManager.MOVIE_SETTINGS.isMovieRenamerCreateMoviesetForSingleMovie())) {
        newDestination = replaceToken(newDestination, "$N", movie.getMovieSet().getTitle());
      }
      else {
        newDestination = newDestination.replace("$N", "");
      }
    }

    // replace token IMDBid ($I)
    if (newDestination.contains("$I")) {
      newDestination = replaceToken(newDestination, "$I", movie.getImdbId());
    }

    // replace token sort title ($E)
    if (newDestination.contains("$E")) {
      newDestination = replaceToken(newDestination, "$E", movie.getTitleSortable());
    }

    // replace token language ($L)
    if (newDestination.contains("$L")) {
      newDestination = replaceToken(newDestination, "$L", movie.getSpokenLanguages());
    }

    // replace token certification ($C)
    if (newDestination.contains("$C")) {
      if (movie.getCertification() != Certification.NOT_RATED) {
        newDestination = replaceToken(newDestination, "$C", movie.getCertification().getName());
      }
      else {
        newDestination = newDestination.replace("$C", "");
      }
    }

    // replace token genre ($G)
    if (newDestination.contains("$G")) {
      if (!movie.getGenres().isEmpty()) {
        MediaGenres genre = movie.getGenres().get(0);
        newDestination = replaceToken(newDestination, "$G", genre.getLocalizedName());
      }
      else {
        newDestination = newDestination.replace("$G", "");
      }
    }

    // replace token director ($D)
    if (newDestination.contains("$D")) {
      newDestination = replaceToken(newDestination, "$D", movie.getDirector());
    }
    else {
      newDestination = newDestination.replace("$D", "");
    }

    if (movie.getMediaFiles(MediaFileType.VIDEO).size() > 0) {
      MediaFile mf = movie.getMediaFiles(MediaFileType.VIDEO).get(0);
      // replace token resolution ($R)
      if (newDestination.contains("$R")) {
        newDestination = replaceToken(newDestination, "$R", mf.getVideoResolution());
      }

      // replace token 3D format ($3)
      if (newDestination.contains("$3")) {
        // if there is 3D info from MI, take this
        if (StringUtils.isNotBlank(mf.getVideo3DFormat())) {
          newDestination = replaceToken(newDestination, "$3", mf.getVideo3DFormat());
        }
        // no MI info, but flag set from user
        else if (movie.isVideoIn3D()) {
          newDestination = replaceToken(newDestination, "$3", "3D");
        }
        // strip unneeded token
        else {
          newDestination = replaceToken(newDestination, "$3", "");
        }
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
      newDestination = newDestination.replace("$3", "");
      newDestination = newDestination.replace("$A", "");
      newDestination = newDestination.replace("$V", "");
      newDestination = newDestination.replace("$F", "");
    }

    // replace token media source (BluRay|DVD|TV|...) ($S)
    if (newDestination.contains("$S")) {
      if (movie.getMediaSource() != MovieMediaSource.UNKNOWN) {
        newDestination = newDestination.replaceAll("\\$S", movie.getMediaSource().toString());
      }
      else {
        newDestination = newDestination.replaceAll("\\$S", "");
      }
    }

    // replace empty brackets
    newDestination = newDestination.replaceAll("\\(\\)", "");
    newDestination = newDestination.replaceAll("\\[\\]", "");
    newDestination = newDestination.replaceAll("\\{\\}", "");

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

    // replace trailing dots and spaces
    newDestination = newDestination.replaceAll("[ \\.]+$", "");

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
  private static boolean movieFile(File oldFilename, File newFilename) {
    try {
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
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, oldFilename, "message.renamer.failedrename", new String[] { ":",
          e.getLocalizedMessage() }));
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
  private static boolean copyFile(File oldFilename, File newFilename) {
    if (!oldFilename.equals(newFilename)) {
      LOGGER.info("copy file " + oldFilename + " to " + newFilename);
      try {
        FileUtils.copyFile(oldFilename, newFilename, true);
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
   * @return true/false
   */
  public static boolean isFolderPatternUnique(String pattern) {
    if (((pattern.contains("$T") || pattern.contains("$E")) && pattern.contains("$Y")) || pattern.contains("$I")) {
      return true;
    }
    return false;
  }
}
