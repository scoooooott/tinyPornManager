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
package org.tinymediamanager.scraper.imdb;

import static org.tinymediamanager.scraper.imdb.ImdbMetadataProvider.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaCastMember;
import org.tinymediamanager.scraper.MediaEpisode;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.http.CachedUrl;
import org.tinymediamanager.scraper.util.MetadataUtil;

/**
 * The class ImdbTvShowParser is used to parse TV show site of imdb.com
 * 
 * @author Manuel Laggner
 */
public class ImdbTvShowParser extends ImdbParser {
  private static final Logger  LOGGER                  = LoggerFactory.getLogger(ImdbTvShowParser.class);
  private static final Pattern UNWANTED_SEARCH_RESULTS = Pattern.compile(".*\\((TV Movies|TV Episode|Short|Video Game)\\).*"); // stripped out

  private ImdbSiteDefinition   imdbSite;

  public ImdbTvShowParser(ImdbSiteDefinition imdbSite) {
    super(MediaType.TV_SHOW);
    this.imdbSite = imdbSite;
  }

  @Override
  protected Pattern getUnwantedSearchResultPattern() {
    if (ImdbMetadataProviderConfig.SETTINGS.filterUnwantedCategories) {
      return UNWANTED_SEARCH_RESULTS;
    }
    return null;
  }

  @Override
  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  protected ImdbSiteDefinition getImdbSite() {
    return imdbSite;
  }

  @Override
  protected MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    switch (options.getType()) {
      case TV_SHOW:
        return getTvShowMetadata(options);

      case TV_EPISODE:
        return getEpisodeMetadata(options);

      default:
        break;
    }
    return new MediaMetadata(providerInfo.getId());
  }

  @Override
  protected String getSearchCategory() {
    return CAT_TV;
  }

  /**
   * get the TV show metadata
   * 
   * @param options
   *          the scrape options
   * @return the MediaMetadata
   * @throws Exception
   */
  MediaMetadata getTvShowMetadata(MediaScrapeOptions options) throws Exception {
    MediaMetadata md = new MediaMetadata(providerInfo.getId());

    String imdbId = "";

    // imdbId from searchResult
    if (options.getResult() != null) {
      imdbId = options.getResult().getIMDBId();
    }

    // imdbid from scraper option
    if (!MetadataUtil.isValidImdbId(imdbId)) {
      imdbId = options.getImdbId();
    }

    if (!MetadataUtil.isValidImdbId(imdbId)) {
      return md;
    }

    LOGGER.debug("IMDB: getMetadata(imdbId): " + imdbId);

    // get combined data
    CachedUrl url = new CachedUrl(imdbSite.getSite() + "/title/" + imdbId + "/combined");
    url.addHeader("Accept-Language", getAcceptLanguage(options.getLanguage().name(), options.getCountry().getAlpha2()));
    Document doc = Jsoup.parse(url.getInputStream(), imdbSite.getCharset().displayName(), "");

    parseCombinedPage(doc, options, md);

    // get plot
    url = new CachedUrl(imdbSite.getSite() + "/title/" + imdbId + "/plotsummary");
    url.addHeader("Accept-Language", getAcceptLanguage(options.getLanguage().name(), options.getCountry().getAlpha2()));
    doc = Jsoup.parse(url.getInputStream(), imdbSite.getCharset().displayName(), "");
    parsePlotsummaryPage(doc, options, md);

    // populate id
    md.setId(ImdbMetadataProvider.providerInfo.getId(), imdbId);

    return md;
  }

  /**
   * get the episode metadata.
   * 
   * @param options
   *          the scrape options
   * @return the MediaMetaData
   * @throws Exception
   */
  MediaMetadata getEpisodeMetadata(MediaScrapeOptions options) throws Exception {
    MediaMetadata md = new MediaMetadata(providerInfo.getId());

    String imdbId = options.getImdbId();
    if (StringUtils.isBlank(imdbId)) {
      return md;
    }

    // get episode number and season number
    int seasonNr = -1;
    int episodeNr = -1;

    try {
      seasonNr = Integer.parseInt(options.getId(MediaMetadata.SEASON_NR));
      episodeNr = Integer.parseInt(options.getId(MediaMetadata.EPISODE_NR));
    }
    catch (Exception e) {
      LOGGER.warn("error parsing season/episode number");
    }

    if (seasonNr == -1 || episodeNr == -1) {
      return md;
    }

    // first get the base episode metadata which can be gathered via
    // getEpisodeList()
    List<MediaEpisode> episodes = getEpisodeList(options);

    MediaEpisode wantedEpisode = null;
    for (MediaEpisode episode : episodes) {
      if (episode.season == seasonNr && episode.episode == episodeNr) {
        wantedEpisode = episode;
        break;
      }
    }

    // we did not find the episode; return
    if (wantedEpisode == null) {
      return md;
    }

    // then parse the actors page to get the rest
    CachedUrl url = new CachedUrl(imdbSite.getSite() + "/title/" + imdbId + "/epcast");
    url.addHeader("Accept-Language", getAcceptLanguage(options.getLanguage().name(), options.getCountry().getAlpha2()));
    Document doc = Jsoup.parse(url.getInputStream(), imdbSite.getCharset().displayName(), "");

    // the base content of this page starts here
    Element content = doc.getElementById("tn15content");
    Elements episodeStart = content.getElementsByTag("h4");
    // every episode starts with an h4 containing the title
    for (Element h4 : episodeStart) {
      Elements anchors = h4.getElementsByAttributeValueStarting("href", "/title/tt");
      if (anchors == null || anchors.isEmpty()) {
        continue;
      }

      // now we match the corresponding result from the episode parsing list
      if (anchors.get(0).attr("href").endsWith(wantedEpisode.ids.get(providerInfo.getId()) + "/")) {
        md.setId(providerInfo.getId(), wantedEpisode.ids.get(providerInfo.getId()));
        md.storeMetadata(MediaMetadata.EPISODE_NR, wantedEpisode.episode);
        md.storeMetadata(MediaMetadata.SEASON_NR, wantedEpisode.season);
        md.storeMetadata(MediaMetadata.TITLE, wantedEpisode.title);
        md.storeMetadata(MediaMetadata.RATING, wantedEpisode.rating);
        md.storeMetadata(MediaMetadata.PLOT, "");

        // parse release date
        Element releaseDate = h4.nextElementSibling();
        if (releaseDate.tag().getName().equals("b")) {
          try {
            SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy", Locale.US);
            Date parsedDate = sdf.parse(releaseDate.ownText());
            md.storeMetadata(MediaMetadata.RELEASE_DATE, parsedDate);
          }
          catch (ParseException ignored) {
            ignored.printStackTrace();
          }

          // parse plot - this is really tricky, because the plot is not in any
          // tag for itself:
          // <b>7 July 2006</b><br>The police department in Santa Barbara hires
          // someone they think is a psychic detective.<br/>
          // first store the reference of the preceeding <b>
          Element b = releaseDate.nextElementSibling();
          // and then iterate over all text nodes of the parent until we get the
          // right node
          for (TextNode node : content.textNodes()) {
            if (node.previousSibling() == b) {
              md.storeMetadata(MediaMetadata.PLOT, node.text());
              break;
            }
          }
        }

        // and finally the cast which is the nextmost <div> after the h4
        Element next = h4.nextElementSibling();
        while (true) {
          if (next.tag().getName().equals("div")) {
            Elements rows = next.getElementsByTag("tr");
            for (Element row : rows) {
              MediaCastMember cm = parseCastMember(row);
              if (StringUtils.isNotEmpty(cm.getName()) && StringUtils.isNotEmpty(cm.getCharacter())) {
                cm.setType(MediaCastMember.CastType.ACTOR);
                md.addCastMember(cm);
              }
            }
            break;
          }
          next = next.nextElementSibling();
        }
        break;
      }
    }

    return md;
  }

  /**
   * parse the episode list from the ratings overview
   * 
   * @param options
   *          the scrape options
   * @return the episode list
   * @throws Exception
   */
  List<MediaEpisode> getEpisodeList(MediaScrapeOptions options) throws Exception {
    List<MediaEpisode> episodes = new ArrayList<>();

    // parse the episodes from the ratings overview page (e.g.
    // http://www.imdb.com/title/tt0491738/epdate )
    String imdbId = options.getImdbId();
    if (StringUtils.isBlank(imdbId)) {
      return episodes;
    }

    CachedUrl url = new CachedUrl(imdbSite.getSite() + "/title/" + imdbId + "/epdate");
    url.addHeader("Accept-Language", getAcceptLanguage(options.getLanguage().name(), options.getCountry().getAlpha2()));
    Document doc = Jsoup.parse(url.getInputStream(), imdbSite.getCharset().displayName(), "");

    Pattern rowPattern = Pattern.compile("([0-9]*)\\.([0-9]*)");
    // parse episodes
    Elements tables = doc.getElementsByTag("table");
    for (Element table : tables) {
      Elements rows = table.getElementsByTag("tr");
      for (Element row : rows) {
        Matcher matcher = rowPattern.matcher(row.text());
        if (matcher.find() && matcher.groupCount() >= 2) {
          try {
            // we found a row containing episode data
            MediaEpisode ep = new MediaEpisode(providerInfo.getId());

            // parse season and ep number
            ep.season = Integer.parseInt(matcher.group(1));
            ep.episode = Integer.parseInt(matcher.group(2));

            // get ep title and id
            Elements anchors = row.getElementsByAttributeValueStarting("href", "/title/tt");
            ep.title = anchors.get(0).text();

            String id = "";
            Matcher idMatcher = IMDB_ID_PATTERN.matcher(anchors.get(0).attr("href"));
            while (idMatcher.find()) {
              if (idMatcher.group(1) != null) {
                id = idMatcher.group(1);
              }
            }

            if (StringUtils.isNotBlank(id)) {
              ep.ids.put(providerInfo.getId(), id);
            }

            Elements cols = row.getElementsByTag("td");
            if (cols != null && cols.size() >= 3) {
              try {
                // rating is the third column
                ep.rating = Double.parseDouble(cols.get(2).ownText());
              }
              catch (Exception ignored) {
              }
            }

            episodes.add(ep);
          }
          catch (Exception e) {
            LOGGER.warn("failed parsing: " + row.text() + " for ep data; " + e.getMessage());
          }
        }
      }
    }

    return episodes;
  }
}
