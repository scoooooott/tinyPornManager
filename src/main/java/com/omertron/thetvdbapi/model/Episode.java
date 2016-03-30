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
public class Episode implements Serializable {

    // Default serial UID
    private static final long serialVersionUID = 1L;
    private String id;
    private String combinedEpisodeNumber;
    private String combinedSeason;
    private String dvdChapter;
    private String dvdDiscId;
    private String dvdEpisodeNumber;
    private String dvdSeason;
    private List<String> directors = new ArrayList<String>();
    private String epImgFlag;
    private String episodeName;
    private int episodeNumber;
    private String firstAired;
    private List<String> guestStars = new ArrayList<String>();
    private String imdbId;
    private String language;
    private String overview;
    private String productionCode;
    private String rating;
    private String ratingCount;
    private int seasonNumber;
    private List<String> writers = new ArrayList<String>();
    private String absoluteNumber;
    private int airsAfterSeason;
    private int airsBeforeSeason;
    private int airsBeforeEpisode;
    private String filename;
    private String lastUpdated;
    private String seriesId;
    private String seasonId;

    public String getId() {
        return id;
    }

    public String getAbsoluteNumber() {
        return absoluteNumber;
    }

    public int getAirsAfterSeason() {
        return airsAfterSeason;
    }

    public int getAirsBeforeEpisode() {
        return airsBeforeEpisode;
    }

    public int getAirsBeforeSeason() {
        return airsBeforeSeason;
    }

    public String getCombinedEpisodeNumber() {
        return combinedEpisodeNumber;
    }

    public String getCombinedSeason() {
        return combinedSeason;
    }

    public List<String> getDirectors() {
        return directors;
    }

    public String getDvdChapter() {
        return dvdChapter;
    }

    public String getDvdDiscId() {
        return dvdDiscId;
    }

    public String getDvdEpisodeNumber() {
        return dvdEpisodeNumber;
    }

    public String getDvdSeason() {
        return dvdSeason;
    }

    public String getEpImgFlag() {
        return epImgFlag;
    }

    public String getEpisodeName() {
        return episodeName;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public String getFilename() {
        return filename;
    }

    public String getFirstAired() {
        return firstAired;
    }

    public List<String> getGuestStars() {
        return guestStars;
    }

    public String getImdbId() {
        return imdbId;
    }

    public String getLanguage() {
        return language;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public String getOverview() {
        return overview;
    }

    public String getProductionCode() {
        return productionCode;
    }

    public String getRating() {
        return rating;
    }
    
    public String getRatingCount(){
        return ratingCount;
    }

    public String getSeasonId() {
        return seasonId;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public String getSeriesId() {
        return seriesId;
    }

    public List<String> getWriters() {
        return writers;
    }

    public void setAbsoluteNumber(String absoluteNumber) {
        this.absoluteNumber = absoluteNumber;
    }

    public void setAirsAfterSeason(int airsAfterSeason) {
        this.airsAfterSeason = airsAfterSeason;
    }

    public void setAirsBeforeEpisode(int airsBeforeEpisode) {
        this.airsBeforeEpisode = airsBeforeEpisode;
    }

    public void setAirsBeforeSeason(int airsBeforeSeason) {
        this.airsBeforeSeason = airsBeforeSeason;
    }

    public void setCombinedEpisodeNumber(String combinedEpisodeNumber) {
        this.combinedEpisodeNumber = combinedEpisodeNumber;
    }

    public void setCombinedSeason(String combinedSeason) {
        this.combinedSeason = combinedSeason;
    }

    public void setDirectors(List<String> directors) {
        this.directors = directors;
    }

    public void setDvdChapter(String dvdChapter) {
        this.dvdChapter = dvdChapter;
    }

    public void setDvdDiscId(String dvdDiscId) {
        this.dvdDiscId = dvdDiscId;
    }

    public void setDvdEpisodeNumber(String dvdEpisodeNumber) {
        this.dvdEpisodeNumber = dvdEpisodeNumber;
    }

    public void setDvdSeason(String dvdSeason) {
        this.dvdSeason = dvdSeason;
    }

    public void setEpImgFlag(String epImgFlag) {
        this.epImgFlag = epImgFlag;
    }

    public void setEpisodeName(String episodeName) {
        this.episodeName = episodeName;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setFirstAired(String firstAired) {
        this.firstAired = firstAired;
    }

    public void setGuestStars(List<String> guestStars) {
        this.guestStars = guestStars;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setProductionCode(String productionCode) {
        this.productionCode = productionCode;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setRatingCount(String ratingCount){
        this.ratingCount = ratingCount;
    }
    
    public void setSeasonId(String seasonId) {
        this.seasonId = seasonId;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    public void setWriters(List<String> writers) {
        this.writers = writers;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
