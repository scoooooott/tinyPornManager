/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.scraper.thetvdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.MetadataUtil;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;

import com.omertron.thetvdbapi.TheTVDBApi;
import com.omertron.thetvdbapi.model.Banner;
import com.omertron.thetvdbapi.model.Banners;
import com.omertron.thetvdbapi.model.Series;

/**
 * The Class TheTvDbMetadataProvider.
 * 
 * @author Manuel Laggner
 */
public class TheTvDbMetadataProvider implements IMediaMetadataProvider, IMediaArtworkProvider {

  /** The Constant LOGGER. */
  private static final Logger      LOGGER       = Logger.getLogger(TmdbMetadataProvider.class);

  /** The Constant instance. */
  private static TheTVDBApi        tvdb;

  /** The provider info. */
  private static MediaProviderInfo providerInfo = new MediaProviderInfo("tvdb", "thetvdb.com",
                                                    "Scraper for thetvdb.com which is able to scrape tv series metadata and artwork");

  /**
   * Instantiates a new the tv db metadata provider.
   */
  public TheTvDbMetadataProvider() {
    if (tvdb == null) {
      tvdb = new TheTVDBApi("1A4971671264D790");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaMetadataProvider#getProviderInfo()
   */
  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaMetadataProvider#getMetaData(org. tinymediamanager.scraper.MediaScrapeOptions)
   */
  @Override
  public MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getMetadata() " + options.toString());
    MediaMetadata md = null;

    switch (options.getType()) {
      case TV_SHOW:
        md = getTvShowMetadata(options);
        break;

      case TV_EPISODE:
        md = getTvShowEpisodeMetadata(options);
        break;

      default:
        throw new Exception("wrong media type for this scraper");
    }

    return md;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaMetadataProvider#search(org.tinymediamanager .scraper.MediaSearchOptions)
   */
  @Override
  public List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
    LOGGER.debug("search() " + options.toString());
    List<MediaSearchResult> results = new ArrayList<MediaSearchResult>();

    if (options.getMediaType() != MediaType.TV_SHOW) {
      throw new Exception("wrong media type for this scraper");
    }

    // detect the string to search
    String searchString = "";
    if (StringUtils.isNotEmpty(options.get(MediaSearchOptions.SearchParam.QUERY))) {
      searchString = options.get(MediaSearchOptions.SearchParam.QUERY);
    }

    if (StringUtils.isEmpty(searchString) && StringUtils.isNotEmpty(options.get(MediaSearchOptions.SearchParam.TITLE))) {
      searchString = options.get(MediaSearchOptions.SearchParam.TITLE);
    }

    // search via the api
    List<Series> series = null;
    synchronized (tvdb) {
      series = tvdb.searchSeries(searchString, Globals.settings.getScraperLanguage().name());
    }

    // first add all tv shows in the preferred langu
    HashMap<String, MediaSearchResult> storedResults = new HashMap<String, MediaSearchResult>();
    for (Series show : series) {
      if (show.getLanguage().equalsIgnoreCase(Globals.settings.getScraperLanguage().name()) && !storedResults.containsKey(show.getId())) {
        MediaSearchResult sr = createSearchResult(show, options, searchString);
        results.add(sr);

        // remember for later check
        storedResults.put(show.getId(), sr);
      }
    }

    // then check if there are other results
    for (Series show : series) {
      if (!storedResults.containsKey(show.getId())) {
        MediaSearchResult sr = createSearchResult(show, options, searchString);
        results.add(sr);

        // remember for later check
        storedResults.put(show.getId(), sr);
      }
    }

    // sort
    Collections.sort(results);
    Collections.reverse(results);

    return results;
  }

  /**
   * Creates the search result from the given input.
   * 
   * @param show
   *          the show
   * @param options
   *          the options
   * @param searchString
   *          the search string
   * @return the media search result
   */
  private MediaSearchResult createSearchResult(Series show, MediaSearchOptions options, String searchString) {
    MediaSearchResult sr = new MediaSearchResult(providerInfo.getId());
    sr.setId(show.getId());
    sr.setIMDBId(show.getImdbId());
    sr.setTitle(show.getSeriesName());
    sr.setPosterUrl(show.getPoster());

    if (show.getFirstAired() != null && show.getFirstAired().length() > 3) {
      sr.setYear(show.getFirstAired().substring(0, 4));
    }

    // populate extra args
    MetadataUtil.copySearchQueryToSearchResult(options, sr);

    sr.setScore(MetadataUtil.calculateScore(searchString, show.getSeriesName()));

    return sr;
  }

  /**
   * Gets the tv show metadata.
   * 
   * @param options
   *          the options
   * @return the tv show metadata
   */
  private MediaMetadata getTvShowMetadata(MediaScrapeOptions options) {
    MediaMetadata md = new MediaMetadata(providerInfo.getId());
    String id = "";

    // id from result
    if (options.getResult() != null) {
      id = options.getResult().getId();
    }

    // do we have an id from the options?
    if (StringUtils.isEmpty(id)) {
      id = options.getId(providerInfo.getId());
    }

    if (StringUtils.isEmpty(id)) {
      return md;
    }

    Series show = null;
    synchronized (tvdb) {
      show = tvdb.getSeries(id, Globals.settings.getScraperLanguage().name());
    }

    // populate metadata
    md.setId(providerInfo.getId(), show.getId());
    md.setTitle(show.getSeriesName());
    md.setImdbId(show.getImdbId());
    md.setPlot(show.getOverview());

    try {
      md.setRating(Double.parseDouble(show.getRating()));
    }
    catch (NumberFormatException e) {
      md.setRating(0);
    }

    return md;
  }

  /**
   * Gets the tv show episode metadata.
   * 
   * @param options
   *          the options
   * @return the tv show episode metadata
   */
  private MediaMetadata getTvShowEpisodeMetadata(MediaScrapeOptions options) {
    MediaMetadata md = new MediaMetadata(providerInfo.getId());

    return md;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.tinymediamanager.scraper.IMediaArtworkProvider#getArtwork(org.tinymediamanager.scraper.MediaScrapeOptions)
   */
  @Override
  public List<MediaArtwork> getArtwork(MediaScrapeOptions options) throws Exception {
    List<MediaArtwork> artwork = new ArrayList<MediaArtwork>();
    String id = "";

    // check if there is a metadata containing an id
    if (options.getMetadata() != null) {
      id = options.getMetadata().getId(providerInfo.getId());
    }

    // get the id from the options
    if (StringUtils.isEmpty(id)) {
      id = options.getId(providerInfo.getId());
    }

    if (StringUtils.isEmpty(id)) {
      return artwork;
    }

    // get artwork from thetvdb
    Banners banners = null;
    synchronized (tvdb) {
      banners = tvdb.getBanners(id);
    }

    List<Banner> bannerList = null;
    switch (options.getArtworkType()) {
      case ALL:
        bannerList = new ArrayList<Banner>(banners.getSeasonList());
        bannerList.addAll(banners.getSeriesList());
        bannerList.addAll(banners.getPosterList());
        bannerList.addAll(banners.getFanartList());
        break;

      case POSTER:
        bannerList = banners.getPosterList();
        break;

      case BACKGROUND:
        bannerList = banners.getFanartList();
        break;

      case BANNER:
      default:
        bannerList = banners.getSeriesList();
        break;

    }

    if (bannerList == null) {
      return artwork;
    }

    // sort bannerlist
    Collections.sort(bannerList, new BannerComparator());

    // build output
    for (Banner banner : bannerList) {
      MediaArtwork ma = new MediaArtwork();
      ma.setDefaultUrl(banner.getUrl());
      ma.setPreviewUrl(banner.getThumb());
      ma.setLanguage(banner.getLanguage());

      // set banner type
      switch (banner.getBannerType()) {
        case poster:
          ma.setType(MediaArtworkType.POSTER);
          break;

        case series:
          ma.setType(MediaArtworkType.BANNER);
          break;

        case season:
          ma.setType(MediaArtworkType.SEASON);
          break;

        case fanart:
        default:
          ma.setType(MediaArtworkType.BACKGROUND);
          break;
      }

      artwork.add(ma);
    }

    return artwork;
  }

  /**
   * The Class ArtworkComparator.
   * 
   * @author Manuel Laggner
   */
  private static class BannerComparator implements Comparator<Banner> {
    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     * 
     * sort artwork: primary by language: preferred lang (ie de), en, others; then: score
     */
    @Override
    public int compare(Banner arg0, Banner arg1) {
      String preferredLangu = Globals.settings.getScraperLanguage().name();

      // check if first image is preferred langu
      if (preferredLangu.equals(arg0.getLanguage()) && !preferredLangu.equals(arg1.getLanguage())) {
        return -1;
      }

      // check if second image is preferred langu
      if (!preferredLangu.equals(arg0.getLanguage()) && preferredLangu.equals(arg1.getLanguage())) {
        return 1;
      }

      // check if the first image is en
      if ("en".equals(arg0.getLanguage()) && !"en".equals(arg1.getLanguage())) {
        return -1;
      }

      // check if the second image is en
      if (!"en".equals(arg0.getLanguage()) && "en".equals(arg1.getLanguage())) {
        return 1;
      }

      // if rating is the same, return 0
      if (arg0.getRating() == arg1.getRating()) {
        return 0;
      }

      // we did not sort until here; so lets sort with the rating
      return arg0.getRating() > arg1.getRating() ? -1 : 1;
    }

  }
}
