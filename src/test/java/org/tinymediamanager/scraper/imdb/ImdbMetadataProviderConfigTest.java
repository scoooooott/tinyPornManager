package org.tinymediamanager.scraper.imdb;

import org.junit.Assert;
import org.junit.Test;

public class ImdbMetadataProviderConfigTest {

  @Test
  public void testConfig() {
    ImdbMetadataProvider mp = new ImdbMetadataProvider();

    mp.getProviderInfo().getConfig().setValue("useTmdb", true);
    mp.getProviderInfo().getConfig().setValue("scrapeCollectionInfo", true);
    mp.getProviderInfo().getConfig().setValue("filterUnwantedCategories", false);
    mp.getProviderInfo().getConfig().saveToDir("target");

    mp = new ImdbMetadataProvider();
    // force loading from target
    mp.getProviderInfo().getConfig().loadFromDir("target");
    Assert.assertTrue(mp.getProviderInfo().getConfig().getValueAsBool("useTmdb"));
    Assert.assertTrue(mp.getProviderInfo().getConfig().getValueAsBool("scrapeCollectionInfo"));
    Assert.assertFalse(mp.getProviderInfo().getConfig().getValueAsBool("filterUnwantedCategories"));
  }
}
