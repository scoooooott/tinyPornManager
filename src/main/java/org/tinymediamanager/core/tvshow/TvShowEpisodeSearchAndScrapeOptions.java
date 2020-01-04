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
package org.tinymediamanager.core.tvshow;

import org.tinymediamanager.scraper.MediaSearchAndScrapeOptions;
import org.tinymediamanager.scraper.entities.MediaType;

/**
 * The class TvShowEpisodeSearchAndScrapeOptions is used to hold scrape and search related data for episodes
 * 
 * @author Manuel Laggner
 */
public class TvShowEpisodeSearchAndScrapeOptions extends MediaSearchAndScrapeOptions {

  public TvShowEpisodeSearchAndScrapeOptions() {
    super(MediaType.TV_EPISODE);
  }

  /**
   * copy constructor
   * 
   * @param original
   *          the original to copy
   */
  public TvShowEpisodeSearchAndScrapeOptions(TvShowEpisodeSearchAndScrapeOptions original) {
    super(original);
  }

  /**
   * Load default Settings.
   */
  public void loadDefaults() {
    // language
    language = TvShowModuleManager.SETTINGS.getScraperLanguage();

    // metadata
    metadataScraper = TvShowList.getInstance().getDefaultMediaScraper();

    // artwork
    artworkScrapers.addAll(TvShowList.getInstance().getDefaultArtworkScrapers());

    // trailer
    trailerScrapers.addAll(TvShowList.getInstance().getDefaultTrailerScrapers());

    // subtitle
    subtitleScrapers.addAll(TvShowList.getInstance().getDefaultSubtitleScrapers());
  }
}
