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
package org.tinymediamanager.scraper.kodi;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.scraper.ArtworkSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMediaProvider;
import org.tinymediamanager.scraper.interfaces.IMovieArtworkProvider;
import org.tinymediamanager.scraper.interfaces.IMovieMetadataProvider;
import org.w3c.dom.Document;

/**
 * This is the real Kodi meta data provider for movies
 *
 * @author Manuel Laggner
 */
public class KodiMovieMetadataProvider extends AbstractKodiMetadataProvider implements IMovieMetadataProvider, IMovieArtworkProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(KodiMovieMetadataProvider.class);

  public KodiMovieMetadataProvider(KodiScraper scraper) {
    super(scraper);
  }

  @Override
  public SortedSet<MediaSearchResult> search(MovieSearchAndScrapeOptions options) throws ScrapeException {
    return _search(options);
  }

  @Override
  public MediaMetadata getMetadata(MovieSearchAndScrapeOptions options) throws ScrapeException, MissingIdException, NothingFoundException {
    LOGGER.debug("Kodi: getMetadata(): {}", options);
    if (options.getSearchResult() == null || !scraper.getProviderInfo().getId().equals(options.getSearchResult().getProviderId())) {
      throw new MissingIdException("scraping with Kodi scrapers only with a prior result possible");
    }
    return _getMetadata(options);
  }

  @Override
  protected void processXmlContent(String xmlDetails, MediaMetadata md, MediaSearchResult result) throws Exception {
    if (xmlDetails == null || StringUtils.isEmpty(xmlDetails)) {
      LOGGER.warn("Cannot process empty Xml Contents.");
      return;
    }

    LOGGER.debug("******* BEGIN XML ***********");
    LOGGER.debug(xmlDetails);
    LOGGER.debug("******* END XML ***********");

    Document xml = parseXmlString(xmlDetails);
    addMetadata(md, xml.getDocumentElement());
    LOGGER.debug("MetaData: {}", md);
  }

  @Override
  public List<IMediaProvider> getPluginsForType(MediaType type) {
    return null;
  }

  @Override
  public List<MediaArtwork> getArtwork(ArtworkSearchAndScrapeOptions options) throws ScrapeException {
    LOGGER.debug("******* BEGIN ARTWORK XML FOR {} ***********", options.getArtworkType());
    List<MediaArtwork> mas = new ArrayList<>();
    // scrape again to get Kodi XML (thank god we have a mem cachedUrl)
    try {
      if (options.getSearchResult() == null || !scraper.getProviderInfo().getId().equals(options.getSearchResult().getProviderId())) {
        throw new MissingIdException("scraping with Kodi scrapers only with a prior result possible");
      }

      MediaMetadata md = _getMetadata(options);
      mas.addAll(md.getMediaArt(options.getArtworkType()));
      LOGGER.debug("******* END ARTWORK XML FOR {} ***********", options.getArtworkType());
    }
    catch (Exception e) {
      LOGGER.error("problem getting artwork: {}", e.getMessage());
      throw new ScrapeException(e);
    }

    return mas;
  }
}
