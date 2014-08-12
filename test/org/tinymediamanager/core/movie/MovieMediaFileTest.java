package org.tinymediamanager.core.movie;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.tinymediamanager.core.MediaFileType;
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

  @Test
  public void ExtrasTest() {
    // video
    MediaFileType mft = MediaFileType.VIDEO;
    checkExtra("E.T. el extraterrestre", mft);
    checkExtra("E.T. the Extra-Terrestrial", mft);
    checkExtra("Extra", mft);
    checkExtra("Extras", mft);
    checkExtra("Extra 2012", mft);
    checkExtra("Extras", mft);
    checkExtra("LazyTown Extra", mft);
    checkExtra("Extra! Extra!", mft);
    checkExtra("Extra.Das.RTL.Magazin.2014-06-02.GERMAN.Doku.WS.dTV.x264", mft);
    checkExtra("Person.of.Interest.S02E14.Extravaganzen.German.DL.720p.BluRay.x264", mft);
    checkExtra("The.Client.List.S02E04.Extra.gefaellig.GERMAN.DUBBED.DL.720p.WebHD.h264", mft);
    checkExtra("The.Amazing.World.of.Gumball.S03E06.The.Extras.720p.HDTV.x264", mft);
    checkExtra("", mft);
    checkExtra("", mft);
    checkExtra("", mft);
    checkExtra("", mft);

    // video_extra
    mft = MediaFileType.VIDEO_EXTRA;
    checkExtra("Red.Shoe.Diaries.S01.EXTRAS.DVDRip.X264", mft);
    checkExtra("extras/someExtForSomeMovie", mft);
    checkExtra("extra/The.Amazing.World.of.Gumball.S03E06.720p.HDTV.x264", mft);
    checkExtra("bla-blubb-extra", mft);
    checkExtra("bla-blubb-extra-something", mft);
    checkExtra("bla-blubb-extra-", mft);
    checkExtra("", mft);
    checkExtra("", mft);
    checkExtra("", mft);

    System.out.println("All fine :)");
  }

  private void checkExtra(String filename, MediaFileType mft) {
    if (filename.isEmpty()) {
      return;
    }
    File f = new File(".", filename + ".avi");
    System.out.print("testing " + f + " for " + mft.name());
    MediaFile mf = new MediaFile(f);
    assertEquals(mft, mf.getType());
    System.out.println("  ...ok");
  }
}
