/*
 * Copyright 2012 - 2018 Manuel Laggner
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

import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.ALL;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.BACKGROUND;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.BANNER;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.POSTER;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_BANNER;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_POSTER;

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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaCastMember.CastType;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaRating;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.exceptions.UnsupportedMediaTypeException;
import org.tinymediamanager.scraper.http.TmmHttpClient;
import org.tinymediamanager.scraper.mediaprovider.ITvShowArtworkProvider;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.util.ApiKey;
import org.tinymediamanager.scraper.util.MetadataUtil;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.scraper.util.TvUtils;

import com.uwetrottmann.thetvdb.TheTvdb;
import com.uwetrottmann.thetvdb.entities.Actor;
import com.uwetrottmann.thetvdb.entities.ActorsResponse;
import com.uwetrottmann.thetvdb.entities.Episode;
import com.uwetrottmann.thetvdb.entities.EpisodeResponse;
import com.uwetrottmann.thetvdb.entities.EpisodesResponse;
import com.uwetrottmann.thetvdb.entities.Language;
import com.uwetrottmann.thetvdb.entities.LanguagesResponse;
import com.uwetrottmann.thetvdb.entities.Series;
import com.uwetrottmann.thetvdb.entities.SeriesImageQueryResult;
import com.uwetrottmann.thetvdb.entities.SeriesImageQueryResultResponse;
import com.uwetrottmann.thetvdb.entities.SeriesImagesQueryParam;
import com.uwetrottmann.thetvdb.entities.SeriesImagesQueryParamResponse;
import com.uwetrottmann.thetvdb.entities.SeriesResponse;
import com.uwetrottmann.thetvdb.entities.SeriesResultsResponse;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import okhttp3.OkHttpClient;

/**
 * The Class TheTvDbMetadataProvider.
 * 
 * @author Manuel Laggner
 */
@PluginImplementation
public class TheTvDbMetadataProvider implements ITvShowMetadataProvider, ITvShowArtworkProvider {
  private static final Logger     LOGGER      = LoggerFactory.getLogger(TheTvDbMetadataProvider.class);
  private static List<Language>   tvdbLanguages;
  private static String           artworkUrl  = "http://thetvdb.com/banners/";
  private static final String     TMM_API_KEY = ApiKey.decryptApikey("7bHHg4k0XhRERM8xd3l+ElhMUXOA5Ou4vQUEzYLGHt8=");

  private final MediaProviderInfo providerInfo;
  private TheTvdb                 tvdb;

  public TheTvDbMetadataProvider() {
    // create the providerinfo
    providerInfo = createMediaProviderInfo();
  }

  private synchronized void initAPI() throws ScrapeException {
    String apiKey = TMM_API_KEY;
    String userApiKey = providerInfo.getConfig().getValue("apiKey");

    // check if the API should change from current key to user key
    if (StringUtils.isNotBlank(userApiKey) && tvdb != null && !userApiKey.equals(tvdb.apiKey())) {
      tvdb = null;
      apiKey = userApiKey;
    }

    // check if the API should change from current key to tmm key
    if (StringUtils.isBlank(userApiKey) && tvdb != null && !TMM_API_KEY.equals(tvdb.apiKey())) {
      tvdb = null;
      apiKey = TMM_API_KEY;
    }

    if (tvdb == null) {
      try {
        tvdb = new TheTvdb(apiKey) {
          // tell the tvdb api to use our OkHttp client
          private OkHttpClient okHttpClient;

          @Override
          protected synchronized OkHttpClient okHttpClient() {
            if (this.okHttpClient == null) {
              OkHttpClient.Builder builder = TmmHttpClient.newBuilder();
              this.setOkHttpClientDefaults(builder);
              this.okHttpClient = builder.build();
            }

            return this.okHttpClient;
          }
        };
        LanguagesResponse response = tvdb.languages().allAvailable().execute().body();
        if (response == null) {
          throw new Exception("Could not connect to TVDB");
        }
        tvdbLanguages = response.data;
      }
      catch (Exception e) {
        LOGGER.warn("could not initialize API: " + e.getMessage());
        throw new ScrapeException(e);
      }
    }
  }

  private MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo("tvdb", "thetvdb.com",
        "<html><h3>The TV DB</h3><br />An open database for television fans. This scraper is able to scrape TV series metadata and artwork",
        TheTvDbMetadataProvider.class.getResource("/thetvdb_com.png"));
    providerInfo.setVersion(TheTvDbMetadataProvider.class);

    providerInfo.getConfig().addText("apiKey", "", true);

    ArrayList<String> fallbackLanguages = new ArrayList<>();
    for (MediaLanguages mediaLanguages : MediaLanguages.values()) {
      fallbackLanguages.add(mediaLanguages.toString());
    }
    providerInfo.getConfig().addSelect("fallbackLanguage", fallbackLanguages.toArray(new String[0]), MediaLanguages.en.toString());
    providerInfo.getConfig().load();

    return providerInfo;
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public MediaMetadata getMetadata(MediaScrapeOptions mediaScrapeOptions)
      throws ScrapeException, MissingIdException, UnsupportedMediaTypeException, NothingFoundException {
    // lazy initialization of the api
    initAPI();

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
  public List<MediaSearchResult> search(MediaSearchOptions options) throws ScrapeException, UnsupportedMediaTypeException {
    // lazy initialization of the api
    initAPI();

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
    searchString = cleanString(searchString);

    // return an empty search result if no query provided
    if (StringUtils.isEmpty(searchString)) {
      return results;
    }

    String language = options.getLanguage().getLanguage();
    String fallbackLanguage = MediaLanguages.get(providerInfo.getConfig().getValue("fallbackLanguage")).getLanguage();

    // search via the api; 2 times if the language of the options and fallback language differ
    List<Series> series = new ArrayList<>();
    // first with the desired scraping language
    try {
      SeriesResultsResponse response = tvdb.search().series(searchString, null, null, language).execute().body();
      series.addAll(response.data);
    }
    catch (Exception e) {
      LOGGER.error("problem getting data vom tvdb: " + e.getMessage());
    }

    // second with the fallback language
    if (!fallbackLanguage.equals(language)) {
      try {
        SeriesResultsResponse response = tvdb.search().series(searchString, null, null, fallbackLanguage).execute().body();
        series.addAll(response.data);
      }
      catch (Exception e) {
        LOGGER.error("problem getting data vom tvdb: " + e.getMessage());
      }
    }

    LOGGER.debug("found " + series.size() + " results with TMDB id");

    if (series.isEmpty()) {
      return results;
    }

    // make sure there are no duplicates (e.g. if a show has been found in both languages)
    Map<Integer, MediaSearchResult> resultMap = new HashMap<>();

    for (Series show : series) {
      // check if that show has already a result
      if (resultMap.containsKey(show.id)) {
        continue;
      }

      // build up a new result
      MediaSearchResult result = new MediaSearchResult(providerInfo.getId(), options.getMediaType());
      result.setId(show.id.toString());
      result.setTitle(show.seriesName);
      try {
        result.setYear(Integer.parseInt(show.firstAired.substring(0, 4)));
      }
      catch (Exception ignored) {
      }

      // do not get banners instead of posters
      // if (StringUtils.isNotBlank(show.banner)) {
      // result.setPosterUrl(artworkUrl + show.banner);
      // }

      // for how the api responds only a banner - we would like to have a poster here;
      // just try to fetch the poster url
      try {
        SeriesImageQueryResultResponse response = tvdb.series().imagesQuery(show.id, "poster", null, null, null).execute().body();
        if (response != null && !response.data.isEmpty()) {
          result.setPosterUrl(artworkUrl + response.data.get(0).fileName);
        }
      }
      catch (Exception e) {
        LOGGER.warn("could not get poster for search result: " + e.getMessage());
      }

      float score = MetadataUtil.calculateScore(searchString, show.seriesName);
      if (yearDiffers(options.getYear(), result.getYear())) {
        float diff = (float) Math.abs(options.getYear() - result.getYear()) / 100;
        LOGGER.debug("parsed year does not match search result year - downgrading score by " + diff);
        score -= diff;
      }
      result.setScore(score);

      // results.add(result);
      resultMap.put(show.id, result);
    }

    // and convert all entries from the map to a list
    results.addAll(resultMap.values());

    // sort
    Collections.sort(results);
    Collections.reverse(results);

    return results;
  }

  private MediaMetadata getTvShowMetadata(MediaScrapeOptions options) throws ScrapeException, MissingIdException, NothingFoundException {
    MediaMetadata md = new MediaMetadata(providerInfo.getId());
    Integer id = 0;

    // id from result
    if (options.getResult() != null) {
      try {
        id = Integer.parseInt(options.getResult().getId());
      }
      catch (Exception ignored) {
      }
    }

    // do we have an id from the options?
    if (id == 0) {
      id = options.getIdAsInteger(providerInfo.getId());
    }

    if (id == 0) {
      LOGGER.warn("no id available");
      throw new MissingIdException(providerInfo.getId());
    }

    Series show = null;
    try {
      SeriesResponse response = tvdb.series().series(id, options.getLanguage().getLanguage()).execute().body();
      show = response.data;
    }
    catch (NullPointerException e) {
      // do nothing here - just ignore it; retrofit returns null if something has gone wrong
      LOGGER.error("failed to get meta data - tvdb returned nothing");
    }
    catch (Exception e) {
      LOGGER.error("failed to get meta data: " + e.getMessage());
      throw new ScrapeException(e);
    }

    if (show == null) {
      throw new NothingFoundException();
    }

    // if there is no localized content and we have a fallback language, rescrape in the fallback language
    String fallbackLanguage = MediaLanguages.get(providerInfo.getConfig().getValue("fallbackLanguage")).getLanguage();
    if (StringUtils.isAnyBlank(show.seriesName, show.overview) && !fallbackLanguage.equals(options.getLanguage().getLanguage())) {
      try {
        SeriesResponse response = tvdb.series().series(id, fallbackLanguage).execute().body();
        Series fallBackShow = response.data;
        if (StringUtils.isBlank(show.seriesName) && StringUtils.isNotBlank(fallBackShow.seriesName)) {
          show.seriesName = fallBackShow.seriesName;
        }
        if (StringUtils.isBlank(show.overview) && StringUtils.isNotBlank(fallBackShow.overview)) {
          show.overview = fallBackShow.overview;
        }
      }
      catch (Exception e) {
        LOGGER.error("failed to get meta data: " + e.getMessage());
      }
    }

    // populate metadata
    md.setId(providerInfo.getId(), show.id);
    md.setTitle(show.seriesName);
    if (StringUtils.isNotBlank(show.imdbId)) {
      md.setId(MediaMetadata.IMDB, show.imdbId);
    }
    if (StringUtils.isNotBlank(show.zap2itId)) {
      md.setId("zap2it", show.zap2itId);
    }
    md.setPlot(show.overview);

    try {
      md.setRuntime(Integer.valueOf(show.runtime));
    }
    catch (NumberFormatException e) {
      md.setRuntime(0);
    }

    MediaRating rating = new MediaRating(getProviderInfo().getId());
    rating.setRating(show.siteRating);
    rating.setVoteCount(TvUtils.parseInt(show.siteRatingCount));
    rating.setMaxValue(10);
    md.addRating(rating);

    try {
      md.setReleaseDate(StrgUtils.parseDate(show.firstAired));
    }
    catch (ParseException ignored) {
    }

    try {
      Date date = StrgUtils.parseDate(show.firstAired);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      int y = calendar.get(Calendar.YEAR);
      md.setYear(y);
      if (y != 0 && md.getTitle().contains(String.valueOf(y))) {
        LOGGER.debug("Weird TVDB entry - removing date " + y + " from title");
        String t = show.seriesName.replaceAll(String.valueOf(y), "").replaceAll("\\(\\)", "").trim();
        md.setTitle(t);
      }
    }
    catch (Exception ignored) {
    }

    md.setStatus(show.status);
    md.addProductionCompany(show.network);

    List<Actor> actors = new ArrayList<>();
    try {
      ActorsResponse response = tvdb.series().actors(id).execute().body();
      actors.addAll(response.data);
    }
    catch (Exception e) {
      LOGGER.error("failed to get actors: " + e.getMessage());
    }

    for (Actor actor : actors) {
      MediaCastMember member = new MediaCastMember(CastType.ACTOR);
      member.setId(providerInfo.getId(), actor.id);
      member.setName(actor.name);
      member.setCharacter(actor.role);
      if (StringUtils.isNotBlank(actor.image)) {
        member.setImageUrl(artworkUrl + actor.image);
      }

      md.addCastMember(member);
    }

    md.addCertification(Certification.findCertification(show.rating));

    // genres
    for (String genreAsString : show.genre) {
      md.addGenre(MediaGenres.getGenre(genreAsString));
    }

    return md;
  }

  private MediaMetadata getEpisodeMetadata(MediaScrapeOptions options) throws ScrapeException, NothingFoundException, MissingIdException {
    MediaMetadata md = new MediaMetadata(providerInfo.getId());

    boolean useDvdOrder = false;
    Integer id = 0;

    // id from result
    if (options.getResult() != null) {
      try {
        id = Integer.parseInt(options.getResult().getId());
      }
      catch (Exception ignored) {
      }
    }

    // do we have an id from the options?
    if (id == 0) {
      id = options.getIdAsInteger(providerInfo.getId());
    }

    if (id == 0) {
      LOGGER.warn("no id available");
      throw new MissingIdException(providerInfo.getId());
    }

    // get episode number and season number
    int seasonNr = options.getIdAsIntOrDefault(MediaMetadata.SEASON_NR, -1);
    int episodeNr = options.getIdAsIntOrDefault(MediaMetadata.EPISODE_NR, -1);

    if (seasonNr == -1 || episodeNr == -1) {
      seasonNr = options.getIdAsIntOrDefault(MediaMetadata.SEASON_NR_DVD, -1);
      episodeNr = options.getIdAsIntOrDefault(MediaMetadata.EPISODE_NR_DVD, -1);
      useDvdOrder = true;
    }

    String aired = "";
    if (options.getMetadata() != null && options.getMetadata().getReleaseDate() != null) {
      Format formatter = new SimpleDateFormat("yyyy-MM-dd");
      aired = formatter.format(options.getMetadata().getReleaseDate());
    }
    if (aired.isEmpty() && (seasonNr == -1 || episodeNr == -1)) {
      LOGGER.warn("no aired date/season number/episode number found");
      throw new MissingIdException(MediaMetadata.EPISODE_NR, MediaMetadata.SEASON_NR);
    }

    // get the episode data in desired language
    Episode.FullEpisode episode = null;
    try {
      episode = getFullEpisode(options.getLanguage().getLanguage(), useDvdOrder, id, seasonNr, episodeNr, aired);
    }
    catch (NullPointerException ignored) {
      // do nothing here - just ignore it; retrofit returns null if something has gone wrong
      LOGGER.error("failed to get meta data - tvdb returned nothing");
    }
    catch (Exception e) {
      LOGGER.error("failed to get meta data: " + e.getMessage());
      throw new ScrapeException(e);
    }
    if (episode == null) {
      throw new NothingFoundException();
    }

    // if there is no localized content and we have a fallback language, rescrape in the fallback language
    String fallbackLanguage = MediaLanguages.get(providerInfo.getConfig().getValue("fallbackLanguage")).getLanguage();
    if (StringUtils.isAnyBlank(episode.episodeName, episode.overview) && !fallbackLanguage.equals(options.getLanguage().getLanguage())) {
      try {
        Episode.FullEpisode episodeFallback = getFullEpisode(fallbackLanguage, useDvdOrder, id, seasonNr, episodeNr, aired);
        if (episodeFallback != null) {
          if (StringUtils.isBlank(episode.episodeName) && StringUtils.isNotBlank(episodeFallback.episodeName)) {
            episode.episodeName = episodeFallback.episodeName;
          }
          if (StringUtils.isBlank(episode.overview) && StringUtils.isNotBlank(episodeFallback.overview)) {
            episode.overview = episodeFallback.overview;
          }
        }
      }
      catch (Exception e) {
        LOGGER.warn("could not fetch localized content: " + e.getMessage());
      }
    }

    md.setEpisodeNumber(TvUtils.getEpisodeNumber(episode.airedEpisodeNumber));
    md.setSeasonNumber(TvUtils.getSeasonNumber(episode.airedSeason));
    md.setDvdEpisodeNumber(TvUtils.getEpisodeNumber(episode.dvdEpisodeNumber));
    md.setDvdSeasonNumber(TvUtils.getSeasonNumber(episode.dvdSeason));
    md.setAbsoluteNumber(TvUtils.getEpisodeNumber(episode.absoluteNumber));

    md.setTitle(episode.episodeName);
    md.setPlot(episode.overview);

    MediaRating rating = new MediaRating(getProviderInfo().getId());
    rating.setRating(episode.siteRating);
    rating.setVoteCount(TvUtils.parseInt(episode.siteRatingCount));
    rating.setMaxValue(10);
    md.addRating(rating);

    try {
      md.setReleaseDate(StrgUtils.parseDate(episode.firstAired));
    }
    catch (ParseException ignored) {
    }
    md.setId(providerInfo.getId(), episode.id);
    if (StringUtils.isNotBlank(episode.imdbId)) {
      md.setId(MediaMetadata.IMDB, episode.imdbId);
    }

    // directors
    for (String director : episode.directors) {
      MediaCastMember cm = new MediaCastMember(CastType.DIRECTOR);
      cm.setName(director);
      md.addCastMember(cm);
    }

    // writers
    for (String writer : episode.writers) {
      MediaCastMember cm = new MediaCastMember(CastType.WRITER);
      cm.setName(writer);
      md.addCastMember(cm);
    }

    // actors (guests?)
    for (String guest : episode.guestStars) {
      MediaCastMember cm = new MediaCastMember(CastType.ACTOR);
      cm.setName(guest);
      md.addCastMember(cm);
    }

    // Thumb
    if (StringUtils.isNotBlank(episode.filename) && options.getArtworkType() == ALL || options.getArtworkType() == MediaArtworkType.THUMB) {
      MediaArtwork ma = new MediaArtwork(providerInfo.getId(), MediaArtworkType.THUMB);
      ma.setPreviewUrl(artworkUrl + episode.filename);
      ma.setDefaultUrl(artworkUrl + episode.filename);
      md.addMediaArt(ma);
    }

    return md;
  }

  private Episode.FullEpisode getFullEpisode(String language, boolean useDvdOrder, Integer id, int seasonNr, int episodeNr, String aired)
      throws Exception {
    EpisodesResponse response = null;

    // get by season/ep number
    if (useDvdOrder) {
      response = tvdb.series().episodesQuery(id, null, null, null, seasonNr, (double) episodeNr, null, null, 1, language).execute().body();
    }
    else {
      response = tvdb.series().episodesQuery(id, null, seasonNr, episodeNr, null, null, null, null, 1, language).execute().body();
    }

    // not found? try to match by date
    if (response == null && !aired.isEmpty()) {
      response = tvdb.series().episodesQuery(id, null, null, null, null, null, null, aired, 1, language).execute().body();
    }

    if (response != null && !response.data.isEmpty()) {
      EpisodeResponse response1 = tvdb.episodes().get(response.data.get(0).id, language).execute().body();
      return response1.data;
    }

    return null;
  }

  @Override
  public List<MediaArtwork> getArtwork(MediaScrapeOptions options) throws ScrapeException, MissingIdException {
    // lazy initialization of the api
    initAPI();

    LOGGER.debug("getting artwork: " + options);
    List<MediaArtwork> artwork = new ArrayList<>();
    Integer id = 0;

    // id from result
    if (options.getResult() != null) {
      try {
        id = Integer.parseInt(options.getResult().getId());
      }
      catch (Exception ignored) {
      }
    }

    // do we have an id from the options?
    if (id == 0) {
      id = options.getIdAsInteger(providerInfo.getId());
    }

    if (id == null || id == 0) {
      LOGGER.warn("no id available");
      throw new MissingIdException(providerInfo.getId());
    }

    // get artwork from thetvdb
    List<SeriesImageQueryResult> images = new ArrayList<>();
    try {
      // get all types of artwork we can get
      SeriesImagesQueryParamResponse response = tvdb.series().imagesQueryParams(id).execute().body();
      for (SeriesImagesQueryParam param : response.data) {
        if (options.getArtworkType() == ALL || ("fanart".equals(param.keyType) && options.getArtworkType() == BACKGROUND)
            || ("poster".equals(param.keyType) && options.getArtworkType() == POSTER)
            || ("season".equals(param.keyType) && options.getArtworkType() == SEASON_POSTER)
            || ("seasonwide".equals(param.keyType) && options.getArtworkType() == SEASON_BANNER)
            || ("series".equals(param.keyType) && options.getArtworkType() == BANNER)) {
          SeriesImageQueryResultResponse response1 = tvdb.series().imagesQuery(id, param.keyType, null, null, null).execute().body();
          images.addAll(response1.data);
        }
      }
    }
    catch (NullPointerException e) {
      // do nothing here - just ignore it; retrofit returns null if something has gone wrong
      LOGGER.error("failed to get artwork - tvdb returned nothing");
    }
    catch (Exception e) {
      LOGGER.error("failed to get artwork: " + e.getMessage());
      throw new ScrapeException(e);
    }

    if (images.isEmpty()) {
      return artwork;
    }

    // sort it
    images.sort(new ImageComparator(options.getLanguage().getLanguage()));

    // build output
    for (SeriesImageQueryResult image : images) {
      MediaArtwork ma = null;

      // set artwork type
      switch (image.keyType) {
        case "fanart":
          ma = new MediaArtwork(providerInfo.getId(), BACKGROUND);
          break;

        case "poster":
          ma = new MediaArtwork(providerInfo.getId(), POSTER);
          break;

        case "season":
          ma = new MediaArtwork(providerInfo.getId(), SEASON_POSTER);
          try {
            ma.setSeason(Integer.parseInt(image.subKey));
          }
          catch (Exception e) {
            LOGGER.warn("could not parse season: " + image.subKey);
          }
          break;

        case "seasonwide":
          ma = new MediaArtwork(providerInfo.getId(), SEASON_BANNER);
          try {
            ma.setSeason(Integer.parseInt(image.subKey));
          }
          catch (Exception e) {
            LOGGER.warn("could not parse season: " + image.subKey);
          }
          break;

        case "series":
          ma = new MediaArtwork(providerInfo.getId(), BANNER);
          break;

        default:
          continue;
      }

      // extract image sizes
      if (StringUtils.isNotBlank(image.resolution)) {
        try {
          Pattern pattern = Pattern.compile("([0-9]{3,4})x([0-9]{3,4})");
          Matcher matcher = pattern.matcher(image.resolution);
          if (matcher.matches() && matcher.groupCount() > 1) {
            int width = Integer.parseInt(matcher.group(1));
            int height = Integer.parseInt(matcher.group(2));
            ma.addImageSize(width, height, artworkUrl + image.fileName);

            // set image size
            switch (ma.getType()) {
              case POSTER:
                if (width >= 1000) {
                  ma.setSizeOrder(MediaArtwork.PosterSizes.LARGE.getOrder());
                }
                else if (width >= 500) {
                  ma.setSizeOrder(MediaArtwork.PosterSizes.BIG.getOrder());
                }
                else if (width >= 342) {
                  ma.setSizeOrder(MediaArtwork.PosterSizes.MEDIUM.getOrder());
                }
                else {
                  ma.setSizeOrder(MediaArtwork.PosterSizes.SMALL.getOrder());
                }
                break;

              case BACKGROUND:
                if (width >= 3840) {
                  ma.setSizeOrder(MediaArtwork.FanartSizes.XLARGE.getOrder());
                }
                if (width >= 1920) {
                  ma.setSizeOrder(MediaArtwork.FanartSizes.LARGE.getOrder());
                }
                else if (width >= 1280) {
                  ma.setSizeOrder(MediaArtwork.FanartSizes.MEDIUM.getOrder());
                }
                else {
                  ma.setSizeOrder(MediaArtwork.FanartSizes.SMALL.getOrder());
                }
                break;

              default:
                break;
            }
          }
        }
        catch (Exception e) {
          LOGGER.debug("could not extract size from artwork: " + image.resolution);
        }
      }

      // set size for banner & season poster (resolution not in api)
      if (ma.getType() == SEASON_BANNER || ma.getType() == SEASON_POSTER) {
        ma.setSizeOrder(MediaArtwork.FanartSizes.LARGE.getOrder());
      }
      else if (ma.getType() == BANNER) {
        ma.setSizeOrder(MediaArtwork.FanartSizes.MEDIUM.getOrder());
      }

      ma.setDefaultUrl(artworkUrl + image.fileName);
      if (StringUtils.isNotBlank(image.thumbnail)) {
        ma.setPreviewUrl(artworkUrl + image.thumbnail);
      }
      else {
        ma.setPreviewUrl(ma.getDefaultUrl());
      }

      // ma.setLanguage(banner.getLanguage());

      artwork.add(ma);
    }

    return artwork;
  }

  @Override
  public List<MediaMetadata> getEpisodeList(MediaScrapeOptions options) throws ScrapeException, MissingIdException {
    // lazy initialization of the api
    initAPI();

    LOGGER.debug("getting episode list: " + options);
    List<MediaMetadata> episodes = new ArrayList<>();
    Integer id = 0;

    // id from result
    if (options.getResult() != null) {
      try {
        id = Integer.parseInt(options.getResult().getId());
      }
      catch (Exception ignored) {
      }
    }

    // do we have an id from the options?
    if (id == 0) {
      id = options.getIdAsInteger(providerInfo.getId());
    }

    if (id == 0) {
      LOGGER.warn("no id available");
      throw new MissingIdException(providerInfo.getId());
    }

    List<Episode> eps = new ArrayList<>();
    List<Episode> fallbackEps = new ArrayList<>();
    try {
      String fallbackLanguage = MediaLanguages.get(providerInfo.getConfig().getValue("fallbackLanguage")).getLanguage();

      // 100 results per page
      int counter = 1;
      while (true) {
        EpisodesResponse response = tvdb.series().episodes(id, counter, options.getLanguage().getLanguage()).execute().body();

        // and get the episode listing in the fallback language too
        if (!fallbackLanguage.equals(options.getLanguage().getLanguage())) {
          EpisodesResponse responseFallback = tvdb.series().episodes(id, counter, fallbackLanguage).execute().body();
          fallbackEps.addAll(responseFallback.data);
        }

        eps.addAll(response.data);
        if (response.data.size() < 100) {
          break;
        }

        counter++;
      }
    }
    catch (NullPointerException e) {
      LOGGER.error("failed to get episode list - tvdb returned nothing");
      // do nothing here - just ignore it; retrofit returns null if something has gone wrong
      return episodes;
    }
    catch (Exception e) {
      LOGGER.error("failed to get episode list: " + e.getMessage());
      throw new ScrapeException(e);
    }

    // build the fallback language episode map for faster lookup
    Map<String, Episode> fallbackEpsMap = new HashMap<>();
    for (Episode ep : fallbackEps) {
      fallbackEpsMap.put("S" + ep.airedSeason + "E" + ep.airedEpisodeNumber, ep);
    }

    for (Episode ep : eps) {
      MediaMetadata episode = new MediaMetadata(providerInfo.getId());

      episode.setId(providerInfo.getId(), ep.id);
      episode.setSeasonNumber(TvUtils.getSeasonNumber(ep.airedSeason));
      episode.setEpisodeNumber(TvUtils.getEpisodeNumber(ep.airedEpisodeNumber));
      episode.setDvdSeasonNumber(TvUtils.getSeasonNumber(ep.dvdSeason));
      episode.setDvdEpisodeNumber(TvUtils.getEpisodeNumber(ep.dvdEpisodeNumber));

      Episode fallbackEpisode = fallbackEpsMap.get("S" + ep.airedSeason + "E" + ep.airedEpisodeNumber);
      if (StringUtils.isNotBlank(ep.episodeName)) {
        episode.setTitle(ep.episodeName);
      }
      else if (fallbackEpisode != null && StringUtils.isNotBlank(fallbackEpisode.episodeName)) {
        episode.setTitle(fallbackEpisode.episodeName);
      }

      if (StringUtils.isNotBlank(ep.overview)) {
        episode.setPlot(ep.overview);
      }
      else if (fallbackEpisode != null && StringUtils.isNotBlank(fallbackEpisode.overview)) {
        episode.setPlot(fallbackEpisode.overview);
      }

      try {
        episode.setReleaseDate(StrgUtils.parseDate(ep.firstAired));
      }
      catch (Exception ignored) {
      }

      episodes.add(episode);
    }

    return episodes;
  }

  /**
   * Is i1 != i2 (when >0)
   */
  private boolean yearDiffers(Integer i1, Integer i2) {
    return i1 != null && i1 != 0 && i2 != null && i2 != 0 && i1 != i2;
  }

  private String cleanString(String oldString) {
    if (StringUtils.isEmpty(oldString)) {
      return "";
    }
    // remove semicolons (for searies like "ChäoS;Child" or "Steins;Gate")
    String newString = oldString.replaceAll(";", "");

    return newString.trim();
  }

  /**********************************************************************
   * local helper classes
   **********************************************************************/
  private static class ImageComparator implements Comparator<SeriesImageQueryResult> {
    private int preferredLangu = 0;
    private int english        = 0;

    private ImageComparator(String language) {
      for (Language lang : tvdbLanguages) {
        if (language.equals(lang.abbreviation)) {
          preferredLangu = lang.id;
        }
        if ("en".equals(lang.abbreviation)) {
          english = lang.id;
        }
      }
    }

    /*
     * sort artwork: primary by language: preferred lang (ie de), en, others; then: score
     */
    @Override
    public int compare(SeriesImageQueryResult arg0, SeriesImageQueryResult arg1) {
      // check if first image is preferred langu

      // FIXME deactivated until tvdb add this in their API responses
      // if (arg0.languageId == preferredLangu && arg1.languageId != preferredLangu) {
      // return -1;
      // }
      //
      // // check if second image is preferred langu
      // if (arg0.languageId != preferredLangu && arg1.languageId == preferredLangu) {
      // return 1;
      // }
      //
      // // check if the first image is en
      // if (arg0.languageId == english && arg1.languageId != english) {
      // return -1;
      // }
      //
      // // check if the second image is en
      // if (arg0.languageId != english && arg1.languageId == english) {
      // return 1;
      // }

      // swap arg0 and arg1 to sort reverse
      int result = Double.compare(arg1.ratingsInfo.average, arg0.ratingsInfo.average);

      // equal rating; sort by votes
      if (result == 0) {
        result = Integer.compare(arg1.ratingsInfo.count, arg0.ratingsInfo.count);
      }

      return result;
    }
  }
}
