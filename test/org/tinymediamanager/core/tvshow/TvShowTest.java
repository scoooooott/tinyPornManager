/*
 * Copyright 2012 - 2013 Manuel Laggner
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

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Assert;
import org.junit.Test;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.tvshow.TvShowEpisodeAndSeasonParser.EpisodeMatchingResult;
import org.tinymediamanager.scraper.util.StrgUtils;

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
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("tmm.odb");
    Globals.entityManager = emf.createEntityManager();

    TvShowList instance = TvShowList.getInstance();

    // instance.findTvShowsInPath("/home/manuel/Videos/Test_Serien/");
    // instance.findTvShowsInPath("D:\\_neu\\Test_Serien");
    for (TvShow show : instance.getTvShows()) {
      for (TvShowEpisode ep : show.getEpisodes()) {
        System.out.println(show.getTitle() + " - Season " + ep.getSeason() + " - Episode " + ep.getEpisode());
      }
    }

    Globals.entityManager.close();
    emf.close();
  }

  /**
   * Test episode matching.
   */
  @Test
  public void testEpisodeMatching() {
    // http://wiki.xbmc.org/index.php?title=Video_library/Naming_files/TV_shows
    // with season
    Assert.assertEquals("E:2", detectEpisode("name.s01e02.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.s01.e02.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.s1e2.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.s01_e02.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.1x02.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.102.ext"));

    // without season
    Assert.assertEquals("E:2", detectEpisode("name.ep02.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.ep_02.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.part.II.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.pt.II.ext"));
    Assert.assertEquals("E:2", detectEpisode("name.pt_II.ext"));

    // multi episode
    Assert.assertEquals("E:1 E:2", detectEpisode("name.s01e01.s01e02.ext"));
    Assert.assertEquals("E:1 E:2", detectEpisode("name.s01e01.episode1.title.s01e02.episode2.title.ext"));
    Assert.assertEquals("E:1 E:2 E:3", detectEpisode("name.s01e01.s01e02.s01e03.ext"));
    Assert.assertEquals("E:1 E:2", detectEpisode("name.1x01_1x02.ext"));

    Assert.assertEquals("E:1 E:2", detectEpisode("name.s01e01 1x02.ext")); // we won't support this

    Assert.assertEquals("E:1 E:2", detectEpisode("name.ep01.ep02.ext"));
    // multi episode short
    Assert.assertEquals("E:1 E:2", detectEpisode("name.s01e01e02.ext"));
    // Assert.assertEquals("E:1 E:2 E:3", detectEpisode("name.s01e01-02-03.ext"));
    // Assert.assertEquals("E:1 E:2", detectEpisode("name.1x01x02.ext"));
    // Assert.assertEquals("E:1 E:2", detectEpisode("name.ep01_02.ext"));
    // multi episode mixed; weird, but valid :p
    // Assert.assertEquals("E:1 E:2 E:3 E:4", detectEpisode("name.1x01e02_03-x-04.ext"));

    // split episode
    // TODO: detect split?
    Assert.assertEquals("E:1", detectEpisode("name.s01e01.1.ext"));
    Assert.assertEquals("E:1", detectEpisode("name.s01e01a.ext"));
    Assert.assertEquals("E:1", detectEpisode("name.1x01.1.ext"));
    Assert.assertEquals("E:1", detectEpisode("name.1x01a.ext"));
    Assert.assertEquals("E:1", detectEpisode("name.ep01.1.ext"));
    Assert.assertEquals("E:1", detectEpisode("name.101.1.ext"));
    Assert.assertEquals("E:1", detectEpisode("name.ep01a_01b.ext"));
    Assert.assertEquals("E:1", detectEpisode("name.s01e01.1.s01e01.2.ext"));
    Assert.assertEquals("E:1", detectEpisode("name.1x01.1x01.2.ext")); // (note this is (1x01.1)x(01.2) not (1x01).(1x01.2))

    detectEpisode("Episode.11.Ocean.Deep.BluRay.720p.x264-x264Crew.mkv");
    detectEpisode("tvs-castle-dl-ituneshd-xvid-101.avi");
    detectEpisode("440 - 2x09 - .avi");
    // detectEpisode("");

    // parseInt testing
    // Assert.assertEquals("E:2", detectEpisode("name.s01e02435454715743435435554.ext"));
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
    EpisodeMatchingResult result = TvShowEpisodeAndSeasonParser.detectEpisodeFromFilename(new File(name));
    // EpisodeMatchingResult result = TvShowEpisodeAndSeasonParser.detectEpisodeFromFilenameAlternative(new File(name), "");
    for (int ep : result.episodes) {
      sb.append(" E:");
      sb.append(ep);
    }
    System.out.println(StrgUtils.padRight(sb.toString().trim(), 40) + name);
    return sb.toString().trim();
  }

}
