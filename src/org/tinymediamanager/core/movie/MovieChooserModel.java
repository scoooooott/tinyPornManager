package org.tinymediamanager.core.movie;

import java.io.IOException;
import java.util.List;

import org.tinymediamanager.core.AbstractModelObject;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaArt;
import org.tinymediamanager.scraper.MediaArtifactType;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;

public class MovieChooserModel extends AbstractModelObject {

  // private net.sf.jtmdb.Movie movie;
  private IMediaMetadataProvider metadataProvider;
  private MediaSearchResult      result;
  private MediaMetadata          metadata;
  private String                 name;
  private String                 overview;
  private String                 year;
  private String                 combinedName;
  private String                 posterUrl;
  private boolean                scraped = false;

  /* new scraper logic */
  public MovieChooserModel(IMediaMetadataProvider metadataProvider, MediaSearchResult result) {
    this.metadataProvider = metadataProvider;
    this.result = result;
    // name
    setName(result.getTitle());
    // year
    setYear(result.getYear());
    // combined name (name (year))
    setCombinedName();
  }

  public void setName(String name) {
    String oldValue = this.name;
    this.name = name;
    firePropertyChange("name", oldValue, name);
  }

  public void setOverview(String overview) {
    String oldValue = this.overview;
    this.overview = overview;
    firePropertyChange("overview", oldValue, overview);
  }

  public String getName() {
    return name;
  }

  public String getOverview() {
    // if (metadata == null) {
    // scrapeMetaData();
    // }
    return overview;
  }

  public String getPosterUrl() {
    return posterUrl;
  }

  public void setPosterUrl(String newValue) {
    String oldValue = posterUrl;
    posterUrl = newValue;
    firePropertyChange("posterUrl", oldValue, newValue);
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    String oldValue = this.year;
    this.year = year;
    firePropertyChange("year", oldValue, year);
  }

  public void setCombinedName() {
    String oldValue = this.combinedName;
    this.combinedName = getName() + " (" + getYear() + ")";
    firePropertyChange("combinedName", oldValue, this.combinedName);
  }

  public String getCombinedName() {
    return combinedName;
  }

  public void scrapeMetaData() {
    try {
      metadata = metadataProvider.getMetaData(result);
      setOverview(metadata.getPlot());

      // poster for preview
      List<MediaArt> mediaArt = metadata.getFanart();
      for (MediaArt art : mediaArt) {
        if (art.getType() == MediaArtifactType.POSTER) {
          setPosterUrl(art.getDownloadUrl());

          break;
        }
      }

      scraped = true;

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public MediaMetadata getMetadata() {
    // if (metadata == null) {
    // scrapeMetaData();
    // }
    return metadata;
  }

  public boolean isScraped() {
    return scraped;
  }

}
