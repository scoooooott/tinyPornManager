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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 * @author matthew.altman
 */
public class Banners implements Serializable {

    // Default serial UID
    private static final long serialVersionUID = 1L;
    private int seriesId = 0;
  private List<Banner>      seriesList       = new ArrayList<>();
  private List<Banner>      seasonList       = new ArrayList<>();
  private List<Banner>      posterList       = new ArrayList<>();
  private List<Banner>      fanartList       = new ArrayList<>();

    public int getSeriesId() {
        return seriesId;
    }

    public List<Banner> getSeriesList() {
        return seriesList;
    }

    public List<Banner> getSeasonList() {
        return seasonList;
    }

    public List<Banner> getPosterList() {
        return posterList;
    }

    public List<Banner> getFanartList() {
        return fanartList;
    }

    public void setSeriesId(int seriesId) {
        this.seriesId = seriesId;
    }

    public void setSeriesList(List<Banner> seriesList) {
        this.seriesList = seriesList;
    }

    public void addSeriesBanner(Banner banner) {
        this.seriesList.add(banner);
    }

    public void setSeasonList(List<Banner> seasonList) {
        this.seasonList = seasonList;
    }

    public void addSeasonBanner(Banner banner) {
        this.seasonList.add(banner);
    }

    public void setPosterList(List<Banner> posterList) {
        this.posterList = posterList;
    }

    public void addPosterBanner(Banner banner) {
        this.posterList.add(banner);
    }

    public void setFanartList(List<Banner> fanartList) {
        this.fanartList = fanartList;
    }

    public void addFanartBanner(Banner banner) {
        this.fanartList.add(banner);
    }

    public void addBanner(Banner banner) {
        if (banner != null) {
            if (banner.getBannerType() == BannerListType.SERIES) {
                addSeriesBanner(banner);
            } else if (banner.getBannerType() == BannerListType.SEASON) {
                addSeasonBanner(banner);
            } else if (banner.getBannerType() == BannerListType.POSTER) {
                addPosterBanner(banner);
            } else if (banner.getBannerType() == BannerListType.FANART) {
                addFanartBanner(banner);
            }
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
