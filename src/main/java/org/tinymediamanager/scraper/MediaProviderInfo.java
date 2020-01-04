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
package org.tinymediamanager.scraper;

import java.net.URL;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.tinymediamanager.scraper.config.MediaProviderConfig;

/**
 * The class ProviderInfo is used to store provider related information for further usage.
 * 
 * @author Manuel Laggner
 * @since 1.0
 */
public class MediaProviderInfo {
  private static final URL    EMPTY_LOGO  = MediaProviderInfo.class.getResource("emtpyLogo.png");

  private String              id;
  private String              name        = "";
  private String              description = "";
  private String              version     = "";
  private URL                 providerLogo;
  private MediaProviderConfig config;

  /**
   * Instantiates a new provider info.
   * 
   * @param id
   *          the id of the provider
   * @param name
   *          the name of the provider
   * @param description
   *          a description of the provider
   */
  public MediaProviderInfo(String id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.config = new MediaProviderConfig(this);
  }

  /**
   * Instantiates a new provider info.
   *
   * @param id
   *          the id of the provider
   * @param name
   *          the name of the provider
   * @param description
   *          a description of the provider
   * @param providerLogo
   *          the URL to the (embedded) provider logo
   */
  public MediaProviderInfo(String id, String name, String description, URL providerLogo) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.providerLogo = providerLogo;
    this.config = new MediaProviderConfig(this);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public URL getProviderLogo() {
    if (providerLogo != null) {
      return providerLogo;
    }
    else {
      return EMPTY_LOGO;
    }
  }

  public void setProviderLogo(URL providerLogo) {
    this.providerLogo = providerLogo;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getVersion() {
    return version;
  }

  /**
   * get the configuration for this scraper
   * 
   * @return the configuration for this scraper
   */
  public MediaProviderConfig getConfig() {
    return config;
  }
}
