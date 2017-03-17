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
package org.tinymediamanager.scraper.thetvdb;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.UnsupportedMediaTypeException;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaCastMember.CastType;
import org.tinymediamanager.scraper.entities.MediaEpisode;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.ITvShowArtworkProvider;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.util.ApiKey;
import org.tinymediamanager.scraper.util.MetadataUtil;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.scraper.util.TvUtils;

import com.omertron.thetvdbapi.TheTVDBApi;
import com.omertron.thetvdbapi.model.Actor;
import com.omertron.thetvdbapi.model.Banner;
import com.omertron.thetvdbapi.model.BannerType;
import com.omertron.thetvdbapi.model.Banners;
import com.omertron.thetvdbapi.model.Episode;
import com.omertron.thetvdbapi.model.Series;

import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 * The Class TheTvDbMetadataProvider.
 * 
 * @author Manuel Laggner
 */
@PluginImplementation
public class TheTvDbMetadataProvider implements ITvShowMetadataProvider, ITvShowArtworkProvider {
  private static final Logger      LOGGER       = LoggerFactory.getLogger(TheTvDbMetadataProvider.class);
  private static TheTVDBApi        tvdb;
  private static MediaProviderInfo providerInfo = createMediaProviderInfo();

  public TheTvDbMetadataProvider() throws Exception {
    initAPI();
  }

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo("tvdb", "thetvdb.com",
        "<html><h3>The TV DB</h3><br />An open database for television fans. This scraper is able to scrape TV series metadata and artwork",
        TheTvDbMetadataProvider.class.getResource("/thetvdb_com.png"));
    providerInfo.setVersion(TheTvDbMetadataProvider.class);
    return providerInfo;
  }

  private static synchronized void initAPI() throws Exception {
    if (tvdb == null) {
      try {
        tvdb = new TheTVDBApi(ApiKey.decryptApikey("7bHHg4k0XhRERM8xd3l+ElhMUXOA5Ou4vQUEzYLGHt8=")); // when changing key, check other occurrences!
      }
      catch (Exception e) {
        LOGGER.error("TheTvDbMetadataProvider", e);
        throw e;
      }
    }
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public MediaMetadata getMetadata(MediaScrapeOptions mediaScrapeOptions) throws Exception {
    LOGGER.debug("getting metadata: " + mediaScrapeOptions);
    switch (mediaScrapeOptions.getType()) {
      case TV_SHOW:
        return getTvShowMetadata(mediaScrapeOptions);

      case TV_EPISODE:
        return getEpisodeMetadata(mediaScrapeOptions);

      default:
        throw new UnsupportedMediaTypeException(mediaScrapeOptions.getType());
    }
  }

  @Override
  public List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
    LOGGER.debug("search() " + options.toString());
    List<MediaSearchResult> results = new ArrayList<>();

    if (options.getMediaType() != MediaType.TV_SHOW) {
      throw new UnsupportedMediaTypeException(options.getMediaType());
    }

    // detect the string to search
    String searchString = "";
    if (StringUtils.isNotEmpty(options.getQuery())) {
      searchString = options.getQuery();
    }

    // return an empty search result if no query provided
    if (StringUtils.isEmpty(searchString)) {
      return results;
    }

    String language = options.getLanguage().getLanguage();
    String country = options.getCountry().name(); // for passing the country to the scrape

    // search via the api
    List<Series> series = null;
    synchronized (tvdb) {
      series = tvdb.searchSeries(searchString, language);
    }

    if (series == null || series.isEmpty()) {
      return results;
    }

    // first add all tv shows in the preferred language
    HashMap<String, MediaSearchResult> storedResults = new HashMap<>();
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

    // if there weren't any result AND the searchstring consist only of digits,
    // we try to scrape it directly
    if (results.isEmpty() && searchString.matches("^[0-9]+$")) {
      MediaScrapeOptions scrapeOptions = new MediaScrapeOptions(MediaType.TV_SHOW);
      scrapeOptions.setId(providerInfo.getId(), searchString);
      scrapeOptions.setLanguage(options.getLanguage());
      scrapeOptions.setCountry(CountryCode.valueOf(country));

      MediaMetadata md = getTvShowMetadata(scrapeOptions);

      if (md != null && StringUtils.isNotBlank(md.getTitle())) {
        MediaSearchResult result = new MediaSearchResult(providerInfo.getId(), options.getMediaType());
        result.setId((String) md.getId(providerInfo.getId()));
        result.setTitle(md.getTitle());

        if (!md.getMediaArt(MediaArtworkType.POSTER).isEmpty()) {
          MediaArtwork poster = md.getMediaArt(MediaArtworkType.POSTER).get(0);
          result.setPosterUrl(poster.getPreviewUrl());
        }

        results.add(result);
      }
    }

    // sort
    Collections.sort(results);
    Collections.reverse(results);

    return results;
  }

  private MediaSearchResult createSearchResult(Series show, MediaSearchOptions options, String searchString) {
    MediaSearchResult sr = new MediaSearchResult(providerInfo.getId(), options.getMediaType());
    sr.setId(show.getId());
    sr.setIMDBId(show.getImdbId());
    sr.setTitle(show.getSeriesName());
    sr.setPosterUrl(show.getPoster());

    if (show.getFirstAired() != null && show.getFirstAired().length() > 3) {
      try {
        sr.setYear(Integer.parseInt(show.getFirstAired().substring(0, 4)));
      }
      catch (Exception ignored) {
      }
    }

    float score = MetadataUtil.calculateScore(searchString, show.getSeriesName());
    if (yearDiffers(options.getYear(), sr.getYear())) {
      float diff = (float) Math.abs(options.getYear() - sr.getYear()) / 100;
      LOGGER.debug("parsed year does not match search result year - downgrading score by " + diff);
      score -= diff;
    }
    sr.setScore(score);

    return sr;
  }

  private MediaMetadata getTvShowMetadata(MediaScrapeOptions options) throws Exception {
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
      show = tvdb.getSeries(id, options.getLanguage().getLanguage());
    }

    if (show == null) {
      return md;
    }

    // populate metadata
    md.setId(providerInfo.getId(), show.getId());
    md.setTitle(show.getSeriesName());
    md.setId(MediaMetadata.IMDB, show.getImdbId());
    md.setPlot(show.getOverview());

    try {
      md.setRuntime(Integer.valueOf(show.getRuntime()));
    }
    catch (NumberFormatException e) {
      md.setRuntime(0);
    }

    // Poster
    MediaArtwork ma = new MediaArtwork(providerInfo.getId(), MediaArtwork.MediaArtworkType.POSTER);
    ma.setPreviewUrl(show.getPoster());
    ma.setDefaultUrl(show.getPoster());
    ma.setLanguage(options.getLanguage().getLanguage());
    md.addMediaArt(ma);

    md.setRating(NumberUtils.toFloat(show.getRating()));
    md.setVoteCount(TvUtils.parseInt(show.getRatingCount()));

    try {
      md.setReleaseDate(StrgUtils.parseDate(show.getFirstAired()));
    }
    catch (ParseException ignored) {
    }

    try {
      Date date = StrgUtils.parseDate(show.getFirstAired());
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      int y = calendar.get(Calendar.YEAR);
      md.setYear(y);
      if (y != 0 && md.getTitle().contains(String.valueOf(y))) {
        LOGGER.debug("Weird TVDB entry - removing date " + y + " from title");
        String t = show.getSeriesName().replaceAll(String.valueOf(y), "").replaceAll("\\(\\)", "").trim();
        md.setTitle(t);
      }
    }
    catch (Exception e) {
    }

    md.setStatus(show.getStatus());
    md.addProductionCompany(show.getNetwork());

    // actors
    List<Actor> actors = new ArrayList<>();
    synchronized (tvdb) {
      actors.addAll(tvdb.getActors(id));
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

  private MediaMetadata getEpisodeMetadata(MediaScrapeOptions options) throws Exception {
    MediaMetadata md = new MediaMetadata(providerInfo.getId());

    boolean useDvdOrder = false;
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
      String option = options.getId(MediaMetadata.SEASON_NR);
      if (option != null) {
        seasonNr = Integer.parseInt(options.getId(MediaMetadata.SEASON_NR));
        episodeNr = Integer.parseInt(options.getId(MediaMetadata.EPISODE_NR));
      }
      else {
        seasonNr = Integer.parseInt(options.getId(MediaMetadata.SEASON_NR_DVD));
        episodeNr = Integer.parseInt(options.getId(MediaMetadata.EPISODE_NR_DVD));
        useDvdOrder = true;
      }
    }
    catch (Exception e) {
      LOGGER.warn("error parsing season/episode number");
    }

    String aired = "";
    if (options.getMetadata() != null && options.getMetadata().getReleaseDate() != null) {
      Format formatter = new SimpleDateFormat("yyyy-MM-dd");
      aired = formatter.format(options.getMetadata().getReleaseDate());
    }
    if (aired.isEmpty() && (seasonNr == -1 || episodeNr == -1)) {
      return md; // not even date set? return
    }

    List<Episode> episodes = new ArrayList<>();
    synchronized (tvdb) {
      // switched to getAllEpisodes for performance - only 1 request needed for
      // scraping multiple episodes of one tv show
      episodes.addAll(tvdb.getAllEpisodes(id, options.getLanguage().getLanguage()));
    }

    Episode episode = null;

    // filter out the matching episode
    for (Episode ep : episodes) {
      if (useDvdOrder) {
        int s = TvUtils.getSeasonNumber(ep.getDvdSeason());
        int e = TvUtils.getEpisodeNumber(ep.getDvdEpisodeNumber());
        if (s == seasonNr && e == episodeNr) {
          episode = ep;
          break;
        }
      }
      else {
        if (TvUtils.getSeasonNumber(ep.getSeasonNumber()) == seasonNr && TvUtils.getEpisodeNumber(ep.getEpisodeNumber()) == episodeNr) {
          episode = ep;
          break;
        }
      }

      // not found? try to match by date
      if (!aired.isEmpty() && ep.getFirstAired().equals(aired)) {
        episode = ep;
        break;
      }
    }

    if (episode == null) {
      return md;
    }

    md.setEpisodeNumber(TvUtils.getEpisodeNumber(episode.getEpisodeNumber()));
    md.setSeasonNumber(TvUtils.getSeasonNumber(episode.getSeasonNumber()));
    md.setDvdEpisodeNumber(TvUtils.getEpisodeNumber(episode.getDvdEpisodeNumber()));
    md.setDvdSeasonNumber(TvUtils.getSeasonNumber(episode.getDvdSeason()));
    md.setAbsoluteNumber(TvUtils.getEpisodeNumber(episode.getAbsoluteNumber()));

    md.setTitle(episode.getEpisodeName());
    md.setPlot(episode.getOverview());
    md.setRating(NumberUtils.toFloat(episode.getRating()));
    md.setVoteCount(TvUtils.parseInt(episode.getRatingCount()));

    try {
      md.setReleaseDate(StrgUtils.parseDate(episode.getFirstAired()));
    }
    catch (ParseException ignored) {
    }
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
      MediaArtwork ma = new MediaArtwork(providerInfo.getId(), MediaArtworkType.THUMB);
      ma.setPreviewUrl(episode.getFilename());
      ma.setDefaultUrl(episode.getFilename());
      md.addMediaArt(ma);
    }

    return md;
  }

  @Override
  public List<MediaArtwork> getArtwork(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getting artwork: " + options);
    List<MediaArtwork> artwork = new ArrayList<>();
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

    List<Banner> bannerList = null;
    switch (options.getArtworkType()) {
      case ALL:
        bannerList = new ArrayList<>(banners.getSeasonList());
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
    Collections.sort(bannerList, new BannerComparator(options.getLanguage().getLanguage()));

    // build output
    for (Banner banner : bannerList) {
      MediaArtwork ma = null;

      // set banner type
      switch (banner.getBannerType()) {
        case POSTER:
          ma = new MediaArtwork(providerInfo.getId(), MediaArtworkType.POSTER);
          break;

        case SERIES:
          ma = new MediaArtwork(providerInfo.getId(), MediaArtworkType.BANNER);
          break;

        case SEASON:
          if (banner.getBannerType2() == BannerType.SEASONWIDE) {
            // we do not use season wide banners at the moment
            continue;
          }

          ma = new MediaArtwork(providerInfo.getId(), MediaArtworkType.SEASON);
          ma.setSeason(banner.getSeason());
          break;

        case FANART:
        default:
          ma = new MediaArtwork(providerInfo.getId(), MediaArtworkType.BACKGROUND);
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

      ma.setDefaultUrl(banner.getUrl());
      if (StringUtils.isNotBlank(banner.getThumb())) {
        ma.setPreviewUrl(banner.getThumb());
      }
      else {
        ma.setPreviewUrl(banner.getUrl());
      }
      ma.setLanguage(banner.getLanguage());

      artwork.add(ma);
    }

    return artwork;
  }

  @Override
  public List<MediaEpisode> getEpisodeList(MediaScrapeOptions options) throws Exception {
    LOGGER.debug("getting episode list: " + options);
    List<MediaEpisode> episodes = new ArrayList<>();
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

    List<Episode> eps = new ArrayList<>();
    synchronized (tvdb) {
      // switched to getAllEpisodes for performance - only 1 request needed for
      // scraping multiple episodes of one tv show
      eps.addAll(tvdb.getAllEpisodes(id, options.getLanguage().getLanguage()));
    }

    for (Episode ep : eps) {
      MediaEpisode episode = new MediaEpisode(providerInfo.getId());
      episode.season = TvUtils.getSeasonNumber(ep.getSeasonNumber());
      episode.episode = TvUtils.getEpisodeNumber(ep.getEpisodeNumber());
      episode.dvdSeason = TvUtils.getSeasonNumber(ep.getDvdSeason());
      episode.dvdEpisode = TvUtils.getEpisodeNumber(ep.getDvdEpisodeNumber());

      episode.title = ep.getEpisodeName();
      episode.plot = ep.getOverview();
      episode.rating = NumberUtils.toFloat(ep.getRating());
      episode.voteCount = TvUtils.parseInt(ep.getRatingCount());
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
      MediaArtwork ma = new MediaArtwork(providerInfo.getId(), MediaArtworkType.THUMB);
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
    else if (genre.equals("Action")) {
      g = MediaGenres.ACTION;
    }
    else if (genre.equals("Adventure")) {
      g = MediaGenres.ADVENTURE;
    }
    else if (genre.equals("Animation")) {
      g = MediaGenres.ANIMATION;
    }
    else if (genre.equals("Children")) {
      g = MediaGenres.FAMILY;
    }
    else if (genre.equals("Comedy")) {
      g = MediaGenres.COMEDY;
    }
    else if (genre.equals("Crime")) {
      g = MediaGenres.CRIME;
    }
    else if (genre.equals("Documentary")) {
      g = MediaGenres.DOCUMENTARY;
    }
    else if (genre.equals("Drama")) {
      g = MediaGenres.DRAMA;
    }
    else if (genre.equals("Family")) {
      g = MediaGenres.FAMILY;
    }
    else if (genre.equals("Fantasy")) {
      g = MediaGenres.FANTASY;
    }
    else if (genre.equals("Food")) {
      g = MediaGenres.DOCUMENTARY;
    }
    else if (genre.equals("Game Show")) {
      g = MediaGenres.GAME_SHOW;
    }
    else if (genre.equals("Home and Garden")) {
      g = MediaGenres.DOCUMENTARY;
    }
    else if (genre.equals("Horror")) {
      g = MediaGenres.HORROR;
    }
    else if (genre.equals("Mini-Series")) {
      g = MediaGenres.SERIES;
    }
    else if (genre.equals("News")) {
      g = MediaGenres.NEWS;
    }
    else if (genre.equals("Reality")) {
      g = MediaGenres.REALITY_TV;
    }
    else if (genre.equals("Science-Fiction")) {
      g = MediaGenres.SCIENCE_FICTION;
    }
    else if (genre.equals("Soap")) {
      g = MediaGenres.SERIES;
    }
    else if (genre.equals("Special Interest")) {
      g = MediaGenres.INDIE;
    }
    else if (genre.equals("Sport")) {
      g = MediaGenres.SPORT;
    }
    else if (genre.equals("Suspense")) {
      g = MediaGenres.SUSPENSE;
    }
    else if (genre.equals("Talk Show")) {
      g = MediaGenres.TALK_SHOW;
    }
    else if (genre.equals("Thriller")) {
      g = MediaGenres.THRILLER;
    }
    else if (genre.equals("Travel")) {
      g = MediaGenres.HOLIDAY;
    }
    else if (genre.equals("Western")) {
      g = MediaGenres.WESTERN;
    }
    // @formatter:on
    if (g == null) {
      g = MediaGenres.getGenre(genre);
    }
    return g;
  }

  /**
   * Is i1 != i2 (when >0)
   */
  private boolean yearDiffers(Integer i1, Integer i2) {
    return i1 != null && i1 != 0 && i2 != null && i2 != 0 && i1 != i2;
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
