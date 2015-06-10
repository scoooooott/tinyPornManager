package org.tinymediamanager.scraper;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.PluginManager;
import org.tinymediamanager.scraper.xbmc.XbmcScraper;
import org.tinymediamanager.scraper.xbmc.XbmcUtil;

/**
 * Class representing a MediaScraper; (type, info, description...)<br>
 * replacement of MovieScrapers /TvShowScrapers ENUM
 * 
 * @author Manuel Laggner
 */
public class MediaScraper {
  private String         id;
  private String         version;
  private String         name;
  private String         summary;
  private String         description;
  private URL            logoUrl;
  private ScraperType    type;
  private IMediaProvider mediaProvider;
  private boolean        xbmcScraper = false;

  public MediaScraper(ScraperType type, IMediaProvider mediaProvider, String id, String name) {
    this.mediaProvider = mediaProvider;
    this.type = type;
    this.id = id;
    this.name = name;
    if (mediaProvider != null) {
      this.description = this.summary = mediaProvider.getProviderInfo().getDescription();
      this.logoUrl = mediaProvider.getProviderInfo().getProviderLogo();
    }
    else {
      this.description = "";
      this.summary = "";
    }
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

  public boolean isXbmcScraper() {
    return xbmcScraper;
  }

  public void setXbmcScraper(boolean xbmcScraper) {
    this.xbmcScraper = xbmcScraper;
  }

  public IMediaProvider getMediaProvider() {
    return this.mediaProvider;
  }

  public URL getLogoURL() {
    return this.logoUrl;
  }

  /**
   * returns a MediaScraper from a given type - found via plugins<br>
   * use .toArray() for putting this in a ComboBox
   * 
   * @param type
   *          Movie or Tv
   * @return
   */
  public static List<MediaScraper> getMediaScrapers(ScraperType type) {
    ArrayList<MediaScraper> scraper = new ArrayList<MediaScraper>();

    ArrayList<IMediaProvider> plugins = new ArrayList<IMediaProvider>();
    switch (type) {
      case MOVIE:
        plugins.addAll(PluginManager.getInstance().getMoviePlugins());
        break;
      case TV_SHOW:
        plugins.addAll(PluginManager.getInstance().getTvShowPlugins());
        break;
      case ARTWORK:
        plugins.addAll(PluginManager.getInstance().getArtworkPlugins());
        break;
      case TRAILER:
        plugins.addAll(PluginManager.getInstance().getTrailerPlugins());
        break;
      case SUBTITLE:
        plugins.addAll(PluginManager.getInstance().getSubtitlePlugins());
        break;
      default:
        break;
    }
    // plugins.remove(XbmcMetadataProvider.class); // remove the "base" xbmc scraper

    for (IMediaProvider p : plugins) {
      MediaProviderInfo pi = p.getProviderInfo();
      MediaScraper ms = new MediaScraper(type, p, pi.getId(), pi.getName());
      scraper.add(ms);
    }

    // XBMC scrapers
    for (XbmcScraper sc : XbmcUtil.getAllScrapers()) {
      MediaScraper ms = (MediaScraper) sc;
      if (ms.getType() == type) {
        scraper.add(ms);
      }
    }

    return scraper;
  }

  public static MediaScraper getMediaScraperById(String id, ScraperType type) {
    if (StringUtils.isBlank(id)) {
      return null;
    }

    List<MediaScraper> scrapers = getMediaScrapers(type);
    for (MediaScraper scraper : scrapers) {
      if (scraper.id.equals(id)) {
        return scraper;
      }
    }
    return null;
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
