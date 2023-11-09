package org.tinymediamanager.scraper.spisample;

import org.joda.time.DateTime;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaCertification;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMovieMetadataProvider;

import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;

public class SampleMovieMetadataProvider implements IMovieMetadataProvider {

  private final MediaProviderInfo providerInfo;

  public SampleMovieMetadataProvider() {
    providerInfo = createProviderInfo();
  }

  private MediaProviderInfo createProviderInfo() {
    MediaProviderInfo info = new MediaProviderInfo("spi-sample", "movie", "SPI Sample", "A sample for a dynamic movie metadata scraper", SampleMovieMetadataProvider.class.getResource("/org/tinymediamanager/scraper/spisample/tmm_logo.svg"));
    // the ResourceBundle to offer i18n support for scraper options
    info.setResourceBundle(ResourceBundle.getBundle("org.tinymediamanager.scraper.spisample.messages"));

    // create configuration properties
    info.getConfig().addText("text", "", false);
    info.getConfig().addBoolean("boolean", true);
    info.getConfig().addInteger("integer", 10);
    info.getConfig().addSelect("select", new String[]{"A", "B", "C"}, "A");

    // load any existing values from the storage
    info.getConfig().load();

    return info;
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public SortedSet<MediaSearchResult> search(MovieSearchAndScrapeOptions movieSearchAndScrapeOptions) throws ScrapeException {
    SortedSet<MediaSearchResult> results = new TreeSet<>();
    MediaSearchResult result = new MediaSearchResult(getId(), MediaType.MOVIE);

    // set the values of the search result.
    // id, title, year and score is needed
    result.setId(getId());
    result.setTitle("This is a result");
    result.setYear(2021);

    // set the search score. should be calculated from the search query
    // you can use the helper either
    result.calculateScore(movieSearchAndScrapeOptions);
    // or set the score directly (0...1)
    result.setScore(1.0f);

    results.add(result);

    // more detailed example can be found in the tmm repo:
    // https://gitlab.com/tinyMediaManager/tinyMediaManager/-/tree/devel/src/main/java/org/tinymediamanager/scraper

    return results;
  }

  @Override
  public MediaMetadata getMetadata(MovieSearchAndScrapeOptions movieSearchAndScrapeOptions) throws ScrapeException {
    MediaMetadata mediaMetadata = new MediaMetadata(getId());

    // here you may set all available data
    mediaMetadata.setTitle("title");
    mediaMetadata.setOriginalTitle("original title");
    mediaMetadata.setPlot("the plot of the movie");
    mediaMetadata.setYear(2021);
    mediaMetadata.setReleaseDate(DateTime.parse("2010-06-30"));
    mediaMetadata.setId(getId(), 1234);
    mediaMetadata.setId(MediaMetadata.IMDB, "tt123456");
    mediaMetadata.addRating(new MediaRating(getId(), 5.5f, 200, 10));
    mediaMetadata.addRating(new MediaRating(MediaMetadata.IMDB, 7.8f, 20000));
    mediaMetadata.addCertification(MediaCertification.US_G);
    mediaMetadata.addCertification(MediaCertification.DE_FSK12);

    // more detailed example can be found in the tmm repo:
    // https://gitlab.com/tinyMediaManager/tinyMediaManager/-/tree/devel/src/main/java/org/tinymediamanager/scraper

    return mediaMetadata;
  }
}

