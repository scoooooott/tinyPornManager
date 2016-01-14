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

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaEpisode;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.util.DOMUtils;
import org.w3c.dom.Document;

/**
 * This is the real Kodi meta data provider for TV shows
 * 
 * @author Manuel Laggner
 */
public class KodiTvShowMetadataProvider extends AbstractKodiMetadataProvider implements ITvShowMetadataProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(KodiTvShowMetadataProvider.class);

  public KodiTvShowMetadataProvider(KodiScraper scraper) {
    super(scraper);
    throw new NotImplementedException("not yet implemented");
  }

  @Override
  public MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    throw new NotImplementedException("not yet implemented");
    // LOGGER.debug("Kodi: getMetadata(): " + options);
    // if (options.getResult() == null ||
    // !providerInfo.getId().equals(options.getResult().getProviderId())) {
    // throw new Exception("scraping with Kodi scrapers only with a prior result
    // possible");
    // }
    // return _getMetadata(options);
  }

  @Override
  public List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
    throw new NotImplementedException("not yet implemented");
  }

  @Override
  protected void processXmlContent(String xmlDetails, MediaMetadata md, MediaSearchResult result) throws Exception {
    LOGGER.debug("*** PROCESSING TV ***");
    if (xmlDetails == null || StringUtils.isEmpty(xmlDetails)) {
      LOGGER.warn("Cannot process empty Xml Contents.");
      return;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("******* BEGIN XML ***********");
      LOGGER.debug(xmlDetails);
      LOGGER.debug("******* END XML ***********");
    }

    Document xml = parseXmlString(xmlDetails);

    addMetadata(md, xml.getDocumentElement());

    LOGGER.debug("Fetching Episode Guide url");

    // now check for episode and guide url
    String episodeUrl = DOMUtils.getElementValue(xml.getDocumentElement(), "episodeguide");
    if (StringUtils.isEmpty(episodeUrl)) {
      LOGGER.error("No Episode Data!");
    }
    else {
      // FIXME needs to be implemented
      // if
      // (!StringUtils.isEmpty(result.getExtra().get(MediaSearchOptions.SearchParam.SEASON.name())))
      // {
      // int findEpisode =
      // NumberUtils.toInt(result.getExtra().get(MediaSearchOptions.SearchParam.EPISODE.name()));
      // int findSeason =
      // NumberUtils.toInt(result.getExtra().get(MediaSearchOptions.SearchParam.SEASON.name()));
      // // int findDisc =
      // NumberUtils.toInt(result.getExtra().get(MediaSearchOptions.SearchParam.DISC.name()));
      //
      // KodiUrl url = new KodiUrl(episodeUrl);
      // // Call get Episode List
      // KodiAddonProcessor processor = new KodiAddonProcessor(scraper);
      //
      // if (findEpisode > 0) {
      // String epListXml = processor.getEpisodeList(url);
      //
      // LOGGER.debug("******** BEGIN EPISODE LIST XML ***********");
      // LOGGER.debug(epListXml);
      // LOGGER.debug("******** END EPISODE LIST XML ***********");
      //
      // Document epListDoc = parseXmlString(epListXml);
      //
      // NodeList nl = epListDoc.getElementsByTagName("episode");
      // int s = nl.getLength();
      // int season, ep;
      // String id = null;
      // String epUrl = null;
      // for (int i = 0; i < s; i++) {
      // Element el = (Element) nl.item(i);
      // season = DOMUtils.getElementIntValue(el, "season");
      // ep = DOMUtils.getElementIntValue(el, "epnum");
      // if (season == findSeason && ep == findEpisode) {
      // id = DOMUtils.getElementValue(el, "id");
      // epUrl = DOMUtils.getElementValue(el, "url");
      // break;
      // }
      // }
      //
      // if (id == null) {
      // throw new Exception("Could Not Find Seaons and Episode for: " +
      // findSeason + "x" + findEpisode);
      // }
      //
      // LOGGER.debug("We have an episdoe id for season and episode... fetching
      // details...");
      //
      // processor = new KodiAddonProcessor(scraper);
      // xmlDetails = processor.getEpisodeDetails(new KodiUrl(epUrl), id);
      //
      // LOGGER.debug("******** BEGIN EPISODE DETAILS XML ***********");
      // LOGGER.debug(xmlDetails);
      // LOGGER.debug("******** END EPISODE DETAILS XML ***********");
      //
      // // update again, using the episode specific data
      // xml = parseXmlString(xmlDetails);
      // Element el = xml.getDocumentElement();
      // addMetadata(md, el);
      //
      // // add/update tv specific stuff
      // String plot = DOMUtils.getElementValue(el, "plot");
      // }
      // }
    }
  }

  @Override
  public List<MediaEpisode> getEpisodeList(MediaScrapeOptions options) throws Exception {
    throw new NotImplementedException("not yet implemented");
  }

  @Override
  public List<IMediaProvider> getPluginsForType(MediaType type) {
    return getPluginsForType(type);
  }
}
