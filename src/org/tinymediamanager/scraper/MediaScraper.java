package org.tinymediamanager.scraper;

import java.util.ArrayList;
import java.util.List;

import org.tinymediamanager.core.PluginManager;

/**
 * Class representing a MediaScraper; (type, info, description...)<br>
 * replacement of MovieScrapers /TvShowScrapers ENUM
 * 
 * @author Manuel Laggner
 */
public class MediaScraper {

  private String      id;
  private String      version;
  private String      name;
  private String      summary;
  private String      description;
  private ScraperType type;

  public MediaScraper(ScraperType type, String id, String name) {
    this.type = type;
    this.id = id;
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ScraperType getType() {
    return type;
  }

  public void setType(ScraperType type) {
    this.type = type;
  }

  /**
   * returns a MediaScraper from a given type - found via plugins<br>
   * use .toArray() for putting this in a ComboBox
   * 
   * @param type
   *          Movie or Tv
   * @return
   */
  public static List<MediaScraper> getMediaScraper(ScraperType type) {
    ArrayList<MediaScraper> scraper = new ArrayList<MediaScraper>();

    ArrayList<IMediaProvider> plugins = new ArrayList<IMediaProvider>();
    switch (type) {
      case MOVIE:
        plugins.addAll(PluginManager.getInstance().getMetadataPlugins());
        break;
      case TV_SHOW:
        plugins.addAll(PluginManager.getInstance().getTvShowPlugins());
        break;
      default:
        break;
    }

    // TODO: add XBMC scrapers
    for (IMediaProvider p : plugins) {
      MediaProviderInfo pi = p.getProviderInfo();
      MediaScraper ms = new MediaScraper(type, pi.getId(), pi.getName());
      ms.setSummary(pi.getDescription());
      scraper.add(ms);
    }
    return scraper;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MediaScraper other = (MediaScraper) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    if (type != other.type)
      return false;
    return true;
  }

}
