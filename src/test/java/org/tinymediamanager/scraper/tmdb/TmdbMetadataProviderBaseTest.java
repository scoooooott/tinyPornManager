package org.tinymediamanager.scraper.tmdb;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;
import org.tinymediamanager.scraper.mediaprovider.IMovieSetMetadataProvider;
import org.tinymediamanager.scraper.mediaprovider.ITvShowMetadataProvider;

import java.util.List;

/**
 * @author Nikolas Mavropoylos
 */
public abstract class TmdbMetadataProviderBaseTest {

    TmdbMetadataProvider metadataProvider = null;
    IMovieMetadataProvider movieMetadataProvider;
    ITvShowMetadataProvider tvShowMetadataProvider;
    IMovieSetMetadataProvider movieSetMetadataProvider;

    MediaScrapeOptions scrapeOptions = null;
    MediaSearchOptions searchOptions = null;
    List<MediaSearchResult> searchResults = null;
    MediaMetadata md = null;

    static final Logger LOGGER = LoggerFactory.getLogger(TmdbMetadataProviderBaseTest.class);

    @Before
    public void setUp() throws Exception {
        metadataProvider = new TmdbMetadataProvider();
        movieMetadataProvider = metadataProvider;
        tvShowMetadataProvider = metadataProvider;
        movieSetMetadataProvider = metadataProvider;

        searchResults = null;
    }
}
