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
package org.tinymediamanager.scraper.fanarttv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.List;

import org.apache.commons.lang3.LocaleUtils;
import org.junit.Test;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMovieArtworkProvider;

public class FanartTvMetadataProviderTest {

  @Test
  public void testFanartTvMetadataProvider() {
    IMovieArtworkProvider artworkProvider;

    /**
     * Test movie artwork
     */
    try {
      artworkProvider = new FanartTvMetadataProvider();

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);
      options.setTmdbId(19995);
      options.setLanguage(LocaleUtils.toLocale(MediaLanguages.en.name()));
      options.setArtworkType(MediaArtwork.MediaArtworkType.POSTER);

      List<MediaArtwork> images = artworkProvider.getArtwork(options);
      assertThat(images).isNotNull().isNotEmpty();
      assertThat(images.size()).isGreaterThan(0);
      assertThat(images.get(0).getSizeOrder()).isEqualTo(MediaArtwork.PosterSizes.LARGE.getOrder());
      assertThat(images.get(0).getImageSizes()).isNotNull().isNotEmpty();
      assertThat(images.get(0).getImageSizes().get(0).getHeight()).isGreaterThan(images.get(0).getImageSizes().get(0).getWidth());
      assertThat(images.get(0).getImageSizes().get(0).getWidth()).isLessThan(images.get(0).getImageSizes().get(0).getHeight());
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
