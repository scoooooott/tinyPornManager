package org.tinymediamanager.core.movie;

import java.io.File;

import org.junit.Test;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;

public class MovieMediaFileTest {

  @Test
  public void testUpdateMediaFilePath() {
    Movie movie = new Movie();
    movie.setPath("C:\\private\\Test_Filme\\Alien Collecion\\Alien 1");
    File mediaFile = new File("C:\\private\\Test_Filme\\Alien Collecion\\Alien 1\\asdf\\jklö\\VIDEO_TS\\VIDEO_TS.IFO");
    MediaFile mf = new MediaFile(mediaFile);
    movie.addToMediaFiles(mf);

    System.out.println("Movie Path: " + movie.getPath());
    System.out.println("File Path:  " + movie.getMediaFiles().get(0).getFile().getAbsolutePath());

    File oldPath = new File(movie.getPath());
    File newPath = new File("C:\\private\\Test_Filme\\Alien 1");
    movie.updateMediaFilePath(oldPath, newPath);
    movie.setPath(newPath.getPath());

    System.out.println("Movie Path: " + movie.getPath());
    System.out.println("File Path:  " + movie.getMediaFiles().get(0).getFile().getAbsolutePath());
  }
}
