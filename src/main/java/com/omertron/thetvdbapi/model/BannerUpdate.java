/*
 *      Copyright (c) 2004-2016 Matthew Altman & Stuart Boston
 *
 *      This file is part of TheTVDB API.
 *
 *      TheTVDB API is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      any later version.
 *
 *      TheTVDB API is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with TheTVDB API.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.omertron.thetvdbapi.model;

import java.io.Serializable;

public class BannerUpdate extends BaseUpdate implements Serializable {

  // Default serial UID
  private static final long serialVersionUID = 1L;
  private String            seasonNum;
  private String            format;
  private String            language;
  private String            path;
  private String            type;

  public String getSeasonNum() {
    return seasonNum;
  }

  public String getFormat() {
    return format;
  }

  public String getLanguage() {
    return language;
  }

  public String getPath() {
    return path;
  }

  public String getType() {
    return type;
  }

  public void setSeasonNum(String seasonNum) {
    this.seasonNum = seasonNum;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setType(String type) {
    this.type = type;
  }

}
