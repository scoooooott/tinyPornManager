/*
 * Copyright 2012 - 2014 Manuel Laggner
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.scraper.Certification;
import org.tinymediamanager.scraper.CountryCode;
import org.tinymediamanager.scraper.IMediaArtworkProvider;
import org.tinymediamanager.scraper.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.MediaArtwork;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.MediaCastMember;
import org.tinymediamanager.scraper.MediaCastMember.CastType;
import org.tinymediamanager.scraper.MediaEpisode;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaLanguages;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchOptions.SearchParam;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.MetadataUtil;
import org.tinymediamanager.scraper.util.CachedUrl;

import com.omertron.thetvdbapi.TheTVDBApi;
import com.omertron.thetvdbapi.model.Actor;
import com.omertron.thetvdbapi.model.Banner;
import com.omertron.thetvdbapi.model.Banners;
import com.omertron.thetvdbapi.model.Episode;
import com.omertron.thetvdbapi.model.Series;

/**
 * The Class TheTvDbMetadataProvider.
 * 
 * @author Manuel Laggner
 */
public class TheTvDbMetadataProvider implements ITvShowMetadataProvider, IMediaArtworkProvider {
  private static final Logger      LOGGER       = LoggerFactory.getLogger(TheTvDbMetadataProvider.class);
  private static TheTVDBApi        tvdb;
  private static MediaProviderInfo providerInfo = new MediaProviderInfo(Constants.TVDBID, "thetvdb.com",
                                                    "Scraper for thetvdb.com which is able to scrape tv series metadata and artwork");

  public TheTvDbMetadataProvider() throws Exception {
    initAPI();
  }

  private static synchronized void initAPI() throws Exception {
    if (tvdb == null) {
      try {
        tvdb = new TheTVDBApi("1A4971671264D790");
      }
      catch (Exception e) {
        LOGGER.error("TheTvDbMetadataProvider", e);
        // remove cached request
        clearTvdbCache();
        throw e;
      }
    }
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

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

    // return an empty search result if no query provided
    if (StringUtils.isEmpty(searchString)) {
      return results;
    }

    String language = options.get(SearchParam.LANGUAGE);
    String country = options.get(SearchParam.COUNTRY); // for passing the country to the scrape

    searchString = clearSearchString(searchString);

    // search via the api
    List<Series> series = null;
    synchronized (tvdb) {
      series = tvdb.searchSeries(searchString, language);
    }

    if (series == null || series.isEmpty()) {
      // maybe broken request - delete cache to be sure
      clearTvdbCache();
      return results;
    }

    // first add all tv shows in the preferred language
    HashMap<String, MediaSearchResult> storedResults = new HashMap<String, MediaSearchResult>();
    for (Series show : series) {
      if (show.getLanguage().equalsIgnoreCase(language) && !storedResults.containsKey(show.getId())) {
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

    // if there weren't any result AND the searchstring consist only of digits, we try to scrape it directly
    if (results.isEmpty() && searchString.matches("^[0-9]+$")) {
      MediaScrapeOptions scrapeOptions = new MediaScrapeOptions();
      scrapeOptions.setId(providerInfo.getId(), searchString);
      scrapeOptions.setType(MediaType.TV_SHOW);
      scrapeOptions.setLanguage(MediaLanguages.valueOf(language));
      scrapeOptions.setCountry(CountryCode.valueOf(country));

      MediaMetadata md = getTvShowMetadata(scrapeOptions);

      if (md != null && StringUtils.isNotBlank(md.getStringValue(MediaMetadata.TITLE))) {
        MediaSearchResult result = new MediaSearchResult(providerInfo.getId());
        result.setId((String) md.getId(providerInfo.getId()));
        result.setTitle(md.getStringValue(MediaMetadata.TITLE));
        result.setPosterUrl(md.getStringValue(MediaMetadata.POSTER_URL));
        results.add(result);
      }
    }

    // sort
    Collections.sort(results);
    Collections.reverse(results);

    return results;
  }

  /*
   * clear the search string to minimize search problem with the API
   */
  private String clearSearchString(String searchString) {
    String cleanedString = searchString.replace("\'", "");
    cleanedString = cleanedString.replace(",", "");
    cleanedString = cleanedString.replace("ä", "a");
    cleanedString = cleanedString.replace("ö", "o");
    cleanedString = cleanedString.replace("ü", "u");
    return cleanedString;
  }

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

  @Override
  public MediaMetadata getTvShowMetadata(MediaScrapeOptions options) throws Exception {
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

    // do we have the id in the alternate form?
    if (StringUtils.isEmpty(id)) {
      id = options.getId("tvdb");
    }

    if (StringUtils.isEmpty(id)) {
      return md;
    }

    Series show = null;
    synchronized (tvdb) {
      show = tvdb.getSeries(id, options.getLanguage().name());
    }

    if (show == null) {
      // maybe broken request - delete cache to be sure
      clearTvdbCache();
      return md;
    }

    // populate metadata
    md.setId(providerInfo.getId(), show.getId());
    md.storeMetadata(MediaMetadata.TITLE, show.getSeriesName());
    md.setId(MediaMetadata.IMDBID, show.getImdbId());
    md.storeMetadata(MediaMetadata.PLOT, show.getOverview());

    try {
      md.storeMetadata(MediaMetadata.RUNTIME, Integer.valueOf(show.getRuntime()));
    }
    catch (NumberFormatException e) {
      md.storeMetadata(MediaMetadata.RUNTIME, 0);
    }
    md.storeMetadata(MediaMetadata.POSTER_URL, show.getPoster());

    try {
      md.storeMetadata(MediaMetadata.RATING, Double.parseDouble(show.getRating()));
    }
    catch (NumberFormatException e) {
      md.storeMetadata(MediaMetadata.RATING, 0);
    }
    md.storeMetadata(MediaMetadata.RELEASE_DATE, show.getFirstAired());

    try {
      Date date = org.tinymediamanager.scraper.util.StrgUtils.parseDate(show.getFirstAired());
      md.storeMetadata(MediaMetadata.YEAR, new SimpleDateFormat("yyyy").format(date));
    }
    catch (Exception e) {
    }

    md.storeMetadata(MediaMetadata.STATUS, show.getStatus());
    md.storeMetadata(MediaMetadata.PRODUCTION_COMPANY, show.getNetwork());

    // actors
    List<Actor> actors = new ArrayList<Actor>();
    synchronized (tvdb) {
      actors.addAll(tvdb.getActors(id));
    }

    if (actors == null || actors.isEmpty()) {
      // maybe broken request - delete cache to be sure
      clearTvdbCache();
    }

    for (Actor actor : actors) {
      MediaCastMember member = new MediaCastMember(CastType.ACTOR);
      member.setName(actor.getName());
      member.setCharacter(actor.getRole());
      member.setImageUrl(actor.getImage());

      md.addCastMember(member);
    }

    md.addCertification(Certification.findCertification(show.getContentRating()));

    // genres
    for (String genreAsString : show.getGenres()) {
      md.addGenre(getTmmGenre(genreAsString));
    }

    return md;
  }

  @Override
  public MediaMetadata getEpisodeMetadata(MediaScrapeOptions options) throws Exception {
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

    // still no ID? try the old one
    if (StringUtils.isEmpty(id)) {
      id = options.getId("tvdb");
    }

    if (StringUtils.isEmpty(id)) {
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

    List<Episode> episodes = new ArrayList<Episode>();
    synchronized (tvdb) {
      // switched to getAllEpisodes for performance - only 1 request needed for scraping multiple episodes of one tv show
      episodes.addAll(tvdb.getAllEpisodes(id, options.getLanguage().name()));
    }

    if (episodes.isEmpty()) {
      // maybe broken request - delete cache to be sure
      clearTvdbCache();
    }

    Episode episode = null;

    // filter out the episode
    for (Episode ep : episodes) {
      if (ep.getSeasonNumber() == seasonNr && ep.getEpisodeNumber() == episodeNr) {
        episode = ep;
        break;
      }
    }

    if (episode == null) {
      return md;
    }

    md.storeMetadata(MediaMetadata.EPISODE_NR_DVD, episode.getDvdEpisodeNumber());
    md.storeMetadata(MediaMetadata.SEASON_NR_DVD, episode.getDvdSeason());
    md.storeMetadata(MediaMetadata.EPISODE_NR_COMBINED, episode.getCombinedEpisodeNumber());
    md.storeMetadata(MediaMetadata.SEASON_NR_COMBINED, episode.getCombinedSeason());
    md.storeMetadata(MediaMetadata.ABSOLUTE_NR, episode.getAbsoluteNumber());

    md.storeMetadata(MediaMetadata.TITLE, episode.getEpisodeName());
    md.storeMetadata(MediaMetadata.PLOT, episode.getOverview());
    try {
      md.storeMetadata(MediaMetadata.RATING, Float.parseFloat(episode.getRating()));
    }
    catch (NumberFormatException e) {
      md.storeMetadata(MediaMetadata.RATING, 0);
    }
    md.storeMetadata(MediaMetadata.RELEASE_DATE, episode.getFirstAired());
    md.setId(providerInfo.getId(), episode.getId());

    // directors
    for (String director : episode.getDirectors()) {
      MediaCastMember cm = new MediaCastMember(CastType.DIRECTOR);
      cm.setName(director);
      md.addCastMember(cm);
    }

    // writers
    for (String writer : episode.getWriters()) {
      MediaCastMember cm = new MediaCastMember(CastType.WRITER);
      cm.setName(writer);
      md.addCastMember(cm);
    }

    // actors (guests?)
    for (String guest : episode.getGuestStars()) {
      MediaCastMember cm = new MediaCastMember(CastType.ACTOR);
      cm.setName(guest);
      md.addCastMember(cm);
    }

    // Thumb
    if (options.getArtworkType() == MediaArtworkType.ALL || options.getArtworkType() == MediaArtworkType.THUMB) {
      MediaArtwork ma = new MediaArtwork();
      ma.setType(MediaArtworkType.THUMB);
      ma.setDefaultUrl(episode.getFilename());
      md.addMediaArt(ma);
    }

    return md;
  }

  @Override
  public List<MediaArtwork> getArtwork(MediaScrapeOptions options) throws Exception {
    List<MediaArtwork> artwork = new ArrayList<MediaArtwork>();
    String id = "";

    // check if there is a metadata containing an id
    if (options.getMetadata() != null) {
      id = (String) options.getMetadata().getId(providerInfo.getId());
    }

    // get the id from the options
    if (StringUtils.isEmpty(id)) {
      id = options.getId(providerInfo.getId());
    }

    // do we have the id in the alternate form?
    if (StringUtils.isEmpty(id)) {
      id = options.getId("tvdb");
    }

    if (StringUtils.isEmpty(id)) {
      return artwork;
    }

    // get artwork from thetvdb
    Banners banners = null;
    synchronized (tvdb) {
      banners = tvdb.getBanners(id);
    }

    if (banners == null || banners.getSeriesId() == 0) {
      // maybe broken request - delete cache to be sure
      clearTvdbCache();
    }

    List<Banner> bannerList = null;
    switch (options.getArtworkType()) {
      case ALL:
        bannerList = new ArrayList<Banner>(banners.getSeasonList());
        bannerList.addAll(banners.getSeriesList());
        bannerList.addAll(banners.getPosterList());
        bannerList.addAll(banners.getFanartList());
        bannerList.addAll(banners.getSeasonList());
        break;

      case POSTER:
        bannerList = banners.getPosterList();
        break;

      case BACKGROUND:
        bannerList = banners.getFanartList();
        break;

      case SEASON:
        bannerList = banners.getSeasonList();
        break;

      case BANNER:
        bannerList = banners.getSeriesList();
        break;

      default:
        break;

    }

    if (bannerList == null) {
      return artwork;
    }

    // sort bannerlist
    Collections.sort(bannerList, new BannerComparator(options.getLanguage().name()));

    // build output
    for (Banner banner : bannerList) {
      MediaArtwork ma = new MediaArtwork();
      ma.setDefaultUrl(banner.getUrl());
      if (StringUtils.isNotBlank(banner.getThumb())) {
        ma.setPreviewUrl(banner.getThumb());
      }
      else {
        ma.setPreviewUrl(banner.getUrl());
      }

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
          ma.setSeason(banner.getSeason());
          break;

        case fanart:
        default:
          ma.setType(MediaArtworkType.BACKGROUND);
          // extract image sizes
          if (StringUtils.isNotBlank(banner.getBannerType2().getType())) {
            try {
              Pattern pattern = Pattern.compile("([0-9]{3,4})x([0-9]{3,4})");
              Matcher matcher = pattern.matcher(banner.getBannerType2().getType());
              if (matcher.matches() && matcher.groupCount() > 1) {
                ma.addImageSize(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), banner.getUrl());
              }

            }
            catch (Exception e) {
              LOGGER.debug("could not extract size from bannertype 2: " + banner.getBannerType2());
            }

          }
          break;
      }

      artwork.add(ma);
    }

    return artwork;
  }

  @Override
  public List<MediaEpisode> getEpisodeList(MediaScrapeOptions options) throws Exception {
    List<MediaEpisode> episodes = new ArrayList<MediaEpisode>();
    String id = "";

    // id from result
    if (options.getResult() != null) {
      id = options.getResult().getId();
    }

    // do we have an id from the options?
    if (StringUtils.isEmpty(id)) {
      id = options.getId(providerInfo.getId());
    }

    // do we have the id in the alternate form?
    if (StringUtils.isEmpty(id)) {
      id = options.getId("tvdb");
    }

    if (StringUtils.isEmpty(id)) {
      return episodes;
    }

    List<Episode> eps = new ArrayList<Episode>();
    synchronized (tvdb) {
      // switched to getAllEpisodes for performance - only 1 request needed for scraping multiple episodes of one tv show
      eps.addAll(tvdb.getAllEpisodes(id, options.getLanguage().name()));
    }

    if (eps.isEmpty()) {
      // maybe broken request - delete cache to be sure
      clearTvdbCache();
    }

    for (Episode ep : eps) {
      MediaEpisode episode = new MediaEpisode(providerInfo.getId());
      episode.season = ep.getSeasonNumber();
      episode.episode = ep.getEpisodeNumber();
      episode.dvdSeason = ep.getDvdSeason();
      episode.dvdEpisode = ep.getDvdEpisodeNumber();
      episode.combinedSeason = ep.getCombinedSeason();
      episode.combinedEpisode = ep.getCombinedEpisodeNumber();
      episode.absoluteNumber = ep.getAbsoluteNumber();
      episode.title = ep.getEpisodeName();
      episode.plot = ep.getOverview();

      try {
        episode.rating = Float.parseFloat(ep.getRating());
      }
      catch (NumberFormatException e) {
        episode.rating = 0f;
      }

      episode.firstAired = ep.getFirstAired();
      episode.ids.put(providerInfo.getId(), ep.getId());

      // directors
      for (String director : ep.getDirectors()) {
        MediaCastMember cm = new MediaCastMember(CastType.DIRECTOR);
        cm.setName(director);
        episode.castMembers.add(cm);
      }

      // writers
      for (String writer : ep.getWriters()) {
        MediaCastMember cm = new MediaCastMember(CastType.WRITER);
        cm.setName(writer);
        episode.castMembers.add(cm);
      }

      // actors (guests?)
      for (String guest : ep.getGuestStars()) {
        MediaCastMember cm = new MediaCastMember(CastType.ACTOR);
        cm.setName(guest);
        episode.castMembers.add(cm);
      }

      // Thumb
      MediaArtwork ma = new MediaArtwork();
      ma.setType(MediaArtworkType.THUMB);
      ma.setDefaultUrl(ep.getFilename());
      episode.artwork.add(ma);

      episodes.add(episode);
    }

    return episodes;
  }

  /**
   * Maps scraper Genres to internal TMM genres
   */
  private MediaGenres getTmmGenre(String genre) {
    MediaGenres g = null;
    if (genre.isEmpty()) {
      return g;
    }
    // @formatter:off
    else if (genre.equals("Action"))           { g = MediaGenres.ACTION; }
    else if (genre.equals("Adventure"))        { g = MediaGenres.ADVENTURE; }
    else if (genre.equals("Animation"))        { g = MediaGenres.ANIMATION; }
    else if (genre.equals("Children"))         { g = MediaGenres.FAMILY; }
    else if (genre.equals("Comedy"))           { g = MediaGenres.COMEDY; }
    else if (genre.equals("Crime"))            { g = MediaGenres.CRIME; }
    else if (genre.equals("Documentary"))      { g = MediaGenres.DOCUMENTARY; }
    else if (genre.equals("Drama"))            { g = MediaGenres.DRAMA; }
    else if (genre.equals("Family"))           { g = MediaGenres.FAMILY; }
    else if (genre.equals("Fantasy"))          { g = MediaGenres.FANTASY; }
    else if (genre.equals("Food"))             { g = MediaGenres.DOCUMENTARY; }
    else if (genre.equals("Game Show"))        { g = MediaGenres.GAME_SHOW; }
    else if (genre.equals("Home and Garden"))  { g = MediaGenres.DOCUMENTARY; }
    else if (genre.equals("Horror"))           { g = MediaGenres.HORROR; }
    else if (genre.equals("Mini-Series"))      { g = MediaGenres.SERIES; }
    else if (genre.equals("News"))             { g = MediaGenres.NEWS; }
    else if (genre.equals("Reality"))          { g = MediaGenres.REALITY_TV; }
    else if (genre.equals("Science-Fiction"))  { g = MediaGenres.SCIENCE_FICTION; }
    else if (genre.equals("Soap"))             { g = MediaGenres.SERIES; }
    else if (genre.equals("Special Interest")) { g = MediaGenres.INDIE; }
    else if (genre.equals("Sport"))            { g = MediaGenres.SPORT; }
    else if (genre.equals("Suspense"))         { g = MediaGenres.SUSPENSE; }
    else if (genre.equals("Talk Show"))        { g = MediaGenres.TALK_SHOW; }
    else if (genre.equals("Thriller"))         { g = MediaGenres.THRILLER; }
    else if (genre.equals("Travel"))           { g = MediaGenres.HOLIDAY; }
    else if (genre.equals("Western"))          { g = MediaGenres.WESTERN; }
    // @formatter:on
    if (g == null) {
      g = MediaGenres.getGenre(genre);
    }
    return g;
  }

  private static void clearTvdbCache() {
    CachedUrl.cleanupCacheForSpecificHost("thetvdb.com");
  }

  /**********************************************************************
   * local helper classes
   **********************************************************************/
  private static class BannerComparator implements Comparator<Banner> {
    private String preferredLangu;

    private BannerComparator(String language) {
      this.preferredLangu = language;
    }

    /*
     * sort artwork: primary by language: preferred lang (ie de), en, others; then: score
     */
    @Override
    public int compare(Banner arg0, Banner arg1) {
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
      if (arg0.getRating().equals(arg1.getRating())) {
        return 0;
      }

      // we did not sort until here; so lets sort with the rating
      return arg0.getRating() > arg1.getRating() ? -1 : 1;
    }

  }
}
