package org.tinymediamanager.core;

import org.junit.Test;
import org.tinymediamanager.BasicTest;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IKodiMetadataProvider;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.scraper.mediaprovider.IMovieArtworkProvider;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;
import org.tinymediamanager.scraper.mediaprovider.IMovieTrailerProvider;
import org.tinymediamanager.scraper.mediaprovider.ITvShowArtworkProvider;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.util.PluginManager;

public class Plugins extends BasicTest {

  @Test
  public void load() {
    PluginManager pm = PluginManager.getInstance();

    System.out.println("------------------");
    System.out.println("classes implementing movie metadata scraping:");
    for (IMovieMetadataProvider p : pm.getPluginsForInterface(IMovieMetadataProvider.class)) {
      System.out.println("  " + p.getProviderInfo());
    }

    System.out.println("------------------");
    System.out.println("classes implementing movie artwork scraping:");
    for (IMovieArtworkProvider p : pm.getPluginsForInterface(IMovieArtworkProvider.class)) {
      System.out.println("  " + p.getProviderInfo());
    }

    System.out.println("------------------");
    System.out.println("classes implementing movie trailer scraping:");
    for (IMovieTrailerProvider p : pm.getPluginsForInterface(IMovieTrailerProvider.class)) {
      System.out.println("  " + p.getProviderInfo());
    }

    System.out.println("------------------");
    System.out.println("classes implementing TV show meta data scraping:");
    for (ITvShowMetadataProvider p : pm.getPluginsForInterface(ITvShowMetadataProvider.class)) {
      System.out.println("  " + p.getProviderInfo());
    }

    System.out.println("------------------");
    System.out.println("classes implementing TV show artwork scraping:");
    for (ITvShowArtworkProvider p : pm.getPluginsForInterface(ITvShowArtworkProvider.class)) {
      System.out.println("  " + p.getProviderInfo());
    }

    System.out.println("------------------");
    for (IKodiMetadataProvider kodi : pm.getPluginsForInterface(IKodiMetadataProvider.class)) {
      System.out.println("  " + kodi.getProviderInfo());

      for (MediaType mt : MediaType.values()) {
        System.out.println("Kodi scraper implementing type " + mt);
        for (IMediaProvider p : kodi.getPluginsForType(mt)) {
          System.out.println("  " + p.getProviderInfo());
        }
      }

    }
  }
}
