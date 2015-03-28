/*
 * Copyright 2012 - 2015 Manuel Laggner
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

import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.scraper.fanarttv.entities.Images;
import org.tinymediamanager.scraper.util.ApiKey;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class FanartTvTest {
  private static final String CRLF = "\n";

  @BeforeClass
  public static void setUp() {
    StringBuilder config = new StringBuilder("handlers = java.util.logging.ConsoleHandler\n");
    config.append(".level = ALL").append(CRLF);
    config.append("java.util.logging.ConsoleHandler.level = ALL").append(CRLF);
    // Only works with Java 7 or later
    config.append("java.util.logging.SimpleFormatter.format = [%1$tH:%1$tM:%1$tS %4$6s] %2$s - %5$s %6$s%n").append(CRLF);
    // Exclude http logging
    config.append("sun.net.www.protocol.http.HttpURLConnection.level = OFF").append(CRLF);
    InputStream ins = new ByteArrayInputStream(config.toString().getBytes());
    try {
      LogManager.getLogManager().readConfiguration(ins);
    }
    catch (IOException ignored) {
    }
  }

  @Test
  public void testMovieService() {
    FanartTv api = new FanartTv(ApiKey.decryptApikey("2gkQtSYPIxfyThxPXveHiCGXEcqJJwClUDrB5JV60OnQeQ85Ft65kFIk1SBKoge3"));

    // Avatar; tmdb_id 19995
    try {
      Images images = api.getMovieService().getMovieImages("19995");

      assertThat(images).isNotNull();
      assertThat(images.hdmovielogo).isNotNull();
      assertThat(images.hdmovielogo.size()).isEqualTo(7);
      assertThat(images.movieposter).isNotNull();
      assertThat(images.movieposter.size()).isEqualTo(19);
      assertThat(images.movielogo).isNotNull();
      assertThat(images.movielogo.size()).isEqualTo(3);
      assertThat(images.movieart).isNotNull();
      assertThat(images.movieart.size()).isEqualTo(2);
      assertThat(images.hdmovieclearart).isNotNull();
      assertThat(images.hdmovieclearart.size()).isEqualTo(12);
      assertThat(images.moviedisc).isNotNull();
      assertThat(images.moviedisc.size()).isEqualTo(27);
      assertThat(images.moviebackground).isNotNull();
      assertThat(images.moviebackground.size()).isEqualTo(31);
      assertThat(images.moviebanner).isNotNull();
      assertThat(images.moviebanner.size()).isEqualTo(10);
      assertThat(images.moviethumb).isNotNull();
      assertThat(images.moviethumb.size()).isEqualTo(12);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }

    // Avatar, imdb_id tt0499549
    try {
      Images images = api.getMovieService().getMovieImages("tt0499549");

      assertThat(images).isNotNull();
      assertThat(images.hdmovielogo).isNotNull();
      assertThat(images.hdmovielogo.size()).isEqualTo(7);
      assertThat(images.movieposter).isNotNull();
      assertThat(images.movieposter.size()).isEqualTo(19);
      assertThat(images.movielogo).isNotNull();
      assertThat(images.movielogo.size()).isEqualTo(3);
      assertThat(images.movieart).isNotNull();
      assertThat(images.movieart.size()).isEqualTo(2);
      assertThat(images.hdmovieclearart).isNotNull();
      assertThat(images.hdmovieclearart.size()).isEqualTo(12);
      assertThat(images.moviedisc).isNotNull();
      assertThat(images.moviedisc.size()).isEqualTo(27);
      assertThat(images.moviebackground).isNotNull();
      assertThat(images.moviebackground.size()).isEqualTo(31);
      assertThat(images.moviebanner).isNotNull();
      assertThat(images.moviebanner.size()).isEqualTo(10);
      assertThat(images.moviethumb).isNotNull();
      assertThat(images.moviethumb.size()).isEqualTo(12);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  @Test
  public void testTvShowService() {
    FanartTv api = new FanartTv(ApiKey.decryptApikey("2gkQtSYPIxfyThxPXveHiCGXEcqJJwClUDrB5JV60OnQeQ85Ft65kFIk1SBKoge3"));

    // Breaking Bad; tvdb_id 81189
    try {
      Images images = api.getTvShowService().getTvShowImages(81189);

      assertThat(images).isNotNull();
      assertThat(images.hdtvlogo).isNotNull();
      assertThat(images.hdtvlogo.size()).isEqualTo(6);
      assertThat(images.tvposter).isNotNull();
      assertThat(images.tvposter.size()).isEqualTo(6);
      assertThat(images.seasonposter).isNotNull();
      assertThat(images.seasonposter.size()).isEqualTo(6);
      assertThat(images.clearlogo).isNotNull();
      assertThat(images.clearlogo.size()).isEqualTo(9);
      assertThat(images.clearart).isNotNull();
      assertThat(images.clearart.size()).isEqualTo(11);
      assertThat(images.hdclearart).isNotNull();
      assertThat(images.hdclearart.size()).isEqualTo(17);
      assertThat(images.characterart).isNotNull();
      assertThat(images.characterart.size()).isEqualTo(3);
      assertThat(images.tvthumb).isNotNull();
      assertThat(images.tvthumb.size()).isEqualTo(12);
      assertThat(images.seasonthumb).isNotNull();
      assertThat(images.seasonthumb.size()).isEqualTo(12);
      assertThat(images.showbackground).isNotNull();
      assertThat(images.showbackground.size()).isEqualTo(41);
      assertThat(images.tvbanner).isNotNull();
      assertThat(images.tvbanner.size()).isEqualTo(8);
      assertThat(images.seasonbanner).isNotNull();
      assertThat(images.seasonbanner.size()).isEqualTo(5);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
