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
package org.tinymediamanager.ui.tvshows;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.TvShowScraperMetadataConfig;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaScraper;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaLanguages;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.mediaprovider.ITvShowArtworkProvider;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;
import org.tinymediamanager.ui.UTF8Control;

/**
 * 
 * @author Manuel Laggner
 */
public class TvShowChooserModel extends AbstractModelObject {
  private static final ResourceBundle    BUNDLE          = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$
  private static final Logger            LOGGER          = LoggerFactory.getLogger(TvShowChooserModel.class);
  public static final TvShowChooserModel emptyResult     = new TvShowChooserModel();

  private MediaScraper                   mediaScraper    = null;
  private List<MediaScraper>             artworkScrapers = null;
  private MediaLanguages                 language        = null;
  private MediaSearchResult              result          = null;
  private MediaMetadata                  metadata        = null;
  private String                         name            = "";
  private String                         overview        = "";
  private String                         year            = "";
  private String                         combinedName    = "";
  private String                         posterUrl       = "";
  private String                         tagline         = "";
  private boolean                        scraped         = false;

  public TvShowChooserModel(MediaScraper mediaScraper, List<MediaScraper> artworkScrapers, MediaSearchResult result, MediaLanguages language) {
    this.mediaScraper = mediaScraper;
    this.artworkScrapers = artworkScrapers;
    // this.trailerProviders = trailerProviders;
    this.result = result;
    this.language = language;

    // name
    setName(result.getTitle());
    // year
    if (result.getYear() != 0) {
      setYear(Integer.toString(result.getYear()));
    }
    else {
      setYear("");
    }
    // combined name (name (year))
    setCombinedName();
  }

  /**
   * create the empty search result.
   */
  private TvShowChooserModel() {
    setName(BUNDLE.getString("chooser.nothingfound")); //$NON-NLS-1$
    combinedName = name;
  }

  public void setName(String name) {
    String oldValue = this.name;
    this.name = name;
    firePropertyChange("name", oldValue, name);
  }

  public void setOverview(String overview) {
    String oldValue = this.overview;
    this.overview = overview;
    firePropertyChange("overview", oldValue, overview);
  }

  public String getName() {
    return name;
  }

  public String getOverview() {
    return overview;
  }

  public String getPosterUrl() {
    return posterUrl;
  }

  public void setPosterUrl(String newValue) {
    String oldValue = posterUrl;
    posterUrl = newValue;
    firePropertyChange("posterUrl", oldValue, newValue);
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    String oldValue = this.year;
    this.year = year;
    firePropertyChange("year", oldValue, year);
  }

  public void setCombinedName() {
    String oldValue = this.combinedName;

    if (StringUtils.isNotBlank(getYear())) {
      this.combinedName = getName() + " (" + getYear() + ")";
    }
    else {
      this.combinedName = getName();
    }
    firePropertyChange("combinedName", oldValue, this.combinedName);
  }

  public String getCombinedName() {
    return combinedName;
  }

  /**
   * Scrape meta data.
   */
  public void scrapeMetaData() {
    try {
      // poster for preview
      setPosterUrl(result.getPosterUrl());

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_SHOW);
      options.setResult(result);
      options.setLanguage(LocaleUtils.toLocale(language.name()));
      options.setCountry(TvShowModuleManager.SETTINGS.getCertificationCountry());
      LOGGER.info("=====================================================");
      LOGGER.info("Scraper metadata with scraper: " + mediaScraper.getMediaProvider().getProviderInfo().getId());
      LOGGER.info(options.toString());
      LOGGER.info("=====================================================");
      metadata = ((ITvShowMetadataProvider) mediaScraper.getMediaProvider()).getMetadata(options);
      setOverview(metadata.getPlot());
      setTagline(metadata.getTagline());

      if (StringUtils.isBlank(posterUrl) && !metadata.getMediaArt(MediaArtworkType.POSTER).isEmpty()) {
        setPosterUrl(metadata.getMediaArt(MediaArtworkType.POSTER).get(0).getPreviewUrl());
      }

      scraped = true;

    }
    catch (Exception e) {
      LOGGER.error("scrapeMedia", e);
      MessageManager.instance.pushMessage(
          new Message(MessageLevel.ERROR, "TvShowChooser", "message.scrape.threadcrashed", new String[] { ":", e.getLocalizedMessage() }));
    }
  }

  public MediaMetadata getMetadata() {
    return metadata;
  }

  public boolean isScraped() {
    return scraped;
  }

  public void setTagline(String newValue) {
    String oldValue = this.tagline;
    this.tagline = newValue;
    firePropertyChange("tagline", oldValue, newValue);
  }

  public String getTagline() {
    return tagline;
  }

  public MediaLanguages getLanguage() {
    return language;
  }

  public void startArtworkScrapeTask(TvShow tvShow, TvShowScraperMetadataConfig config) {
    TmmTaskManager.getInstance().addUnnamedTask(new ArtworkScrapeTask(tvShow, config));
  }

  private class ArtworkScrapeTask extends TmmTask {
    private TvShow                      tvShowToScrape;
    private TvShowScraperMetadataConfig config;

    public ArtworkScrapeTask(TvShow tvShow, TvShowScraperMetadataConfig config) {
      super(BUNDLE.getString("message.scrape.artwork") + " " + tvShow.getTitle(), 0, TaskType.BACKGROUND_TASK);
      this.tvShowToScrape = tvShow;
      this.config = config;
    }

    @Override
    protected void doInBackground() {
      if (!scraped) {
        return;
      }

      List<MediaArtwork> artwork = new ArrayList<>();

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.TV_SHOW);
      options.setArtworkType(MediaArtworkType.ALL);
      options.setMetadata(metadata);
      for (Entry<String, Object> entry : tvShowToScrape.getIds().entrySet()) {
        options.setId(entry.getKey(), entry.getValue().toString());
      }

      options.setLanguage(LocaleUtils.toLocale(language.name()));
      options.setCountry(TvShowModuleManager.SETTINGS.getCertificationCountry());

      // scrape providers till one artwork has been found
      for (MediaScraper artworkScraper : artworkScrapers) {
        ITvShowArtworkProvider artworkProvider = (ITvShowArtworkProvider) artworkScraper.getMediaProvider();
        try {
          artwork.addAll(artworkProvider.getArtwork(options));
        }
        catch (Exception e) {
          LOGGER.warn("could not get artwork from " + artworkProvider.getProviderInfo().getName() + ": " + e.getMessage());
        }
      }

      // at last take the poster from the result
      if (StringUtils.isNotBlank(getPosterUrl())) {
        MediaArtwork ma = new MediaArtwork(result.getProviderId(), MediaArtworkType.POSTER);
        ma.setDefaultUrl(getPosterUrl());
        ma.setPreviewUrl(getPosterUrl());
        artwork.add(ma);
      }

      tvShowToScrape.setArtwork(artwork, config);
    }
  }
}
