package org.tinymediamanager.scraper.pornhub.v1.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.Date;
import java.util.List;

public class LDJson {
    @JsonAlias("@context")
    private String context;
    @JsonAlias("@type")
    private String type;

    private String name;

    private String embedUrl;

    private String duration;

    private String thumbnailUrl;

    private Date uploadDate;

    private String description;

    private String author;

    private List<InteractionStatistic> interactionStatistic;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmbedUrl() {
        return embedUrl;
    }

    public void setEmbedUrl(String embedUrl) {
        this.embedUrl = embedUrl;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<InteractionStatistic> getInteractionStatistic() {
        return interactionStatistic;
    }

    public void setInteractionStatistic(
        List<InteractionStatistic> interactionStatistic) {
        this.interactionStatistic = interactionStatistic;
    }
}