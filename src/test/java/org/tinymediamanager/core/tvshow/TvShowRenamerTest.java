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
    assertEqual("\\singleshow (2009)\\Season 1\\singleshow - S01E02 - singleEP.avi", gen(single, "$N ($Y)", "Season $1", "$N - S$2E$E - $T", true));
    assertEqual("\\singleshow (2009)\\Season 1\\E02 - singleEP.avi", gen(single, "$N ($Y)", "Season $1", "E$E - $T", true));
    assertEqual("\\singleshow (2009)\\Season 1\\S01E02 - singleEP.avi", gen(single, "$N ($Y)", "Season $1", "S$2E$E - $T", true));
    assertEqual("\\singleshow (2009)\\Season 1\\1x04 - singleEP.avi", gen(single, "$N ($Y)", "Season $1", "$1x$D - $T", true));
    assertEqual("\\singleshow (2009)\\102 - singleEP.avi", gen(single, "$N ($Y)", "", "$1$E - $T", true));
    assertEqual("\\singleshow (2009)\\1x04 - singleEP.avi", gen(single, "$N ($Y)", "", "$1x$D - $T", true));

    // SINGLE - not recommended, but working
    assertEqual("\\singleshow (2009)\\Season 1\\S01 - singleEP.avi", gen(single, "$N ($Y)", "Season $1", "S$2 - $T", false));
    assertEqual("\\singleshow (2009)\\E02 - singleEP.avi", gen(single, "$N ($Y)", "", "E$E - $T", false));
    assertEqual("\\singleshow (2009)\\E02.avi", gen(single, "$N ($Y)", "", "E$E", false));
    assertEqual("\\singleshow (2009)\\Season 01\\102 303- singleEP.avi", gen(single, "$N ($Y)", "Season $2", "$1$E $3$4- $T", false));
    assertEqual("\\singleshow (2009)\\Season 01\\102 3x04- singleEP.avi", gen(single, "$N ($Y)", "Season $2", "$1$E $3x$D- $T", false));
    assertEqual("\\singleshow (2009)\\singleEP.avi", gen(single, "$N ($Y)", "", "$T", false));
    assertEqual("\\singleshow (2009)\\singleEPsingleEP.avi", gen(single, "$N ($Y)", "", "$T$T", false));
    assertEqual("\\singleshow (2009)\\singleshow - S101E02 - singleEP.avi", gen(single, "$N ($Y)", "", "$N - S$1$2E$E - $T", false)); // double
    assertEqual("\\singleshow (2009)\\singleshow - S1E0204 - singleEP.avi", gen(single, "$N ($Y)", "", "$N - S$1E$E$D - $T", false)); // double

    // *******************
    // COPY 1:1 FROM ABOVE
    // *******************

    // MULTI - RECOMMENDED
    assertEqual("\\multishow (2009)\\Season 1\\multishow - S01E02 S01E03 - multiEP2 - multiEP3.avi",
        gen(multi, "$N ($Y)", "Season $1", "$N - S$2E$E - $T", true));
    assertEqual("\\multishow (2009)\\Season 1\\E02 E03 - multiEP2 - multiEP3.avi", gen(multi, "$N ($Y)", "Season $1", "E$E - $T", true));
    assertEqual("\\multishow (2009)\\Season 1\\S01E02 S01E03 - multiEP2 - multiEP3.avi", gen(multi, "$N ($Y)", "Season $1", "S$2E$E - $T", true));
    assertEqual("\\multishow (2009)\\Season 1\\1x04 1x05 - multiEP2 - multiEP3.avi", gen(multi, "$N ($Y)", "Season $1", "$1x$D - $T", true));
    assertEqual("\\multishow (2009)\\102 103 - multiEP2 - multiEP3.avi", gen(multi, "$N ($Y)", "", "$1$E - $T", true));
    assertEqual("\\multishow (2009)\\1x04 1x05 - multiEP2 - multiEP3.avi", gen(multi, "$N ($Y)", "", "$1x$D - $T", true));

    // MULTI - not recommended, but working
    assertEqual("\\multishow (2009)\\Season 1\\S01 S01 - multiEP2 - multiEP3.avi", gen(multi, "$N ($Y)", "Season $1", "S$2 - $T", false));
    assertEqual("\\multishow (2009)\\E02 E03 - multiEP2 - multiEP3.avi", gen(multi, "$N ($Y)", "", "E$E - $T", false));
    assertEqual("\\multishow (2009)\\E02 E03.avi", gen(multi, "$N ($Y)", "", "E$E", false));
    assertEqual("\\multishow (2009)\\Season 01\\102 103 303 - multiEP2 - multiEP3.avi", gen(multi, "$N ($Y)", "Season $2", "$1$E $3$4 - $T", false));
    assertEqual("\\multishow (2009)\\Season 01\\102 103 3x04 - multiEP2 - multiEP3.avi",
        gen(multi, "$N ($Y)", "Season $2", "$1$E $3x$D - $T", false));
    assertEqual("\\multishow (2009)\\multiEP2 - multiEP3.avi", gen(multi, "$N ($Y)", "", "$T", false));
    assertEqual("\\multishow (2009)\\multiEP2 - multiEP3 multiEP2 - multiEP3.avi", gen(multi, "$N ($Y)", "", "$T$T", false));
    assertEqual("\\multishow (2009)\\multishow - S101E02 - multiEP2 - multiEP3.avi", gen(multi, "$N ($Y)", "", "$N - S$1$2E$E - $T", false)); // double
    assertEqual("\\multishow (2009)\\multishow - S1E02 S1E0304 - multiEP2 - multiEP3.avi", gen(multi, "$N ($Y)", "", "$N - S$1E$E$D - $T", false)); // double

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
    String se = TvShowRenamer.generateSeasonDir(seasonPattern, show.getEpisodes().get(0));
    String ep = TvShowRenamer.generateFilename(filePattern, show, show.getEpisodesMediaFiles().get(0));
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
