package org.tinymediamanager.core.tvshow;

import java.io.File;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;

public class TvShowRenamerTest {

  private static TvShow single = new TvShow();
  private static TvShow multi  = new TvShow();

  @Test
  public void tvRenamerPatterns() {
    // SINGLE - RECOMMENDED
    assertEqual(File.separator + "singleshow (2009)" + File.separator + "Season 1" + File.separator + "singleshow - S01E02 - singleEP.avi",
        gen(single, "$N ($Y)", "Season $1", "$N - S$2E$E - $T", true));
    assertEqual(File.separator + "singleshow (2009)" + File.separator + "Season 1" + File.separator + "E02 - singleEP.avi",
        gen(single, "$N ($Y)", "Season $1", "E$E - $T", true));
    assertEqual(File.separator + "singleshow (2009)" + File.separator + "Season 1" + File.separator + "S01E02 - singleEP.avi",
        gen(single, "$N ($Y)", "Season $1", "S$2E$E - $T", true));
    assertEqual(File.separator + "singleshow (2009)" + File.separator + "Season 1" + File.separator + "1x04 - singleEP.avi",
        gen(single, "$N ($Y)", "Season $1", "$1x$D - $T", true));
    assertEqual(File.separator + "singleshow (2009)" + File.separator + "102 - singleEP.avi", gen(single, "$N ($Y)", "", "$1$E - $T", true));
    assertEqual(File.separator + "singleshow (2009)" + File.separator + "1x04 - singleEP.avi", gen(single, "$N ($Y)", "", "$1x$D - $T", true));

    // SINGLE - not recommended, but working
    assertEqual(File.separator + "singleshow (2009)" + File.separator + "Season 1" + File.separator + "S01 - singleEP.avi",
        gen(single, "$N ($Y)", "Season $1", "S$2 - $T", false));
    assertEqual(File.separator + "singleshow (2009)" + File.separator + "E02 - singleEP.avi", gen(single, "$N ($Y)", "", "E$E - $T", false));
    assertEqual(File.separator + "singleshow (2009)" + File.separator + "E02.avi", gen(single, "$N ($Y)", "", "E$E", false));
    assertEqual(File.separator + "singleshow (2009)" + File.separator + "Season 01" + File.separator + "102 303- singleEP.avi",
        gen(single, "$N ($Y)", "Season $2", "$1$E $3$4- $T", false));
    assertEqual(File.separator + "singleshow (2009)" + File.separator + "Season 01" + File.separator + "102 3x04- singleEP.avi",
        gen(single, "$N ($Y)", "Season $2", "$1$E $3x$D- $T", false));
    assertEqual(File.separator + "singleshow (2009)" + File.separator + "singleEP.avi", gen(single, "$N ($Y)", "", "$T", false));
    assertEqual(File.separator + "singleshow (2009)" + File.separator + "singleEPsingleEP.avi", gen(single, "$N ($Y)", "", "$T$T", false));
    assertEqual(File.separator + "singleshow (2009)" + File.separator + "singleshow - S101E02 - singleEP.avi",
        gen(single, "$N ($Y)", "", "$N - S$1$2E$E - $T", false)); // double
    assertEqual(File.separator + "singleshow (2009)" + File.separator + "singleshow - S1E0204 - singleEP.avi",
        gen(single, "$N ($Y)", "", "$N - S$1E$E$D - $T", false)); // double

    // *******************
    // COPY 1:1 FROM ABOVE
    // *******************

    // MULTI - RECOMMENDED
    assertEqual(
        File.separator + "multishow (2009)" + File.separator + "Season 1" + File.separator + "multishow - S01E02 S01E03 - multiEP2 - multiEP3.avi",
        gen(multi, "$N ($Y)", "Season $1", "$N - S$2E$E - $T", true));
    assertEqual(File.separator + "multishow (2009)" + File.separator + "Season 1" + File.separator + "E02 E03 - multiEP2 - multiEP3.avi",
        gen(multi, "$N ($Y)", "Season $1", "E$E - $T", true));
    assertEqual(File.separator + "multishow (2009)" + File.separator + "Season 1" + File.separator + "S01E02 S01E03 - multiEP2 - multiEP3.avi",
        gen(multi, "$N ($Y)", "Season $1", "S$2E$E - $T", true));
    assertEqual(File.separator + "multishow (2009)" + File.separator + "Season 1" + File.separator + "1x04 1x05 - multiEP2 - multiEP3.avi",
        gen(multi, "$N ($Y)", "Season $1", "$1x$D - $T", true));
    assertEqual(File.separator + "multishow (2009)" + File.separator + "102 103 - multiEP2 - multiEP3.avi",
        gen(multi, "$N ($Y)", "", "$1$E - $T", true));
    assertEqual(File.separator + "multishow (2009)" + File.separator + "1x04 1x05 - multiEP2 - multiEP3.avi",
        gen(multi, "$N ($Y)", "", "$1x$D - $T", true));

    // MULTI - not recommended, but working
    assertEqual(File.separator + "multishow (2009)" + File.separator + "Season 1" + File.separator + "S01 S01 - multiEP2 - multiEP3.avi",
        gen(multi, "$N ($Y)", "Season $1", "S$2 - $T", false));
    assertEqual(File.separator + "multishow (2009)" + File.separator + "E02 E03 - multiEP2 - multiEP3.avi",
        gen(multi, "$N ($Y)", "", "E$E - $T", false));
    assertEqual(File.separator + "multishow (2009)" + File.separator + "E02 E03.avi", gen(multi, "$N ($Y)", "", "E$E", false));
    assertEqual(File.separator + "multishow (2009)" + File.separator + "Season 01" + File.separator + "102 103 303 - multiEP2 - multiEP3.avi",
        gen(multi, "$N ($Y)", "Season $2", "$1$E $3$4 - $T", false));
    assertEqual(File.separator + "multishow (2009)" + File.separator + "Season 01" + File.separator + "102 103 3x04 - multiEP2 - multiEP3.avi",
        gen(multi, "$N ($Y)", "Season $2", "$1$E $3x$D - $T", false));
    assertEqual(File.separator + "multishow (2009)" + File.separator + "multiEP2 - multiEP3.avi", gen(multi, "$N ($Y)", "", "$T", false));
    assertEqual(File.separator + "multishow (2009)" + File.separator + "multiEP2 - multiEP3 multiEP2 - multiEP3.avi",
        gen(multi, "$N ($Y)", "", "$T$T", false));
    assertEqual(File.separator + "multishow (2009)" + File.separator + "multishow - S101E02 - multiEP2 - multiEP3.avi",
        gen(multi, "$N ($Y)", "", "$N - S$1$2E$E - $T", false)); // double
    assertEqual(File.separator + "multishow (2009)" + File.separator + "multishow - S1E02 S1E0304 - multiEP2 - multiEP3.avi",
        gen(multi, "$N ($Y)", "", "$N - S$1E$E$D - $T", false)); // double

  }

  @BeforeClass
  public static void init() {
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

  }

  private String gen(TvShow show, String showPattern, String seasonPattern, String filePattern, boolean recommended) {
    assertEqual(recommended, TvShowRenamer.isRecommended(seasonPattern, filePattern));
    String sh = TvShowRenamer.generateTvShowDir(showPattern, show);
    String se = TvShowRenamer.getSeasonFoldername(seasonPattern, show.getEpisodes().get(0));
    String ep = TvShowRenamer.generateEpisodeFilenames(filePattern, show, show.getEpisodesMediaFiles().get(0)).get(0).getFilename();
    System.out.println(new File(sh, se + File.separator + ep).toString());
    return new File(sh, se + File.separator + ep).toString();
  }

  // own method to get some logging ;)
  public static void assertEqual(Object expected, Object actual) {
    try {
      Assert.assertEquals(expected, actual);
      // System.out.println(expected + " - passed");
    }
    catch (AssertionError e) {
      System.err.println(expected + " - FAILED: " + e.getMessage());
      throw e;
    }
  }

}
