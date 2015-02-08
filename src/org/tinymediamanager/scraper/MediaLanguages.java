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

/**
 * The Enum MediaLanguages.
 * 
 * @author Manuel Laggner
 */
public enum MediaLanguages {






  //@formatter:off
  cs("Český"),
  de("Deutsch"),
  da("Dansk"),
  en("English"),
  es("Español"),
  fi("Suomi"),
  fr("Française"),
  hu("Magyar"),
  it("Italiano"),
  nl("Nederlands"),
  no("Norsk"),
  pl("Język polski"),
  pt("Portuguese"),
  ru("русский язык"),
  sl("Slovenščina"),
  sk("Slovenčina"),
  sv("Svenska"),
  tr("Türkçe");
  //@formatter:on

  /** The title. */
  private String title;

  /**
   * Instantiates a new languages.
   * 
   * @param title
   *          the title
   */
  private MediaLanguages(String title) {
    this.title = title;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Enum#toString()
   */
  public String toString() {
    return this.title;
  }

}
