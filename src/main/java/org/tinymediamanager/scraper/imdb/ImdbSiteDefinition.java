package org.tinymediamanager.scraper.imdb;/*
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

import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;

/**
 * The Enum org.tinymediamanager.scraper.imdb.ImdbSiteDefinition.
 *
 * @author Manuel Laggner
 */
public enum ImdbSiteDefinition {
  // www.imdb.com - international site; now the only used one
  IMDB_COM(
      "http://www.imdb.com/",
      "UTF-8",
      "Tagline",
      "Genre",
      "Runtime",
      "Production Companies",
      "Writing credits",
      "Certification",
      "Release Date",
      "Produced by");

  private ImdbSiteDefinition(String site, String charsetName, String tagline, String genre, String runtime, String productionCompanies,
      String writers, String certification, String releaseDate, String producers) {
    this.site = site;
    if (StringUtils.isBlank(charsetName)) {
      this.charset = Charset.defaultCharset();
    }
    else {
      this.charset = Charset.forName(charsetName);
    }
    this.tagline = tagline;
    this.genre = genre;
    this.runtime = runtime;
    this.productionCompanies = productionCompanies;
    this.writer = writers;
    this.certification = certification;
    this.releaseDate = releaseDate;
    this.producers = producers;
  }

  private String  site;
  private Charset charset;
  private String  tagline;
  private String  genre;
  private String  runtime;
  private String  productionCompanies;
  private String  writer;
  private String  certification;
  private String  releaseDate;
  private String  producers;

  public String getSite() {
    return site;
  }

  public Charset getCharset() {
    return charset;
  }

  public String getTagline() {
    return tagline;
  }

  public String getGenre() {
    return genre;
  }

  public String getRuntime() {
    return runtime;
  }

  public String getProductionCompanies() {
    return productionCompanies;
  }

  public String getWriter() {
    return writer;
  }

  public String getCertification() {
    return certification;
  }

  public String getReleaseDate() {
    return releaseDate;
  }

  public String getProducers() {
    return producers;
  }

  public String toString() {
    return site;
  }
}
