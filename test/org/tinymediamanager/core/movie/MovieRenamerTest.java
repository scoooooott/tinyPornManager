package org.tinymediamanager.core.movie;

import java.io.File;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.MediaFile;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Utils;

public class MovieRenamerTest {
  private final static Logger LOGGER = LoggerFactory.getLogger(MovieRenamerTest.class);

  @Test
  public void checkDiff() {
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("tmm.odb");
    Globals.entityManager = emf.createEntityManager();
    MovieList instance = MovieList.getInstance();
    instance.loadMoviesFromDatabase();

    LOGGER.debug("path expression: " + Globals.settings.getMovieSettings().getMovieRenamerPathname());
    LOGGER.debug("file expression: " + Globals.settings.getMovieSettings().getMovieRenamerFilename());
    for (Movie movie : instance.getMovies()) {
      System.out.println(movie.getTitle());

      // VIDEO needs to be renamed first, since all others depend on that name!!!
      for (MediaFile mf : movie.getMediaFiles(MediaFileType.VIDEO)) {
        File org = mf.getFile();
        File gen = generateFilename(movie, mf);
        if (org.equals(gen)) {
          System.out.println("true  - " + gen);
        }
        else {
          System.out.println("false - " + org + " -> " + gen);
        }
      }
    }

    Globals.entityManager.close();
    emf.close();
  }

  public File generateFilename(Movie m, MediaFile mf) {
    String newPathname = MovieRenamer.createDestinationForFoldername(Globals.settings.getMovieSettings().getMovieRenamerPathname(), m);
    String newFilename = MovieRenamer.createDestinationForFilename(Globals.settings.getMovieSettings().getMovieRenamerFilename(), m);

    switch (mf.getType()) {
      case VIDEO:
        newFilename += getStackingString(mf);
        break;

      default:
        break;
    }

    newFilename += "." + mf.getExtension();

    return new File(m.getDataSource() + File.separatorChar + newPathname + File.separatorChar + newFilename);
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
