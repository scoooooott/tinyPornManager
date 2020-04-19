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

package org.tinymediamanager.core.tvshow.connector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_BANNER;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_POSTER;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_THUMB;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.core.MediaAiredStatus;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.MediaFileHelper;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaFileAudioStream;
import org.tinymediamanager.core.entities.MediaFileSubtitle;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.core.tvshow.filenaming.TvShowEpisodeNfoNaming;
import org.tinymediamanager.scraper.MediaMetadata;

public class TvShowEpisodeToNfoConnectorTest extends BasicTest {
  @BeforeClass
  public static void setup() {
    // create a default config file for config access
    deleteSettingsFolder();
    Settings.getInstance(getSettingsFolder());
  }

  @Test
  public void testXbmcNfo() {
    FileUtils.deleteQuietly(new File(getSettingsFolder(), "xbmc_nfo"));
    try {
      Files.createDirectories(Paths.get(getSettingsFolder(), "xbmc_nfo"));
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    try {
      TvShow tvShow = createTvShow("xbmc_nfo");
      List<TvShowEpisode> episodes = createEpisodes(tvShow, true);

      // write it
      List<TvShowEpisodeNfoNaming> nfoNames = Collections.singletonList(TvShowEpisodeNfoNaming.FILENAME);
      TvShowEpisodeToXbmcConnector connector = new TvShowEpisodeToXbmcConnector(episodes);
      connector.write(nfoNames);

      Path nfoFile = Paths.get(getSettingsFolder(), "xbmc_nfo/S01E01E02.nfo");
      assertThat(Files.exists(nfoFile)).isTrue();

      // unmarshal it
      TvShowEpisodeNfoParser tvShowEpisodeNfoParser = TvShowEpisodeNfoParser.parseNfo(nfoFile);
      List<TvShowEpisode> newEpisodes = tvShowEpisodeNfoParser.toTvShowEpisodes();
      for (TvShowEpisode episode : newEpisodes) {
        episode.setTvShow(tvShow);
      }
      compareEpisodes(episodes, newEpisodes);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testKodiNfo() {
    FileUtils.deleteQuietly(new File(getSettingsFolder(), "kodi_nfo"));
    try {
      Files.createDirectories(Paths.get(getSettingsFolder(), "kodi_nfo"));
    }
    catch (Exception e) {
      Assertions.fail(e.getMessage());
    }

    try {
      TvShow tvShow = createTvShow("kodi_nfo");
      List<TvShowEpisode> episodes = createEpisodes(tvShow, true);

      // write it
      List<TvShowEpisodeNfoNaming> nfoNames = Collections.singletonList(TvShowEpisodeNfoNaming.FILENAME);
      TvShowEpisodeToXbmcConnector connector = new TvShowEpisodeToXbmcConnector(episodes);
      connector.write(nfoNames);

      Path nfoFile = Paths.get(getSettingsFolder(), "kodi_nfo/S01E01E02.nfo");
      assertThat(Files.exists(nfoFile)).isTrue();

      // unmarshal it
      TvShowEpisodeNfoParser tvShowEpisodeNfoParser = TvShowEpisodeNfoParser.parseNfo(nfoFile);
      List<TvShowEpisode> newEpisodes = tvShowEpisodeNfoParser.toTvShowEpisodes();
      for (TvShowEpisode episode : newEpisodes) {
        episode.setTvShow(tvShow);
      }
      compareEpisodes(episodes, newEpisodes);
    }
    catch (Exception e) {
      e.printStackTrace();
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
    for (Map.Entry<Integer, String> entry : tvShow.getSeasonArtworkUrls(SEASON_POSTER).entrySet()) {
      String seasonPoster = newTvShow.getSeasonArtworkUrl(entry.getKey(), SEASON_POSTER);
      assertThat(seasonPoster).isEqualTo(entry.getValue());
    }
    for (Map.Entry<Integer, String> entry : tvShow.getSeasonArtworkUrls(SEASON_BANNER).entrySet()) {
      String seasonBanner = newTvShow.getSeasonArtworkUrl(entry.getKey(), SEASON_BANNER);
      assertThat(seasonBanner).isEqualTo(entry.getValue());
    }
    for (Map.Entry<Integer, String> entry : tvShow.getSeasonArtworkUrls(SEASON_THUMB).entrySet()) {
      String seasonThumb = newTvShow.getSeasonArtworkUrl(entry.getKey(), SEASON_THUMB);
      assertThat(seasonThumb).isEqualTo(entry.getValue());
    }
    assertThat(newTvShow.getImdbId()).isEqualTo(tvShow.getImdbId());
    assertThat(newTvShow.getTvdbId()).isEqualTo(tvShow.getTvdbId());
    assertThat(newTvShow.getProductionCompany()).isEqualTo(tvShow.getProductionCompany());
    assertThat(newTvShow.getCertification()).isEqualTo(tvShow.getCertification());
    assertThat(newTvShow.getIds().size()).isEqualTo(tvShow.getIds().size());
    assertThat(newTvShow.getId("trakt")).isEqualTo(tvShow.getId("trakt"));
    assertThat(newTvShow.getFirstAired()).isEqualTo(tvShow.getFirstAired());
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
    tvShow.setPath(Paths.get(getSettingsFolder(), path).toString());
    tvShow.setTitle("21 Jump Street");
    tvShow.setRating(new MediaRating(MediaRating.NFO, 9.0f, 8));
    tvShow.setYear(1987);
    tvShow.setPlot(
        "21 Jump Street was a FOX action/drama series that ran for five seasons (1987-1991). The show revolved around a group of young cops who would use their youthful appearance to go undercover and solve crimes involving teenagers and young adults. 21 Jump Street propelled Johnny Depp to stardom and was the basis for a 2012 comedy/action film of the same name.");
    tvShow.setRuntime(45);
    tvShow.setArtworkUrl("http://poster", MediaFileType.POSTER);
    tvShow.setArtworkUrl("http://fanart", MediaFileType.FANART);
    tvShow.setSeasonArtworkUrl(1, "http://season1", SEASON_POSTER);
    tvShow.setSeasonArtworkUrl(2, "http://season2", SEASON_POSTER);
    tvShow.setSeasonArtworkUrl(1, "http://season-banner1", SEASON_BANNER);
    tvShow.setSeasonArtworkUrl(2, "http://season-banner2", SEASON_BANNER);
    tvShow.setSeasonArtworkUrl(1, "http://season-thumb1", SEASON_THUMB);
    tvShow.setSeasonArtworkUrl(2, "http://season-thumb2", SEASON_THUMB);
    tvShow.setImdbId("tt0103639");
    tvShow.setTvdbId("812");
    tvShow.setId("trakt", 655);
    tvShow.setProductionCompany("FOX (US)");
    tvShow.setCertification(MediaCertification.US_TVPG);
    tvShow.setStatus(MediaAiredStatus.ENDED);

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

  private void compareEpisodes(List<TvShowEpisode> episodes, List<TvShowEpisode> newEpisodes) {
    assertThat(episodes.size()).isEqualTo(newEpisodes.size());

    for (int i = 0; i < episodes.size(); i++) {
      TvShowEpisode episode = episodes.get(i);
      TvShowEpisode newEpisode = newEpisodes.get(i);

      assertThat(newEpisode.getTitle()).isEqualTo(episode.getTitle());
      assertThat(newEpisode.getSeason()).isEqualTo(episode.getSeason());
      assertThat(newEpisode.getEpisode()).isEqualTo(episode.getEpisode());
      assertThat(newEpisode.getDisplaySeason()).isEqualTo(episode.getDisplaySeason());
      assertThat(newEpisode.getDisplayEpisode()).isEqualTo(episode.getDisplayEpisode());
      assertThat(newEpisode.getIds()).isEqualTo(episode.getIds());
      assertThat(newEpisode.getPlot()).isEqualTo(episode.getPlot());
      assertThat(newEpisode.getRating().getRating()).isEqualTo(episode.getRating().getRating());
      assertThat(newEpisode.getRating().getVotes()).isEqualTo(episode.getRating().getVotes());
      assertThat(newEpisode.getArtworkUrl(MediaFileType.THUMB)).isEqualTo(episode.getArtworkUrl(MediaFileType.THUMB));
      assertThat(newEpisode.isWatched()).isEqualTo(episode.isWatched());
      assertThat(newEpisode.getFirstAired()).isEqualTo(episode.getFirstAired());
      assertThat(newEpisode.getTags()).isEqualTo(episode.getTags());

      // since we do not write show actors to the episodes, we need to adopt this test
      for (Person person : newEpisode.getGuests()) {
        assertThat(episode.getActors()).contains(person);
        assertThat(newEpisode.getTvShow().getActors()).doesNotContain(person);
      }

      assertThat(newEpisode.getDirectors().size()).isEqualTo(episode.getDirectors().size());
      assertThat(newEpisode.getDirectors().get(0)).isEqualTo(episode.getDirectors().get(0));
      assertThat(newEpisode.getWriters().size()).isEqualTo(episode.getWriters().size());
      assertThat(newEpisode.getWriters().get(0)).isEqualTo(episode.getWriters().get(0));
    }
  }

  private List<TvShowEpisode> createEpisodes(TvShow tvShow, boolean multiEp) throws Exception {
    List<TvShowEpisode> episodes = new ArrayList<>();

    TvShowEpisode episode1 = new TvShowEpisode();
    episode1.setTvShow(tvShow);
    episode1.setPath(tvShow.getPathNIO().toString());
    episode1.setTitle("Pilot (1)");
    episode1.setSeason(1);
    episode1.setEpisode(1);
    episode1.setDisplaySeason(1);
    episode1.setDisplayEpisode(1);
    episode1.setId(MediaMetadata.TVDB, 1234);
    episode1.setPlot(
        "Hanson gets assigned to the Jump Street unit, a special division of the police force which uses young cops to go undercover and stop juvenile crime, when his youthful appearance causes him to be underestimated while on patrol. His first case involves catching drug dealers.");
    episode1.setRating(new MediaRating(MediaRating.NFO, 9.0f, 8));
    episode1.setArtworkUrl("http://thumb1", MediaFileType.THUMB);
    episode1.setWatched(true);

    episode1.addToTags("Pilot");

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    episode1.setFirstAired(sdf.parse("1987-04-12"));

    episode1.addDirector(new Person(Person.Type.DIRECTOR, "Kim Manners", "Director"));
    episode1.addWriter(new Person(Person.Type.DIRECTOR, "Patrick Hasburgh", "Writer"));

    episode1.addActor(new Person(Person.Type.ACTOR, "Charles Payne", "Unknown", "http://thumb1"));
    episode1.addActor(new Person(Person.Type.ACTOR, "Reginald T. Dorsey", "", "http://thumb2"));

    MediaFile mf = new MediaFile();
    mf.setType(MediaFileType.VIDEO);
    mf.setFilename("S01E01E02.mkv");
    mf.setVideoCodec("h264");
    mf.setVideoHeight(720);
    mf.setVideoWidth(1280);
    mf.setDuration(5403);
    mf.setVideo3DFormat(MediaFileHelper.VIDEO_3D_SBS);

    MediaFileAudioStream audio = new MediaFileAudioStream();
    audio.setCodec("AC3");
    audio.setLanguage("en");
    audio.setAudioChannels(6);
    mf.setAudioStreams(Collections.singletonList(audio));

    MediaFileSubtitle sub = new MediaFileSubtitle();
    sub.setLanguage("de");
    mf.addSubtitle(sub);

    episode1.addToMediaFiles(mf);

    episodes.add(episode1);

    if (multiEp) {
      TvShowEpisode episode2 = new TvShowEpisode();
      episode2.setTvShow(tvShow);
      episode2.setTitle("Pilot (2)");
      episode2.setSeason(1);
      episode2.setEpisode(2);
      episode2.setDisplaySeason(1);
      episode2.setDisplayEpisode(2);
      episode2.setId(MediaMetadata.TVDB, 2345);
      episode2.setPlot(
          "Hanson gets assigned to the Jump Street unit, a special division of the police force which uses young cops to go undercover and stop juvenile crime, when his youthful appearance causes him to be underestimated while on patrol. His first case involves catching drug dealers.");
      episode2.setRating(new MediaRating(MediaRating.NFO, 8.0f, 10));
      episode2.setArtworkUrl("http://thumb1", MediaFileType.THUMB);
      episode2.setWatched(false);

      sdf = new SimpleDateFormat("yyyy-MM-dd");
      episode2.setFirstAired(sdf.parse("1987-04-19"));

      episode2.addDirector(new Person(Person.Type.DIRECTOR, "Kim Manners", "Director"));
      episode2.addWriter(new Person(Person.Type.DIRECTOR, "Patrick Hasburgh", "Writer"));

      episode2.addActor(new Person(Person.Type.ACTOR, "Charles Payne", "Unknown", "http://thumb1"));
      episode2.addActor(new Person(Person.Type.ACTOR, "Reginald T. Dorsey", "", "http://thumb2"));

      mf = new MediaFile();
      mf.setType(MediaFileType.VIDEO);
      mf.setFilename("S01E01E02.mkv");
      mf.setVideoCodec("h264");
      mf.setVideoHeight(720);
      mf.setVideoWidth(1280);
      mf.setDuration(5403);
      mf.setVideo3DFormat(MediaFileHelper.VIDEO_3D_SBS);

      audio = new MediaFileAudioStream();
      audio.setCodec("AC3");
      audio.setLanguage("en");
      audio.setAudioChannels(6);
      mf.setAudioStreams(Collections.singletonList(audio));

      sub = new MediaFileSubtitle();
      sub.setLanguage("de");
      mf.addSubtitle(sub);

      episode2.addToMediaFiles(mf);

      episodes.add(episode2);
    }

    return episodes;
  }
}
