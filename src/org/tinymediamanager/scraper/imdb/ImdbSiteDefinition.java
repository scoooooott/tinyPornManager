/*
 *      Copyright (c) 2004-2012 YAMJ Members
 *      http://code.google.com/p/moviejukebox/people/list
 *
 *      Web: http://code.google.com/p/moviejukebox/
 *
 *      This software is licensed under a Creative Commons License
 *      See this page: http://code.google.com/p/moviejukebox/wiki/License
 *
 *      For any reuse or distribution, you must make clear to others the
 *      license terms of this work.
 */
package org.tinymediamanager.scraper.imdb;

import java.nio.charset.Charset;

import org.apache.commons.lang3.StringUtils;

public class ImdbSiteDefinition {
  private String  site;
  private String  director;
  private String  cast;
  private String  releaseDate;
  private String  runtime;
  private String  country;
  private String  company;
  private String  genre;
  private String  quotes;
  private String  plot;
  private String  rated;
  private String  certification;
  private String  originalAirDate;
  private String  writer;
  private String  taglines;
  private Charset charset;

  public ImdbSiteDefinition(String site, String charsetName, String director, String cast, String releaseDate, String runtime, String country,
      String company, String genre, String quotes, String plot, String rated, String certification, String originalAirDate, String writer,
      String taglines) {
    super();
    this.site = site;
    this.director = director;
    this.cast = cast;
    this.releaseDate = releaseDate;
    this.runtime = runtime;
    this.country = country;
    this.company = company;
    this.genre = genre;
    this.quotes = quotes;
    this.plot = plot;
    this.rated = rated;
    this.certification = certification;
    this.originalAirDate = originalAirDate;
    this.writer = writer;
    this.taglines = taglines;

    if (StringUtils.isBlank(charsetName)) {
      this.charset = Charset.defaultCharset();
    }
    else {
      this.charset = Charset.forName(charsetName);
    }
  }

  public String getSite() {
    return site;
  }

  public String getDirector() {
    return director;
  }

  public String getCast() {
    return cast;
  }

  public String getReleaseDate() {
    return releaseDate;
  }

  public String getRuntime() {
    return runtime;
  }

  public String getCountry() {
    return country;
  }

  public String getCompany() {
    return company;
  }

  public String getGenre() {
    return genre;
  }

  public String getQuotes() {
    return quotes;
  }

  public String getPlot() {
    return plot;
  }

  public String getRated() {
    return rated;
  }

  public String getCertification() {
    return certification;
  }

  public String getOriginalAirDate() {
    return originalAirDate;
  }

  public String getWriter() {
    return writer;
  }

  public String getTaglines() {
    return taglines;
  }

  public Charset getCharset() {
    return charset;
  }

}