/*
 * Copyright 2012 - 2015 Manuel Laggner
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
package org.tinymediamanager.scraper.imdb;

import java.util.Map;

import org.tinymediamanager.scraper.config.ConfigHelper;
import org.tinymediamanager.scraper.config.ScraperSetting;

/**
 * The class ImdbMetadataProviderConfig used to store/persist and serve settings
 * for the ImdbMetadataProvider
 *
 * @author Manuel Laggner
 * @since 1.0
 */
class ImdbMetadataProviderConfig {
  static final ImdbMetadataProviderConfig SETTINGS = loadSettings();

  @ScraperSetting
  public Boolean useTmdb = false;

  @ScraperSetting
  public Boolean scrapeCollectionInfo = false;

  private static ImdbMetadataProviderConfig loadSettings() {
    ImdbMetadataProviderConfig config = new ImdbMetadataProviderConfig();

    // load the config via reflection
    ConfigHelper.loadConfig(ImdbMetadataProvider.providerInfo, config);

    return config;
  }

  void save() {
    // save config via reflection
    ConfigHelper.saveConfig(ImdbMetadataProvider.providerInfo, this);
  }

  Map<String, Object> getConfigMap() {
    return ConfigHelper.getConfigElementsAsMap(this);
  }

  void setConfig(Map<String, Object> configMap) {
    ConfigHelper.setConfigElementsFromMap(configMap, this);
  }
}