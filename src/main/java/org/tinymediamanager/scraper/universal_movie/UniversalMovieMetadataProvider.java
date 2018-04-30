//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.tinymediamanager.scraper.universal_movie;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.UnsupportedMediaTypeException;
import org.tinymediamanager.scraper.config.MediaProviderConfig;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMovieImdbMetadataProvider;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;
import org.tinymediamanager.scraper.mediaprovider.IMovieTmdbMetadataProvider;
import org.tinymediamanager.scraper.util.MetadataUtil;
import org.tinymediamanager.scraper.util.PluginManager;

import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * This is a metadata provider which is highly configurable and combines the results of various other providers
 *
 * @author Manuel Laggner
 */
@PluginImplementation
public class UniversalMovieMetadataProvider implements IMovieMetadataProvider {
  private static final String                       UNDEFINED = "-";
  private static final String                       SEARCH    = "search";
  private static final Logger                       LOGGER    = LoggerFactory.getLogger(UniversalMovieMetadataProvider.class);
  private final MediaProviderInfo                   providerInfo;

  private final Map<String, IMovieMetadataProvider> compatibleScrapers;

  public UniversalMovieMetadataProvider() {
    // create the providerinfo
    providerInfo = createMediaProviderInfo();

    // get all loaded and compatible plugins
    compatibleScrapers = new HashMap<>();
    List<String> compatibleScraperIds = new ArrayList<>();
    compatibleScraperIds.add(UNDEFINED); // no scraper

    for (IMovieMetadataProvider plugin : PluginManager.getInstance().getPluginsForInterface(IMovieMetadataProvider.class)) {
      // only the ones implementing IMovieTmdbMetadataProvider and IMovieImdbMetadataProvider can be used further
      if (plugin instanceof IMovieTmdbMetadataProvider || plugin instanceof IMovieImdbMetadataProvider) {
        compatibleScrapers.put(plugin.getProviderInfo().getId(), plugin);
        compatibleScraperIds.add(plugin.getProviderInfo().getId());
      }
    }

    MediaProviderConfig config = providerInfo.getConfig();

    config.addSelect(SEARCH, compatibleScraperIds, UNDEFINED);
    // use the right key to let reflection work
    // getter in MediaMetadata must be get + Key (first letter upper case)
    config.addSelect("title", "metatag.title", compatibleScraperIds, UNDEFINED);
    config.addSelect("originalTitle", "metatag.originaltitle", compatibleScraperIds, UNDEFINED);
    config.addSelect("tagline", "metatag.tagline", compatibleScraperIds, UNDEFINED);
    config.addSelect("year", "metatag.year", compatibleScraperIds, UNDEFINED);
    config.addSelect("releaseDate", "metatag.releasedate", compatibleScraperIds, UNDEFINED);
    config.addSelect("plot", "metatag.plot", compatibleScraperIds, UNDEFINED);
    config.addSelect("runtime", "metatag.runtime", compatibleScraperIds, UNDEFINED);
    config.addSelect("ratings", "metatag.rating", compatibleScraperIds, UNDEFINED);
    config.addSelect("top250", "metatag.top250",
        compatibleScraperIds.contains(MediaMetadata.IMDB) ? Arrays.asList(UNDEFINED, MediaMetadata.IMDB) : Collections.singletonList(UNDEFINED),
        UNDEFINED);
    config.addSelect("genres", "metatag.genre", compatibleScraperIds, UNDEFINED);
    config.addSelect("certifications", "metatag.certification", compatibleScraperIds, UNDEFINED);
    config.addSelect("productionCompanies", "metatag.production", compatibleScraperIds, UNDEFINED);
    config.addSelect("castMembers", "metatag.cast", compatibleScraperIds, UNDEFINED);
    config.addSelect("spokenLanguages", "metatag.spokenlanguages", compatibleScraperIds, UNDEFINED);
    config.addSelect("countries", "metatag.country", compatibleScraperIds, UNDEFINED);
    config.addSelect("tags", "metatag.tags", compatibleScraperIds, UNDEFINED);
    config.addSelect("mediaArt", "metatag.artwork", compatibleScraperIds, UNDEFINED);
    config.addSelect("collectionName", "metatag.movieset",
        compatibleScraperIds.contains(MediaMetadata.TMDB) ? Arrays.asList(UNDEFINED, MediaMetadata.TMDB) : Collections.singletonList(UNDEFINED),
        UNDEFINED);
    config.load();
  }

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo("universal_movie", "Universal movie scraper",
        "<html><h3>Universal movie scraper</h3><br />A meta scraper which allows to collect data from several other scrapers</html>", null);
    providerInfo.setVersion(UniversalMovieMetadataProvider.class);
    return providerInfo;
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
    LOGGER.debug("search() " + options.toString());
    if (options.getMediaType() != MediaType.MOVIE) {
      throw new UnsupportedMediaTypeException(options.getMediaType());
    }

    List<MediaSearchResult> resultList = new ArrayList<>();

    IMovieMetadataProvider mp = compatibleScrapers.get(providerInfo.getConfig().getValue(SEARCH));
    if (mp == null) {
      return resultList;
    }

    try {
      resultList.addAll(mp.search(options));
    }
    catch (Exception e) {
      LOGGER.warn("Could not call search method of " + mp.getProviderInfo().getId() + " : " + e.getMessage());
    }

    return resultList;
  }

  @Override
  public MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getMetadata() " + options.toString());
    if (options.getType() != MediaType.MOVIE) {
      throw new UnsupportedMediaTypeException(options.getType());
    }

    MediaMetadata md = new MediaMetadata(providerInfo.getId());

    // check which scrapers should be used
    Set<IMovieMetadataProvider> metadataProviders = getRelevantMetadataProviders();
    if (metadataProviders.isEmpty()) {
      return md;
    }

    // call all scrapers in different workers and wait for them to finish
    Map<String, MediaMetadata> metadataMap = getMetadataMap(metadataProviders, options);

    // and now assign the values per reflection
    assignResults(md, metadataMap);

    return md;
  }

  private Set<IMovieMetadataProvider> getRelevantMetadataProviders() {
    Set<IMovieMetadataProvider> metadataProviders = new HashSet<>();
    for (Map.Entry<String, String> entry : providerInfo.getConfig().getConfigKeyValuePairs().entrySet()) {
      if (!UNDEFINED.equals(entry.getValue())) {
        IMovieMetadataProvider mp = compatibleScrapers.get(entry.getValue());
        if (mp != null) {
          metadataProviders.add(mp);
        }
      }
    }
    return metadataProviders;
  }

  private Map<String, MediaMetadata> getMetadataMap(Set<IMovieMetadataProvider> metadataProviders, MediaScrapeOptions options) {
    // check if we have all needed IDs
    // this scraper supports scraping via TMDB id and IMDB id; but not all used providers support both
    // we may need to get the missing ones
    String imdbId = options.getImdbId();
    int tmdbId = options.getTmdbId();

    Map<String, MediaMetadata> metadataMap = new HashMap<>();

    for (IMovieMetadataProvider mp : metadataProviders) {
      if (mp instanceof IMovieImdbMetadataProvider && MetadataUtil.isValidImdbId(imdbId)) {
        // everything is good ;)
        continue;
      }
      if (mp instanceof IMovieTmdbMetadataProvider && tmdbId > 0) {
        // everything is good ;)
        continue;
      }

      // we've come here, so we have not the needed ID
      // TMDB offers scraping by both and returns both (if available)
      if (tmdbId > 0 || MetadataUtil.isValidImdbId(imdbId)) {
        // try to get the meta data via TMDB
        // anything cached?
        MediaMetadata md = metadataMap.get(MediaMetadata.TMDB);
        if (md == null) {
          try {
            IMovieMetadataProvider tmdb = compatibleScrapers.get(MediaMetadata.TMDB);
            if (tmdb != null) {
              md = tmdb.getMetadata(options);
              if (md != null) {
                // cache the result for later usage
                metadataMap.put(MediaMetadata.TMDB, md);
              }
            }
          }
          catch (Exception e) {
            LOGGER.warn("Could not get a result from scraper: " + e.getMessage());
          }
        }
        // we got a response - parse out TMDB id and IMDB id if needed
        if (md != null) {
          if (tmdbId == 0) {
            try {
              tmdbId = Integer.parseInt((String) md.getId(MediaMetadata.TMDB));
            }
            catch (Exception ignored) {
            }
          }
          if (!MetadataUtil.isValidImdbId(imdbId) && MetadataUtil.isValidImdbId((String) md.getId(MediaMetadata.IMDB))) {
            imdbId = (String) md.getId(MediaMetadata.IMDB);
          }
        }
      }
    }

    // inject the found TMDB id and IMDB id into the search options
    if (MetadataUtil.isValidImdbId(imdbId)) {
      options.setImdbId(imdbId);
    }
    if (tmdbId > 0) {
      options.setTmdbId(tmdbId);
    }

    // start the workers to get the metadata from the different providers
    ExecutorService executorService = Executors.newFixedThreadPool(metadataProviders.size());
    ExecutorCompletionService<MediaMetadata> completionService = new ExecutorCompletionService<>(executorService);
    List<Future<MediaMetadata>> futures = new ArrayList<>();
    for (IMovieMetadataProvider mp : metadataProviders) {
      // look into the cache - maybe we do not need to call it again
      if (metadataMap.get(mp.getProviderInfo().getId()) == null) {
        futures.add(completionService.submit(new MetadataProviderWorker(mp, options)));
      }
    }

    // wait for all workers to finish
    for (Future<MediaMetadata> future : futures) {
      try {
        MediaMetadata mediaMetadata = future.get();
        if (mediaMetadata != null) {
          metadataMap.put(mediaMetadata.getProviderId(), mediaMetadata);
        }
      }
      catch (Exception e) {
        LOGGER.warn("Could not get a result from scraper: " + e.getMessage());
      }
    }
    return metadataMap;
  }

  private void assignResults(MediaMetadata md, Map<String, MediaMetadata> metadataMap) {
    // take all ids we can get
    for (Map.Entry<String, MediaMetadata> entry : metadataMap.entrySet()) {
      for (Map.Entry<String, Object> id : entry.getValue().getIds().entrySet()) {
        md.setId(id.getKey(), id.getValue());
      }
    }

    // assign the requested metadata
    for (Map.Entry<String, String> entry : providerInfo.getConfig().getConfigKeyValuePairs().entrySet()) {
      if (!UNDEFINED.equals(entry.getValue()) && !SEARCH.equals(entry.getValue())) {
        MediaMetadata mediaMetadata = metadataMap.get(entry.getValue());
        if (mediaMetadata != null) {
          try {
            Method getter = new PropertyDescriptor(entry.getKey(), MediaMetadata.class).getReadMethod();
            Method setter = new PropertyDescriptor(entry.getKey(), MediaMetadata.class).getWriteMethod();

            setter.invoke(md, getter.invoke(mediaMetadata));
          }
          catch (Exception e) {
            LOGGER.warn("Problem assigning " + entry.getKey() + " : " + e.getMessage());
          }
        }
      }
    }
  }

  /****************************************************************************
   * local helper classes
   ****************************************************************************/
  protected class MetadataProviderWorker implements Callable<MediaMetadata> {
    private final IMovieMetadataProvider metadataProvider;
    private final MediaScrapeOptions     mediaScrapeOptions;

    public MetadataProviderWorker(IMovieMetadataProvider metadataProvider, MediaScrapeOptions mediaScrapeOptions) {
      this.metadataProvider = metadataProvider;
      this.mediaScrapeOptions = mediaScrapeOptions;
    }

    @Override
    public MediaMetadata call() throws Exception {
      return metadataProvider.getMetadata(mediaScrapeOptions);
    }
  }
}
