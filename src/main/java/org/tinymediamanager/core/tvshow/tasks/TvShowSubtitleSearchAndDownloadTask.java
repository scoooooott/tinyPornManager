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
package org.tinymediamanager.core.tvshow.tasks;

import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.threading.TmmThreadPool;
import org.tinymediamanager.core.tvshow.entities.TvShowEpisode;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.SubtitleSearchOptions;
import org.tinymediamanager.scraper.SubtitleSearchResult;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.mediaprovider.IMediaSubtitleProvider;
import org.tinymediamanager.scraper.util.MediaIdUtil;
import org.tinymediamanager.ui.UTF8Control;

/**
 * The class TvShowSubtitleSearchAndDownloadTask is used to search and download subtitles by hash
 * 
 * @author Manuel Laggner
 */
public class TvShowSubtitleSearchAndDownloadTask extends TmmThreadPool {
  private final static Logger         LOGGER = LoggerFactory.getLogger(TvShowSubtitleSearchAndDownloadTask.class);
  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages", new UTF8Control());           //$NON-NLS-1$

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

    for (int i = 0; i < episodes.size(); i++) {
      TvShowEpisode episode = episodes.get(i);
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

            IMediaSubtitleProvider subtitleProvider = (IMediaSubtitleProvider) scraper.getMediaProvider();
            SubtitleSearchOptions options = new SubtitleSearchOptions(mf.getFileAsPath().toFile());
            options.setLanguage(LocaleUtils.toLocale(language.name()));
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
          catch (Exception e) {
            LOGGER.error("Error at subtitle searching: " + e.getMessage());
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
