/*
 * Copyright 2012 - 2015 Manuel Laggner
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
package org.tinymediamanager.scraper.kodi;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.IKodiMetadataProvider;
import org.tinymediamanager.scraper.IMediaProvider;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaType;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;

/**
 * The entry point for all Kodi meta data providers.
 * 
 * @author Manuel Laggner
 */
@PluginImplementation
public class KodiMetadataProvider implements IKodiMetadataProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(KodiMetadataProvider.class);

  private static MediaProviderInfo providerInfo = new MediaProviderInfo("kodi", "kodi.tv", "Generic Kodi type scraper");

  // private static Pattern mpaaRatingParser = Pattern.compile("Rated\\s+([^ ]+).*");

  public KodiMetadataProvider() {
    // empty constructor just for creating the factory
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Init
  public void init() {
      // preload scrapers
      new KodiUtil();
  }

  /**
   * the factory for creating all instances
   */
  @Override
  public List<IMediaProvider> getPluginsForType(MediaType type) {
    LOGGER.debug("get Kodi scrapers for " + type);
    List<IMediaProvider> metadataProviders = new ArrayList<>();

    List<KodiScraper> scrapers = KodiUtil.scrapers;
    for (KodiScraper scraper : scrapers) {
      if (type == scraper.type) {
        switch (type) {
          case MOVIE:
            metadataProviders.add(new KodiMovieMetadataProvider(scraper));
            break;

          case TV_SHOW:
            metadataProviders.add(new KodiTvShowMetadataProvider(scraper));
            break;

          default:
            break;
        }
      }
    }
    return metadataProviders;
  }

  // public MediaMetadata getMetadataForIMDBId(String imdbid) {
  // if (providerInfo.getId().contains("imdb")) {
  // MediaSearchResult sr = new MediaSearchResult(providerInfo.getId());
  // sr.setIMDBId(imdbid);
  // sr.setId(imdbid);
  // sr.setUrl(createDetailUrl(imdbid));
  // try {
  // return getMetaData(sr);
  // }
  // catch (Exception e) {
  // LOGGER.warn("Failed to search by IMDB URL: " + sr.getUrl(), e);
  // }
  // }
  // return null;
  // }
  //
  // /**
  // * Given a string like, "Rated PG-13 for..." it tries to return PG-13, or the entire string, if cannot find it.
  // *
  // * @param imdbString
  // * @return
  // */
  // public static String parseMPAARating(String imdbString) {
  // if (imdbString != null) {
  // Matcher m = mpaaRatingParser.matcher(imdbString);
  // if (m.find()) {
  // return m.group(1);
  // }
  // else {
  // return imdbString;
  // }
  // }
  // return null;
  // }
  //
  // public static String createDetailUrl(String imdbid) {
  // return String.format(IMDB_TITLE_URL, IMDB_DOMAIN, imdbid);
  // }
}
