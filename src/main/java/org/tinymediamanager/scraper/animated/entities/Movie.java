package org.tinymediamanager.scraper.animated.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "imdbid", "tmdbid", "title", "year", "entries" })
public class Movie {

  @JsonProperty("imdbid")
  private String              imdbid;
  @JsonProperty("tmdbid")
  private String              tmdbid;
  @JsonProperty("title")
  private String              title;
  @JsonProperty("year")
  private String              year;
  @JsonProperty("entries")
  private List<Entry>         entries              = null;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  public List<Entry> getPosters() {
    List<Entry> posters = new ArrayList<Entry>();
    if (entries != null) {
      for (Entry e : entries) {
        if (e.getType().equalsIgnoreCase("poster")) {
          posters.add(e);
        }
      }
    }
    return posters;
  }

  public List<Entry> getBackgrounds() {
    List<Entry> backgrounds = new ArrayList<Entry>();
    if (entries != null) {
      for (Entry e : entries) {
        if (e.getType().equalsIgnoreCase("background")) {
          backgrounds.add(e);
        }
      }
    }
    return backgrounds;
  }

  @JsonProperty("imdbid")
  public String getImdbid() {
    return imdbid;
  }

  @JsonProperty("imdbid")
  public void setImdbid(String imdbid) {
    this.imdbid = imdbid;
  }

  @JsonProperty("tmdbid")
  public String getTmdbid() {
    return tmdbid;
  }

  @JsonProperty("tmdbid")
  public void setTmdbid(String tmdbid) {
    this.tmdbid = tmdbid;
  }

  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  @JsonProperty("title")
  public void setTitle(String title) {
    this.title = title;
  }

  @JsonProperty("year")
  public String getYear() {
    return year;
  }

  @JsonProperty("year")
  public void setYear(String year) {
    this.year = year;
  }

  @JsonProperty("entries")
  public List<Entry> getEntries() {
    return entries;
  }

  @JsonProperty("entries")
  public void setEntries(List<Entry> entries) {
    this.entries = entries;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
