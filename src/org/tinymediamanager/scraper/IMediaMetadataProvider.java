package org.tinymediamanager.scraper;

import java.util.List;

public interface IMediaMetadataProvider {
    public ProviderInfo getInfo();
    public MediaMetadata getMetaData(MediaSearchResult result) throws Exception;
    public List<MediaSearchResult> search(SearchQuery query) throws Exception;
    public MediaType[] getSupportedSearchTypes();
}

