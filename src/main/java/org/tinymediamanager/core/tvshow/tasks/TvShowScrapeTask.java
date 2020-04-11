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
package org.tinymediamanager.core.tvshow.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.ScraperMetadataConfig;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.TvShowEpisodeScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowEpisodeSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.TvShowHelpers;
import org.tinymediamanager.core.tvshow.TvShowList;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.TvShowSearchAndScrapeOptions;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.ArtworkSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.TrailerSearchAndScrapeOptions;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.ITvShowArtworkProvider;
import org.tinymediamanager.scraper.interfaces.ITvShowMetadataProvider;
import org.tinymediamanager.scraper.interfaces.ITvShowTrailerProvider;
import org.tinymediamanager.thirdparty.trakttv.SyncTraktTvTask;

/**
 * The class TvShowScrapeTask. This starts scraping of TV shows
 *
 * @author Manuel Laggner
 */
public class TvShowScrapeTask extends TmmThreadPool {
  private static final Logger LOGGER = LoggerFactory.getLogger(TvShowScrapeTask.class);
  private static final ResourceBundle                    BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  private final List<TvShow>                             tvShowsToScrape;
  private final boolean                                  doSearch;
  private final TvShowSearchAndScrapeOptions             scrapeOptions;
  private final List<TvShowScraperMetadataConfig>        tvShowScraperMetadataConfig;
  private final List<TvShowEpisodeScraperMetadataConfig> episodeScraperMetadataConfig;

  /**
   * Instantiates a new tv show scrape task.
   * 
   * @param tvShowsToScrape
   *          the tv shows to scrape
   * @param doSearch
   *          the do search
   * @param options
   *          the options
   */
  public TvShowScrapeTask(List<TvShow> tvShowsToScrape, boolean doSearch, TvShowSearchAndScrapeOptions options,
      List<TvShowScraperMetadataConfig> tvShowScraperMetadataConfig, List<TvShowEpisodeScraperMetadataConfig> episodeScraperMetadataConfig) {
    super(BUNDLE.getString("tvshow.scraping"));
    this.tvShowsToScrape = tvShowsToScrape;
    this.doSearch = doSearch;
    this.scrapeOptions = options;
    this.tvShowScraperMetadataConfig = tvShowScraperMetadataConfig;
    this.episodeScraperMetadataConfig = episodeScraperMetadataConfig;
  }

  @Override
  protected void doInBackground() {
    LOGGER.debug("start scraping tv shows...");
    start();

    initThreadPool(3, "scrape");
    for (TvShow tvShow : tvShowsToScrape) {
      submitTask(new Worker(tvShow));
    }

    waitForCompletionOrCancel();

    if (TvShowModuleManager.SETTINGS.getSyncTrakt()) {
      TmmTask task = new SyncTraktTvTask(null, tvShowsToScrape);
      TmmTaskManager.getInstance().addUnnamedTask(task);
    }

    LOGGER.debug("done scraping tv shows...");
  }

  private class Worker implements Runnable {
    private TvShowList tvShowList = TvShowList.getInstance();
    private TvShow     tvShow;

    private Worker(TvShow tvShow) {
      this.tvShow = tvShow;
    }

    @Override
    public void run() {
      try {
        // set up scrapers
        MediaScraper mediaMetadataScraper = scrapeOptions.getMetadataScraper();
        List<MediaScraper> trailerScrapers = scrapeOptions.getTrailerScrapers();

        // scrape tv show

        // search for tv show
        MediaSearchResult result1 = null;
        if (doSearch) {
          List<MediaSearchResult> results = tvShowList.searchTvShow(tvShow.getTitle(), tvShow.getYear(), tvShow.getIds(), mediaMetadataScraper);
          if (results != null && !results.isEmpty()) {
            result1 = results.get(0);
            // check if there is an other result with 100% score
            if (results.size() > 1) {
              MediaSearchResult result2 = results.get(1);
              // if both results have 100% score - do not take any result
              if (result1.getScore() == 1 && result2.getScore() == 1) {
                LOGGER.info("two 100% results, can't decide which to take - ignore result");
                MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, tvShow, "tvshow.scrape.nomatchfound"));
                return;
              }
              // create a treshold of 0.75 - to minimize false positives
              if (result1.getScore() < 0.75) {
                LOGGER.info("score is lower than 0.75 ({}) - ignore result", result1.getScore());
                MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, tvShow, "tvshow.scrape.nomatchfound"));
                return;
              }
            }
          }
          else {
            LOGGER.info("no result found for {}", tvShow.getTitle());
            MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, tvShow, "tvshow.scrape.nomatchfound"));
          }
        }

        // get metadata and artwork
        if ((doSearch && result1 != null) || !doSearch) {
          try {
            TvShowSearchAndScrapeOptions options = new TvShowSearchAndScrapeOptions(scrapeOptions);
            options.setSearchResult(result1);

            if (doSearch) {
              options.setIds(result1.getIds());
            }
            else {
              options.setIds(tvShow.getIds());
            }

            // override scraper with one from search result
            mediaMetadataScraper = tvShowList.getMediaScraperById(result1.getProviderId());
            // scrape metadata if wanted
            MediaMetadata md = null;

            if (ScraperMetadataConfig.containsAnyMetadata(tvShowScraperMetadataConfig)
                || ScraperMetadataConfig.containsAnyCast(tvShowScraperMetadataConfig)) {
              LOGGER.info("=====================================================");
              LOGGER.info("Scraper metadata with scraper: {}", mediaMetadataScraper.getMediaProvider().getProviderInfo().getId());
              LOGGER.info(options.toString());
              LOGGER.info("=====================================================");
              md = ((ITvShowMetadataProvider) mediaMetadataScraper.getMediaProvider()).getMetadata(options);
              tvShow.setMetadata(md, tvShowScraperMetadataConfig);
            }

            // always add all episode data (for missing episodes and episode list)
            List<TvShowEpisode> episodes = new ArrayList<>();
            try {
              for (MediaMetadata me : ((ITvShowMetadataProvider) mediaMetadataScraper.getMediaProvider()).getEpisodeList(options)) {
                TvShowEpisode ep = new TvShowEpisode();
                ep.setEpisode(me.getEpisodeNumber());
                ep.setSeason(me.getSeasonNumber());
                ep.setDvdEpisode(me.getDvdEpisodeNumber());
                ep.setDvdSeason(me.getDvdSeasonNumber());
                ep.setTitle(me.getTitle());
                ep.setOriginalTitle(me.getOriginalTitle());
                ep.setPlot(me.getPlot());
                ep.setActors(me.getCastMembers(Person.Type.ACTOR));
                ep.setDirectors(me.getCastMembers(Person.Type.DIRECTOR));
                ep.setWriters(me.getCastMembers(Person.Type.WRITER));

                episodes.add(ep);
              }
            }
            catch (ScrapeException e) {
              LOGGER.error("searchMovieFallback", e);
              MessageManager.instance.pushMessage(
                  new Message(Message.MessageLevel.ERROR, tvShow, "message.scrape.episodelistfailed", new String[] { ":", e.getLocalizedMessage() }));
            }
            catch (MissingIdException e) {
              LOGGER.warn("missing id for scrape");
              MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, tvShow, "scraper.error.missingid"));
            }
            tvShow.setDummyEpisodes(episodes);
            tvShow.saveToDb();

            // scrape episodes
            if (!episodeScraperMetadataConfig.isEmpty()) {
              List<TvShowEpisode> episodesToScrape = tvShow.getEpisodesToScrape();
              // scrape episodes in a task
              if (!episodesToScrape.isEmpty()) {
                TvShowEpisodeSearchAndScrapeOptions options1 = new TvShowEpisodeSearchAndScrapeOptions();
                options1.setDataFromOtherOptions(options);
                TvShowEpisodeScrapeTask task = new TvShowEpisodeScrapeTask(episodesToScrape, options1, episodeScraperMetadataConfig);
                TmmTaskManager.getInstance().addUnnamedTask(task);
              }
            }

            // scrape artwork if wanted
            if (ScraperMetadataConfig.containsAnyArtwork(tvShowScraperMetadataConfig)) {
              tvShow.setArtwork(getArtwork(tvShow, md), tvShowScraperMetadataConfig);
            }

            // scrape trailer if wanted
            if (tvShowScraperMetadataConfig.contains(TvShowScraperMetadataConfig.TRAILER)) {
              tvShow.setTrailers(getTrailers(tvShow, md, trailerScrapers));
              tvShow.saveToDb();
              tvShow.writeNFO();

              // start automatic movie trailer download
              TvShowHelpers.startAutomaticTrailerDownload(tvShow);
            }

          } catch (ScrapeException e) {
            LOGGER.error("getTvShowMetadata", e);
            MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, tvShow, "message.scrape.metadatatvshowfailed",
                new String[] { ":", e.getLocalizedMessage() }));
          }
          catch (MissingIdException e) {
            LOGGER.warn("missing id for scrape");
            MessageManager.instance.pushMessage(new Message(Message.MessageLevel.ERROR, tvShow, "scraper.error.missingid"));
          }
          catch (NothingFoundException e) {
            LOGGER.debug("nothing found");
          }
        }
      }

      catch (Exception e) {
        LOGGER.error("Thread crashed", e);
        MessageManager.instance.pushMessage(
            new Message(MessageLevel.ERROR, "TvShowScraper", "message.scrape.threadcrashed", new String[] { ":", e.getLocalizedMessage() }));
      }
    }

    /**
     * Gets the artwork.
     * 
     * @param metadata
     *          the metadata
     * @return the artwork
     */
    public List<MediaArtwork> getArtwork(TvShow tvShow, MediaMetadata metadata) {
      List<MediaArtwork> artwork = new ArrayList<>();

      ArtworkSearchAndScrapeOptions options = new ArtworkSearchAndScrapeOptions(MediaType.TV_SHOW);
      options.setDataFromOtherOptions(scrapeOptions);
      options.setArtworkType(MediaArtworkType.ALL);
      options.setMetadata(metadata);

      for (Entry<String, Object> entry : tvShow.getIds().entrySet()) {
        options.setId(entry.getKey(), entry.getValue().toString());
      }

      // scrape providers till one artwork has been found
      for (MediaScraper artworkScraper : scrapeOptions.getArtworkScrapers()) {
        ITvShowArtworkProvider artworkProvider = (ITvShowArtworkProvider) artworkScraper.getMediaProvider();
        try {
          artwork.addAll(artworkProvider.getArtwork(options));
        }
        catch (ScrapeException e) {
          LOGGER.error("getArtwork", e);
          MessageManager.instance.pushMessage(
                  new Message(Message.MessageLevel.ERROR, tvShow, "message.scrape.tvshowartworkfailed", new String[]{":", e.getLocalizedMessage()}));
        } catch (MissingIdException ignored) {
          LOGGER.debug("no id avaiable for scraper {}", artworkScraper.getId());
        }
      }
      return artwork;
    }

    private List<MediaTrailer> getTrailers(TvShow tvShow, MediaMetadata metadata, List<MediaScraper> trailerScrapers) {
      List<MediaTrailer> trailers = new ArrayList<>();

      TrailerSearchAndScrapeOptions options = new TrailerSearchAndScrapeOptions(MediaType.TV_SHOW);

      options.setDataFromOtherOptions(scrapeOptions);
      options.setMetadata(metadata);

      for (Entry<String, Object> entry : tvShow.getIds().entrySet()) {
        options.setId(entry.getKey(), entry.getValue().toString());
      }

      // scrape trailers
      for (MediaScraper trailerScraper : trailerScrapers) {
        try {
          ITvShowTrailerProvider trailerProvider = (ITvShowTrailerProvider) trailerScraper.getMediaProvider();
          trailers.addAll(trailerProvider.getTrailers(options));
        } catch (ScrapeException e) {
          LOGGER.error("getTrailers", e);
          MessageManager.instance.pushMessage(
                  new Message(MessageLevel.ERROR, tvShow, "message.scrape.trailerfailed", new String[]{":", e.getLocalizedMessage()}));
        } catch (MissingIdException e) {
          LOGGER.debug("no usable ID found for scraper {}", trailerScraper.getMediaProvider().getProviderInfo().getId());
        }
      }

      return trailers;
    }

  }

  @Override
  public void callback(Object obj) {
    // do not publish task description here, because with different workers the text is never right
    publishState(progressDone);
  }
}
