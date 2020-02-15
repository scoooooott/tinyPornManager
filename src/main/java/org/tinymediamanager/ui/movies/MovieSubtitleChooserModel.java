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
package org.tinymediamanager.ui.movies;

import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.core.UTF8Control;
import org.tinymediamanager.scraper.SubtitleSearchResult;
import org.tinymediamanager.scraper.entities.MediaLanguages;

/**
 * This is the model for the MovieSubtitleChooser
 * 
 * @author Manuel Laggner
 */
public class MovieSubtitleChooserModel extends AbstractModelObject {
  private static final ResourceBundle           BUNDLE       = ResourceBundle.getBundle("messages", new UTF8Control());
  private static final Logger                   LOGGER       = LoggerFactory.getLogger(MovieSubtitleChooserModel.class);
  public static final MovieSubtitleChooserModel EMPTY_RESULT = new MovieSubtitleChooserModel();

  private MediaLanguages                        language     = null;
  private SubtitleSearchResult                  result       = null;

  private String                                name         = "";
  private String                                releaseName  = "";
  private String                                downloadUrl  = "";

  public MovieSubtitleChooserModel(SubtitleSearchResult result, MediaLanguages language) {
    this.result = result;
    this.language = language;

    name = result.getTitle();
    releaseName = result.getReleaseName();
    downloadUrl = result.getUrl();
  }

  /**
   * create the empty search result.
   */
  private MovieSubtitleChooserModel() {
    name = BUNDLE.getString("chooser.nothingfound");
  }

  public String getName() {
    return name;
  }

  public String getReleaseName() {
    return releaseName;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public MediaLanguages getLanguage() {
    return language;
  }
}
