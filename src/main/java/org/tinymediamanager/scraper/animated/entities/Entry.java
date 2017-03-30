package org.tinymediamanager.scraper.animated.entities;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "id", "source", "image", "type", "dateAdded", "contributedBy", "language", "size" })
public class Entry {

  @JsonProperty("id")
  private String              id;
  @JsonProperty("source")
  private String              source;
  @JsonProperty("image")
  private String              image;
  @JsonProperty("type")
  private String              type;
  @JsonProperty("dateAdded")
  private String              dateAdded;
  @JsonProperty("contributedBy")
  private String              contributedBy;
  @JsonProperty("language")
  private String              language;
  @JsonProperty("size")
  private String              size;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  /**
   * gets the high-res image
   * 
   * @return
   */
  public String getOriginal() {
    return FilenameUtils.getBaseName(image) + "_original." + FilenameUtils.getExtension(image);
  }

  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  @JsonProperty("source")
  public String getSource() {
    return source;
  }

  @JsonProperty("source")
  public void setSource(String source) {
    this.source = source;
  }

  @JsonProperty("image")
  public String getImage() {
    return image;
  }

  @JsonProperty("image")
  public void setImage(String image) {
    this.image = image;
  }

  @JsonProperty("type")
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  @JsonProperty("dateAdded")
  public String getDateAdded() {
    return dateAdded;
  }

  @JsonProperty("dateAdded")
  public void setDateAdded(String dateAdded) {
    this.dateAdded = dateAdded;
  }

  @JsonProperty("contributedBy")
  public String getContributedBy() {
    return contributedBy;
  }

  @JsonProperty("contributedBy")
  public void setContributedBy(String contributedBy) {
    this.contributedBy = contributedBy;
  }

  @JsonProperty("language")
  public String getLanguage() {
    return language;
  }

  @JsonProperty("language")
  public void setLanguage(String language) {
    this.language = language;
  }

  @JsonProperty("size")
  public String getSize() {
    return size;
  }

  @JsonProperty("size")
  public void setSize(String size) {
    this.size = size;
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
