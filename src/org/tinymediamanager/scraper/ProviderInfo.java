package org.tinymediamanager.scraper;

public class ProviderInfo{
    private static final long serialVersionUID = 1L;
    private String            id, name, description, iconUrl;

    public ProviderInfo() {
    }

    public ProviderInfo(String id, String name, String description, String iconUrl) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
