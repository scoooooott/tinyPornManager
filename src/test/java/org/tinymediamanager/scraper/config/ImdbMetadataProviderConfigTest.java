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

package org.tinymediamanager.scraper.config;

import org.junit.Assert;
import org.junit.Test;
import org.tinymediamanager.scraper.imdb.ImdbMetadataProvider;

public class ImdbMetadataProviderConfigTest {

  @Test
  public void testConfig() {
    ImdbMetadataProvider mp = new ImdbMetadataProvider();

    mp.getProviderInfo().getConfig().setValue(ImdbMetadataProvider.USE_TMDB_FOR_MOVIES, true);
    mp.getProviderInfo().getConfig().setValue(ImdbMetadataProvider.USE_TMDB_FOR_TV_SHOWS, false);
    mp.getProviderInfo().getConfig().setValue("scrapeCollectionInfo", true);
    mp.getProviderInfo().getConfig().setValue("filterUnwantedCategories", false);
    mp.getProviderInfo().getConfig().saveToDir("target");

    mp = new ImdbMetadataProvider();
    // force loading from target
    mp.getProviderInfo().getConfig().loadFromDir("target");
    Assert.assertTrue(mp.getProviderInfo().getConfig().getValueAsBool(ImdbMetadataProvider.USE_TMDB_FOR_MOVIES));
    Assert.assertFalse(mp.getProviderInfo().getConfig().getValueAsBool(ImdbMetadataProvider.USE_TMDB_FOR_TV_SHOWS));
    Assert.assertTrue(mp.getProviderInfo().getConfig().getValueAsBool("scrapeCollectionInfo"));
    Assert.assertFalse(mp.getProviderInfo().getConfig().getValueAsBool("filterUnwantedCategories"));
  }
}
