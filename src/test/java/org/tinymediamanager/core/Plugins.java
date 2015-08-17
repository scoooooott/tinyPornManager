package org.tinymediamanager.core;

import org.junit.Test;
import org.tinymediamanager.scraper.IMovieArtworkProvider;
import org.tinymediamanager.scraper.IMovieMetadataProvider;
import org.tinymediamanager.scraper.IMovieTrailerProvider;
import org.tinymediamanager.scraper.ITvShowArtworkProvider;
import org.tinymediamanager.scraper.ITvShowMetadataProvider;

public class Plugins {

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
  }
}
