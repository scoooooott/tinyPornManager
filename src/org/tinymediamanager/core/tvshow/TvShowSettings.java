/*
 * Copyright 2012 - 2013 Manuel Laggner
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
package org.tinymediamanager.core.tvshow;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.jdesktop.observablecollections.ObservableCollections;
import org.tinymediamanager.core.AbstractModelObject;

/**
 * The Class TvShowSettings.
 * 
 * @author Manuel Laggner
 */
@XmlRootElement(name = "TvShowSettings")
public class TvShowSettings extends AbstractModelObject {

  /** The Constant TV_SHOW_DATA_SOURCE. */
  private final static String TV_SHOW_DATA_SOURCE = "tvShowDataSource";

  /** The Constant TV_SHOW_SCRAPER. */
  private final static String TV_SHOW_SCRAPER     = "tvShowScraper";

  /** The Constant PATH. */
  private final static String PATH                = "path";

  /** The movie data sources. */
  @XmlElementWrapper(name = TV_SHOW_DATA_SOURCE)
  @XmlElement(name = PATH)
  private final List<String>  tvShowDataSources   = ObservableCollections.observableList(new ArrayList<String>());

  /** The tv show scraper. */
  private TvShowScrapers      tvShowScraper       = TvShowScrapers.TVDB;

  /**
   * Instantiates a new tv show settings.
   */
  public TvShowSettings() {
  }

  /**
   * Adds the tv show data sources.
   * 
   * @param path
   *          the path
   */
  public void addTvShowDataSources(String path) {
    tvShowDataSources.add(path);
    firePropertyChange(TV_SHOW_DATA_SOURCE, null, tvShowDataSources);
  }

  /**
   * Removes the tv show data sources.
   * 
   * @param path
   *          the path
   */
  public void removeTvShowDataSources(String path) {
    TvShowList tvShowList = TvShowList.getInstance();
    tvShowList.removeDatasource(path);
    tvShowDataSources.remove(path);
    firePropertyChange(TV_SHOW_DATA_SOURCE, null, tvShowDataSources);
  }

  /**
   * Gets the tv show data source.
   * 
   * @return the tv show data source
   */
  public List<String> getTvShowDataSource() {
    return tvShowDataSources;
  }

  /**
   * Gets the tv show scraper.
   * 
   * @return the tv show scraper
   */
  public TvShowScrapers getTvShowScraper() {
    return tvShowScraper;
  }

  /**
   * Sets the tv show scraper.
   * 
   * @param newValue
   *          the new tv show scraper
   */
  public void setTvShowScraper(TvShowScrapers newValue) {
    TvShowScrapers oldValue = this.tvShowScraper;
    this.tvShowScraper = newValue;
    firePropertyChange(TV_SHOW_SCRAPER, oldValue, newValue);
  }
}