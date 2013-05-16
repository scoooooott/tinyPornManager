package org.tinymediamanager.core.movie;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.tinymediamanager.core.MediaFile;

public class MovieMediaFileTest {

  @Test
  public void testUpdateMediaFilePath() {
    Movie movie = new Movie();
    movie.setPath("C:\\private\\Test_Filme\\21");
    File mediaFile = new File("C:\\private\\Test_Filme\\21\\movie.avi");
    MediaFile mf = new MediaFile(mediaFile);
    movie.addToMediaFiles(mf);

    File oldPath = new File(movie.getPath());
    File newPath = new File("C:\\private\\Test_Filme\\300");
    movie.updateMediaFilePath(oldPath, newPath);

    Assert.assertEquals("C:\\private\\Test_Filme\\300\\movie.avi", mf.getPath() + File.separator + mf.getFilename());
  }
}
