package org.tinymediamanager.core.movie;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.MediaFileHelper;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.entities.Movie;

public class MovieMediaFileTest extends BasicTest {

  @BeforeClass
  public static void beforeClass() {
    deleteSettingsFolder();
    Settings.getInstance(getSettingsFolder());
  }

  @Test
  public void testUpdateMediaFilePath() {
    Movie movie = new Movie();
    movie.setPath("/private/Test_Filme/Alien Collecion/Alien 1");
    Path mediaFile = Paths.get("/private/Test_Filme/Alien Collecion/Alien 1/asdf/jklö/VIDEO_TS/VIDEO_TS.IFO");
    MediaFile mf = new MediaFile(mediaFile);
    movie.addToMediaFiles(mf);

    System.out.println("Movie Path: " + movie.getPathNIO());
    System.out.println("File Path:  " + movie.getMediaFiles().get(0).getFileAsPath());

    Path oldPath = movie.getPathNIO();
    Path newPath = Paths.get("/private/Test_Filme/Alien 1");
    movie.updateMediaFilePath(oldPath, newPath);
    movie.setPath(newPath.toAbsolutePath().toString());

    System.out.println("Movie Path: " + movie.getPathNIO());
    System.out.println("File Path:  " + movie.getMediaFiles().get(0).getFileAsPath());
  }

  @Test
  public void filenameWithoutStacking() {
    MediaFile mf = new MediaFile(Paths.get(".", "hp7 - part 1"));
    System.out.println(mf.getFilenameWithoutStacking()); // not stacked
    mf.setStacking(1);
    mf.setStackingMarker("part 1");
    System.out.println(mf.getFilenameWithoutStacking()); // stacked
  }

  @Test
  public void testGetMainVideoFile() {
    Movie movie = new Movie();

    MediaFile first = new MediaFile(Paths.get("target/test-classes/testmovies/Stack/StackSingle/StackSingleFile CD1.avi"));
    movie.addToMediaFiles(first);

    MediaFile second = new MediaFile(Paths.get("target/test-classes/testmovies/Stack/StackSingle/StackSingleFile CD2.avi"));
    movie.addToMediaFiles(second);

    movie.reEvaluateStacking();

    assertThat(movie.getMainVideoFile()).isEqualTo(first);
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

    // video_extra
    mft = MediaFileType.EXTRA;
    checkExtra("Red.Shoe.Diaries.S01.EXTRAS.DVDRip.X264", mft);
    checkExtra("Extra/extras/some-trailer", mft);
    checkExtra("extras/someExtForSomeMovie-trailer", mft);
    checkExtra("extras/someExtForSomeMovie", mft);
    checkExtra("extra/The.Amazing.World.of.Gumball.S03E06.720p.HDTV.x264", mft);
    checkExtra("bla-blubb-extra", mft);
    checkExtra("bla-blubb-extra-something", mft);
    checkExtra("bla-blubb-extra-", mft);
    checkExtra("", mft);
    checkExtra("Extras/another/someExtForSomeMovie-trailer", mft);

    System.out.println("All fine :)");
  }

  private void checkExtra(String filename, MediaFileType mft) {
    if (filename.isEmpty()) {
      return;
    }
    Path f = Paths.get(".", filename + ".avi");
    System.out.println("testing " + f + " for ");
    MediaFile mf = new MediaFile(f);
    assertEqual(mft, mf.getType());
  }

  @Test
  public void testAudioChannels() {
    assertEqual(0, MediaFileHelper.parseChannelsAsInt(""));
    assertEqual(4, MediaFileHelper.parseChannelsAsInt("4"));
    assertEqual(6, MediaFileHelper.parseChannelsAsInt("5.1"));
    assertEqual(6, MediaFileHelper.parseChannelsAsInt("5.1channels"));
    assertEqual(8, MediaFileHelper.parseChannelsAsInt("8 / 6"));
    assertEqual(8, MediaFileHelper.parseChannelsAsInt("8 / 6 Ch"));
    assertEqual(11, MediaFileHelper.parseChannelsAsInt("4 / 5.2 / 8 / 6 / 7.3.1 / 9"));
    assertEqual(8, MediaFileHelper.parseChannelsAsInt("Object Based / 8 channels"));
    assertEqual(6, MediaFileHelper.parseChannelsAsInt("11 objects / 6 channels"));
    assertEqual(6, MediaFileHelper.parseChannelsAsInt("11 objects / 5.1 channels"));
  }
}
