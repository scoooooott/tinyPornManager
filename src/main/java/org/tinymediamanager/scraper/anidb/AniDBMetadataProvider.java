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
package org.tinymediamanager.scraper.anidb;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.tvshow.TvShowEpisodeSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.scraper.ArtworkSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.http.InMemoryCachedUrl;
import org.tinymediamanager.scraper.http.OnDiskCachedUrl;
import org.tinymediamanager.scraper.http.Url;
import org.tinymediamanager.scraper.interfaces.IMediaArtworkProvider;
import org.tinymediamanager.scraper.interfaces.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.util.RingBuffer;
import org.tinymediamanager.scraper.util.Similarity;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.scraper.util.UrlUtil;

/**
 * The class AnimeDBMetadataProvider - a metadata provider for ANIME (AniDB) https://wiki.anidb.net/w/UDP_API_Definition
 *
 * @author Manuel Laggner
 */
public class AniDBMetadataProvider implements ITvShowMetadataProvider, IMediaArtworkProvider {
  public static final String               ID                = "anidb";
  private static final Logger              LOGGER            = LoggerFactory.getLogger(AniDBMetadataProvider.class);
  private static final String              IMAGE_SERVER      = "http://img7.anidb.net/pics/anime/";
  // flood: pager every 2 seconds
  // protection: https://wiki.anidb.net/w/HTTP_API_Definition
  private static final RingBuffer<Long>    connectionCounter = new RingBuffer<>(1);
  private static MediaProviderInfo         providerInfo      = createMediaProviderInfo();

  private HashMap<String, List<AniDBShow>> showsForLookup    = new HashMap<>();

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo(ID, "aniDB",
        "<html><h3>aniDB</h3><br />AniDB stands for Anime DataBase. AniDB is a non-profit anime database that is open freely to the public.</html>",
        AniDBMetadataProvider.class.getResource("/org/tinymediamanager/scraper/anidb_net.png"));

    // configure/load settings
    providerInfo.getConfig().addInteger("numberOfTags", 10);
    providerInfo.getConfig().addInteger("minimumTagsWeight", 200);
    providerInfo.getConfig().load();

    return providerInfo;
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public MediaMetadata getMetadata(TvShowSearchAndScrapeOptions options) throws ScrapeException, MissingIdException, NothingFoundException {
    LOGGER.debug("getMetadata(): {}", options);
    MediaMetadata md = new MediaMetadata(providerInfo.getId());
    String langu = options.getLanguage().getLanguage();

    // do we have an id from the options?
    String id = options.getIdAsString(providerInfo.getId());

    if (StringUtils.isEmpty(id)) {
      throw new MissingIdException("anidb");
    }

    // call API
    // http://api.anidb.net:9001/httpapi?request=anime&client=tinymediamanager&clientver=2&protover=1&aid=4242
    Document doc;

    InMemoryCachedUrl cachedUrl;
    try {
      cachedUrl = new InMemoryCachedUrl("http://api.anidb.net:9001/httpapi?request=anime&client=tinymediamanager&clientver=2&protover=1&aid=" + id);
    }
    catch (Exception e) {
      LOGGER.error("failed to get TV show metadata: {}", e.getMessage());
      throw new ScrapeException(e);
    }

    try {
      trackConnections();
      try (InputStream is = cachedUrl.getInputStream()) {
        doc = Jsoup.parse(is, UrlUtil.UTF_8, "", Parser.xmlParser());
      }
    }
    catch (InterruptedException | InterruptedIOException e) {
      // do not swallow these Exceptions
      Thread.currentThread().interrupt();
      return null;
    }
    catch (Exception e) {
      throw new ScrapeException(e);
    }

    if (doc == null || doc.children().isEmpty()) {
      throw new NothingFoundException();
    }

    md.setId(providerInfo.getId(), id);

    Element anime = doc.child(0);

    for (Element e : anime.children()) {
      if ("startdate".equalsIgnoreCase(e.tagName())) {
        try {
          Date date = StrgUtils.parseDate(e.text());
          md.setReleaseDate(date);

          Calendar calendar = Calendar.getInstance();
          calendar.setTime(date);
          md.setYear(calendar.get(Calendar.YEAR));
        }
        catch (ParseException ex) {
          LOGGER.debug("could not parse date: {}", e.text());
        }
      }

      if ("titles".equalsIgnoreCase(e.tagName())) {
        parseTitle(md, langu, e);
      }

      if ("description".equalsIgnoreCase(e.tagName())) {
        md.setPlot(e.text());
      }

      if ("ratings".equalsIgnoreCase(e.tagName())) {
        getRating(md, e);
      }

      if ("tags".equalsIgnoreCase(e.tagName())) {
        getTags(md, e);
      }

      if ("picture".equalsIgnoreCase(e.tagName())) {
        // Poster
        MediaArtwork ma = new MediaArtwork(providerInfo.getId(), MediaArtwork.MediaArtworkType.POSTER);
        ma.setPreviewUrl(IMAGE_SERVER + e.text());
        ma.setDefaultUrl(IMAGE_SERVER + e.text());
        ma.setLanguage(options.getLanguage().getLanguage());
        md.addMediaArt(ma);
      }

      if ("characters".equalsIgnoreCase(e.tagName())) {
        getActors(md, e);
      }

    }

    // add static "Anime" genre
    md.addGenre(MediaGenres.ANIME);

    return md;
  }

  @Override
  public MediaMetadata getMetadata(TvShowEpisodeSearchAndScrapeOptions options) throws ScrapeException, MissingIdException, NothingFoundException {
    LOGGER.debug("getMetadata(): {}", options);
    MediaMetadata md = null;

    // get episode number and season number
    int seasonNr = options.getIdAsIntOrDefault(MediaMetadata.SEASON_NR, -1);
    int episodeNr = options.getIdAsIntOrDefault(MediaMetadata.EPISODE_NR, -1);

    if (seasonNr == -1 || episodeNr == -1) {
      throw new MissingIdException(MediaMetadata.SEASON_NR, MediaMetadata.EPISODE_NR);
    }

    // get full episode listing
    List<MediaMetadata> episodes = getEpisodeList(options);

    // filter out the wanted episode
    for (MediaMetadata episode : episodes) {
      if (episode.getEpisodeNumber() == episodeNr && episode.getSeasonNumber() == seasonNr) {
        md = episode;
        break;
      }
    }

    if (md == null) {
      throw new NothingFoundException();
    }

    return md;
  }

  private void getActors(MediaMetadata md, Element e) {
    for (Element character : e.children()) {
      Person member = new Person(Person.Type.ACTOR);
      for (Element characterInfo : character.children()) {
        if ("name".equalsIgnoreCase(characterInfo.tagName())) {
          member.setRole(characterInfo.text());
        }
        if ("seiyuu".equalsIgnoreCase(characterInfo.tagName())) {
          member.setName(characterInfo.text());
          String image = characterInfo.attr("picture");
          if (StringUtils.isNotBlank(image)) {
            member.setThumbUrl("http://img7.anidb.net/pics/anime/" + image);
          }
        }
      }
      md.addCastMember(member);
    }
  }

  private void getRating(MediaMetadata md, Element e) {
    for (Element rating : e.children()) {
      if ("temporary".equalsIgnoreCase(rating.tagName())) {
        try {
          MediaRating mediaRating = new MediaRating("anidb");
          mediaRating.setRating(Float.parseFloat(rating.text()));
          mediaRating.setVotes(Integer.parseInt(rating.attr("count")));
          mediaRating.setMaxValue(10);
          md.addRating(mediaRating);
          break;
        }
        catch (NumberFormatException ex) {
          LOGGER.debug("could not rating: {} - {}", rating.text(), rating.attr("count"));
        }
      }
    }
  }

  private void getTags(MediaMetadata md, Element e) {
    Integer maxTags = providerInfo.getConfig().getValueAsInteger("numberOfTags");
    Integer minWeight = providerInfo.getConfig().getValueAsInteger("minimumTagsWeight");
    for (Element tag : e.children()) {
      Element name = tag.getElementsByTag("name").first();
      int weight = 0;
      try {
        weight = Integer.parseInt(tag.attr("weight"));
      }
      catch (Exception ex) {
        LOGGER.trace("Could not parse tags weight: {}", ex.getMessage());
      }
      if (name != null && weight >= minWeight) {
        md.addTag(name.text());
        if (md.getTags().size() >= maxTags) {
          break;
        }
      }
    }
  }

  private void parseTitle(MediaMetadata md, String langu, Element e) {
    String titleEN = "";
    String titleScraperLangu = "";
    String titleFirst = "";
    for (Element title : e.children()) {
      // store first title if neither the requested one nor the english one
      // available
      if (StringUtils.isBlank(titleFirst)) {
        titleFirst = title.text();
      }

      // store the english one for fallback
      if ("en".equalsIgnoreCase(title.attr("xml:lang"))) {
        titleEN = title.text();
      }

      // search for the requested one
      if (langu.equalsIgnoreCase(title.attr("xml:lang"))) {
        titleScraperLangu = title.text();
      }

    }

    if (StringUtils.isNotBlank(titleScraperLangu)) {
      md.setTitle(titleScraperLangu);
    }
    else if (StringUtils.isNotBlank(titleEN)) {
      md.setTitle(titleEN);
    }
    else {
      md.setTitle(titleFirst);
    }
  }

  private List<Episode> parseEpisodes(Document doc) {
    List<Episode> episodes = new ArrayList<>();

    Element anime = doc.child(0);
    Element eps = null;
    // find the "episodes" child
    for (Element e : anime.children()) {
      if ("episodes".equalsIgnoreCase(e.tagName())) {
        eps = e;
        break;
      }
    }

    if (eps == null) {
      return episodes;
    }

    for (Element e : eps.children()) {
      // filter out the desired episode
      if ("episode".equals(e.tagName())) {
        Episode episode = new Episode();
        try {
          episode.id = Integer.parseInt(e.attr("id"));
        }
        catch (NumberFormatException ignored) {
        }
        for (Element episodeInfo : e.children()) {
          if ("epno".equalsIgnoreCase(episodeInfo.tagName())) {
            try {
              // looks like anidb is storing anything in a single season, so put
              // 1 to season, if type = 1
              if ("1".equals(episodeInfo.attr("type"))) {
                episode.season = 1;
                episode.episode = Integer.parseInt(episodeInfo.text());
              }
              else {
                // else - we see them as "specials"
                episode.season = 0;
                episode.episode = Integer.parseInt(episodeInfo.text().replaceAll("[^0-9]+", ""));
              }

            }
            catch (NumberFormatException ignored) {
            }
            continue;
          }

          if ("length".equalsIgnoreCase(episodeInfo.tagName())) {
            try {
              episode.runtime = Integer.parseInt(episodeInfo.text());
            }
            catch (NumberFormatException ignored) {
            }
            continue;
          }

          if ("airdate".equalsIgnoreCase(episodeInfo.tagName())) {
            try {
              episode.airdate = StrgUtils.parseDate(episodeInfo.text());
            }
            catch (Exception ignored) {
            }
            continue;
          }

          if ("rating".equalsIgnoreCase(episodeInfo.tagName())) {
            try {
              episode.rating = Float.parseFloat(episodeInfo.text());
              episode.votes = Integer.parseInt(episodeInfo.attr("votes"));
            }
            catch (NumberFormatException ignored) {
            }
            continue;
          }

          if ("title".equalsIgnoreCase(episodeInfo.tagName())) {
            try {
              episode.titles.put(episodeInfo.attr("xml:lang").toLowerCase(Locale.ROOT), episodeInfo.text());
            }
            catch (Exception ignored) {
            }
            continue;
          }

          if ("summary".equalsIgnoreCase(episodeInfo.tagName())) {
            episode.summary = episodeInfo.text();
            continue;
          }
        }
        episodes.add(episode);
      }
    }

    return episodes;
  }

  @Override
  public SortedSet<MediaSearchResult> search(TvShowSearchAndScrapeOptions options) {
    LOGGER.debug("search(): {}", options);

    synchronized (AniDBMetadataProvider.class) {
      // first run: build up the anime name list
      if (showsForLookup.isEmpty()) {
        buildTitleHashMap();
      }
    }

    SortedSet<MediaSearchResult> results = new TreeSet<>();

    // detect the string to search
    String searchString = "";
    if (StringUtils.isNotEmpty(options.getSearchQuery())) {
      searchString = options.getSearchQuery();
    }

    // return an empty search result if no query provided
    if (StringUtils.isEmpty(searchString)) {
      return results;
    }

    List<Integer> foundIds = new ArrayList<>();
    for (Entry<String, List<AniDBShow>> entry : showsForLookup.entrySet()) {
      String title = entry.getKey();
      float score = Similarity.compareStrings(title, searchString);
      if (score > 0.4) {
        for (AniDBShow show : entry.getValue()) {
          if (!foundIds.contains(show.aniDbId)) {
            MediaSearchResult result = new MediaSearchResult(providerInfo.getId(), MediaType.TV_SHOW);
            result.setId(String.valueOf(show.aniDbId));
            result.setTitle(show.title);
            result.setScore(score);
            results.add(result);
            foundIds.add(show.aniDbId);
          }
        }
      }
    }

    return results;
  }

  @Override
  public List<MediaMetadata> getEpisodeList(TvShowSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    return _getEpisodeList(options);
  }

  @Override
  public List<MediaMetadata> getEpisodeList(TvShowEpisodeSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    return _getEpisodeList(options);
  }

  private List<MediaMetadata> _getEpisodeList(MediaSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    List<MediaMetadata> episodes = new ArrayList<>();
    String langu = options.getLanguage().getLanguage();

    // do we have an id from the options?
    String id = options.getIdAsString(providerInfo.getId());

    if (StringUtils.isEmpty(id)) {
      throw new MissingIdException(providerInfo.getId());
    }

    Document doc = null;

    InMemoryCachedUrl url;
    try {
      url = new InMemoryCachedUrl("http://api.anidb.net:9001/httpapi?request=anime&client=tinymediamanager&clientver=2&protover=1&aid=" + id);
    }
    catch (Exception e) {
      LOGGER.error("error getting episode list: {}", e.getMessage());
      throw new ScrapeException(e);
    }

    try {
      trackConnections();
      try (InputStream is = url.getInputStream()) {
        doc = Jsoup.parse(is, UrlUtil.UTF_8, "", Parser.xmlParser());
      }
    }
    catch (InterruptedException | InterruptedIOException e) {
      // do not swallow these Exceptions
      Thread.currentThread().interrupt();
    }
    catch (Exception e) {
      LOGGER.error("error getting episode list: {}", e.getMessage());
      throw new ScrapeException(e);
    }

    if (doc == null || doc.children().isEmpty()) {
      return episodes;
    }

    // filter out the episode
    for (Episode ep : parseEpisodes(doc)) {
      MediaMetadata md = new MediaMetadata(getProviderInfo().getId());
      md.setTitle(ep.titles.get(langu));
      md.setSeasonNumber(ep.season);
      md.setEpisodeNumber(ep.episode);

      if (StringUtils.isBlank(md.getTitle())) {
        md.setTitle(ep.titles.get("en"));
      }
      if (StringUtils.isBlank(md.getTitle())) {
        md.setTitle(ep.titles.get("x-jat"));
      }

      md.setPlot(ep.summary);

      if (ep.rating > 0) {
        MediaRating rating = new MediaRating(getProviderInfo().getId());
        rating.setRating(ep.rating);
        rating.setVotes(ep.votes);
        rating.setMaxValue(10);
        md.addRating(rating);
      }
      md.setRuntime(ep.runtime);
      md.setReleaseDate(ep.airdate);
      md.setId(providerInfo.getId(), ep.id);
      episodes.add(md);
    }

    return episodes;
  }

  /*
   * build up the hashmap for a fast title search
   */
  private void buildTitleHashMap() {
    // <aid>|<type>|<language>|<title>
    // type:
    // 1=primary title (one per anime),
    // 2=synonyms (multiple per anime),
    // 3=shorttitles (multiple per anime),
    // 4=official title (one per language)
    Pattern pattern = Pattern.compile("^(?!#)(\\d+)[|](\\d)[|]([\\w-]+)[|](.+)$");

    // we are only allowed to fetch this file once per 24 hrs
    // see https://wiki.anidb.net/w/API#Anime_Titles
    Url animeList;

    try {
      animeList = new OnDiskCachedUrl("http://anidb.net/api/anime-titles.dat.gz", 2, TimeUnit.DAYS); // use 2 days instead of 1
    }
    catch (Exception e) {
      LOGGER.error("error getting AniDB index: {}", e.getMessage());
      return;
    }

    try (InputStream is = animeList.getInputStream(); Scanner scanner = new Scanner(new GZIPInputStream(is), UrlUtil.UTF_8)) {
      while (scanner.hasNextLine()) {
        Matcher matcher = pattern.matcher(scanner.nextLine());

        if (matcher.matches()) {
          AniDBShow show = new AniDBShow();
          show.aniDbId = Integer.parseInt(matcher.group(1));
          show.language = matcher.group(3);
          show.title = matcher.group(4);

          List<AniDBShow> shows = showsForLookup.computeIfAbsent(show.title, k -> new ArrayList<>());
          shows.add(show);
        }
      }
    }
    catch (InterruptedException | InterruptedIOException e) {
      // do not swallow these Exceptions
      Thread.currentThread().interrupt();
    }
    catch (IOException e) {
      LOGGER.error("error getting AniDB index: {}", e.getMessage());
    }
  }

  /*
   * Track connections and throttle if needed.
   */
  private static synchronized void trackConnections() throws InterruptedException {
    long currentTime = System.currentTimeMillis();
    if (connectionCounter.count() == connectionCounter.maxSize()) {
      long oldestConnection = connectionCounter.getTailItem();
      if (oldestConnection > (currentTime - 2000)) {
        LOGGER.debug("connection limit reached, throttling...");
        do {
          AniDBMetadataProvider.class.wait(2000 - (currentTime - oldestConnection));
          currentTime = System.currentTimeMillis();
        } while (oldestConnection > (currentTime - 2000));
      }
    }

    currentTime = System.currentTimeMillis();
    connectionCounter.add(currentTime);
  }

  @Override
  public List<MediaArtwork> getArtwork(ArtworkSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    List<MediaArtwork> artwork = new ArrayList<>();
    String id = "";

    // check if there is a metadata containing an id
    if (options.getMetadata() != null) {
      id = (String) options.getMetadata().getId(providerInfo.getId());
    }

    // get the id from the options
    if (StringUtils.isEmpty(id)) {
      id = options.getIdAsString(providerInfo.getId());
    }

    if (StringUtils.isEmpty(id)) {
      throw new MissingIdException(providerInfo.getId());
    }

    switch (options.getArtworkType()) {
      // AniDB only offers Poster
      case ALL:
      case POSTER:
        MediaMetadata md;
        try {
          TvShowSearchAndScrapeOptions tvShowSearchAndScrapeOptions = new TvShowSearchAndScrapeOptions();
          tvShowSearchAndScrapeOptions.setDataFromOtherOptions(options);
          md = getMetadata(tvShowSearchAndScrapeOptions);
        }
        catch (Exception e) {
          LOGGER.error("could not get artwork: {}", e.getMessage());
          throw new ScrapeException(e);
        }

        artwork.addAll(md.getMediaArt(MediaArtworkType.POSTER));
        break;

      default:
        return artwork;
    }

    return artwork;
  }

  /****************************************************************************
   * helper class to buffer search results from AniDB
   ****************************************************************************/
  private static class AniDBShow {
    int    aniDbId;
    String language;
    String title;
  }

  /****************************************************************************
   * helper class for episode extraction
   ****************************************************************************/
  private static class Episode {
    int                     id      = -1;
    int                     episode = -1;
    int                     season  = -1;
    int                     runtime = 0;
    Date                    airdate = null;
    float                   rating  = 0;
    int                     votes   = 0;
    String                  summary = "";
    HashMap<String, String> titles  = new HashMap<>();
  }
}
