package org.tinymediamanager.scraper.pornhub.v1.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class InteractionStatistic {

    @JsonAlias("@type")
    private String type;

    private String interactionType;

    private String userInteractionCount;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(String interactionType) {
        this.interactionType = interactionType;
    }

    public String getUserInteractionCount() {
        return userInteractionCount;
    }

    public void setUserInteractionCount(String userInteractionCount) {
        this.userInteractionCount = userInteractionCount;
    }
}

