package org.tinymediamanager.thirdparty;

import org.junit.Test;
import org.tinymediamanager.core.PluginManager;
import org.tinymediamanager.scraper.IMediaProvider;
import org.tinymediamanager.scraper.IMovieArtworkProvider;
import org.tinymediamanager.scraper.IMovieMetadataProvider;
import org.tinymediamanager.scraper.IMovieTrailerProvider;
import org.tinymediamanager.scraper.ITvShowArtworkProvider;
import org.tinymediamanager.scraper.IKodiMetadataProvider;

public class Plugins {

  @Test
  public void load() {
    PluginManager pm = PluginManager.getInstance();

    System.out.println("------------------");
    System.out.println("classes implementing our base impl:");
    for (IMediaProvider p : pm.getPlugins()) {
      System.out.println("  " + p.getProviderInfo() + "   " + (p instanceof IKodiMetadataProvider));
    }

    System.out.println("------------------");
    System.out.println("classes implementing metadata scraping:");
    for (IMovieMetadataProvider p : pm.getMoviePlugins()) {
      System.out.println("  " + p.getProviderInfo());
    }

    System.out.println("------------------");
    System.out.println("classes implementing movie artwork scraping:");
    for (IMovieArtworkProvider p : pm.getMovieArtworkPlugins()) {
      System.out.println("  " + p.getProviderInfo());
    }

    System.out.println("------------------");
    System.out.println("classes implementing TV show artwork scraping:");
    for (ITvShowArtworkProvider p : pm.getTvShowArtworkPlugins()) {
      System.out.println("  " + p.getProviderInfo());
    }

    System.out.println("------------------");
    System.out.println("classes implementing trailer scraping:");
    for (IMovieTrailerProvider p : pm.getTrailerPlugins()) {
      System.out.println("  " + p.getProviderInfo());
    }
  }
}
