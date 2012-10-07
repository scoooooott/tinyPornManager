package org.tinymediamanager.scraper.thetvdb;

import java.util.List;

import org.apache.log4j.Logger;
import org.tinymediamanager.scraper.HasFindByIMDBID;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.ProviderInfo;
import org.tinymediamanager.scraper.SearchQuery;
import org.tinymediamanager.scraper.tmdb.TmdbMetadataProvider;

import com.moviejukebox.thetvdb.TheTVDB;

public class TheTvDbMetadataProvider implements IMediaMetadataProvider, HasFindByIMDBID {

  private static final Logger                  log      = Logger.getLogger(TmdbMetadataProvider.class);
  private static final TheTvDbMetadataProvider instance = new TheTvDbMetadataProvider();

  private TheTvDbMetadataProvider() {
    TheTVDB tvDB = new TheTVDB("1A4971671264D790");
  }

  @Override
  public MediaMetadata getMetadataForIMDBId(String imdbid) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ProviderInfo getInfo() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MediaMetadata getMetaData(MediaSearchResult result) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<MediaSearchResult> search(SearchQuery query) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MediaType[] getSupportedSearchTypes() {
    // TODO Auto-generated method stub
    return null;
  }

}
