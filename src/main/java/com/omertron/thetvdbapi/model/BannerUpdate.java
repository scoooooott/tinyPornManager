/*
 *      Copyright (c) 2004-2015 Matthew Altman & Stuart Boston
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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class BannerUpdate implements Serializable {

    // Default serial UID
    private static final long serialVersionUID = 1L;
    private String seasonNum;
    private String series;
    private String format;
    private String language;
    private String path;
    private String time;
    private String type;

    public String getSeasonNum() {
        return seasonNum;
    }

    public String getSeries() {
        return series;
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

    public String getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public void setSeasonNum(String seasonNum) {
        this.seasonNum = seasonNum;
    }

    public void setSeries(String series) {
        this.series = series;
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

    public void setTime(String time) {
        this.time = time;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}