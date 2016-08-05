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
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TVDBUpdates implements Serializable {

    // Default serial UID
    private static final long serialVersionUID = 1L;
    private String time;
    private List<SeriesUpdate> seriesUpdates;
    private List<EpisodeUpdate> episodeUpdates;
    private List<BannerUpdate> bannerUpdates;

    public String getTime() {
        return time;
    }

    public List<SeriesUpdate> getSeriesUpdates() {
        return seriesUpdates;
    }

    public List<EpisodeUpdate> getEpisodeUpdates() {
        return episodeUpdates;
    }

    public List<BannerUpdate> getBannerUpdates() {
        return bannerUpdates;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setSeriesUpdates(List<SeriesUpdate> seriesUpdates) {
        this.seriesUpdates = seriesUpdates;
    }

    public void setEpisodeUpdates(List<EpisodeUpdate> episodeUpdates) {
        this.episodeUpdates = episodeUpdates;
    }

    public void setBannerUpdates(List<BannerUpdate> bannerUpdates) {
        this.bannerUpdates = bannerUpdates;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
