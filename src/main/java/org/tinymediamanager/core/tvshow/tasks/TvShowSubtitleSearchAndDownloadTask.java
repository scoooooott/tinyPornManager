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

import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.SubtitleSearchAndScrapeOptions;
import org.tinymediamanager.scraper.SubtitleSearchResult;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.ISubtitleProvider;
import org.tinymediamanager.scraper.util.MediaIdUtil;

/**
 * The class TvShowSubtitleSearchAndDownloadTask is used to search and download subtitles by hash
 * 
 * @author Manuel Laggner
 */
public class TvShowSubtitleSearchAndDownloadTask extends TmmThreadPool {
  private static final Logger         LOGGER = LoggerFactory.getLogger(TvShowSubtitleSearchAndDownloadTask.class);
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());

  private final List<TvShowEpisode>   episodes;
  private final List<MediaScraper>    subtitleScrapers;
  private final MediaLanguages        language;

  public TvShowSubtitleSearchAndDownloadTask(List<TvShowEpisode> episodes, List<MediaScraper> subtitleScrapers, MediaLanguages language) {
    super(BUNDLE.getString("tvshow.download.subtitles"));
    this.episodes = episodes;
    this.subtitleScrapers = subtitleScrapers;
    this.language = language;
  }

  @Override
  protected void doInBackground() {
    initThreadPool(3, "searchAndDownloadSubtitles");
    start();

    for (TvShowEpisode episode : episodes) {
      submitTask(new Worker(episode));
    }

    waitForCompletionOrCancel();

    LOGGER.info("Done searching and downloading subtitles");
  }

  @Override
  public void callback(Object obj) {
    // do not publish task description here, because with different workers the text is never right
    publishState(progressDone);
  }

  /****************************************************************************************
   * Helper classes
   ****************************************************************************************/
  private class Worker implements Runnable {
    private TvShowEpisode episode;

    Worker(TvShowEpisode episode) {
      this.episode = episode;
    }

    @Override
    public void run() {
      try {
        for (MediaScraper scraper : subtitleScrapers) {
          try {
            MediaFile mf = episode.getMediaFiles(MediaFileType.VIDEO).get(0);

            ISubtitleProvider subtitleProvider = (ISubtitleProvider) scraper.getMediaProvider();
            SubtitleSearchAndScrapeOptions options = new SubtitleSearchAndScrapeOptions(MediaType.TV_EPISODE);
            options.setFile(mf.getFileAsPath().toFile());
            options.setLanguage(language);
            options.setSeason(episode.getSeason());
            options.setEpisode(episode.getEpisode());

            String imdbId = episode.getTvShow().getImdbId();
            if (StringUtils.isBlank(imdbId)) {
              // try to get the IMDB Id via TheTVDB
              MediaIdUtil.getImdbIdFromTvdbId(episode.getTvShow().getTvdbId());
            }
            if (StringUtils.isNotBlank(imdbId)) {
              options.setImdbId(imdbId);
            }

            List<SubtitleSearchResult> searchResults = subtitleProvider.search(options);
            if (searchResults.isEmpty()) {
              continue;
            }

            Collections.sort(searchResults);
            Collections.reverse(searchResults);

            SubtitleSearchResult firstResult = searchResults.get(0);
            if (firstResult.getScore() < 1.0f || StringUtils.isBlank(firstResult.getUrl())) {
              continue;
            }

            String filename = FilenameUtils.getBaseName(mf.getFilename()) + "." + language.name();
            TmmTaskManager.getInstance()
                .addDownloadTask(new TvShowSubtitleDownloadTask(firstResult.getUrl(), episode.getPathNIO().resolve(filename), episode));
          }
          catch (ScrapeException e) {
            LOGGER.error("getSubtitles", e);
            MessageManager.instance.pushMessage(
                new Message(MessageLevel.ERROR, episode, "message.scrape.subtitlefailed", new String[] { ":", e.getLocalizedMessage() }));
          }
          catch (MissingIdException ignored) {
          }
        }
      }
      catch (Exception e) {
        LOGGER.error("Thread crashed", e);
        MessageManager.instance.pushMessage(
            new Message(MessageLevel.ERROR, "SubtitleDownloader", "message.scrape.threadcrashed", new String[] { ":", e.getLocalizedMessage() }));
      }
    }
  }
}
