/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.tinymediamanager.Globals;
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
public class TvShowTest {

  /**
   * Test tv shows.
   */
  @Test
  public void testTvShows() {
    try {
      TmmModuleManager.getInstance().startUp();
      TvShowModuleManager.getInstance().startUp();
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
   */
  @Test
  public void testRenamerParams() {
    // setup dummy
    MediaFile dmf = new MediaFile(new File("/path/to", "video.avi"));

    TvShow show = new TvShow();
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

    // parameters (global)
    Globals.settings.getTvShowSettings().setRenamerAddSeason(true);
    Globals.settings.getTvShowSettings().setRenamerAddShow(true);
    Globals.settings.getTvShowSettings().setRenamerAddTitle(true);
    Globals.settings.getTvShowSettings().setRenamerFormat(TvShowEpisodeNaming.WITH_SE);
    Globals.settings.getTvShowSettings().setRenamerSeparator(".");

    // display renamed EP name :)
    System.out.println(TvShowRenamer.generateFilename(show, dmf));

    Globals.settings.getTvShowSettings().setRenamerAddTitle(false);
    System.out.println(TvShowRenamer.generateFilename(show, dmf));

    Globals.settings.getTvShowSettings().setRenamerAddShow(false);
    System.out.println(TvShowRenamer.generateFilename(show, dmf));

    Globals.settings.getTvShowSettings().setRenamerAddSeason(false);
    System.out.println(TvShowRenamer.generateFilename(show, dmf));
  }

  /**
   * Test episode matching.
   */
  @Test
  public void testEpisodeMatching() {
    // detectEpisode("");
    Assert.assertEquals("S:-1", detectEpisode(""));

    // ************************************************************************
    // various real world examples
    Assert.assertEquals("S:-1 E:11", detectEpisode("Episode.11.Ocean.Deep.BluRay.720p.x264-x264Crew.mkv"));
    Assert.assertEquals("S:1 E:1", detectEpisode("tvs-castle-dl-ituneshd-xvid-101.avi"));
    Assert.assertEquals("S:2 E:9", detectEpisode("440 - 2x09 - .avi"));
    Assert.assertEquals("S:-1 E:2", detectEpisode("\\Good L G (1 - 13)\\[CBM]_Good_L_G!_-_02_-_The_Battle_Begins_[720p]_[4A34853E].mkv"));
    Assert.assertEquals("S:3 E:1", detectEpisode("s8-vierfrauen-s03e01-repack.avi"));
    Assert.assertEquals("S:-1 E:3", detectEpisode("tvp-wildesskandinavien-e03-720p.mkv"));
    Assert.assertEquals("S:4 E:13", detectEpisode("s800The Mentalist_S04E13_Die goldene Feder.avi"));
    Assert.assertEquals("S:1 E:1", detectEpisode("AwesomeTvShow.S01E01-480p.mkv"));
    Assert.assertEquals("S:7 E:9 E:10", detectEpisode("stvs7ep9-10.avi"));
    Assert.assertEquals("S:1 E:545", detectEpisode("s01e545 - Steamtown USA.mkv")); // http://thetvdb.com/?tab=season&seriesid=188331&seasonid=311381&lid=7
    Assert.assertEquals("S:13 E:2", detectEpisode("Doctor.Who.S13.E2.Part4.Planet.of.Evil.DVDRip.XviD-m00tv.avi"));
    Assert.assertEquals("S:3 E:5", detectEpisode("vs-once-upon-a-time-_S03XE05_dd51-ded-dl-7p-bd-x264-305.mkv"));
    Assert.assertEquals("S:5 E:1", detectEpisode("Live_at_the_Apollo_Series_5_-_Episode_1_b00p86mz_default"));
    Assert.assertEquals("S:6 E:1", detectEpisode("The.League.S06E01.720p.WEB-DL.DD5.1.H.264-pcsyndicate.mkv"));
    Assert.assertEquals("S:2 E:9", detectEpisode("Season 02/CSI.Crime.Scene.Investigation.S02E09.And.Then.There.Were.None.360p.DVDRip.MP3.XviD.avi"));
    Assert.assertEquals("S:7 E:15", detectEpisode("The.Big.Bang.Theory.S07E15.Eisenbahnromantik.German.DD51.Dubbed.DL.1080p.BD.x264-TVS.mkv"));
    Assert.assertEquals("S:1946 E:5", detectEpisode("S1946E05.mkv"));
    Assert.assertEquals("S:3 E:8", detectEpisode("Game of Thrones - 3x08 - Die Zweitgeborenen (Second sons)[1080p AAC-6ch de en].avi"));
    Assert.assertEquals("S:10 E:5", detectEpisode("Looney Tunes - 10x05 - Episodename"));
    Assert.assertEquals("S:1960 E:5", detectEpisode("Looney Tunes - 1960x05 - Episodename"));
    Assert.assertEquals("S:4 E:1", detectEpisode("The Big Bang Theory_S04E01_31 Liebhaber, aufgerundet.m4v"));
    Assert.assertEquals("S:1 E:2 E:4", detectEpisode("Shaun das Schaf - S01E02_1x04 - Badetag_Summen der Bienen.ts"));

    // FIXME: TV test pattern which currently do not work...
    // Assert.assertEquals("S:1 E:13 E:14 E:15", detectEpisode("Peter Pan S01E13_1x14_1x15 - El Hookato.ts")); // finds 1&13

    // ************************************************************************
    // 1-3 chars, if they are the ONLY numbers in file
    Assert.assertEquals("S:-1 E:2", detectEpisode("2.mkv"));
    Assert.assertEquals("S:-1 E:2", detectEpisode("2 name.mkv"));
    Assert.assertEquals("S:-1 E:2", detectEpisode("name 2.mkv"));

    Assert.assertEquals("S:-1 E:2", detectEpisode("02.mkv"));
    Assert.assertEquals("S:-1 E:2", detectEpisode("02 name.mkv"));
    Assert.assertEquals("S:-1 E:2", detectEpisode("name 02.mkv"));

    Assert.assertEquals("S:1 E:2", detectEpisode("102.mkv"));
    Assert.assertEquals("S:1 E:2", detectEpisode("102 name.mkv"));
    Assert.assertEquals("S:1 E:2", detectEpisode("name 102.mkv"));

    Assert.assertEquals("S:1 E:2", detectEpisode("season 1\\nam.e.2.mkv"));
    Assert.assertEquals("S:1 E:2", detectEpisode("season 1/nam.e.2.mkv"));
    Assert.assertEquals("S:-1", detectEpisode("2 3 6.mkv")); // ohm... NO we shouldn't not detect this as 3 EPs
    Assert.assertEquals("S:-1", detectEpisode("02 03 04 name.mkv")); // same here

    // ************************************************************************
    // http://wiki.xbmc.org/index.php?title=Video_library/Naming_files/TV_shows
    // with season
    Assert.assertEquals("S:1 E:2", detectEpisode("name.s01e02.ext"));
    Assert.assertEquals("S:1 E:2", detectEpisode("name.s01.e02.ext"));
    Assert.assertEquals("S:1 E:2", detectEpisode("name.s1e2.ext"));
    Assert.assertEquals("S:1 E:2", detectEpisode("name.s01_e02.ext"));
    Assert.assertEquals("S:1 E:2", detectEpisode("name.1x02.blablubb.ext"));
    Assert.assertEquals("S:1 E:2", detectEpisode("name.1x02.ext"));
    Assert.assertEquals("S:1 E:2", detectEpisode("name.102.ext"));

    // without season
    Assert.assertEquals("S:-1 E:2", detectEpisode("name.ep02.ext"));
    Assert.assertEquals("S:-1 E:2", detectEpisode("name.ep_02.ext"));
    Assert.assertEquals("S:-1 E:2", detectEpisode("name.part.II.ext"));
    Assert.assertEquals("S:-1 E:2", detectEpisode("name.pt.II.ext"));
    Assert.assertEquals("S:-1 E:2", detectEpisode("name.pt_II.ext"));

    // multi episode
    Assert.assertEquals("S:1 E:1 E:2", detectEpisode("name.s01e01.s01e02.ext"));
    Assert.assertEquals("S:1 E:1 E:2", detectEpisode("name.s01e01.episode1.title.s01e02.episode2.title.ext"));
    Assert.assertEquals("S:1 E:1 E:2 E:3", detectEpisode("name.s01e01.s01e02.s01e03.ext"));
    Assert.assertEquals("S:1 E:1 E:2", detectEpisode("name.1x01_1x02.ext"));
    Assert.assertEquals("S:1 E:1 E:2", detectEpisode("name.s01e01 1x02.ext"));
    Assert.assertEquals("S:-1 E:1 E:2", detectEpisode("name.ep01.ep02.ext"));

    // multi episode short
    Assert.assertEquals("S:1 E:1 E:2", detectEpisode("name.s01e01e02.ext"));
    Assert.assertEquals("S:1 E:1 E:2 E:3", detectEpisode("name.s01e01-02-03.ext"));
    Assert.assertEquals("S:1 E:1 E:2", detectEpisode("name.1x01x02.ext"));
    Assert.assertEquals("S:-1 E:1 E:2", detectEpisode("name.ep01_02.ext"));

    // multi episode mixed; weird, but valid :p
    Assert.assertEquals("S:1 E:1 E:2 E:3 E:4", detectEpisode("name.1x01e02_03-x-04.ext"));

    // split episode
    // TODO: detect split?
    Assert.assertEquals("S:1 E:1", detectEpisode("name.s01e01.1.ext"));
    Assert.assertEquals("S:1 E:1", detectEpisode("name.s01e01a.ext"));
    Assert.assertEquals("S:1 E:1", detectEpisode("name.1x01.1.ext"));
    Assert.assertEquals("S:1 E:1", detectEpisode("name.1x01a.ext"));
    Assert.assertEquals("S:-1 E:1", detectEpisode("name.ep01.1.ext"));
    // Assert.assertEquals("S:1 E:1", detectEpisode("name.101.1.ext"));
    Assert.assertEquals("S:-1 E:1", detectEpisode("name.ep01a_01b.ext"));
    Assert.assertEquals("S:1 E:1", detectEpisode("name.s01e01.1.s01e01.2.ext"));
    Assert.assertEquals("S:1 E:1", detectEpisode("name.1x01.1x01.2.ext"));

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
    System.out.println(padRight(sb.toString().trim(), 40) + name);
    return sb.toString().trim();
  }

  private String padRight(String s, int n) {
    return String.format("%1$-" + n + "s", s);
  }

}
