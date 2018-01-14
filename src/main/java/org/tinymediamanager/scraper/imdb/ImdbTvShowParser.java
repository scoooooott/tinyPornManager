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
package org.tinymediamanager.scraper.imdb;

import static org.tinymediamanager.scraper.imdb.ImdbMetadataProvider.CAT_TV;
import static org.tinymediamanager.scraper.imdb.ImdbMetadataProvider.providerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaRating;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.http.CachedUrl;
import org.tinymediamanager.scraper.http.Url;
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
    if (ImdbMetadataProvider.providerInfo.getConfig().getValueAsBool("filterUnwantedCategories")) {
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

    // get reference data
    CachedUrl url = new CachedUrl(imdbSite.getSite() + "/title/" + imdbId + "/reference");
    url.addHeader("Accept-Language", getAcceptLanguage(options.getLanguage().getLanguage(), options.getCountry().getAlpha2()));
    Document doc = Jsoup.parse(url.getInputStream(), imdbSite.getCharset().displayName(), "");

    parseReferencePage(doc, options, md);

    // get plot
    url = new CachedUrl(imdbSite.getSite() + "/title/" + imdbId + "/plotsummary");
    url.addHeader("Accept-Language", getAcceptLanguage(options.getLanguage().getLanguage(), options.getCountry().getAlpha2()));
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
    int seasonNr = options.getIdAsIntOrDefault(MediaMetadata.SEASON_NR, -1);
    int episodeNr = options.getIdAsIntOrDefault(MediaMetadata.EPISODE_NR, -1);

    if (seasonNr == -1 || episodeNr == -1) {
      return md;
    }

    // first get the base episode metadata which can be gathered via
    // getEpisodeList()
    List<MediaMetadata> episodes = getEpisodeList(options);

    MediaMetadata wantedEpisode = null;
    for (MediaMetadata episode : episodes) {
      if (episode.getSeasonNumber() == seasonNr && episode.getEpisodeNumber() == episodeNr) {
        wantedEpisode = episode;
        break;
      }
    }

    // we did not find the episode; return
    if (wantedEpisode == null) {
      return md;
    }

    md.setId(providerInfo.getId(), wantedEpisode.getId(providerInfo.getId()));
    md.setEpisodeNumber(wantedEpisode.getEpisodeNumber());
    md.setSeasonNumber(wantedEpisode.getSeasonNumber());
    md.setTitle(wantedEpisode.getTitle());
    md.setPlot(wantedEpisode.getPlot());
    md.setRatings(wantedEpisode.getRatings());
    md.setReleaseDate(wantedEpisode.getReleaseDate());

    // and finally the cast which needed to be fetched from the fullcredits page
    if (wantedEpisode.getId(providerInfo.getId()) instanceof String && StringUtils.isNotBlank((String) wantedEpisode.getId(providerInfo.getId()))) {
      Url url = new Url(imdbSite.getSite() + "/title/" + wantedEpisode.getId(providerInfo.getId()) + "/fullcredits");
      url.addHeader("Accept-Language", "en"); // force EN for parsing by HTMl texts
      Document doc = Jsoup.parse(url.getInputStream(), imdbSite.getCharset().displayName(), "");

      // director & writer
      Element fullcredits = doc.getElementById("fullcredits_content");
      if (fullcredits != null) {
        Elements tables = fullcredits.getElementsByTag("table");

        // first table are directors
        if (tables.get(0) != null) {
          for (Element director : tables.get(0).getElementsByClass("name")) {
            MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.DIRECTOR);
            cm.setName(director.text());
            // profile path
            Element anchor = director.getElementsByAttributeValueStarting("href", "/name/").first();
            if (anchor != null) {
              Matcher matcher = PERSON_ID_PATTERN.matcher(anchor.attr("href"));
              if (matcher.find()) {
                if (matcher.group(0) != null) {
                  cm.setProfileUrl("http://www.imdb.com" + matcher.group(0));
                }
                if (matcher.group(1) != null) {
                  cm.setId(providerInfo.getId(), matcher.group(1));
                }
              }
            }
            md.addCastMember(cm);
          }
        }

        // second table are writers
        if (tables.get(0) != null) {
          for (Element writer : tables.get(0).getElementsByClass("name")) {
            MediaCastMember cm = new MediaCastMember(MediaCastMember.CastType.WRITER);
            cm.setName(writer.text());
            // profile path
            Element anchor = writer.getElementsByAttributeValueStarting("href", "/name/").first();
            if (anchor != null) {
              Matcher matcher = PERSON_ID_PATTERN.matcher(anchor.attr("href"));
              if (matcher.find()) {
                if (matcher.group(0) != null) {
                  cm.setProfileUrl("http://www.imdb.com" + matcher.group(0));
                }
                if (matcher.group(1) != null) {
                  cm.setId(providerInfo.getId(), matcher.group(1));
                }
              }
            }
            md.addCastMember(cm);
          }
        }
      }

      // actors
      Element castTableElement = doc.getElementsByClass("cast_list").first();
      if (castTableElement != null) {
        Elements tr = castTableElement.getElementsByTag("tr");
        for (Element row : tr) {
          MediaCastMember cm = parseCastMember(row);
          if (cm != null && StringUtils.isNotEmpty(cm.getName()) && StringUtils.isNotEmpty(cm.getCharacter())) {
            cm.setType(MediaCastMember.CastType.ACTOR);
            md.addCastMember(cm);
          }
        }
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
   *           any exception which has not been catched
   */
  List<MediaMetadata> getEpisodeList(MediaScrapeOptions options) throws Exception {
    List<MediaMetadata> episodes = new ArrayList<>();

    // parse the episodes from the ratings overview page (e.g.
    // http://www.imdb.com/title/tt0491738/epdate )
    String imdbId = options.getImdbId();
    if (StringUtils.isBlank(imdbId)) {
      return episodes;
    }

    // we need to parse every season for its own _._
    // first the specials
    CachedUrl url = new CachedUrl(imdbSite.getSite() + "/title/" + imdbId + "/epdate");
    url.addHeader("Accept-Language", getAcceptLanguage(options.getLanguage().getLanguage(), options.getCountry().getAlpha2()));
    Document doc = Jsoup.parse(url.getInputStream(), imdbSite.getCharset().displayName(), "");

    parseEpisodeList(0, episodes, doc);

    // then parse every season
    for (int i = 1;; i++) {
      try {
        url = new CachedUrl(imdbSite.getSite() + "/title/" + imdbId + "/epdate?season=" + i);
        url.addHeader("Accept-Language", getAcceptLanguage(options.getLanguage().getLanguage(), options.getCountry().getAlpha2()));
        doc = Jsoup.parse(url.getInputStream(), imdbSite.getCharset().displayName(), "");
        // if the given season number and the parsed one does not match, break here
        if (!parseEpisodeList(i, episodes, doc)) {
          break;
        }
      }
      catch (Exception e) {
        LOGGER.warn("problem parsing ep list: " + e.getMessage());
      }
    }

    return episodes;
  }

  private boolean parseEpisodeList(int season, List<MediaMetadata> episodes, Document doc) {
    Pattern unknownPattern = Pattern.compile("Unknown");
    Pattern seasonEpisodePattern = Pattern.compile("S([0-9]*), Ep([0-9]*)");
    int episodeCounter = 0;

    // parse episodes
    Elements tables = doc.getElementsByClass("eplist");
    for (Element table : tables) {
      Elements rows = table.getElementsByClass("list_item");
      for (Element row : rows) {
        Matcher matcher = season == 0 ? unknownPattern.matcher(row.text()) : seasonEpisodePattern.matcher(row.text());
        if (matcher.find() && (season == 0 || matcher.groupCount() >= 2)) {
          try {
            // we found a row containing episode data
            MediaMetadata ep = new MediaMetadata(providerInfo.getId());

            // parse season and ep number
            if (season == 0) {
              ep.setSeasonNumber(season);
              ep.setEpisodeNumber(++episodeCounter);
            }
            else {
              ep.setSeasonNumber(Integer.parseInt(matcher.group(1)));
              ep.setEpisodeNumber(Integer.parseInt(matcher.group(2)));
            }

            // check if we have still valid data
            if (season > 0 && season != ep.getSeasonNumber()) {
              return false;
            }

            // get ep title and id
            Elements anchors = row.getElementsByAttributeValueStarting("href", "/title/tt");
            for (Element anchor : anchors) {
              if ("name".equals(anchor.attr("itemprop"))) {
                ep.setTitle(anchor.text());
                break;
              }
            }

            String id = "";
            Matcher idMatcher = IMDB_ID_PATTERN.matcher(anchors.get(0).attr("href"));
            while (idMatcher.find()) {
              if (idMatcher.group(1) != null) {
                id = idMatcher.group(1);
              }
            }

            if (StringUtils.isNotBlank(id)) {
              ep.setId(providerInfo.getId(), id);
            }

            // plot
            Element plot = row.getElementsByClass("item_description").first();
            if (plot != null) {
              ep.setPlot(plot.ownText());
            }

            // rating and rating count
            Element ratingElement = row.getElementsByClass("ipl-rating-star__rating").first();
            if (ratingElement != null) {
              String ratingAsString = ratingElement.ownText().replace(",", ".");

              Element votesElement = row.getElementsByClass("ipl-rating-star__total-votes").first();
              if (votesElement != null) {
                String countAsString = votesElement.ownText().replaceAll("[.,()]", "").trim();
                try {
                  MediaRating rating = new MediaRating(providerInfo.getId());
                  rating.setRating(Float.valueOf(ratingAsString));
                  rating.setVoteCount(Integer.parseInt(countAsString));
                  ep.addRating(rating);
                }
                catch (Exception ignored) {
                }
              }
            }

            // release date
            Element releaseDate = row.getElementsByClass("airdate").first();
            if (releaseDate != null) {
              ep.setReleaseDate(parseDate(releaseDate.ownText()));
            }

            episodes.add(ep);
          }
          catch (Exception e) {
            LOGGER.warn("failed parsing: " + row.text() + " for ep data; " + e.getMessage());
          }
        }
      }
    }
    return true;
  }
}
