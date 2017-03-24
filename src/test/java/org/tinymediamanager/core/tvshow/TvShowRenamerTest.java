package org.tinymediamanager.core.tvshow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

public class TvShowRenamerTest extends BasicTest {
  private static final String FOLDER = getSettingsFolder();

  private static TvShow       single = new TvShow();
  private static TvShow       multi  = new TvShow();
  private static TvShow       disc   = new TvShow();
  private static TvShow       discEP = new TvShow();

  @Test
  public void tvRenamerPatterns() {
    // SINGLE - RECOMMENDED
    assertEqual(p("/singleshow (2009)/Season 1/singleshow - S01E02 - singleEP.avi"), gen(single, "$N ($Y)", "Season $1", "$N - S$2E$E - $T", true));
    assertEqual(p("/singleshow (2009)/Season 1/E02 - singleEP.avi"), gen(single, "$N ($Y)", "Season $1", "E$E - $T", true));
    assertEqual(p("/singleshow (2009)/Season 1/S01E02 - singleEP.avi"), gen(single, "$N ($Y)", "Season $1", "S$2E$E - $T", true));
    assertEqual(p("/singleshow (2009)/Season 1/1x04 - singleEP.avi"), gen(single, "$N ($Y)", "Season $1", "$1x$D - $T", true));
    assertEqual(p("/singleshow (2009)/102 - singleEP.avi"), gen(single, "$N ($Y)", "", "$1$E - $T", true));
    assertEqual(p("/singleshow (2009)/1x04 - singleEP.avi"), gen(single, "$N ($Y)", "", "$1x$D - $T", true));

    // SINGLE - not recommended, but working
    assertEqual(p("/singleshow (2009)/Season 1/S01 - singleEP.avi"), gen(single, "$N ($Y)", "Season $1", "S$2 - $T", false));
    assertEqual(p("/singleshow (2009)/E02 - singleEP.avi"), gen(single, "$N ($Y)", "", "E$E - $T", false));
    assertEqual(p("/singleshow (2009)/E02.avi"), gen(single, "$N ($Y)", "", "E$E", false));
    assertEqual(p("/singleshow (2009)/Season 01/102 303- singleEP.avi"), gen(single, "$N ($Y)", "Season $2", "$1$E $3$4- $T", false));
    assertEqual(p("/singleshow (2009)/Season 01/102 3x04- singleEP.avi"), gen(single, "$N ($Y)", "Season $2", "$1$E $3x$D- $T", false));
    assertEqual(p("/singleshow (2009)/singleEP.avi"), gen(single, "$N ($Y)", "", "$T", false));
    assertEqual(p("/singleshow (2009)/singleEPsingleEP.avi"), gen(single, "$N ($Y)", "", "$T$T", false));
    assertEqual(p("/singleshow (2009)/singleshow - S101E02 - singleEP.avi"), gen(single, "$N ($Y)", "", "$N - S$1$2E$E - $T", false)); // double
    assertEqual(p("/singleshow (2009)/singleshow - S1E0204 - singleEP.avi"), gen(single, "$N ($Y)", "", "$N - S$1E$E$D - $T", false)); // double

    // *******************
    // COPY 1:1 FROM ABOVE
    // *******************

    // MULTI - RECOMMENDED
    assertEqual(p("/multishow (2009)/Season 1/multishow - S01E02 S01E03 - multiEP2 - multiEP3.avi"),
        gen(multi, "$N ($Y)", "Season $1", "$N - S$2E$E - $T", true));
    assertEqual(p("/multishow (2009)/Season 1/E02 E03 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "Season $1", "E$E - $T", true));
    assertEqual(p("/multishow (2009)/Season 1/S01E02 S01E03 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "Season $1", "S$2E$E - $T", true));
    assertEqual(p("/multishow (2009)/Season 1/1x04 1x05 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "Season $1", "$1x$D - $T", true));
    assertEqual(p("/multishow (2009)/102 103 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "", "$1$E - $T", true));
    assertEqual(p("/multishow (2009)/1x04 1x05 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "", "$1x$D - $T", true));

    // MULTI - not recommended, but working
    assertEqual(p("/multishow (2009)/Season 1/S01 S01 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "Season $1", "S$2 - $T", false));
    assertEqual(p("/multishow (2009)/E02 E03 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "", "E$E - $T", false));
    assertEqual(p("/multishow (2009)/E02 E03.avi"), gen(multi, "$N ($Y)", "", "E$E", false));
    assertEqual(p("/multishow (2009)/Season 01/102 103 303 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "Season $2", "$1$E $3$4 - $T", false));
    assertEqual(p("/multishow (2009)/Season 01/102 103 3x04 - multiEP2 - multiEP3.avi"),
        gen(multi, "$N ($Y)", "Season $2", "$1$E $3x$D - $T", false));
    assertEqual(p("/multishow (2009)/multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "", "$T", false));
    assertEqual(p("/multishow (2009)/multiEP2 - multiEP3 multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "", "$T$T", false));
    assertEqual(p("/multishow (2009)/multishow - S101E02 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "", "$N - S$1$2E$E - $T", false)); // double
    assertEqual(p("/multishow (2009)/multishow - S1E02 S1E0304 - multiEP2 - multiEP3.avi"), gen(multi, "$N ($Y)", "", "$N - S$1E$E$D - $T", false)); // double

  }

  @Test
  public void testDiscEpisode() throws IOException {
    Utils.copyDirectoryRecursive(Paths.get("target/test-classes/testtvshows"), Paths.get(FOLDER, "tv"));
    TvShowRenamer.renameEpisode(discEP.getEpisode(1, 1));
    TvShowRenamer.renameEpisode(disc.getEpisode(1, 2));
  }

  /**
   * string to path for unix/linux comparison
   * 
   * @param path
   * @return
   */
  private Path p(String path) {
    return Paths.get(path);
  }

  @BeforeClass
  public static void init() {
    deleteSettingsFolder();
    Settings.getInstance(FOLDER);

    // setup dummy
    MediaFile dmf = new MediaFile(new File("/path/to", "video.avi"));

    single.setTitle("singleshow");
    single.setYear("2009");
    TvShowEpisode ep = new TvShowEpisode();
    ep.setTitle("singleEP");
    ep.setSeason(1);
    ep.setEpisode(2);
    ep.setDvdSeason(3);
    ep.setDvdEpisode(4);
    ep.addToMediaFiles(dmf);
    ep.setTvShow(single);
    single.addEpisode(ep);

    multi.setTitle("multishow");
    multi.setYear("2009");
    ep = new TvShowEpisode();
    ep.setTitle("multiEP2");
    ep.setSeason(1);
    ep.setEpisode(2);
    ep.setDvdSeason(3);
    ep.setDvdEpisode(4);
    ep.addToMediaFiles(dmf);
    ep.setTvShow(multi);
    multi.addEpisode(ep);
    ep = new TvShowEpisode();
    ep.setTitle("multiEP3");
    ep.setSeason(1);
    ep.setEpisode(3);
    ep.setDvdSeason(3);
    ep.setDvdEpisode(5);
    ep.addToMediaFiles(dmf);
    ep.setTvShow(multi);
    multi.addEpisode(ep);

    disc.setTitle("Janosik");
    disc.setYear("2009");
    disc.setPath(FOLDER + "/tv/Janosik DVD");
    ep = new TvShowEpisode();
    ep.setPath(FOLDER + "/tv/Janosik DVD/Janosik S01E07E08E09");
    ep.setTvShow(disc);
    ep.setDisc(true);
    ep.setTitle("discfile");
    ep.setAiredSeason(1);
    ep.setAiredEpisode(2);
    ep.addToMediaFiles(new MediaFile(Paths.get(FOLDER, "/tv/Janosik DVD", "Janosik S01E07E08E09", "VIDEO_TS", "VTS_01_1.VOB").toAbsolutePath()));
    ep.addToMediaFiles(new MediaFile(Paths.get(FOLDER, "/tv/Janosik DVD", "Janosik S01E07E08E09", "VIDEO_TS-thumb.jpg").toAbsolutePath()));
    disc.addEpisode(ep);

    discEP.setTitle("DVDEpisodeInRoot");
    discEP.setYear("2009");
    discEP.setPath(FOLDER + "/tv/DVDEpisodeInRoot");
    ep = new TvShowEpisode();
    ep.setPath(FOLDER + "/tv/DVDEpisodeInRoot/S01EP01 title");
    ep.setTvShow(discEP);
    ep.setDisc(true);
    ep.setTitle("disc ep");
    ep.setAiredSeason(1);
    ep.setAiredEpisode(1);
    ep.addToMediaFiles(new MediaFile(Paths.get(FOLDER, "/tv/DVDEpisodeInRoot", "S01EP01 title", "VTS_01_1.VOB").toAbsolutePath()));
    discEP.addEpisode(ep);
  }

  private Path gen(TvShow show, String showPattern, String seasonPattern, String filePattern, boolean recommended) {
    Assert.assertEquals(recommended, TvShowRenamer.isRecommended(seasonPattern, filePattern));
    String sh = TvShowRenamer.generateTvShowDir(showPattern, show);
    String se = TvShowRenamer.generateSeasonDir(seasonPattern, show.getEpisodes().get(0));
    String ep = TvShowRenamer.generateFilename(filePattern, show, show.getEpisodesMediaFiles().get(0));
    return Paths.get(sh, se, ep);
  }
}
