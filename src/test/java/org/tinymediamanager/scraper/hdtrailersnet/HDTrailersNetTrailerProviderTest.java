package org.tinymediamanager.scraper.hdtrailersnet;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.Test;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMovieTrailerProvider;

public class HDTrailersNetTrailerProviderTest {

  @Test
  public void testScrapeTrailer() {
    IMovieTrailerProvider mp;
    try {
      mp = new HDTrailersNetTrailerProvider();

      MediaScrapeOptions options = new MediaScrapeOptions(MediaType.MOVIE);

      MediaMetadata md = new MediaMetadata("foo");
      md.storeMetadata(MediaMetadata.ORIGINAL_TITLE, "Iron Man 3");
      options.setMetadata(md);

      List<MediaTrailer> trailers = mp.getTrailers(options);
      assertThat(trailers).isNotNull().isNotEmpty();

      MediaTrailer trailer = trailers.get(0);
      assertThat(trailer.getName()).isNotEmpty();
      assertThat(trailer.getUrl()).isNotEmpty();
      assertThat(trailer.getProvider()).isNotEmpty();
      assertThat(trailer.getQuality()).isNotEmpty();
    }
    catch (Exception e) {
      fail(e.getMessage());
    }
  }
}
