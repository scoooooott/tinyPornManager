//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.tinymediamanager.scraper.universal_movie;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.UnsupportedMediaTypeException;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMovieImdbMetadataProvider;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;
import org.tinymediamanager.scraper.mediaprovider.IMovieTmdbMetadataProvider;
import org.tinymediamanager.scraper.util.PluginManager;

import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * This is a metadata provider which is highly configurable and combines the results of various other providers
 *
 * @author Manuel Laggner
 */
@PluginImplementation
public class UniversalMovieMetadataProvider implements IMovieMetadataProvider {
  private static final Logger                LOGGER = LoggerFactory.getLogger(UniversalMovieMetadataProvider.class);
  private final MediaProviderInfo            providerInfo;

  private final List<IMovieMetadataProvider> compatibleScrapers;

  public UniversalMovieMetadataProvider() {
    // create the providerinfo
    providerInfo = createMediaProviderInfo();

    // get all loaded and compatible plugins
    compatibleScrapers = new ArrayList<>();
    List<String> compatibleScraperIds = new ArrayList<>();
    compatibleScraperIds.add("-"); // no scraper

    for (IMovieMetadataProvider plugin : PluginManager.getInstance().getPluginsForInterface(IMovieMetadataProvider.class)) {
      // only the ones implementing IMovieTmdbMetadataProvider and IMovieImdbMetadataProvider can be used futher
      if (plugin instanceof IMovieTmdbMetadataProvider || plugin instanceof IMovieImdbMetadataProvider) {
        compatibleScrapers.add(plugin);
        compatibleScraperIds.add(plugin.getProviderInfo().getId());
      }
    }

    providerInfo.getConfig().addSelect("title", compatibleScraperIds, "-");
    providerInfo.getConfig().addSelect("originalTitle", compatibleScraperIds, "-");
    providerInfo.getConfig().addSelect("year", compatibleScraperIds, "-");
    providerInfo.getConfig().addSelect("plot", compatibleScraperIds, "-");
    providerInfo.getConfig().addSelect("rating", compatibleScraperIds, "-");
    providerInfo.getConfig().load();
  }

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo("universal_movie", "Universal movie scraper",
        "<html><h3>Universal movie scraper</h3><br />A meta scraper which allows to collect data from several other scrapers</html>", null);
    providerInfo.setVersion(UniversalMovieMetadataProvider.class);
    return providerInfo;
  }

  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  public MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getMetadata() " + options.toString());
    if (options.getType() != MediaType.MOVIE) {
      throw new UnsupportedMediaTypeException(options.getType());
    }
    else {
      MediaMetadata md = new MediaMetadata(providerInfo.getId());
      return md;
    }
  }

  public List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
    LOGGER.debug("search() " + options.toString());
    if (options.getMediaType() != MediaType.MOVIE) {
      throw new UnsupportedMediaTypeException(options.getMediaType());
    }
    else {
      List<MediaSearchResult> resultList = new ArrayList();
      return resultList;
    }
  }
}
