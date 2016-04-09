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
package org.tinymediamanager.scraper.kodi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IKodiMetadataProvider;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;

/**
 * The entry point for all Kodi meta data providers.
 * 
 * @author Manuel Laggner
 */
@PluginImplementation
public class KodiMetadataProvider implements IKodiMetadataProvider {
  private static MediaProviderInfo providerInfo = new MediaProviderInfo("kodi", "kodi.tv", "Generic Kodi type scraper");

  public KodiMetadataProvider() {
    // empty constructor just for creating the factory
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Init
  public void init() {
    // preload scrapers
    new KodiUtil();
  }

  /**
   * the factory for creating all instances
   */
  @Override
  public List<IMediaProvider> getPluginsForType(MediaType type) {
    if (type == null) {
      // unsupported type
      return Collections.emptyList();
    }

    List<IMediaProvider> metadataProviders = new ArrayList<>();

    for (AbstractKodiMetadataProvider metadataProvider : KodiUtil.scrapers) {
      if (type == metadataProvider.scraper.type) {
        metadataProviders.add(metadataProvider);
      }
    }
    return metadataProviders;
  }
}
