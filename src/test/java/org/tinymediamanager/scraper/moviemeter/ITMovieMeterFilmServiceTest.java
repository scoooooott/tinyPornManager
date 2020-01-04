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
package org.tinymediamanager.scraper.moviemeter;

import org.junit.Test;
import org.tinymediamanager.scraper.moviemeter.entities.MMFilm;
import org.tinymediamanager.scraper.moviemeter.services.FilmService;
import org.tinymediamanager.scraper.util.ApiKey;
import retrofit2.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ITMovieMeterFilmServiceTest {

  @Test
  public void testFilmService() {
    MovieMeter movieMeter = new MovieMeter();
    movieMeter.setApiKey(ApiKey.decryptApikey("GK5bRYdcKs3WZzOCa1fOQfIeAJVsBP7buUYjc0q4x2/jX66BlSUDKDAcgN/L0JnM"));

    try {
      FilmService filmService = movieMeter.getFilmService();

      // Avatar by MM id
      Response<MMFilm> resp = filmService.getMovieInfo(17552).execute();
      MMFilm film = resp.body();

      assertThat(film).isNotNull();
      assertThat(film.title).isEqualTo("Avatar");

      // Avatar by imdb id
      film = filmService.getMovieInfoByImdbId("tt0499549").execute().body();

      assertThat(film).isNotNull();
      assertThat(film.title).isEqualTo("Avatar");

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
