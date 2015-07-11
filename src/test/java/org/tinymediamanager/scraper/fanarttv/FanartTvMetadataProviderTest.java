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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.LogManager;

import org.junit.BeforeClass;
import org.junit.Test;
import org.tinymediamanager.scraper.IMovieArtworkProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaLanguages;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaType;

public class FanartTvMetadataProviderTest {
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
  public void testFanartTvMetadataProvider(){
    IMovieArtworkProvider artworkProvider;

    /**
     * Test movie artwork
     */
    try{
      artworkProvider = new FanartTvMetadataProvider("");

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);
      options.setTmdbId(19995);
      options.setLanguage(MediaLanguages.en);
      options.setArtworkType(MediaArtwork.MediaArtworkType.POSTER);

      List<MediaArtwork> images = artworkProvider.getArtwork(options);
      assertThat(images).isNotNull().isNotEmpty();
      assertThat(images.size()).isGreaterThan(0);
      assertThat(images.get(0).getSizeOrder()).isEqualTo(MediaArtwork.PosterSizes.LARGE.getOrder());
      assertThat(images.get(0).getImageSizes()).isNotNull().isNotEmpty();
      assertThat(images.get(0).getImageSizes().get(0).getHeight()).isGreaterThan(images.get(0).getImageSizes().get(0).getWidth());
      assertThat(images.get(0).getImageSizes().get(0).getWidth()).isLessThan(images.get(0).getImageSizes().get(0).getHeight());
    } catch (Exception e){
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
