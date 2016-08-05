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
import org.apache.commons.lang3.math.NumberUtils;

/**
 *
 * @author altman.matthew
 */
public class Banner implements Serializable {

    // Default serial UID
    private static final long serialVersionUID = 1L;
    private int id;
    private String url;
    private BannerListType bannerType;
    private BannerType bannerType2;
    private String colours;
    private Float rating;
    private int ratingCount;
    private String language;
    private boolean seriesName;
    private String thumb;
    private String vignette;
    private int season = 0;

    public int getId() {
        return id;
    }

    public BannerListType getBannerType() {
        return bannerType;
    }

    public BannerType getBannerType2() {
        return bannerType2;
    }

    public Float getRating() {
        return rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public String getColours() {
        return colours;
    }

    public String getLanguage() {
        return language;
    }

    public int getSeason() {
        return season;
    }

    public boolean isSeriesName() {
        return seriesName;
    }

    public String getThumb() {
        return thumb;
    }

    public String getUrl() {
        return url;
    }

    public String getVignette() {
        return vignette;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setVignette(String vignette) {
        this.vignette = vignette;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public void setSeason(String season) {
        this.season = NumberUtils.toInt(season, 0);
    }

    public void setBannerType(BannerListType bannerType) {
        this.bannerType = bannerType;
    }

    public void setBannerType2(BannerType bannerType2) {
        this.bannerType2 = bannerType2;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = NumberUtils.toInt(id, 0);
    }

    public void setColours(String colours) {
        this.colours = colours;
    }

    public void setRating(String rating) {
        this.rating = NumberUtils.toFloat(rating, 0f);
    }

    public void setRatingCount(String ratingCount) {
        this.ratingCount = NumberUtils.toInt(ratingCount, 0);
    }

    public void setSeriesName(boolean seriesName) {
        this.seriesName = seriesName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
