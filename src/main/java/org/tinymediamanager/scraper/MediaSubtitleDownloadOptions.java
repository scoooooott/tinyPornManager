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
package org.tinymediamanager.scraper;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Options for Subtitle downloading
 * 
 * @author Manuel Laggner
 * @since 1.0
 */
public class MediaSubtitleDownloadOptions {
  private MediaSearchResult result;
  private String            downloadTestination;
  private MediaLanguages    language = MediaLanguages.en;
  private CountryCode       country  = CountryCode.US;

  public MediaSearchResult getResult() {
    return result;
  }

  public void setResult(MediaSearchResult result) {
    this.result = result;
  }

  public String getDownloadTestination() {
    return downloadTestination;
  }

  public void setDownloadTestination(String downloadTestination) {
    this.downloadTestination = downloadTestination;
  }

  public MediaLanguages getLanguage() {
    return language;
  }

  public void setLanguage(MediaLanguages language) {
    this.language = language;
  }

  public CountryCode getCountry() {
    return country;
  }

  public void setCountry(CountryCode country) {
    this.country = country;
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a <code>toString</code> for the specified object.
   * </p>
   * 
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return (new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)).toString();
  }
}
