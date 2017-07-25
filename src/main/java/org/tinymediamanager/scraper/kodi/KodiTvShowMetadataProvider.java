/*
 * Copyright 2012 - 2016 Manuel Laggner
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

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaEpisode;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.util.DOMUtils;
import org.tinymediamanager.scraper.util.ListUtils;
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
      MediaMetadata md = getEpisode(options);
      return md;
    }
    else {
      LOGGER.error("Whoops, cannot get MetaData - wrong Type: " + options.getType());
      return null;
    }
  }

  @Override
  public List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
    return _search(options);
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
      md.addExtraData(EPISODEGUIDE, episodeUrl);
      result.setMetadata(md);
      // since we already scrape the SHOW, we could already preload the episode metadata...
      cacheEpisodeMetadata(showId, episodeUrl);
    }
    KodiMetadataProvider.XML_CACHE.put(scraper.getProviderInfo().getId() + "_" + showId + "_" + result.getId(), md);// cache SHOW as provideId_12345
  }

  /**
   * Executes the Kodi plugin, to parse and cache all episode MetaData<br>
   * will be cached in memory
   * 
   * @param episodeguideUrl
   * @throws Exception
   */
  private void cacheEpisodeMetadata(String showId, String episodeguideUrl) throws Exception {
    // get-and-cache
    KodiUrl url = new KodiUrl(episodeguideUrl);
    KodiAddonProcessor processor = new KodiAddonProcessor(scraper);

    String epListXml = processor.getEpisodeList(url);
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
      String id = DOMUtils.getElementValue(el, "id");
      String title = DOMUtils.getElementValue(el, "title");
      KodiUrl epUrl = new KodiUrl(DOMUtils.getElementValue(el, "url"));
      LOGGER.info("Getting episode details S" + lz(season) + " E" + lz(ep) + " - " + title);
      processor = new KodiAddonProcessor(scraper);
      String xmlDetails = processor.getEpisodeDetails(epUrl, id);
      LOGGER.debug("******** BEGIN EPISODE DETAILS XML ***********");
      LOGGER.debug(xmlDetails);
      LOGGER.debug("******** END EPISODE DETAILS XML ***********");

      // update again, using the episode specific data
      Document epXml = parseXmlString(xmlDetails);
      Element epXmlEl = epXml.getDocumentElement();

      MediaMetadata md = new MediaMetadata(scraper.getProviderInfo().getId());
      addMetadata(md, epXmlEl);
      md.setEpisodeNumber(ep);
      md.setSeasonNumber(season);

      // cache EPISODE MetaData as provideId_S00_E00
      KodiMetadataProvider.XML_CACHE.put(scraper.getProviderInfo().getId() + "_" + showId + "_S" + lz(season) + "_E" + lz(ep), md);
    }
  }

  private MediaMetadata getEpisode(MediaScrapeOptions options) {

    // get episode number and season number
    int seasonNr = -1;
    int episodeNr = -1;

    try {
      String option = options.getId(MediaMetadata.SEASON_NR);
      if (option != null) {
        seasonNr = Integer.parseInt(options.getId(MediaMetadata.SEASON_NR));
        episodeNr = Integer.parseInt(options.getId(MediaMetadata.EPISODE_NR));
      }
      else {
        seasonNr = Integer.parseInt(options.getId(MediaMetadata.SEASON_NR_DVD));
        episodeNr = Integer.parseInt(options.getId(MediaMetadata.EPISODE_NR_DVD));
      }
    }
    catch (Exception e) {
      LOGGER.warn("error parsing season/episode number");
    }
    LOGGER.debug("search for " + seasonNr + " " + episodeNr);

    String showId = options.getId(scraper.getProviderInfo().getId()).toString();
    MediaMetadata md = KodiMetadataProvider.XML_CACHE
        .get(scraper.getProviderInfo().getId() + "_" + showId + "_S" + lz(seasonNr) + "_E" + lz(episodeNr));
    if (md == null) {
      // ohm... not cached, we didn't search show first
      LOGGER.error("Cannot get episode S" + lz(seasonNr) + "_E" + lz(episodeNr) + " - did you scrape the show yet?.");
    }

    return md;
  }

  @Override
  public List<MediaEpisode> getEpisodeList(MediaScrapeOptions options) throws Exception {
    List<MediaEpisode> episodeList = new ArrayList<MediaEpisode>();

    String showId = options.getId(scraper.getProviderInfo().getId());
    if (showId == null) {
      showId = options.getResult().getId();
    }
    if (showId == null) {
      LOGGER.error("Coould not find showId!");
      return episodeList;
    }

    if (!StringUtils.isEmpty(showId)) {
      for (String key : KodiMetadataProvider.XML_CACHE.keySet()) {
        if (key.startsWith((scraper.getProviderInfo().getId() + "_" + showId))) {
          MediaMetadata md = KodiMetadataProvider.XML_CACHE.get(key);
          if (md == null) {
            LOGGER.warn("Could not find cached episode for " + key);
            continue;
          }

          MediaEpisode me = new MediaEpisode(scraper.getProviderInfo().getId());
          me.episode = md.getEpisodeNumber();
          me.season = md.getSeasonNumber();
          me.title = md.getTitle();
          me.rating = md.getRating();
          if (md.getReleaseDate() != null) {
            Format formatter = new SimpleDateFormat("yyyy-MM-dd");
            me.firstAired = formatter.format(md.getReleaseDate());
          }
          me.voteCount = md.getVoteCount();
          for (MediaCastMember cast : ListUtils.nullSafe(md.getCastMembers())) {
            me.castMembers.add(cast);
          }
          for (MediaArtwork art : ListUtils.nullSafe(md.getMediaArt(MediaArtworkType.ALL))) {
            me.artwork.add(art);
          }
          episodeList.add(me);
        }
      }
    }
    if (episodeList.size() == 0) {
      LOGGER.warn("Could not find cached episodes - did you scrape show recently?");
    }
    return episodeList;
  }

  @Override
  public List<IMediaProvider> getPluginsForType(MediaType type) {
    return getPluginsForType(type);
  }
}
