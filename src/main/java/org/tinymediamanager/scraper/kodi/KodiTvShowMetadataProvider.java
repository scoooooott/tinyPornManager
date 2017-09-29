/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.util.DOMUtils;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This is the real Kodi meta data provider for TV shows
 * 
 * @author Manuel Laggner
 */
public class KodiTvShowMetadataProvider extends AbstractKodiMetadataProvider implements ITvShowMetadataProvider {
  private static final Logger LOGGER       = LoggerFactory.getLogger(KodiTvShowMetadataProvider.class);
  private static final String EPISODEGUIDE = "episodeguide";

  public KodiTvShowMetadataProvider(KodiScraper scraper) {
    super(scraper);
  }

  @Override
  public List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
    List<MediaSearchResult> results = _search(options);
    if (results.isEmpty() && options.getYear() > 0) {
      // nothing found, try w/o year
      LOGGER.info("Search found nothing, try again without year...");
      options.setYear(0);
      results = _search(options);
    }
    return results;
  }

  @Override
  public MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("Kodi: getMetadata(): " + options);
    // if (options.getResult() == null || !scraper.getProviderInfo().getId().equals(options.getResult().getProviderId())) {
    // throw new Exception("scraping with Kodi scrapers only with a prior result possible");
    // }

    if (options.getType().equals(MediaType.TV_SHOW)) {
      return _getMetadata(options);
    }
    else if (options.getType().equals(MediaType.TV_EPISODE)) {
      // called for every single episode, S/E in options...
      return getEpisodeMetadata(options);
    }
    else {
      LOGGER.error("Whoops, cannot get MetaData - wrong Type: " + options.getType());
      return null;
    }
  }

  /**
   * for TV_SHOW only, called with getShowDetails
   */
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

    String showId = md.getId(scraper.getProviderInfo().getId()).toString();
    String episodeUrl = DOMUtils.getElementValue(xml.getDocumentElement(), EPISODEGUIDE);
    if (StringUtils.isEmpty(episodeUrl)) {
      LOGGER.error("No Episode Data!");
    }
    else {
      KodiMetadataProvider.XML_CACHE.put(scraper.getProviderInfo().getId() + "_" + showId + "_" + "EPISODEGUIDE_URL", episodeUrl);
      md.addExtraData(EPISODEGUIDE, episodeUrl);
      result.setMetadata(md);
    }
    KodiMetadataProvider.XML_CACHE.put(scraper.getProviderInfo().getId() + "_" + showId + "_" + result.getId(), xmlDetails);
  }

  private MediaMetadata getEpisodeMetadata(MediaScrapeOptions options) {
    MediaMetadata md = new MediaMetadata(scraper.getProviderInfo().getId());

    // get episode number and season number
    int seasonNr = options.getIdAsIntOrDefault(MediaMetadata.SEASON_NR, -1);
    int episodeNr = options.getIdAsIntOrDefault(MediaMetadata.EPISODE_NR, -1);

    if (seasonNr == -1 || episodeNr == -1) {
      seasonNr = options.getIdAsIntOrDefault(MediaMetadata.SEASON_NR_DVD, -1);
      episodeNr = options.getIdAsIntOrDefault(MediaMetadata.EPISODE_NR_DVD, -1);
    }

    if (seasonNr == -1 || episodeNr == -1) {
      LOGGER.warn("no aired date/season number/episode number found");
      return md; // not even date set? return
    }

    LOGGER.debug("search for S{} E{}", lz(seasonNr), lz(episodeNr));

    String showId = options.getIdAsString(scraper.getProviderInfo().getId());
    if (showId == null) {
      LOGGER.error("Could not find showId - please scrape show first!");
      return null;
    }

    // get XML - either from cache, or fetched
    String epXml = KodiMetadataProvider.XML_CACHE.get(scraper.getProviderInfo().getId() + "_" + showId + "_S" + lz(seasonNr) + "_E" + lz(episodeNr));
    if (epXml == null) {
      // get episodes list with showId
      MediaScrapeOptions mso = new MediaScrapeOptions(MediaType.TV_SHOW);
      mso.setId(scraper.getProviderInfo().getId(), showId);
      try {
        getEpisodeList(mso);
        // now it should be cached!
        epXml = KodiMetadataProvider.XML_CACHE.get(scraper.getProviderInfo().getId() + "_" + showId + "_S" + lz(seasonNr) + "_E" + lz(episodeNr));
      }
      catch (Exception e) {
        LOGGER.error("Could not fetch episodeslist!", e);
      }
    }

    Document epListDoc;
    try {
      epListDoc = parseXmlString(epXml);
      Element el = epListDoc.getDocumentElement();
      int season = DOMUtils.getElementIntValue(el, "season");
      int ep = DOMUtils.getElementIntValue(el, "epnum");
      String id = DOMUtils.getElementValue(el, "id");
      String title = DOMUtils.getElementValue(el, "title");
      KodiUrl epUrl = new KodiUrl(DOMUtils.getElementValue(el, "url"));

      LOGGER.info("Getting episode details S" + lz(season) + " E" + lz(ep) + " - " + title);
      KodiAddonProcessor processor = new KodiAddonProcessor(scraper);
      processor = new KodiAddonProcessor(scraper);
      String xmlDetails = processor.getEpisodeDetails(epUrl, id);
      LOGGER.debug("******** BEGIN EPISODE DETAILS XML ***********");
      LOGGER.debug(xmlDetails);
      LOGGER.debug("******** END EPISODE DETAILS XML ***********");

      // update again, using the episode specific data
      Document epDetailXml = parseXmlString(xmlDetails);
      Element epXmlEl = epDetailXml.getDocumentElement();

      addMetadata(md, epXmlEl);
      md.setEpisodeNumber(ep);
      md.setSeasonNumber(season);

      // cache EPISODE MetaData as provideId_S00_E00
      KodiMetadataProvider.XML_CACHE.put(scraper.getProviderInfo().getId() + "_" + showId + "_S" + lz(season) + "_E" + lz(ep) + "_DETAIL",
          xmlDetails);

    }
    catch (Exception e) {
      LOGGER.error("Could not get episode details!");
    }

    return md;
  }

  @Override
  public List<MediaMetadata> getEpisodeList(MediaScrapeOptions options) throws Exception {
    List<MediaMetadata> episodeList = new ArrayList<>();

    String showId = options.getIdAsString(scraper.getProviderInfo().getId());
    if (showId == null) {
      showId = options.getResult().getId();
    }
    if (showId == null) {
      LOGGER.error("Could not find showId - please scrape show first!");
      return episodeList;
    }

    String episodeguide = KodiMetadataProvider.XML_CACHE.get(scraper.getProviderInfo().getId() + "_" + showId + "_" + "EPISODEGUIDE_URL");
    if (episodeguide == null || episodeguide.isEmpty()) {
      if (options.getMetadata() != null) {
        episodeguide = options.getMetadata().getExtraData(EPISODEGUIDE).toString();
      }
      // if (episodeguide == null || episodeguide.isEmpty()) {
      // no EP guide url? scrape show first!
      // _getMetadata(options); // TODO: does not work yet; need to SEARCH for url first :|
      // }
      if (episodeguide == null || episodeguide.isEmpty()) {
        LOGGER.error("Could not find episodenguid url - you wanna scrape the show first!");
        return episodeList;
      }
    }

    // get XML - either from cache, or fetched
    String epListXml = KodiMetadataProvider.XML_CACHE.get(scraper.getProviderInfo().getId() + "_" + showId + "_" + EPISODEGUIDE);
    if (epListXml == null) {
      KodiUrl url = new KodiUrl(episodeguide);
      KodiAddonProcessor processor = new KodiAddonProcessor(scraper);
      epListXml = processor.getEpisodeList(url);
      KodiMetadataProvider.XML_CACHE.put(scraper.getProviderInfo().getId() + "_" + showId + "_" + EPISODEGUIDE, epListXml);
    }
    LOGGER.debug("******** BEGIN EPISODE LIST XML ***********");
    LOGGER.debug(epListXml);
    LOGGER.debug("******** END EPISODE LIST XML ***********");
    Document epListDoc = parseXmlString(epListXml);

    // <episode>
    // <title>Bender's Big Score</title>
    // <aired>2008-03-23</aired>
    // <epnum>1</epnum>
    // <season>0</season>
    // <url
    // cache="tmdb-615-en-episode-s0e1.json">http://api.themoviedb.org/3/tv/615/season/0/episode/1?api_key=6889f6089877fd092454d00edb44a84d&amp;language=en&amp;append_to_response=credits,external_ids,images&amp;include_image_language=en,en,null</url>
    // <id>615|0|1</id>
    // </episode>

    NodeList nl = epListDoc.getElementsByTagName("episode");
    for (int i = 0; i < nl.getLength(); i++) {
      Element el = (Element) nl.item(i);

      int season = DOMUtils.getElementIntValue(el, "season");
      int ep = DOMUtils.getElementIntValue(el, "epnum");

      // cache episode XML for later details parsing
      String epXml = elementToString(el);
      if (epXml != null) {
        KodiMetadataProvider.XML_CACHE.put(scraper.getProviderInfo().getId() + "_" + showId + "_S" + lz(season) + "_E" + lz(ep), epXml);
      }

      MediaMetadata md = new MediaMetadata(scraper.getProviderInfo().getId());
      md.setEpisodeNumber(ep);
      md.setSeasonNumber(season);
      md.setTitle(DOMUtils.getElementValue(el, "title"));
      md.setId(scraper.getProviderInfo().getId(), DOMUtils.getElementValue(el, "id"));
      // String epUrl = DOMUtils.getElementValue(el, "url"); // cannot save in ME!!!
      try {
        md.setReleaseDate(StrgUtils.parseDate(DOMUtils.getElementValue(el, "aired")));
      }
      catch (Exception ignored) {
      }

      episodeList.add(md);
    }

    if (episodeList.isEmpty()) {
      LOGGER.warn("Could not find episodes - did you scrape the show recently?");
    }
    return episodeList;
  }

  @Override
  public List<IMediaProvider> getPluginsForType(MediaType type) {
    return null;
  }
}
