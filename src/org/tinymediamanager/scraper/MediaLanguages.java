package org.tinymediamanager.scraper;

public class MediaLanguages {

  private String id;
  private String description;

  public MediaLanguages(String id, String description) {
    this.id = id;
    this.description = description;
  }

  public String getId() {
    return this.id;
  }

  public String getDescription() {
    return this.description;
  }

  public String toString() {
    return this.description;
  }

}
