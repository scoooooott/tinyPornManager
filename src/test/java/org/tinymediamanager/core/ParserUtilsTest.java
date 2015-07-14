package org.tinymediamanager.core;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.tinymediamanager.scraper.util.ParserUtils;

public class ParserUtilsTest {

  @Test
  public void testNamingDetection() {
    Assert.assertEquals("Safety Not Guaranteed | 2012", detectTY("Safety Not Guaranteed [2012, HEVC-1080p].mkv"));
    Assert.assertEquals("Gemma Bovery | 2014", detectTY("Gemma.Bovery.2014.[1920x800].24.000fps.1080p.BRRip.x264.JYK.mkv"));
    Assert.assertEquals("Ai No Korīda | 1976", detectTY("Ai.No.Korīda.1976.[1280x772].23.976fps.720p.x264.CiNEFiLE.mkv"));
    Assert.assertEquals("In The Realm Of The Senses 愛のコリーダ | 1976", detectTY("In The Realm Of The Senses (1976) - 愛のコリーダ"));

    Assert.assertEquals("framerate bla bla", detectTY("framerate 24.000fps bla bla"));
    Assert.assertEquals("framerate", detectTY("framerate 24.000 fps bla bla")); // fps as stopword
    Assert.assertEquals("framerate 0fps bla bla", detectTY("framerate 24.0000fps bla bla")); // one 0 to much
    Assert.assertEquals("framerate bla bla", detectTY("framerate 23.976fps bla bla"));
    Assert.assertEquals("framerate bla bla", detectTY("framerate 23.98fps bla bla"));
    Assert.assertEquals("framerate bla bla", detectTY("framerate 23.98 bla bla"));

    // replace resolution (000-9999 x 000-9999) when delimiter in front
    Assert.assertEquals("RES 1x1", detectTY("RES 1x1"));
    Assert.assertEquals("RES 10x10", detectTY("RES 10x10"));
    Assert.assertEquals("RES", detectTY("RES 100x100")); // <--- remove res
    Assert.assertEquals("RES", detectTY("RES 1000x1000")); // <--- remove res
    Assert.assertEquals("RES 10000x10000", detectTY("RES 10000x10000"));

  }

  private String detectTY(String filename) {
    String[] s = ParserUtils.detectCleanMovienameAndYear(filename);
    String ret = s[0];
    if (!s[1].isEmpty()) {
      ret = ret + " | " + s[1];
    }
    return ret;
  }

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

  @Test
  public void testBadword() {
    File f = new File("/media/Daten/Test_Filme/xxx.avi");
    System.out.println(ParserUtils.detectCleanMoviename(f.getName()));
  }
}
