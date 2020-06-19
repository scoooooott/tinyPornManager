/*
 * Copyright 2012 - 2020 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.core.tvshow;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.TmmModuleManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.tvshow.TvShowEpisodeAndSeasonParser.EpisodeMatchingResult;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.entities.TvShowSeason;

/**
 * The Class TvShowTest.
 * 
 * @author Manuel Laggner
 */
public class TvShowTest extends BasicTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    deleteSettingsFolder();
    Settings.getInstance(getSettingsFolder());
    setTraceLogging();
  }

  @Test
  public void testTvShows() {
    try {
      TmmModuleManager.getInstance().startUp();
      TvShowModuleManager.getInstance().startUp();
      createFakeShow("Show 1");
      createFakeShow("Show 2");
      createFakeShow("Show 3");

      TvShowList instance = TvShowList.getInstance();

      for (TvShow show : instance.getTvShows()) {
        System.out.println(show.getTitle());
        for (TvShowSeason season : show.getSeasons()) {
          System.out.println("Season " + season.getSeason());
          for (MediaFile mf : season.getMediaFiles()) {
            System.out.println(mf.toString());
          }
        }
      }

      TvShowModuleManager.getInstance().shutDown();
      TmmModuleManager.getInstance().shutDown();
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Test TV renamer
   * 
   * @throws Exception
   */
  @Test
  public void testRenamerParams() throws Exception {
    // setup dummy
    MediaFile dmf = new MediaFile(Paths.get("/path/to", "video.avi"));

    TmmModuleManager.getInstance().startUp();
    TvShowModuleManager.getInstance().startUp();

    TvShow show = new TvShow();
    show.setPath("target/test-classes/");
    show.setTitle("showname");

    TvShowEpisode ep = new TvShowEpisode();
    ep.setTitle("episodetitle2");
    ep.setSeason(1);
    ep.setEpisode(2);
    ep.addToMediaFiles(dmf);
    ep.setTvShow(show);
    show.addEpisode(ep);

    ep = new TvShowEpisode();
    ep.setTitle("3rd episodetitle");
    ep.setSeason(1);
    ep.setEpisode(3);
    ep.addToMediaFiles(dmf);
    ep.setTvShow(show);
    show.addEpisode(ep);

    TvShowList.getInstance().addTvShow(show);
    // setup done

    // display renamed EP name :)
    System.out.println(TvShowRenamer.createDestination(TvShowModuleManager.SETTINGS.getRenamerFilename(), show.getEpisodes()));
    System.out.println(TvShowRenamer.generateEpisodeFilenames(show, dmf).get(0).getFilename());

    TvShowModuleManager.getInstance().shutDown();
    TmmModuleManager.getInstance().shutDown();
  }

  /**
   * Test episode matching.
   */
  @Test
  public void testEpisodeMatching() {
    // assertEqual("S: E:", detectEpisode(""));

    // ************************************************************************
    // various real world examples
    assertEqual("S:8 E:4", detectEpisode("Homeland - Temporada 8 [HDTV][Cap.804][Castellano][www.descargas2020.org].avi"));
    assertEqual("S:-1 E:105", detectEpisode("EP105 The Bed of Nails.avi"));
    assertEqual("S:3 E:5", detectEpisode("S03 EP05 The Bed of Nails.avi"));
    assertEqual("S:3 E:105", detectEpisode("S03 EP105 The Bed of Nails.avi"));
    assertEqual("S:3 E:5", detectEpisode("S03.EP05.The.Bed.of.Nails.avi"));
    assertEqual("S:1 E:101", detectEpisode("Eisenbahn-Romantik.S01.E101.mp4"));
    assertEqual("S:2011", detectEpisode("Game of Thrones\\2011-04-17 - Winter Is Coming.avi"));
    assertEqual("S:2011", detectEpisode("Game of Thrones\\17.04.2011 - Winter Is Coming.avi"));
    assertEqual("S:5 E:1", detectEpisode("Breaking Bad S05E01 S05E02 HDTV XViD-xyz\\E01 - Live Free or Die.avi"));
    assertEqual("S:5 E:1", detectEpisode("Breaking Bad S05E01 S05E02 HDTV XViD-xyz\\S05E01 - Live Free or Die.avi"));
    assertEqual("S:2 E:13", detectEpisode("Simon & Simon\\Season 2\\Simon & Simon - S02E13\\VIDEO_TS\\VTS_01_1.VOB"));
    assertEqual("S:1 E:1 E:2 E:3", detectEpisode("Dexter S01E01 S01E02 S01E03\\VIDEO_TS\\VIDEO_TS.VOB"));
    assertEqual("S:1 E:1", detectEpisode("TheShowName S01E01 Episode Name (1920x1080) [UploaderTag].mp4"));
    assertEqual("S:8 E:1", detectEpisode("BlBlub - S08E01 - Messy S08E01 - Messy.mp4"));
    assertEqual("S:2 E:17", detectEpisode("Brooklyn Nine-Nine S02E17 HDTV x264 AAC E-Subs [GWC].mp4"));
    assertEqual("S:2 E:4", detectEpisode("Its Always Sunny In Philadelphia Season 02 Episode 04 Charlie Gets Crippled-1.mp4"));
    assertEqual("S:1 E:4", detectEpisode("Season 1/04 Charlie Has Cancer-1.mp4"));
    assertEqual("S:1 E:9", detectEpisode("Band of Brothers - 109 - Wir Waren Wie Brüder - Warum Wir Kämpfen (2001)"));
    assertEqual("S:1 E:25", detectEpisode("Cowboy Bebop - S01E25 - The Real Folk Blues Part II.mkv")); // roman mixed with normal
    assertEqual("S:1 E:3", detectEpisode("The.Odd.Couple.2015.S01E03.720p.HDTV"));
    assertEqual("S:1 E:1 E:2 E:3", detectEpisode("Stargate Universe (01x01_01x02_01x03) - Air (1)(2)(3)"));
    assertEqual("S:-1 E:11", detectEpisode("Episode.11.Ocean.Deep.BluRay.720p.x264-x264Crew.mkv"));
    assertEqual("S:1 E:1", detectEpisode("tvs-castle-dl-ituneshd-xvid-101.avi"));
    assertEqual("S:2 E:9", detectEpisode("440 - 2x09 - .avi"));
    assertEqual("S:-1 E:2", detectEpisode("\\Good L G (1 - 13)\\[CBM]_Good_L_G!_-_02_-_The_Battle_Begins_[720p]_[4A34853E].mkv"));
    assertEqual("S:3 E:1", detectEpisode("s8-vierfrauen-s03e01-repack.avi"));
    assertEqual("S:-1 E:3", detectEpisode("tvp-wildesskandinavien-e03-720p.mkv"));
    assertEqual("S:4 E:13", detectEpisode("s800The Mentalist_S04E13_Die goldene Feder.avi"));
    assertEqual("S:1 E:1", detectEpisode("AwesomeTvShow.S01E01-480p.mkv"));
    assertEqual("S:7 E:9 E:10", detectEpisode("stvs7ep9-10.avi"));
    assertEqual("S:1 E:545", detectEpisode("s01e545 - Steamtown USA.mkv")); // http://thetvdb.com/?tab=season&seriesid=188331&seasonid=311381&lid=7
    assertEqual("S:13 E:2", detectEpisode("Doctor.Who.S13.E2.Part4.Planet.of.Evil.DVDRip.XviD-m00tv.avi"));
    assertEqual("S:3 E:5", detectEpisode("vs-once-upon-a-time-_S03XE05_dd51-ded-dl-7p-bd-x264-305.mkv"));
    assertEqual("S:5 E:1", detectEpisode("Live_at_the_Apollo_Series_5_-_Episode_1_b00p86mz_default"));
    assertEqual("S:6 E:1", detectEpisode("The.League.S06E01.720p.WEB-DL.DD5.1.H.264-pcsyndicate.mkv"));
    assertEqual("S:2 E:9", detectEpisode("Season 02/CSI.Crime.Scene.Investigation.S02E09.And.Then.There.Were.None.360p.DVDRip.MP3.XviD.avi"));
    assertEqual("S:7 E:15", detectEpisode("The.Big.Bang.Theory.S07E15.Eisenbahnromantik.German.DD51.Dubbed.DL.1080p.BD.x264-TVS.mkv"));
    assertEqual("S:1946 E:5", detectEpisode("S1946E05.mkv"));
    assertEqual("S:3 E:8", detectEpisode("Game of Thrones - 3x08 - Die Zweitgeborenen (Second sons)[1080p AAC-6ch de en].avi"));
    assertEqual("S:10 E:5", detectEpisode("Looney Tunes - 10x05 - Episodename"));
    assertEqual("S:1960 E:5", detectEpisode("Looney Tunes - 1960x05 - Episodename"));
    assertEqual("S:4 E:1", detectEpisode("The Big Bang Theory_S04E01_31 Liebhaber, aufgerundet.m4v"));
    assertEqual("S:1 E:2 E:4", detectEpisode("Shaun das Schaf - S01E02_1x04 - Badetag_Summen der Bienen.ts"));
    assertEqual("S:3 E:3", detectEpisode("Supergirl - S03E03 S03E03 - Far From the Tree - Far From the Tree.mkv"));

    // FIXME: TV test pattern which currently do not work...
    // assertEqual("S:1 E:13 E:14 E:15", detectEpisode("Peter Pan S01E13_1x14_1x15 - El Hookato.ts")); // finds 1&13

    // ************************************************************************
    // 1-3 chars, if they are the ONLY numbers in file
    assertEqual("S:-1 E:2", detectEpisode("2.mkv"));
    assertEqual("S:-1 E:2", detectEpisode("2 name.mkv"));
    assertEqual("S:-1 E:2", detectEpisode("name 2.mkv"));

    assertEqual("S:-1 E:2", detectEpisode("02.mkv"));
    assertEqual("S:-1 E:2", detectEpisode("02 name.mkv"));
    assertEqual("S:-1 E:2", detectEpisode("name 02.mkv"));

    assertEqual("S:1 E:2", detectEpisode("102.mkv"));
    assertEqual("S:1 E:2", detectEpisode("102 name.mkv"));
    assertEqual("S:1 E:2", detectEpisode("name 102.mkv"));

    assertEqual("S:1 E:2", detectEpisode("season 1\\nam.e.2.mkv"));
    assertEqual("S:1 E:2", detectEpisode("season 1/nam.e.2.mkv"));

    // TODO: currently we take the FIRST number and treat it as episode
    // NO multi matching for just numbers!!
    assertEqual("S:-1 E:2", detectEpisode("2 3 6.mkv"));
    assertEqual("S:-1 E:2", detectEpisode("02 03 04 name.mkv"));
    // except for 3 char ones ;)
    assertEqual("S:1 E:1 E:2 E:3", detectEpisode("101 102 103.mkv"));
    assertEqual("S:1 E:3", detectEpisode("1 12 103 25 7.mkv")); // start with highest number

    // ************************************************************************
    // http://wiki.xbmc.org/index.php?title=Video_library/Naming_files/TV_shows
    // with season
    assertEqual("S:1 E:2", detectEpisode("name.s01e02.ext"));
    assertEqual("S:1 E:2", detectEpisode("name.s01.e02.ext"));
    assertEqual("S:1 E:2", detectEpisode("name.s1e2.ext"));
    assertEqual("S:1 E:2", detectEpisode("name.s01_e02.ext"));
    assertEqual("S:1 E:2", detectEpisode("name.1x02.blablubb.ext"));
    assertEqual("S:1 E:2", detectEpisode("name.1x02.ext"));
    assertEqual("S:1 E:2", detectEpisode("name.102.ext"));

    // without season
    assertEqual("S:-1 E:2", detectEpisode("name.ep02.ext"));
    assertEqual("S:-1 E:2", detectEpisode("name.ep_02.ext"));
    assertEqual("S:-1 E:2", detectEpisode("name.part.II.ext"));
    assertEqual("S:-1 E:2", detectEpisode("name.pt.II.ext"));
    assertEqual("S:-1 E:2", detectEpisode("name.pt_II.ext"));

    // multi episode
    assertEqual("S:1 E:1 E:2", detectEpisode("name.s01e01.s01e02.ext"));
    assertEqual("S:1 E:1", detectEpisode("name.s01e01.s01e03.ext")); // second EP must be subsequent number (ascending)!
    assertEqual("S:1 E:2", detectEpisode("name.s01e02.s01e01.ext")); // second EP must be subsequent number (ascending)!
    assertEqual("S:1 E:1 E:2", detectEpisode("name.s01e01.episode1.title.s01e02.episode2.title.ext"));
    assertEqual("S:1 E:1 E:2 E:3", detectEpisode("name.s01e01.s01e02.s01e03.ext"));
    assertEqual("S:1 E:1 E:2", detectEpisode("name.1x01_1x02.ext")); // works but shouldn't ;) _1 is detected as e1
    assertEqual("S:2 E:11 E:12 E:13", detectEpisode("name.2x11_2x12_2x13.ext")); // worst case: _2 is always being detected as e2
    assertEqual("S:1 E:1 E:2", detectEpisode("name.s01e01 1x02.ext"));
    assertEqual("S:-1 E:1 E:2", detectEpisode("name.ep01.ep02.ext"));

    // multi episode short
    assertEqual("S:1 E:1 E:2", detectEpisode("name.s01e01e02.ext"));
    assertEqual("S:1 E:1 E:2 E:3", detectEpisode("name.s01e01-02-03.ext"));
    assertEqual("S:1 E:1 E:2", detectEpisode("name.1x01x02.ext"));
    // assertEqual("S:-1 E:1 E:2", detectEpisode("name.ep01_02.ext"));

    // multi episode mixed; weird, but valid :p - we won't detect that now because the
    // regexp would cause too much false positives
    // assertEqual("S:1 E:1 E:2 E:3 E:4", detectEpisode("name.1x01e02_03-x-04.ext"));

    // split episode
    // TODO: detect split?
    assertEqual("S:1 E:1 Split", detectEpisode("name.s01e01.CD1.ext"));
    assertEqual("S:1 E:1 Split", detectEpisode("name.s01e01.a.ext"));
    assertEqual("S:1 E:1 Split", detectEpisode("name.1x01.part1.ext"));
    assertEqual("S:1 E:1 Split", detectEpisode("name.1x01.pt.1.ext"));
    assertEqual("S:-1 E:1", detectEpisode("name.ep01.1.ext")); // do not detect that one
    // assertEqual("S:1 E:1", detectEpisode("name.101.1.ext"));
    assertEqual("S:-1 E:1 Split", detectEpisode("name.ep01a_01.discb.ext"));
    assertEqual("S:1 E:1 Split", detectEpisode("name.s01e01.1.s01e01.2.of.2.ext"));
    assertEqual("S:1 E:1", detectEpisode("name.1x01.1x01.2.ext")); // do not detect that one

  }

  /**
   * Detect episode.
   * 
   * @param name
   *          the name
   * @return the string
   */
  private String detectEpisode(String name) {
    StringBuilder sb = new StringBuilder();
    // EpisodeMatchingResult result = TvShowEpisodeAndSeasonParser.detectEpisodeFromFilename(new File(name));
    EpisodeMatchingResult result = TvShowEpisodeAndSeasonParser.detectEpisodeFromFilenameAlternative(name, "asdf[.*asdf");
    sb.append("S:");
    sb.append(result.season);
    for (int ep : result.episodes) {
      sb.append(" E:");
      sb.append(ep);
    }
    if (result.stackingMarkerFound) {
      sb.append(" Split");
    }
    System.out.println(padRight(sb.toString().trim(), 40) + name);
    return sb.toString().trim();
  }

  private String padRight(String s, int n) {
    return String.format("%1$-" + n + "s", s);
  }

  /**
   * Test the removal of season/episode string for clean title
   */
  @Test
  public void testRemoveEpisodeString() {
    // assertEqual("S: E:", detectEpisode(""));

    // ************************************************************************
    // various real world examples
    assertEqual("Der Weg nach Uralia", cleanTitle("Die Gummibärenbande - S05E02 - Der Weg nach Uralia.avi", "Die Gummibärenbande"));
    assertEqual("BlBlub Messy", cleanTitle("BlBlub - S08E01 - Messy.mp4", ""));
    assertEqual("Messy", cleanTitle("BlBlub - S08E01 - Messy.mp4", "BlBlub"));
    // assertEqual("episode1 title episode2 title", cleanTitle("name.s01e01.episode1.title.s01e02.episode2.title.ext", "name")); // E1 removed!
    assertEqual("my first title my second title", cleanTitle("name.s01e01.my.first.title.s01e02.my.second.title.ext", "name"));
    assertEqual("ep01 ep02", cleanTitle("name.ep01.ep02.ext", "name")); // no title
  }

  private String cleanTitle(String filename, String showname) {
    return TvShowEpisodeAndSeasonParser.cleanEpisodeTitle(filename, showname);
  }

  @Test
  public void testSeasonFolderDetection() {
    TvShowSettings.getInstance(getSettingsFolder()).setRenamerSeasonFoldername("S${seasonNr}");
    TvShow tvShow = new TvShow();
    tvShow.setPath("/media/tvshows/show");

    // Season 1: 80% of the episodes are in the subfolder "Season 01"
    TvShowEpisode episode = new TvShowEpisode();
    episode.setSeason(1);
    episode.setEpisode(1);
    MediaFile mf = new MediaFile(Paths.get("/media/tvshows/show/Season 01/s01e01.avi"));
    episode.addToMediaFiles(mf);
    tvShow.addEpisode(episode);

    episode = new TvShowEpisode();
    episode.setSeason(1);
    episode.setEpisode(2);
    mf = new MediaFile(Paths.get("/media/tvshows/show/Season 01/ep2/s01e02.avi"));
    episode.addToMediaFiles(mf);
    tvShow.addEpisode(episode);

    episode = new TvShowEpisode();
    episode.setSeason(1);
    episode.setEpisode(3);
    mf = new MediaFile(Paths.get("/media/tvshows/show/Season 01/ep3/extract/s01e03.avi"));
    episode.addToMediaFiles(mf);
    tvShow.addEpisode(episode);

    episode = new TvShowEpisode();
    episode.setSeason(1);
    episode.setEpisode(4);
    mf = new MediaFile(Paths.get("/media/tvshows/show/Season 1/s01e04.avi"));
    episode.addToMediaFiles(mf);
    tvShow.addEpisode(episode);

    episode = new TvShowEpisode();
    episode.setSeason(1);
    episode.setEpisode(5);
    mf = new MediaFile(Paths.get("/media/tvshows/show/Season 01/s01e05.avi"));
    episode.addToMediaFiles(mf);
    tvShow.addEpisode(episode);

    assertThat(TvShowHelpers.detectSeasonFolder(tvShow, 1)).isEqualTo("Season 01");

    // Season 2: every EP is in another subfolder
    episode = new TvShowEpisode();
    episode.setSeason(2);
    episode.setEpisode(1);
    mf = new MediaFile(Paths.get("/media/tvshows/show/Season 2/s02e01.avi"));
    episode.addToMediaFiles(mf);
    tvShow.addEpisode(episode);

    episode = new TvShowEpisode();
    episode.setSeason(2);
    episode.setEpisode(2);
    mf = new MediaFile(Paths.get("/media/tvshows/show/Season 02/ep2/s02e02.avi"));
    episode.addToMediaFiles(mf);
    tvShow.addEpisode(episode);

    episode = new TvShowEpisode();
    episode.setSeason(2);
    episode.setEpisode(3);
    mf = new MediaFile(Paths.get("/media/tvshows/show/S2/ep3/extract/s02e03.avi"));
    episode.addToMediaFiles(mf);
    tvShow.addEpisode(episode);

    episode = new TvShowEpisode();
    episode.setSeason(2);
    episode.setEpisode(4);
    mf = new MediaFile(Paths.get("/media/tvshows/show/s02e04.avi"));
    episode.addToMediaFiles(mf);
    tvShow.addEpisode(episode);

    episode = new TvShowEpisode();
    episode.setSeason(2);
    episode.setEpisode(5);
    mf = new MediaFile(Paths.get("/media/tvshows/show/s02e05/s02e05.avi"));
    episode.addToMediaFiles(mf);
    tvShow.addEpisode(episode);

    assertThat(TvShowHelpers.detectSeasonFolder(tvShow, 2)).isEqualTo("S2");
  }
}
