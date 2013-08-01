package org.tinymediamanager.core;

import java.io.File;

import org.junit.Test;
import org.tinymediamanager.scraper.util.ParserUtils;

public class ParserUtilsTest {

  @Test
  public void getTitle() {
    File f = new File("/media/Daten/Test_Filme");
    File[] fileArray = f.listFiles();
    for (File file : fileArray) {
      if (file.isDirectory()) {
        System.out.println(ParserUtils.detectCleanMoviename(file.getName()));
      }
    }
  }

  @Test
  public void testRenamedImdb() {
    File f = new File("/media/Daten/Test_Filme/this is my [tt0123456] movie (2009)");
    System.out.println(ParserUtils.detectCleanMoviename(f.getName()));
  }

}
