/*
 * Copyright 2012 - 2017 Manuel Laggner
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

package org.tinymediamanager.core.tvshow.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.entities.Rating;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.filenaming.TvShowNfoNaming;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaGenres;

public class TvShowToNfoConnectorTest {
  @BeforeClass
  public static void setup() {
    // create a default config file for config access
    Settings.getInstance("target/test-classes/");
  }

  @Test
  public void testXbmcNfo() {
    FileUtils.deleteQuietly(new File("target/test-classes/xbmc_nfo/"));
    try {
      Files.createDirectories(Paths.get("target/test-classes/xbmc_nfo/"));
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    try {
      TvShow tvShow = createTvShow("target/test-classes/xbmc_nfo");

      // write it
      List<TvShowNfoNaming> nfoNames = Arrays.asList(TvShowNfoNaming.TV_SHOW);
      TvShowToXbmcConnector connector = new TvShowToXbmcConnector(tvShow);
      connector.write(nfoNames);

      Path nfoFile = Paths.get("target/test-classes/xbmc_nfo/tvshow.nfo");
      assertThat(Files.exists(nfoFile)).isTrue();

      // unmarshal it
      TvShowNfoParser tvShowNfoParser = TvShowNfoParser.parseNfo(nfoFile);
      TvShow newTvShow = tvShowNfoParser.toTvShow();
      compareTvShows(tvShow, newTvShow);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testKodiNfo() {
    FileUtils.deleteQuietly(new File("target/test-classes/kodi_nfo/"));
    try {
      Files.createDirectories(Paths.get("target/test-classes/kodi_nfo/"));
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    try {
      TvShow tvShow = createTvShow("target/test-classes/kodi_nfo");

      // write it
      List<TvShowNfoNaming> nfoNames = Arrays.asList(TvShowNfoNaming.TV_SHOW);
      TvShowToKodiConnector connector = new TvShowToKodiConnector(tvShow);
      connector.write(nfoNames);

      Path nfoFile = Paths.get("target/test-classes/kodi_nfo/tvshow.nfo");
      assertThat(Files.exists(nfoFile)).isTrue();

      // unmarshal it
      TvShowNfoParser tvShowNfoParser = TvShowNfoParser.parseNfo(nfoFile);
      TvShow newTvShow = tvShowNfoParser.toTvShow();
      compareTvShows(tvShow, newTvShow);
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }

  private void compareTvShows(TvShow tvShow, TvShow newTvShow) {
    assertThat(newTvShow.getTitle()).isEqualTo(tvShow.getTitle());
    assertThat(newTvShow.getSortTitle()).isEqualTo(tvShow.getSortTitle());
    assertThat(newTvShow.getRating().getRating()).isEqualTo(tvShow.getRating().getRating());
    assertThat(newTvShow.getRating().getVotes()).isEqualTo(tvShow.getRating().getVotes());
    assertThat(newTvShow.getYear()).isEqualTo(tvShow.getYear());
    assertThat(newTvShow.getPlot()).isEqualTo(tvShow.getPlot());
    assertThat(newTvShow.getRuntime()).isEqualTo(tvShow.getRuntime());
    assertThat(newTvShow.getArtworkUrl(MediaFileType.POSTER)).isEqualTo(tvShow.getArtworkUrl(MediaFileType.POSTER));
    assertThat(newTvShow.getArtworkUrl(MediaFileType.FANART)).isEqualTo(tvShow.getArtworkUrl(MediaFileType.FANART));
    for (Map.Entry<Integer, String> entry : tvShow.getSeasonPosterUrls().entrySet()) {
      String seasonPoster = newTvShow.getSeasonPosterUrl(entry.getKey());
      assertThat(seasonPoster).isEqualTo(entry.getValue());
    }
    assertThat(newTvShow.getImdbId()).isEqualTo(tvShow.getImdbId());
    assertThat(newTvShow.getTvdbId()).isEqualTo(tvShow.getTvdbId());
    assertThat(newTvShow.getProductionCompany()).isEqualTo(tvShow.getProductionCompany());
    assertThat(newTvShow.getCertification()).isEqualTo(tvShow.getCertification());
    assertThat(newTvShow.getIds().size()).isEqualTo(tvShow.getIds().size());
    assertThat(newTvShow.getId("trakt")).isEqualTo(tvShow.getId("trakt"));
    assertThat(newTvShow.getFirstAired()).isEqualTo(tvShow.getFirstAired());
    assertThat(newTvShow.getStatus()).isEqualTo(tvShow.getStatus());
    assertThat(newTvShow.getGenres().size()).isEqualTo(tvShow.getGenres().size());
    assertThat(newTvShow.getGenres().get(0)).isEqualTo(tvShow.getGenres().get(0));
    assertThat(newTvShow.getTags().size()).isEqualTo(tvShow.getTags().size());
    if (!newTvShow.getTags().isEmpty()) {
      assertThat(newTvShow.getTags().get(0)).isEqualTo(tvShow.getTags().get(0));
    }
    assertThat(newTvShow.getActors().size()).isEqualTo(tvShow.getActors().size());
    assertThat(newTvShow.getActors().get(0)).isEqualTo(tvShow.getActors().get(0));
  }

  private TvShow createTvShow(String path) throws Exception {
    TvShow tvShow = new TvShow();
    tvShow.setPath(path);
    tvShow.setTitle("21 Jump Street");
    tvShow.setRating(new Rating(Rating.NFO, 9.0f, 8));
    tvShow.setYear(1987);
    tvShow.setPlot(
        "21 Jump Street was a FOX action/drama series that ran for five seasons (1987-1991). The show revolved around a group of young cops who would use their youthful appearance to go undercover and solve crimes involving teenagers and young adults. 21 Jump Street propelled Johnny Depp to stardom and was the basis for a 2012 comedy/action film of the same name.");
    tvShow.setRuntime(45);
    tvShow.setArtworkUrl("http://poster", MediaFileType.POSTER);
    tvShow.setArtworkUrl("http://fanart", MediaFileType.FANART);
    tvShow.setSeasonPosterUrl(1, "http://season1");
    tvShow.setSeasonPosterUrl(2, "http://season2");
    tvShow.setImdbId("tt0103639");
    tvShow.setTvdbId("812");
    tvShow.setId("trakt", 655);
    tvShow.setProductionCompany("FOX (US)");
    tvShow.setCertification(Certification.US_TVPG);
    tvShow.setStatus("Ended");

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    tvShow.setFirstAired(sdf.parse("1987-04-12"));

    tvShow.addGenre(MediaGenres.ACTION);
    tvShow.addGenre(MediaGenres.ADVENTURE);
    tvShow.addGenre(MediaGenres.DRAMA);

    tvShow.addToTags("80s");

    tvShow.addActor(new Person(Person.Type.ACTOR, "Johnny Depp", "Officer Tom Hanson", "http://thumb1"));
    tvShow.addActor(new Person(Person.Type.ACTOR, "Holly Robinson Peete", "Officer Judy Hoffs", "http://thumb2"));

    return tvShow;
  }
}
