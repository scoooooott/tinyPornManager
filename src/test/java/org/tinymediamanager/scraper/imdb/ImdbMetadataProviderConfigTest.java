package org.tinymediamanager.scraper.imdb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ImdbMetadataProviderConfigTest {
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
  public void writeConfig() {
    ImdbMetadataProviderConfig config = ImdbMetadataProviderConfig.SETTINGS;
    config.useTmdb = true;
    config.scrapeCollectionInfo = true;
    config.save();
  }

  @Test
  public void loadConfig() {
    ImdbMetadataProviderConfig config = ImdbMetadataProviderConfig.SETTINGS;
    Assert.assertEquals(true, config.useTmdb);
    Assert.assertEquals(true, config.scrapeCollectionInfo);
  }
}
