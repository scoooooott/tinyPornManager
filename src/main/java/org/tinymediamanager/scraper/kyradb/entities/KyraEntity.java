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

package org.tinymediamanager.scraper.kyradb.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.SerializedName;

public class KyraEntity {

  @SerializedName("base_url_backgrounds")
  private String              baseBackground;
  @SerializedName("base_url_posters")
  private String              basePosters;
  @SerializedName("base_url_character_art")
  private String              baseCharacter;
  @SerializedName("base_url_actor_art")
  private String              baseActor;
  @SerializedName("base_url_logos")
  private String              baseLogos;

  @SerializedName("backgrounds")
  private List<Image>         backgrounds          = null;
  @SerializedName("number_of_backgrounds")
  private int                 backgroundsCount     = 0;
  @SerializedName("posters")
  private List<Image>         posters              = null;
  @SerializedName("number_of_posters")
  private int                 postersCount         = 0;
  @SerializedName("character_art")
  private List<Image>         characters           = null;
  @SerializedName("number_of_character_art")
  private int                 charactersCount      = 0;
  @SerializedName("actor_art")
  private List<Image>         actors               = null;
  @SerializedName("number_of_actor_art")
  private int                 actorsCount          = 0;
  @SerializedName("logos")
  private List<Image>         logos                = null;
  @SerializedName("number_of_logos")
  private int                 logosCount           = 0;

  @SerializedName("error")
  int                         error                = 0;
  @SerializedName("message")
  String                      message;
  @SerializedName("timezone")
  String                      timezone;
  @SerializedName("timezone_str")
  String                      timezoneString;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<>();

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

  public String getBaseBackground() {
    return baseBackground;
  }

  public void setBaseBackground(String baseBackground) {
    this.baseBackground = baseBackground;
  }

  public String getBasePosters() {
    return basePosters;
  }

  public void setBasePosters(String basePosters) {
    this.basePosters = basePosters;
  }

  public String getBaseCharacter() {
    return baseCharacter;
  }

  public void setBaseCharacter(String baseCharacter) {
    this.baseCharacter = baseCharacter;
  }

  public String getBaseActor() {
    return baseActor;
  }

  public void setBaseActor(String baseActor) {
    this.baseActor = baseActor;
  }

  public String getBaseLogos() {
    return baseLogos;
  }

  public void setBaseLogos(String baseLogos) {
    this.baseLogos = baseLogos;
  }

  public List<Image> getBackgrounds() {
    return backgrounds;
  }

  public void setBackgrounds(List<Image> backgrounds) {
    this.backgrounds = backgrounds;
  }

  public int getBackgroundsCount() {
    return backgroundsCount;
  }

  public void setBackgroundsCount(int backgroundsCount) {
    this.backgroundsCount = backgroundsCount;
  }

  public List<Image> getPosters() {
    return posters;
  }

  public void setPosters(List<Image> posters) {
    this.posters = posters;
  }

  public int getPostersCount() {
    return postersCount;
  }

  public void setPostersCount(int postersCount) {
    this.postersCount = postersCount;
  }

  public List<Image> getCharacters() {
    return characters;
  }

  public void setCharacters(List<Image> characters) {
    this.characters = characters;
  }

  public int getCharactersCount() {
    return charactersCount;
  }

  public void setCharactersCount(int charactersCount) {
    this.charactersCount = charactersCount;
  }

  public List<Image> getActors() {
    return actors;
  }

  public void setActors(List<Image> actors) {
    this.actors = actors;
  }

  public int getActorsCount() {
    return actorsCount;
  }

  public void setActorsCount(int actorsCount) {
    this.actorsCount = actorsCount;
  }

  public List<Image> getLogos() {
    return logos;
  }

  public void setLogos(List<Image> logos) {
    this.logos = logos;
  }

  public int getLogosCount() {
    return logosCount;
  }

  public void setLogosCount(int logosCount) {
    this.logosCount = logosCount;
  }

  public int getError() {
    return error;
  }

  public void setError(int error) {
    this.error = error;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getTimezoneString() {
    return timezoneString;
  }

  public void setTimezoneString(String timezoneString) {
    this.timezoneString = timezoneString;
  }

  public void setAdditionalProperties(Map<String, Object> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }
}
