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
package org.tinymediamanager.core.movie;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileSubtitle;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.movie.connector.MovieConnectors;

/**
 * The Class MovieRenamer.
 * 
 * @author Manuel Laggner / Myron Boyle
 */
public class MovieRenamer {

  /** The Constant LOGGER. */
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
        String vname = Utils.cleanStackingMarkers(m.getMediaFiles(MediaFileType.VIDEO).get(0).getBasename()).toLowerCase();
        String shortname = sub.getBasename().toLowerCase().replace(vname, "");

        if (sub.getFilename().toLowerCase().contains("forced")) {
          // add "forced" prior language
          forced = ".forced";
          shortname = shortname.replaceAll("\\p{Punct}*forced", "");
        }
        // shortname = shortname.replaceAll("\\p{Punct}", "").trim(); // NEVER EVER!!!

        for (String l : langArray) {
          if (shortname.equalsIgnoreCase(l) || shortname.matches("(?i).*[ _.-]+" + l + "$")) {
            lang = Utils.getDisplayLanguage(l);
            LOGGER.debug("found language '" + l + "' in subtitle; displaying it as '" + lang + "'");
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
    if (movie.getMovieSet() != null) {
      LOGGER.debug("movieset: " + movie.getMovieSet().getTitle());
    }
    LOGGER.debug("path expression: " + Globals.settings.getMovieSettings().getMovieRenamerPathname());
    LOGGER.debug("file expression: " + Globals.settings.getMovieSettings().getMovieRenamerFilename());

    String newPathname = createDestination(Globals.settings.getMovieSettings().getMovieRenamerPathname(), movie);
    String oldPathname = movie.getPath();
    boolean resetMultidir = false;

    if (!newPathname.isEmpty()) {
      newPathname = movie.getDataSource() + File.separator + newPathname;
      File srcDir = new File(oldPathname);
      File destDir = new File(newPathname);
      // move directory if needed
      if (!srcDir.equals(destDir)) {
        if (!movie.isMultiMovieDir()) {
          boolean ok = false;
          try {
            // FileUtils.moveDirectory(srcDir, destDir);
            ok = Utils.moveDirectorySafe(srcDir, destDir);
            if (ok) {
              movie.updateMediaFilePath(destDir);
              movie.setPath(newPathname);
              movie.saveToDb();
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
            return;
          }
        }
        else {
          // movie in multidir; just create structure...
          if (!destDir.exists()) {
            destDir.mkdirs();
          }
          resetMultidir = true;
        }
      }
    }
    else {
      LOGGER.info("Folder rename settings were empty - NOT renaming folder");
      // set it to current for file renaming
      newPathname = movie.getPath();
    }

    // if empty, do not rename file, but DO move them to movie root
    boolean renameFiles = !Globals.settings.getMovieSettings().getMovieRenamerFilename().isEmpty();

    // cleanup with old movie name
    for (MovieNfoNaming s : MovieNfoNaming.values()) {
      // mark all known variants for cleanup
      MediaFile del = new MediaFile(new File(movie.getPath(), movie.getNfoFilename(s)));
      cleanup.add(del);
    }
    for (MoviePosterNaming s : MoviePosterNaming.values()) {
      // mark all known variants for cleanup
      MediaFile del = new MediaFile(new File(movie.getPath(), movie.getPosterFilename(s)));
      cleanup.add(del);
    }
    for (MovieFanartNaming s : MovieFanartNaming.values()) {
      // mark all known variants for cleanup
      MediaFile del = new MediaFile(new File(movie.getPath(), movie.getFanartFilename(s)));
      cleanup.add(del);
    }

    // ######################################################################
    // ## rename VIDEO
    // ######################################################################
    String newMovieFilename = "";
    for (MediaFile vid : movie.getMediaFiles(MediaFileType.VIDEO)) {
      LOGGER.debug("testing file " + vid.getFile().getAbsolutePath());
      File f = vid.getFile();
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
        LOGGER.warn("File " + vid.getFile().getAbsolutePath() + " is not accessible!");
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, vid.getFilename(), "message.renamer.failedrename"));
        return;
      }
    }
    for (MediaFile vid : movie.getMediaFiles(MediaFileType.VIDEO)) {
      LOGGER.info("rename file " + vid.getFile().getAbsolutePath());

      String newFilename = vid.getFilename();
      // String newPath = movie.getPath() + File.separator;
      String fileExtension = FilenameUtils.getExtension(vid.getFilename());

      if (!movie.isDisc() || vid.isDiscFile()) {
        cleanup.add(new MediaFile(vid)); // mark old file for cleanup (clone current)
        if (renameFiles) {
          // create new filename according to template
          newFilename = createDestination(Globals.settings.getMovieSettings().getMovieRenamerFilename(), movie);
          // is there any stacking information in the filename?
          // use vid.getStacking() != 0 for custom stacking format?
          String stacking = Utils.getStackingMarker(vid.getFilename());
          if (!stacking.isEmpty()) {
            newFilename += " " + stacking;
          }
          else if (vid.getStacking() != 0) {
            newFilename += " CD" + vid.getStacking();
          }
          newFilename += "." + fileExtension;
        }

        // save new movie filename for further operations
        if (StringUtils.isBlank(newMovieFilename)) {
          newMovieFilename = newFilename;
        }

        MediaFile newMF = new MediaFile(vid);
        File newFile = new File(newPathname, newFilename);
        try {
          boolean ok = Utils.moveFileSafe(vid.getFile(), newFile);
          if (ok) {
            newMF.setPath(newPathname);
            newMF.setFilename(newFilename);
          }
          else {
            return; // rename failed
          }
        }
        catch (FileNotFoundException e) {
          LOGGER.error("error moving video file - file not found", e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, vid.getFilename(), "message.renamer.failedrename", new String[] { ":",
              e.getLocalizedMessage() }));
          return; // rename failed
        }
        catch (Exception e) {
          LOGGER.error("error moving video file", e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, vid.getFilename(), "message.renamer.failedrename", new String[] { ":",
              e.getLocalizedMessage() }));
          return; // rename failed
        }
        needed.add(newMF);
      }
      else {
        LOGGER.info("Movie is a DVD/BluRay disc folder - NOT renaming file");
        needed.add(vid); // but keep it
      }
    }

    MediaFile mf = null;

    // ######################################################################
    // ## rename NFO
    // ######################################################################
    List<MediaFile> mfl = movie.getMediaFiles(MediaFileType.NFO);
    if (mfl != null && mfl.size() > 0) {
      mf = mfl.get(0);
      cleanup.add(new MediaFile(mf)); // mark old file for cleanup (clone current)
      String newFilename = mf.getFilename();
      // String newPath = movie.getPath() + File.separator;

      List<MovieNfoNaming> nfonames = new ArrayList<MovieNfoNaming>();
      if (movie.isMultiMovieDir()) {
        // Fixate the name regardless of setting
        nfonames.add(MovieNfoNaming.FILENAME_NFO);
      }
      else {
        nfonames = Globals.settings.getMovieSettings().getMovieNfoFilenames();
      }
      for (MovieNfoNaming name : nfonames) {
        MediaFile newMF = new MediaFile(mf);
        newFilename = movie.getNfoFilename(name, newMovieFilename);
        File newFile = new File(newPathname, newFilename);
        try {
          boolean ok = copyFile(mf.getFile(), newFile);
          if (ok) {
            newMF.setPath(newPathname);
            newMF.setFilename(newFilename);
          }
        }
        catch (Exception e) {
          LOGGER.error("error renaming Nfo", e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, mf.getFilename(), "message.renamer.failedrename", new String[] { ":",
              e.getLocalizedMessage() }));
        }
        needed.add(newMF);
      }
    }

    // ######################################################################
    // ## rename POSTER
    // ######################################################################
    mfl = movie.getMediaFiles(MediaFileType.POSTER);
    if (mfl != null && mfl.size() > 0) {
      mf = mfl.get(0);
      cleanup.add(new MediaFile(mf)); // mark old file for cleanup (clone current)
      String newFilename = mf.getFilename();
      // String newPath = movie.getPath() + File.separator;

      List<MoviePosterNaming> posternames = new ArrayList<MoviePosterNaming>();
      if (movie.isMultiMovieDir()) {
        // Fixate the name regardless of setting
        posternames.add(MoviePosterNaming.FILENAME_POSTER_JPG);
        posternames.add(MoviePosterNaming.FILENAME_POSTER_PNG);
      }
      else {
        posternames = Globals.settings.getMovieSettings().getMoviePosterFilenames();
      }
      for (MoviePosterNaming name : posternames) {
        newFilename = movie.getPosterFilename(name, newMovieFilename);
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
        if (!curExt.equals(FilenameUtils.getExtension(newFilename))) {
          // match extension to not rename PNG to JPG and vice versa
          continue;
        }
        posterRenamed = true;

        MediaFile newMF = new MediaFile(mf);
        File newFile = new File(newPathname, newFilename);
        try {
          boolean ok = copyFile(mf.getFile(), newFile);
          if (ok) {
            newMF.setPath(newPathname);
            newMF.setFilename(newFilename);
          }
        }
        catch (Exception e) {
          LOGGER.error("error renaming poster", e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, mf.getFilename(), "message.renamer.failedrename", new String[] { ":",
              e.getLocalizedMessage() }));
        }
        needed.add(newMF);
      }
    }

    // ######################################################################
    // ## rename FANART
    // ######################################################################
    mfl = movie.getMediaFiles(MediaFileType.FANART);
    if (mfl != null && mfl.size() > 0) {
      mf = mfl.get(0);
      cleanup.add(new MediaFile(mf)); // mark old file for cleanup (clone current)

      String newFilename = mf.getFilename();
      // String newPath = movie.getPath() + File.separator;

      List<MovieFanartNaming> fanartnames = new ArrayList<MovieFanartNaming>();
      if (movie.isMultiMovieDir()) {
        // Fixate the name regardless of setting
        fanartnames.add(MovieFanartNaming.FILENAME_FANART_JPG);
        fanartnames.add(MovieFanartNaming.FILENAME_FANART_PNG);
      }
      else {
        fanartnames = Globals.settings.getMovieSettings().getMovieFanartFilenames();
      }
      for (MovieFanartNaming name : fanartnames) {
        newFilename = movie.getFanartFilename(name, newMovieFilename);
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
        if (!curExt.equals(FilenameUtils.getExtension(newFilename))) {
          // match extension to not rename PNG to JPG and vice versa
          continue;
        }
        fanartRenamed = true;

        MediaFile newMF = new MediaFile(mf);
        File newFile = new File(newPathname, newFilename);
        try {
          boolean ok = copyFile(mf.getFile(), newFile);
          if (ok) {
            newMF.setPath(newPathname);
            newMF.setFilename(newFilename);
          }
        }
        catch (Exception e) {
          LOGGER.error("error renaming fanart", e);
          MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, mf.getFilename(), "message.renamer.failedrename", new String[] { ":",
              e.getLocalizedMessage() }));
        }
        needed.add(newMF);
      }
    }

    // ######################################################################
    // ## rename TRAILER
    // ######################################################################
    mfl = movie.getMediaFiles(MediaFileType.TRAILER);
    if (mfl != null && mfl.size() > 0) {
      mf = mfl.get(0);
      cleanup.add(new MediaFile(mf)); // mark old file for cleanup (clone current)
      String newFilename = mf.getFilename();
      // String newPath = movie.getPath() + File.separator;
      String fileExtension = FilenameUtils.getExtension(mf.getFilename());
      newFilename = createDestination(Globals.settings.getMovieSettings().getMovieRenamerFilename(), movie) + "-trailer." + fileExtension;
      MediaFile newMF = new MediaFile(mf);
      File newFile = new File(newPathname, newFilename);
      try {
        boolean ok = Utils.moveFileSafe(mf.getFile(), newFile);
        if (ok) {
          newMF.setPath(newPathname);
          newMF.setFilename(newFilename);
        }
      }
      catch (Exception e) {
        LOGGER.error("error renaming trailer", e);
        MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, mf.getFilename(), "message.renamer.failedrename", new String[] { ":",
            e.getLocalizedMessage() }));
      }
      needed.add(newMF);
    }

    // ######################################################################
    // ## rename SUBTITLE
    // ######################################################################
    for (MediaFile sub : movie.getMediaFiles(MediaFileType.SUBTITLE)) {
      needed.add(sub); // keep all subtitles, will be cleaned afterwards
    }

    // ######################################################################
    // ## rename EXTRAFANART
    // ######################################################################
    for (MediaFile extra : movie.getMediaFiles(MediaFileType.EXTRAFANART)) {
      needed.add(extra); // keep all unknown
    }

    // ######################################################################
    // ## rename VIDEO_EXTRA
    // ######################################################################
    for (MediaFile vextra : movie.getMediaFiles(MediaFileType.VIDEO_EXTRA)) {
      needed.add(vextra); // keep all extras
      // TODO: movie to extras folder (or name)
    }

    // ######################################################################
    // ## rename UNKNOWN
    // ######################################################################
    for (MediaFile unk : movie.getMediaFiles(MediaFileType.UNKNOWN)) {
      needed.add(unk); // keep all unknown
    }

    // ######################################################################
    // ## invalidade image cache
    // ######################################################################
    for (MediaFile all : movie.getMediaFiles()) {
      switch (all.getType()) {
        case BANNER:
        case FANART:
        case EXTRAFANART:
        case GRAPHIC:
        case POSTER:
        case THUMB:
        case UNKNOWN:
          ImageCache.invalidateCachedImage(all.getPath() + File.separator + all.getFilename());
        default:
          break;
      }
    }

    // remove duplicate MediaFiles
    Set<MediaFile> newMFs = new LinkedHashSet<MediaFile>(needed);
    needed.clear();
    needed.addAll(newMFs);

    if (resetMultidir) {
      movie.setMultiMovieDir(false);
    }
    movie.removeAllMediaFiles();
    movie.addToMediaFiles(needed);
    movie.setPath(newPathname);
    movie.saveToDb();

    // cleanup & rename subtitle files
    renameSubtitles(movie);

    movie.gatherMediaFileInformation(false);
    movie.saveToDb();

    // rewrite NFO if it's a MP NFO and there was a change with poster/fanart
    if (Globals.settings.getMovieSettings().getMovieConnector() == MovieConnectors.MP && (posterRenamed || fanartRenamed)) {
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
        if (cl.getFile().exists()) { // unneded, but for not diplaying wrong deletes in logger...
          LOGGER.debug("Deleting " + cl.getFilename());
          FileUtils.deleteQuietly(cl.getFile()); // delete cleanup file
        }
        File[] list = cl.getFile().getParentFile().listFiles();
        if (list != null && list.length == 0) {
          // if directory is empty, delete it as well
          LOGGER.debug("Deleting empty Directory" + cl.getFile().getParentFile().getAbsolutePath());
          FileUtils.deleteQuietly(cl.getFile().getParentFile());
        }
      }
    }

    // clean all non tmm nfos
    if (Globals.settings.getMovieSettings().isMovieRenamerNfoCleanup()) {
      File[] content = new File(movie.getPath()).listFiles();
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
            LOGGER.debug("Deleting " + file.getName());
            FileUtils.deleteQuietly(file);
          }
        }
      }
    }
  }

  /**
   * Creates the new file/folder name according to template string
   * 
   * @param template
   *          the template
   * @param movie
   *          the movie
   * @return the string
   */
  public static String createDestination(String template, Movie movie) {
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
      if (movie.getMovieSet() != null) {
        newDestination = replaceToken(newDestination, "$M", movie.getMovieSet().getTitleSortable());
      }
      else {
        newDestination = newDestination.replace("$M", "");
      }
    }

    // replace token Movie set title ($N)
    if (newDestination.contains("$N")) {
      if (movie.getMovieSet() != null) {
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

    if (movie.getMediaFiles(MediaFileType.VIDEO).size() > 0) {
      MediaFile mf = movie.getMediaFiles(MediaFileType.VIDEO).get(0);
      // replace token resolution ($R)
      if (newDestination.contains("$R")) {
        newDestination = replaceToken(newDestination, "$R", mf.getVideoResolution());
      }

      // replace token audio codec + channels ($A)
      if (newDestination.contains("$A")) {
        newDestination = replaceToken(newDestination, "$A", mf.getAudioCodec() + (mf.getAudioCodec().isEmpty() ? "" : "-") + mf.getAudioChannels());
      }

      // replace token video codec + channels ($V)
      if (newDestination.contains("$V")) {
        newDestination = replaceToken(newDestination, "$V", mf.getVideoCodec() + (mf.getVideoCodec().isEmpty() ? "" : "-") + mf.getVideoFormat());
      }
    }
    else {
      // no mediafiles; remove at least token (if available)
      newDestination = newDestination.replace("$R", "");
      newDestination = newDestination.replace("$A", "");
      newDestination = newDestination.replace("$V", "");
    }

    // replace token media source (BluRay|DVD|TV|...) ($S)
    // if (newDestination.contains("$S")) {
    // newDestination = newDestination.replaceAll("\\$S",
    // movie.getMediaSource());
    // }

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

    // replace spaces with underscores if needed
    if (Globals.settings.getMovieSettings().isMovieRenamerSpaceSubstitution()) {
      String replacement = Globals.settings.getMovieSettings().getMovieRenamerSpaceReplacement();
      newDestination = newDestination.replace(" ", replacement);

      // also replace triple and double replacements with one to avoid strange looking results;
      // example:
      // Abraham Lincoln - Vapire Hunter -> Abraham-Lincoln---Vampire-Hunter
      newDestination = newDestination.replaceAll(replacement + replacement, replacement);
    }

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
   * copies or moves file.
   * 
   * @param oldFilename
   *          the old filename
   * @param newFilename
   *          the new filename
   * @throws Exception
   *           the exception
   */
  public static boolean copyFile(File oldFilename, File newFilename) throws Exception {
    if (!oldFilename.equals(newFilename)) {
      LOGGER.info("copy file " + oldFilename + " to " + newFilename);
      if (newFilename.exists()) {
        // overwrite?
        LOGGER.warn(newFilename + " exists - do nothing.");
        return true;
      }
      else {
        if (oldFilename.exists()) {
          FileUtils.copyFile(oldFilename, newFilename, true);
          return true;
        }
        else {
          throw new FileNotFoundException(oldFilename.getAbsolutePath());
        }
      }
    }
    else { // file is the same
      return false;
    }
  }
}
