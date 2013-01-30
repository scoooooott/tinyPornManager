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

}
