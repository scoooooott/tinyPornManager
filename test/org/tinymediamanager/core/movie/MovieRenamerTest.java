package org.tinymediamanager.core.movie;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileSubtitle;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;

public class MovieRenamerTest {
  private final static Logger LOGGER = LoggerFactory.getLogger(MovieRenamerTest.class);

  public enum RenamerStatus {
    SAME, REMOVED, ADDED, RENAMED;
  }

  public class FileToRename extends MediaFile {
    boolean       renameFolder = false;
    RenamerStatus renameStatus = RenamerStatus.SAME;

    FileToRename(MediaFile mf) {
      super(mf);
    }
  }

  public void checkDiff(Movie m) {

  }

  @Test
  public void checkDiff() {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("tmm.odb");
    Globals.entityManager = emf.createEntityManager();
    MovieList instance = MovieList.getInstance();
    instance.loadMoviesFromDatabase();

    List<FileToRename> oldFiles = new ArrayList<FileToRename>();
    Set<FileToRename> newFiles = new LinkedHashSet<FileToRename>();
    boolean renameFolder = false;

    LOGGER.debug("path expression: " + Globals.settings.getMovieSettings().getMovieRenamerPathname());
    LOGGER.debug("file expression: " + Globals.settings.getMovieSettings().getMovieRenamerFilename());
    for (Movie movie : instance.getMovies()) {
      oldFiles.clear();
      newFiles.clear();
      System.out.println(movie.getTitle());

      String newVideoFileName = "";

      // VIDEO needs to be renamed first, since all others depend on that name!!!
      for (MediaFile mf : movie.getMediaFiles(MediaFileType.VIDEO)) {
        oldFiles.add(new FileToRename(mf));
        FileToRename ftr = generateFilename(movie, mf, newVideoFileName).get(0); // there can be only one
        newFiles.add(ftr);
        if (newVideoFileName.isEmpty()) {
          // so remember first renamed video file basename (w/o stacking or extension)
          newVideoFileName = Utils.cleanStackingMarkers(ftr.getBasename());
        }
      }

      // all the other MFs...
      for (MediaFile mf : movie.getMediaFilesExceptType(MediaFileType.VIDEO)) {
        oldFiles.add(new FileToRename(mf));
        newFiles.addAll(generateFilename(movie, mf, newVideoFileName)); // N:M
      }

      // movie folder needs a rename?
      File oldMovieFolder = new File(movie.getPath());
      String newPathname = MovieRenamer.createDestinationForFoldername(Globals.settings.getMovieSettings().getMovieRenamerPathname(), movie);
      File newMovieFolder = new File(movie.getDataSource() + File.separator + newPathname);
      if (!oldMovieFolder.equals(newMovieFolder)) {
        renameFolder = true;
        if (!movie.isMultiMovieDir()) {
          System.out.println("rename FOLDER " + oldMovieFolder + " -> " + newMovieFolder);
        }
        else {
          System.out.println(" ... is a MultiMovieDir; create FOLDER " + newMovieFolder);
        }
        // update already the "original" files with new path, so we can check complete MF ;)
        for (MediaFile omf : oldFiles) {
          omf.replacePathForRenamedFolder(oldMovieFolder, newMovieFolder);
        }
      }

      // change status of MFs, if they have been added or not
      System.out.println("=============== NEW");
      for (FileToRename ftr : newFiles) {
        if (!oldFiles.contains(ftr)) {
          ftr.renameStatus = RenamerStatus.ADDED;
        }
        System.out.println(ftr.getFilename() + "  -  " + ftr.renameStatus);
      }
      System.out.println("=============== OLD");
      for (FileToRename mf : oldFiles) {
        if (!newFiles.contains(mf)) {
          mf.renameStatus = RenamerStatus.REMOVED;
        }
        System.out.println(mf.getFilename() + "  -  " + mf.renameStatus);
      }
      System.out.println("==================");

    }

    Globals.entityManager.close();
    emf.close();
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
  public ArrayList<FileToRename> generateFilename(Movie movie, MediaFile mf, String videoFileName) {
    // return list of all generated MFs
    ArrayList<FileToRename> newFiles = new ArrayList<FileToRename>();

    String newPathname = MovieRenamer.createDestinationForFoldername(Globals.settings.getMovieSettings().getMovieRenamerPathname(), movie);
    String movieDir = movie.getDataSource() + File.separatorChar + newPathname + File.separatorChar;

    String newFilename = videoFileName;
    if (newFilename == null || newFilename.isEmpty()) {
      newFilename = MovieRenamer.createDestinationForFilename(Globals.settings.getMovieSettings().getMovieRenamerFilename(), movie);
    }

    switch (mf.getType()) {
      case VIDEO:
        FileToRename vid = new FileToRename(mf);
        if (movie.isDisc() || mf.isDiscFile()) {
          // just replace new path and return file (do not change names!)
          vid.replacePathForRenamedFolder(new File(movie.getPath()), new File(movieDir));
        }
        else {
          newFilename += getStackingString(mf);
          newFilename += "." + mf.getExtension();
          vid.setFile(new File(movieDir + newFilename));
        }
        newFiles.add(vid);
        break;

      case TRAILER:
        FileToRename trail = new FileToRename(mf);
        newFilename += "-trailer." + mf.getExtension();
        trail.setFile(new File(movieDir + newFilename));
        newFiles.add(trail);
        break;

      case SUBTITLE:
        String lang = "";
        String forced = "";
        List<MediaFileSubtitle> mfsl = mf.getSubtitles();

        if (mfsl != null && mfsl.size() > 0) {
          MediaFileSubtitle mfs = mfsl.get(0);
          lang = mfs.getLanguage();
          if (mfs.isForced()) {
            forced = ".forced";
          }
        }
        newFilename += getStackingString(mf);
        newFilename += forced;
        if (!lang.isEmpty()) {
          newFilename += "." + lang;
        }
        newFilename += "." + mf.getExtension();
        FileToRename sub = new FileToRename(mf);
        sub.setFile(new File(movieDir + newFilename));
        newFiles.add(sub);
        break;

      case NFO:
        List<MovieNfoNaming> nfonames = new ArrayList<MovieNfoNaming>();
        if (movie.isMultiMovieDir()) {
          // Fixate the name regardless of setting
          nfonames.add(MovieNfoNaming.FILENAME_NFO);
        }
        else {
          nfonames = Globals.settings.getMovieSettings().getMovieNfoFilenames();
        }
        for (MovieNfoNaming name : nfonames) {
          newFilename = movie.getNfoFilename(name, videoFileName);
          FileToRename nfo = new FileToRename(mf);
          nfo.setFile(new File(movieDir + newFilename));
          newFiles.add(nfo);
        }
        break;

      case POSTER:
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
          newFilename = movie.getPosterFilename(name, videoFileName);
          if (newFilename != null && !newFilename.isEmpty()) {
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
          }
          FileToRename pos = new FileToRename(mf);
          pos.setFile(new File(movieDir + newFilename));
          newFiles.add(pos);
        }
        break;

      case FANART:
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
          newFilename = movie.getFanartFilename(name, videoFileName);
          if (newFilename != null && !newFilename.isEmpty()) {
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
          }
          FileToRename fan = new FileToRename(mf);
          fan.setFile(new File(movieDir + newFilename));
          newFiles.add(fan);
        }
        break;

      default:
        // return 1:1, only with renamed path
        FileToRename def = new FileToRename(mf);
        def.replacePathForRenamedFolder(new File(movie.getPath()), new File(movieDir));
        newFiles.add(def);
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
  private String getStackingString(MediaFile mf) {
    String stacking = Utils.getStackingMarker(mf.getFilename());
    String delimiter = " ";
    if (Globals.settings.getMovieSettings().isMovieRenamerSpaceSubstitution()) {
      delimiter = Globals.settings.getMovieSettings().getMovieRenamerSpaceReplacement();
    }
    if (!stacking.isEmpty()) {
      return delimiter + stacking;
    }
    else if (mf.getStacking() != 0) {
      return delimiter + "CD" + mf.getStacking();
    }
    return "";
  }
}
