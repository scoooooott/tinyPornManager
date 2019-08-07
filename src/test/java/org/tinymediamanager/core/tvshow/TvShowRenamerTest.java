package org.tinymediamanager.core.tvshow;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonBannerNaming;
import org.tinymediamanager.core.tvshow.filenaming.TvShowSeasonPosterNaming;
import org.tinymediamanager.core.tvshow.tasks.TvShowRenameTask;

public class TvShowRenamerTest extends BasicTest {

  private static final String FOLDER = getSettingsFolder();

  private static TvShow       single = new TvShow();
  private static TvShow       multi  = new TvShow();
  private static TvShow       disc   = new TvShow();
  private static TvShow       discEP = new TvShow();

  @BeforeClass
  public static void init() {
    deleteSettingsFolder();
    Settings.getInstance(FOLDER);

    // setup dummy
    MediaFile dmf = new MediaFile(Paths.get("/path/to", "video.avi"));

    single.setTitle("singleshow");
    single.setYear(2009);
    single.setPath("singleshow");
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
    multi.setYear(2009);
    multi.setPath("multishow");
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
    disc.setYear(2009);
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
    discEP.setYear(2009);
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

  @Test
  public void tvRenamerPatterns() {
    // SINGLE - RECOMMENDED
    assertEqual(p("singleshow (2009)/Season 1/singleshow - S01E02 - singleEP.avi"),
        gen(single, "${showTitle} (${showYear})", "Season ${seasonNr}", "${showTitle} - S${seasonNr2}E${episodeNr2} - ${title}", true));
    assertEqual(p("singleshow (2009)/Season 1/E02 - singleEP.avi"),
        gen(single, "${showTitle} (${showYear})", "Season ${seasonNr}", "E${episodeNr2} - ${title}", true));
    assertEqual(p("singleshow (2009)/Season 1/S01E02 - singleEP.avi"),
        gen(single, "${showTitle} (${showYear})", "Season ${seasonNr}", "S${seasonNr2}E${episodeNr2} - ${title}", true));
    assertEqual(p("singleshow (2009)/Season 1/1x04 - singleEP.avi"),
        gen(single, "${showTitle} (${showYear})", "Season ${seasonNr}", "${seasonNr}x${episodeNrDvd2} - ${title}", true));
    assertEqual(p("singleshow (2009)/102 - singleEP.avi"),
        gen(single, "${showTitle} (${showYear})", "", "${seasonNr}${episodeNr2} - ${title}", true));
    assertEqual(p("singleshow (2009)/1x04 - singleEP.avi"),
        gen(single, "${showTitle} (${showYear})", "", "${seasonNr}x${episodeNrDvd2} - ${title}", true));
    assertEqual(p("singleshow (2009)/Season 1/E02 - singleEP.avi"),
        gen(single, "${showTitle} (${showYear})", "Season ${seasonNr}", "E${episodeNr2} - ${title} () [] {} ( ) [ ] { } ", true));

    // SINGLE - not recommended, but working
    assertEqual(p("singleshow (2009)/Season 1/S01 - singleEP.avi"),
        gen(single, "${showTitle} (${showYear})", "Season ${seasonNr}", "S${seasonNr2} - ${title}", false));
    assertEqual(p("singleshow (2009)/E02 - singleEP.avi"), gen(single, "${showTitle} (${showYear})", "", "E${episodeNr2} - ${title}", false));
    assertEqual(p("singleshow (2009)/E02.avi"), gen(single, "${showTitle} (${showYear})", "", "E${episodeNr2}", false));
    assertEqual(p("singleshow (2009)/Season 01/102 303- singleEP.avi"),
        gen(single, "${showTitle} (${showYear})", "Season ${seasonNr2}", "${seasonNr}${episodeNr2} ${seasonNrDvd}${seasonNrDvd2}- ${title}", false));
    assertEqual(p("singleshow (2009)/Season 01/102 3x04- singleEP.avi"), gen(single, "${showTitle} (${showYear})", "Season ${seasonNr2}",
        "${seasonNr}${episodeNr2} ${seasonNrDvd}x${episodeNrDvd2}- ${title}", false));
    assertEqual(p("singleshow (2009)/singleEP.avi"), gen(single, "${showTitle} (${showYear})", "", "${title}", false));
    assertEqual(p("singleshow (2009)/singleEPsingleEP.avi"), gen(single, "${showTitle} (${showYear})", "", "${title}${title}", false));
    assertEqual(p("singleshow (2009)/singleshow - S101E02 - singleEP.avi"),
        gen(single, "${showTitle} (${showYear})", "", "${showTitle} - S${seasonNr}${seasonNr2}E${episodeNr2} - ${title}", false)); // double
    assertEqual(p("singleshow (2009)/singleshow - S1E0204 - singleEP.avi"),
        gen(single, "${showTitle} (${showYear})", "", "${showTitle} - S${seasonNr}E${episodeNr2}${episodeNrDvd2} - ${title}", false)); // double

    // *******************
    // COPY 1:1 FROM ABOVE
    // *******************

    // MULTI - RECOMMENDED
    assertEqual(p("multishow (2009)/Season 1/multishow - S01E02 S01E03 - multiEP2 - multiEP3.avi"),
        gen(multi, "${showTitle} (${showYear})", "Season ${seasonNr}", "${showTitle} - S${seasonNr2}E${episodeNr2} - ${title}", true));
    assertEqual(p("multishow (2009)/Season 1/E02 E03 - multiEP2 - multiEP3.avi"),
        gen(multi, "${showTitle} (${showYear})", "Season ${seasonNr}", "E${episodeNr2} - ${title}", true));
    assertEqual(p("multishow (2009)/Season 1/S01E02 S01E03 - multiEP2 - multiEP3.avi"),
        gen(multi, "${showTitle} (${showYear})", "Season ${seasonNr}", "S${seasonNr2}E${episodeNr2} - ${title}", true));
    assertEqual(p("multishow (2009)/Season 1/1x04 1x05 - multiEP2 - multiEP3.avi"),
        gen(multi, "${showTitle} (${showYear})", "Season ${seasonNr}", "${seasonNr}x${episodeNrDvd2} - ${title}", true));
    assertEqual(p("multishow (2009)/102 103 - multiEP2 - multiEP3.avi"),
        gen(multi, "${showTitle} (${showYear})", "", "${seasonNr}${episodeNr2} - ${title}", true));
    assertEqual(p("multishow (2009)/1x04 1x05 - multiEP2 - multiEP3.avi"),
        gen(multi, "${showTitle} (${showYear})", "", "${seasonNr}x${episodeNrDvd2} - ${title}", true));
    assertEqual(p("multishow (2009)/Season 1/E02 E03 - multiEP2 - multiEP3.avi"),
        gen(multi, "${showTitle} (${showYear})", "Season ${seasonNr}", "E${episodeNr2} - ${title} () [] {} ( ) [ ] { } ", true));

    // MULTI - not recommended, but working
    assertEqual(p("multishow (2009)/Season 1/S01 S01 - multiEP2 - multiEP3.avi"),
        gen(multi, "${showTitle} (${showYear})", "Season ${seasonNr}", "S${seasonNr2} - ${title}", false));
    assertEqual(p("multishow (2009)/E02 E03 - multiEP2 - multiEP3.avi"),
        gen(multi, "${showTitle} (${showYear})", "", "E${episodeNr2} - ${title}", false));
    assertEqual(p("multishow (2009)/E02 E03.avi"), gen(multi, "${showTitle} (${showYear})", "", "E${episodeNr2}", false));
    assertEqual(p("multishow (2009)/Season 01/102 103 303 - multiEP2 - multiEP3.avi"),
        gen(multi, "${showTitle} (${showYear})", "Season ${seasonNr2}", "${seasonNr}${episodeNr2} ${seasonNrDvd}${seasonNrDvd2} - ${title}", false));
    // assertEqual(p("multishow (2009)/Season 01/102 103 3x04 - multiEP2 - multiEP3.avi"),
    // gen(multi, "${showTitle} (${showYear})", "Season ${seasonNr2}", "${seasonNr}${episodeNr2} ${seasonNrDvd}x${episodeNrDvd2} - ${title}", false));
    assertEqual(p("multishow (2009)/multiEP2 - multiEP3.avi"), gen(multi, "${showTitle} (${showYear})", "", "${title}", false));
    assertEqual(p("multishow (2009)/multiEP2 - multiEP3 multiEP2 - multiEP3.avi"),
        gen(multi, "${showTitle} (${showYear})", "", "${title}${title}", false));
    assertEqual(p("multishow (2009)/multishow - S101E02 - multiEP2 - multiEP3.avi"),
        gen(multi, "${showTitle} (${showYear})", "", "${showTitle} - S${seasonNr}${seasonNr2}E${episodeNr2} - ${title}", false)); // double
    assertEqual(p("multishow (2009)/multishow - S1E02 S1E0304 - multiEP2 - multiEP3.avi"),
        gen(multi, "${showTitle} (${showYear})", "", "${showTitle} - S${seasonNr}E${episodeNr2}${episodeNrDvd2} - ${title}", false)); // double
  }

  @Test
  public void testDiscEpisode() throws IOException {
    Utils.copyDirectoryRecursive(Paths.get("target/test-classes/testtvshows"), Paths.get(FOLDER, "tv"));
    TvShowRenamer.renameEpisode(discEP.getEpisode(1, 1));
    TvShowRenamer.renameEpisode(disc.getEpisode(1, 2));
  }

  private Path gen(TvShow show, String showPattern, String seasonPattern, String filePattern, boolean recommended) {
    Assert.assertEquals(recommended, TvShowRenamer.isRecommended(seasonPattern, filePattern));
    String sh = TvShowRenamer.getTvShowFoldername(showPattern, show);
    String se = TvShowRenamer.getSeasonFoldername(seasonPattern, show, show.getEpisodes().get(0));
    String ep = TvShowRenamer.generateEpisodeFilenames(filePattern, show, show.getEpisodesMediaFiles().get(0)).get(0).getFilename();
    System.out.println(new File(sh, se + File.separator + ep).toString());
    // return new File(sh, se + File.separator + ep).toString();
    return Paths.get(sh, se, ep);
  }

  private Path p(String path) {
    return Paths.get(path);
  }

  /**
   * just a test of a simple episode (one EP file with some extra files)
   */
  @Test
  public void testSimpleEpisode() {
    // run with default settings
    TvShowSettings settings = TvShowSettings.getInstance("target/settings");

    // copy over the test files to a new folder
    Path source = Paths.get("target/test-classes/testtvshows/renamer_test/simple");
    Path destination = Paths.get("target/test-classes/tv_show_renamer_simple/ShowForRenamer");
    try {
      FileUtils.deleteDirectory(destination.getParent().toFile());
      FileUtils.copyDirectory(source.toFile(), destination.toFile());
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    TvShow show = new TvShow();
    show.setTitle("Breaking Bad");
    show.setYear(2008);
    show.setDataSource(destination.getParent().toAbsolutePath().toString());
    show.setPath(destination.toAbsolutePath().toString());

    // classical single file episode
    TvShowEpisode ep = new TvShowEpisode();
    ep.setTitle("Pilot");
    ep.setSeason(1);
    ep.setEpisode(1);
    ep.setDvdSeason(1);
    ep.setDvdEpisode(1);
    ep.setPath(destination.toAbsolutePath().toString());
    MediaFile mf = new MediaFile(destination.resolve("S01E01.jpg").toAbsolutePath(), MediaFileType.THUMB);
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01.nfo").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01.de.srt").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    ep.setTvShow(show);
    show.addEpisode(ep);

    renameTvShow(show);

    Path showDir = destination.getParent().resolve("Breaking Bad (2008)");
    assertThat(showDir).exists();

    Path seasonDir = showDir.resolve("Season 1");
    assertThat(seasonDir).exists();

    Path video = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.mkv");
    assertThat(video).exists();
    Path thumb = seasonDir.resolve("Breaking Bad - S01E01 - Pilot-thumb.jpg");
    assertThat(thumb).exists();
    Path nfo = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.nfo");
    assertThat(nfo).exists();
    Path sub = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.deu.srt");
    assertThat(sub).exists();
  }

  /**
   * just a test of a simple episode with extras (two EP files with some extra files)
   */
  @Test
  public void testSimpleEpisodeWithExtras() {
    // run with default settings
    TvShowSettings settings = TvShowSettings.getInstance("target/settings");

    // copy over the test files to a new folder
    Path source = Paths.get("target/test-classes/testtvshows/renamer_test/extra");
    Path destination = Paths.get("target/test-classes/tv_show_renamer_extra/ShowForRenamer");
    try {
      FileUtils.deleteDirectory(destination.getParent().toFile());
      FileUtils.copyDirectory(source.toFile(), destination.toFile());
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    TvShow show = new TvShow();
    show.setTitle("Breaking Bad");
    show.setYear(2008);
    show.setDataSource(destination.getParent().toAbsolutePath().toString());
    show.setPath(destination.toAbsolutePath().toString());
    MediaFile mf = new MediaFile(destination.resolve("extras/Show extra.avi").toAbsolutePath());
    mf.gatherMediaInformation();
    show.addToMediaFiles(mf);

    // classical single file episodes with extras
    TvShowEpisode ep = new TvShowEpisode();
    ep.setTitle("Pilot");
    ep.setSeason(1);
    ep.setEpisode(1);
    ep.setDvdSeason(1);
    ep.setDvdEpisode(1);
    ep.setPath(destination.toAbsolutePath().toString());

    mf = new MediaFile(destination.resolve("S01E01.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);

    mf = new MediaFile(destination.resolve("extras/S01E01 - cut scenes.mkv").toAbsolutePath(), MediaFileType.EXTRA);
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);

    mf = new MediaFile(destination.resolve("S01E01 - sample.avi").toAbsolutePath(), MediaFileType.SAMPLE);
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);

    mf = new MediaFile(destination.resolve("S01E01 - something else.mkv").toAbsolutePath(), MediaFileType.EXTRA);
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);

    ep.setTvShow(show);
    show.addEpisode(ep);

    ep = new TvShowEpisode();
    ep.setTitle("Pilot 2");
    ep.setSeason(1);
    ep.setEpisode(2);
    ep.setDvdSeason(1);
    ep.setDvdEpisode(2);
    ep.setPath(destination.toAbsolutePath().toString());
    mf = new MediaFile(destination.resolve("Season 1/S01E02.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);

    mf = new MediaFile(destination.resolve("Season 1/extras/S01E02 - takeouts.mkv").toAbsolutePath(), MediaFileType.EXTRA);
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);

    ep.setTvShow(show);
    show.addEpisode(ep);

    renameTvShow(show);

    Path showDir = destination.getParent().resolve("Breaking Bad (2008)");
    assertThat(showDir).exists();

    Path seasonDir = showDir.resolve("Season 1");
    assertThat(seasonDir).exists();

    Path video1 = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.mkv");
    assertThat(video1).exists();

    Path extra1 = seasonDir.resolve("extras/Breaking Bad - S01E01 - Pilot-cut scenes.mkv");
    assertThat(extra1).exists();

    Path sample1 = seasonDir.resolve("Breaking Bad - S01E01 - Pilot-sample.avi");
    assertThat(sample1).exists();

    Path extra12 = seasonDir.resolve("extras/Breaking Bad - S01E01 - Pilot-something else.mkv");
    assertThat(extra12).exists();

    Path video2 = seasonDir.resolve("Breaking Bad - S01E02 - Pilot 2.mkv");
    assertThat(video2).exists();

    Path extra2 = seasonDir.resolve("extras/Breaking Bad - S01E02 - Pilot 2-takeouts.mkv");
    assertThat(extra2).exists();
  }

  /**
   * multi episode file test
   */
  @Test
  public void testMultiEpisode() {
    // run with default settings
    TvShowSettings settings = TvShowSettings.getInstance("target/settings");

    // copy over the test files to a new folder
    Path source = Paths.get("target/test-classes/testtvshows/renamer_test/multi");
    Path destination = Paths.get("target/test-classes/tv_show_renamer_multi/ShowForRenamer");
    try {
      FileUtils.deleteDirectory(destination.getParent().toFile());
      FileUtils.copyDirectory(source.toFile(), destination.toFile());
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    TvShow show = new TvShow();
    show.setTitle("Breaking Bad");
    show.setYear(2008);
    show.setDataSource(destination.getParent().toAbsolutePath().toString());
    show.setPath(destination.toAbsolutePath().toString());

    // multi episode file
    TvShowEpisode ep = new TvShowEpisode();
    ep.setTitle("Pilot");
    ep.setSeason(1);
    ep.setEpisode(1);
    ep.setDvdSeason(1);
    ep.setDvdEpisode(1);
    ep.setPath(destination.toAbsolutePath().toString());
    MediaFile mf = new MediaFile(destination.resolve("S01E01E02.jpg").toAbsolutePath(), MediaFileType.THUMB);
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.nfo").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.de.srt").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    ep.setTvShow(show);
    show.addEpisode(ep);

    ep = new TvShowEpisode();
    ep.setTitle("Pilot 2");
    ep.setSeason(1);
    ep.setEpisode(2);
    ep.setDvdSeason(1);
    ep.setDvdEpisode(2);
    ep.setPath(destination.toAbsolutePath().toString());
    mf = new MediaFile(destination.resolve("S01E01E02.jpg").toAbsolutePath(), MediaFileType.THUMB);
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.nfo").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.de.srt").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    ep.setTvShow(show);
    show.addEpisode(ep);

    renameTvShow(show);

    Path showDir = destination.getParent().resolve("Breaking Bad (2008)");
    assertThat(showDir).exists();

    Path seasonDir = showDir.resolve("Season 1");
    assertThat(seasonDir).exists();

    Path video = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.mkv");
    assertThat(video).exists();
    Path thumb = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2-thumb.jpg");
    assertThat(thumb).exists();
    Path nfo = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.nfo");
    assertThat(nfo).exists();
    Path sub = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.deu.srt");
    assertThat(sub).exists();
  }

  /**
   * just a test of a parted episode (two EP files with some extra files)
   */
  @Test
  public void testPartedEpisode() {
    // run with default settings
    TvShowSettings settings = TvShowSettings.getInstance("target/settings");

    // copy over the test files to a new folder
    Path source = Paths.get("target/test-classes/testtvshows/renamer_test/parted");
    Path destination = Paths.get("target/test-classes/tv_show_renamer_parted/ShowForRenamer");
    try {
      FileUtils.deleteDirectory(destination.getParent().toFile());
      FileUtils.copyDirectory(source.toFile(), destination.toFile());
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    TvShow show = new TvShow();
    show.setTitle("Breaking Bad");
    show.setYear(2008);
    show.setDataSource(destination.getParent().toAbsolutePath().toString());
    show.setPath(destination.toAbsolutePath().toString());

    // classical single file episode
    TvShowEpisode ep = new TvShowEpisode();
    ep.setTitle("Pilot");
    ep.setSeason(1);
    ep.setEpisode(1);
    ep.setDvdSeason(1);
    ep.setDvdEpisode(1);
    ep.setPath(destination.toAbsolutePath().toString());
    MediaFile mf = new MediaFile(destination.resolve("S01E01.jpg").toAbsolutePath(), MediaFileType.THUMB);
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01.part1.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01.part2.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01.nfo").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01.de.srt").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    ep.setTvShow(show);
    ep.reEvaluateStacking();
    show.addEpisode(ep);

    renameTvShow(show);

    Path showDir = destination.getParent().resolve("Breaking Bad (2008)");
    assertThat(showDir).exists();

    Path seasonDir = showDir.resolve("Season 1");
    assertThat(seasonDir).exists();

    Path video = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.part1.mkv");
    assertThat(video).exists();
    video = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.part2.mkv");
    assertThat(video).exists();
    Path thumb = seasonDir.resolve("Breaking Bad - S01E01 - Pilot-thumb.jpg");
    assertThat(thumb).exists();
    Path nfo = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.nfo");
    assertThat(nfo).exists();
    Path sub = seasonDir.resolve("Breaking Bad - S01E01 - Pilot.deu.srt");
    assertThat(sub).exists();
  }

  /**
   * this is a really sick test: a parted multi episode (two EP files containing two EPs with some extra files)
   */
  @Test
  public void testComplexEpisode() {
    // run with default settings
    TvShowSettings settings = TvShowSettings.getInstance("target/settings");

    // copy over the test files to a new folder
    Path source = Paths.get("target/test-classes/testtvshows/renamer_test/complex");
    Path destination = Paths.get("target/test-classes/tv_show_renamer_complex/ShowForRenamer");
    try {
      FileUtils.deleteDirectory(destination.getParent().toFile());
      FileUtils.copyDirectory(source.toFile(), destination.toFile());
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    TvShow show = new TvShow();
    show.setTitle("Breaking Bad");
    show.setYear(2008);
    show.setDataSource(destination.getParent().toAbsolutePath().toString());
    show.setPath(destination.toAbsolutePath().toString());

    // classical single file episode
    TvShowEpisode ep = new TvShowEpisode();
    ep.setTitle("Pilot");
    ep.setSeason(1);
    ep.setEpisode(1);
    ep.setDvdSeason(1);
    ep.setDvdEpisode(1);
    ep.setPath(destination.toAbsolutePath().toString());
    MediaFile mf = new MediaFile(destination.resolve("S01E01E02.jpg").toAbsolutePath(), MediaFileType.THUMB);
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.part1.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.part2.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.nfo").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.de.srt").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.sub").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.idx").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    ep.setTvShow(show);
    ep.reEvaluateStacking();
    show.addEpisode(ep);

    ep = new TvShowEpisode();
    ep.setTitle("Pilot 2");
    ep.setSeason(1);
    ep.setEpisode(2);
    ep.setDvdSeason(1);
    ep.setDvdEpisode(1);
    ep.setPath(destination.toAbsolutePath().toString());
    mf = new MediaFile(destination.resolve("S01E01E02.jpg").toAbsolutePath(), MediaFileType.THUMB);
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.part1.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.part2.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.nfo").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.de.srt").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.sub").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    mf = new MediaFile(destination.resolve("S01E01E02.idx").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    ep.setTvShow(show);
    ep.reEvaluateStacking();
    show.addEpisode(ep);

    renameTvShow(show);

    Path showDir = destination.getParent().resolve("Breaking Bad (2008)");
    assertThat(showDir).exists();

    Path seasonDir = showDir.resolve("Season 1");
    assertThat(seasonDir).exists();

    Path video = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.part1.mkv");
    assertThat(video).exists();
    video = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.part2.mkv");
    assertThat(video).exists();
    Path thumb = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2-thumb.jpg");
    assertThat(thumb).exists();
    Path nfo = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.nfo");
    assertThat(nfo).exists();
    Path sub = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.deu.srt");
    assertThat(sub).exists();
    Path sub2 = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.sub");
    assertThat(sub2).exists();
    Path other = seasonDir.resolve("Breaking Bad - S01E01 S01E02 - Pilot - Pilot 2.idx");
    assertThat(other).exists();
  }

  /**
   * just a test of a simple episode (two EPs file with some season artwork)
   */
  @Test
  public void testSimpleEpisodeWithSeasonArtwork() {
    // run with default settings, except season poster filename
    TvShowSettings settings = TvShowSettings.getInstance("target/settings");
    settings.addSeasonPosterFilename(TvShowSeasonPosterNaming.SEASON_FOLDER);
    settings.addSeasonBannerFilename(TvShowSeasonBannerNaming.SEASON_FOLDER);

    // copy over the test files to a new folder
    Path source = Paths.get("target/test-classes/testtvshows/renamer_test/season_artwork");
    Path destination = Paths.get("target/test-classes/tv_show_renamer_season_artwork/ShowForRenamer");
    try {
      FileUtils.deleteDirectory(destination.getParent().toFile());
      FileUtils.copyDirectory(source.toFile(), destination.toFile());
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    TvShow show = new TvShow();
    show.setTitle("Breaking Bad");
    show.setYear(2008);
    show.setDataSource(destination.getParent().toAbsolutePath().toString());
    show.setPath(destination.toAbsolutePath().toString());

    // season artwork
    MediaFile mf = new MediaFile(destination.resolve("season01-banner.jpg").toAbsolutePath(), MediaFileType.SEASON_BANNER);
    mf.gatherMediaInformation();
    show.setSeasonArtwork(1, mf);
    mf = new MediaFile(destination.resolve("season01-poster.jpg").toAbsolutePath(), MediaFileType.SEASON_POSTER);
    mf.gatherMediaInformation();
    show.setSeasonArtwork(1, mf);
    mf = new MediaFile(destination.resolve("season01-thumb.jpg").toAbsolutePath(), MediaFileType.SEASON_THUMB);
    mf.gatherMediaInformation();
    show.setSeasonArtwork(1, mf);

    mf = new MediaFile(destination.resolve("Season 2/season02-banner.jpg").toAbsolutePath(), MediaFileType.SEASON_BANNER);
    mf.gatherMediaInformation();
    show.setSeasonArtwork(2, mf);
    mf = new MediaFile(destination.resolve("Season 2/season02.jpg").toAbsolutePath(), MediaFileType.SEASON_POSTER);
    mf.gatherMediaInformation();
    show.setSeasonArtwork(2, mf);
    mf = new MediaFile(destination.resolve("Season 2/season02-thumb.jpg").toAbsolutePath(), MediaFileType.SEASON_THUMB);
    mf.gatherMediaInformation();
    show.setSeasonArtwork(2, mf);

    mf = new MediaFile(destination.resolve("season03-banner.jpg").toAbsolutePath(), MediaFileType.SEASON_BANNER);
    mf.gatherMediaInformation();
    show.setSeasonArtwork(3, mf);
    mf = new MediaFile(destination.resolve("season03-poster.jpg").toAbsolutePath(), MediaFileType.SEASON_POSTER);
    mf.gatherMediaInformation();
    show.setSeasonArtwork(3, mf);
    mf = new MediaFile(destination.resolve("season03-thumb.jpg").toAbsolutePath(), MediaFileType.SEASON_THUMB);
    mf.gatherMediaInformation();
    show.setSeasonArtwork(3, mf);

    // classical single file episode
    TvShowEpisode ep = new TvShowEpisode();
    ep.setTvShow(show);
    ep.setTitle("Pilot");
    ep.setSeason(1);
    ep.setEpisode(1);
    ep.setDvdSeason(1);
    ep.setDvdEpisode(1);
    ep.setPath(destination.toAbsolutePath().toString());
    mf = new MediaFile(destination.resolve("S01E01.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    show.addEpisode(ep);

    ep = new TvShowEpisode();
    ep.setTvShow(show);
    ep.setTitle("Pilot 2");
    ep.setSeason(2);
    ep.setEpisode(1);
    ep.setDvdSeason(2);
    ep.setDvdEpisode(1);
    ep.setPath(destination.toAbsolutePath().toString());
    mf = new MediaFile(destination.resolve("Season 2/S02E01.mkv").toAbsolutePath());
    mf.gatherMediaInformation();
    ep.addToMediaFiles(mf);
    show.addEpisode(ep);

    renameTvShow(show);

    // check TV show dirs/files
    Path showDir = destination.getParent().resolve("Breaking Bad (2008)");
    assertThat(showDir).exists();

    Path season1Dir = showDir.resolve("Season 1");
    assertThat(season1Dir).exists();
    Path season2Dir = showDir.resolve("Season 2");
    assertThat(season2Dir).exists();

    // season 1 & 2 artwork in season folder and show folder
    Path artwork = season1Dir.resolve("season01.jpg");
    assertThat(artwork).exists();
    artwork = season1Dir.resolve("season01-banner.jpg");
    assertThat(artwork).exists();

    artwork = showDir.resolve("season01-poster.jpg");
    assertThat(artwork).exists();
    artwork = showDir.resolve("season01-banner.jpg");
    assertThat(artwork).exists();
    artwork = showDir.resolve("season01-thumb.jpg");
    assertThat(artwork).exists();

    artwork = season2Dir.resolve("season02.jpg");
    assertThat(artwork).exists();
    artwork = season2Dir.resolve("season02-banner.jpg");
    assertThat(artwork).exists();

    artwork = showDir.resolve("season02-poster.jpg");
    assertThat(artwork).exists();
    artwork = showDir.resolve("season02-banner.jpg");
    assertThat(artwork).exists();
    artwork = showDir.resolve("season02-thumb.jpg");
    assertThat(artwork).exists();

    // season 3 artwork in TV show folder (won't create an own subfolder only for artwork)
    // AND since the banner is not in a season folder, it will have the default name style applied
    artwork = showDir.resolve("season03-poster.jpg");
    assertThat(artwork).exists();
    artwork = showDir.resolve("season03-banner.jpg");
    assertThat(artwork).exists();
    artwork = showDir.resolve("season03-thumb.jpg");
    assertThat(artwork).exists();

    // check episode dirs/files
    Path video = season1Dir.resolve("Breaking Bad - S01E01 - Pilot.mkv");
    assertThat(video).exists();
    video = season2Dir.resolve("Breaking Bad - S02E01 - Pilot 2.mkv");
    assertThat(video).exists();
  }

  private void renameTvShow(TvShow tvShow) {
    TvShowRenameTask task = new TvShowRenameTask(Collections.singletonList(tvShow), null, true);
    task.run(); // blocking
  }
}
