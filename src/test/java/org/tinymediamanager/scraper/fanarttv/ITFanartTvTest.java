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

package org.tinymediamanager.scraper.fanarttv;

import org.junit.Test;
import org.tinymediamanager.scraper.fanarttv.entities.Images;
import org.tinymediamanager.scraper.util.ApiKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ITFanartTvTest {

  @Test
  public void testMovieService() {
    FanartTv api = new FanartTv();
    api.setApiKey(ApiKey.decryptApikey("2gkQtSYPIxfyThxPXveHiCGXEcqJJwClUDrB5JV60OnQeQ85Ft65kFIk1SBKoge3"));

    // Avatar; tmdb_id 19995
    try {
      Images images = api.getMovieService().getMovieImages("19995").execute().body();

      assertThat(images).isNotNull();
      assertThat(images.hdmovielogo).isNotNull();
      assertThat(images.hdmovielogo.size()).isGreaterThan(0);
      assertThat(images.movieposter).isNotNull();
      assertThat(images.movieposter.size()).isGreaterThan(0);
      assertThat(images.movielogo).isNotNull();
      assertThat(images.movielogo.size()).isGreaterThan(0);
      assertThat(images.movieart).isNotNull();
      assertThat(images.movieart.size()).isGreaterThan(0);
      assertThat(images.hdmovieclearart).isNotNull();
      assertThat(images.hdmovieclearart.size()).isGreaterThan(0);
      assertThat(images.moviedisc).isNotNull();
      assertThat(images.moviedisc.size()).isGreaterThan(0);
      assertThat(images.moviebackground).isNotNull();
      assertThat(images.moviebackground.size()).isGreaterThan(0);
      assertThat(images.moviebanner).isNotNull();
      assertThat(images.moviebanner.size()).isGreaterThan(0);
      assertThat(images.moviethumb).isNotNull();
      assertThat(images.moviethumb.size()).isGreaterThan(0);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    // Avatar, imdb_id tt0499549
    try {
      Images images = api.getMovieService().getMovieImages("tt0499549").execute().body();

      assertThat(images).isNotNull();
      assertThat(images.hdmovielogo).isNotNull();
      assertThat(images.hdmovielogo.size()).isGreaterThan(0);
      assertThat(images.movieposter).isNotNull();
      assertThat(images.movieposter.size()).isGreaterThan(0);
      assertThat(images.movielogo).isNotNull();
      assertThat(images.movielogo.size()).isGreaterThan(0);
      assertThat(images.movieart).isNotNull();
      assertThat(images.movieart.size()).isGreaterThan(0);
      assertThat(images.hdmovieclearart).isNotNull();
      assertThat(images.hdmovieclearart.size()).isGreaterThan(0);
      assertThat(images.moviedisc).isNotNull();
      assertThat(images.moviedisc.size()).isGreaterThan(0);
      assertThat(images.moviebackground).isNotNull();
      assertThat(images.moviebackground.size()).isGreaterThan(0);
      assertThat(images.moviebanner).isNotNull();
      assertThat(images.moviebanner.size()).isGreaterThan(0);
      assertThat(images.moviethumb).isNotNull();
      assertThat(images.moviethumb.size()).isGreaterThan(0);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testTvShowService() {
    FanartTv api = new FanartTv();
    api.setApiKey(ApiKey.decryptApikey("2gkQtSYPIxfyThxPXveHiCGXEcqJJwClUDrB5JV60OnQeQ85Ft65kFIk1SBKoge3"));

    // Breaking Bad; tvdb_id 81189
    try {
      Images images = api.getTvShowService().getTvShowImages(81189).execute().body();

      assertThat(images).isNotNull();
      assertThat(images.hdtvlogo).isNotNull();
      assertThat(images.hdtvlogo.size()).isGreaterThan(0);
      assertThat(images.tvposter).isNotNull();
      assertThat(images.tvposter.size()).isGreaterThan(0);
      assertThat(images.seasonposter).isNotNull();
      assertThat(images.seasonposter.size()).isGreaterThan(0);
      assertThat(images.clearlogo).isNotNull();
      assertThat(images.clearlogo.size()).isGreaterThan(0);
      assertThat(images.clearart).isNotNull();
      assertThat(images.clearart.size()).isGreaterThan(0);
      assertThat(images.hdclearart).isNotNull();
      assertThat(images.hdclearart.size()).isGreaterThan(0);
      assertThat(images.characterart).isNotNull();
      assertThat(images.characterart.size()).isGreaterThan(0);
      assertThat(images.tvthumb).isNotNull();
      assertThat(images.tvthumb.size()).isGreaterThan(0);
      assertThat(images.seasonthumb).isNotNull();
      assertThat(images.seasonthumb.size()).isGreaterThan(0);
      assertThat(images.showbackground).isNotNull();
      assertThat(images.showbackground.size()).isGreaterThan(0);
      assertThat(images.tvbanner).isNotNull();
      assertThat(images.tvbanner.size()).isGreaterThan(0);
      assertThat(images.seasonbanner).isNotNull();
      assertThat(images.seasonbanner.size()).isGreaterThan(0);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
