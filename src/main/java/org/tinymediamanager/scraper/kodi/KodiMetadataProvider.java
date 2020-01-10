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
package org.tinymediamanager.scraper.kodi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.interfaces.IKodiMetadataProvider;
import org.tinymediamanager.scraper.interfaces.IMediaProvider;
import org.tinymediamanager.scraper.util.CacheMap;

/**
 * The entry point for all Kodi meta data providers.
 *
 * @author Manuel Laggner
 */
public class KodiMetadataProvider implements IKodiMetadataProvider {
  public static final String                      ID           = "kodi";
  private static MediaProviderInfo                providerInfo = new MediaProviderInfo(ID, "kodi.tv", "Generic Kodi type scraper");
  // cache one hour
  protected static final CacheMap<String, String> XML_CACHE    = new CacheMap<>(60 * 60, 60);

  public KodiMetadataProvider() {
    // preload scrapers
    new KodiUtil();
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public String getId() {
    return ID;
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
