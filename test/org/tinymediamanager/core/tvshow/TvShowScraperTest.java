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

import java.util.List;

import org.junit.Test;
import org.tinymediamanager.scraper.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.thetvdb.TheTvDbMetadataProvider;

/**
 * @author Manuel Laggner
 * 
 */
public class TvShowScraperTest {

  @Test
  public void testSearch() {
    try {
      ITvShowMetadataProvider mp = new TheTvDbMetadataProvider();
      MediaSearchOptions options = new MediaSearchOptions(MediaType.TV_SHOW, "Breaking Bad");
      List<MediaSearchResult> results = null;

      results = mp.search(options);
      for (MediaSearchResult result : results) {
        System.out.println(result);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }

  @Test
  public void testShowMetadata() {
    try {
      ITvShowMetadataProvider mp = new TheTvDbMetadataProvider();
      MediaScrapeOptions options = new MediaScrapeOptions();
      options.setType(MediaType.TV_SHOW);
      options.setId("tvdb", "81189");

      MediaMetadata md = mp.getTvShowMetadata(options);
      System.out.println(md);
    }
    catch (Exception e) {
      e.printStackTrace();
    }

  }

}
