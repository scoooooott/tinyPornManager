package org.tinymediamanager.scraper.imdb;

import org.junit.Assert;
import org.junit.Test;

public class ImdbMetadataProviderConfigTest {

  @Test
  public void testConfig() {
    ImdbMetadataProviderConfig config = ImdbMetadataProviderConfig.SETTINGS;
    config.useTmdb = true;
    config.scrapeCollectionInfo = true;
    config.save();

    config.filterUnwantedCategories = false;
    config.scrapeCollectionInfo = false;
    config.useTmdb = false;

    config = ImdbMetadataProviderConfig.SETTINGS;
    Assert.assertTrue(config.useTmdb);
    Assert.assertTrue(config.scrapeCollectionInfo);
  }

}
