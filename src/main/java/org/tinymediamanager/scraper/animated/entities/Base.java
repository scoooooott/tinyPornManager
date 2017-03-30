package org.tinymediamanager.scraper.animated.entities;

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

@JsonPropertyOrder({ "movies", "version", "lastUpdated", "previousUpdated", "baseURL" })
public class Base {

  @JsonProperty("movies")
  private List<Movie>         movies               = null;
  @JsonProperty("version")
  private Integer             version;
  @JsonProperty("lastUpdated")
  private String              lastUpdated;
  @JsonProperty("previousUpdated")
  private String              previousUpdated;
  @JsonProperty("baseURL")
  private String              baseURL;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  public Movie getMovieByImdbId(String imdbId) {
    for (Movie movie : movies) {
      if (movie.getImdbid().equals(imdbId)) {
        return movie;
      }
    }
    return null;
  }

  @JsonProperty("movies")
  public List<Movie> getMovies() {
    return movies;
  }

  @JsonProperty("movies")
  public void setMovies(List<Movie> movies) {
    this.movies = movies;
  }

  @JsonProperty("version")
  public Integer getVersion() {
    return version;
  }

  @JsonProperty("version")
  public void setVersion(Integer version) {
    this.version = version;
  }

  @JsonProperty("lastUpdated")
  public String getLastUpdated() {
    return lastUpdated;
  }

  @JsonProperty("lastUpdated")
  public void setLastUpdated(String lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  @JsonProperty("previousUpdated")
  public String getPreviousUpdated() {
    return previousUpdated;
  }

  @JsonProperty("previousUpdated")
  public void setPreviousUpdated(String previousUpdated) {
    this.previousUpdated = previousUpdated;
  }

  @JsonProperty("baseURL")
  public String getBaseURL() {
    return baseURL;
  }

  @JsonProperty("baseURL")
  public void setBaseURL(String baseURL) {
    this.baseURL = baseURL;
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