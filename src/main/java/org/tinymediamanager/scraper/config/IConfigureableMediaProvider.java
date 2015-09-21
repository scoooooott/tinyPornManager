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
package org.tinymediamanager.scraper.config;

import java.util.Map;

import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;

/**
 * This interface extends the main media provider interface with loading and setting a configuration
 *
 * @author Manuel Laggner
 * @since 1.0
 */
public interface IConfigureableMediaProvider extends IMediaProvider {

  /**
   * Get all provider settings (already set and/or default ones)
   *
   * @return a map of all settings (key/value pairs)
   */
  public Map<String, Object> getProviderSettings();

  /**
   * Set the given provider settings
   *
   * @param settings
   *          the given settings to be set
   */
  public void setProviderSettings(Map<String, Object> settings);

}
