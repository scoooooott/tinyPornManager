package xyz.ifnotnull.tmm.scraper.pornhub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.ArtworkSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMovieArtworkProvider;

import java.util.List;
import java.util.ResourceBundle;

public class PornhubMovieArtworkProvider implements IMovieArtworkProvider {

  private static final Logger logger = LoggerFactory.getLogger(PornhubMovieArtworkProvider.class);

  private final MediaProviderInfo providerInfo;

  public PornhubMovieArtworkProvider() {
    this.providerInfo = createProviderInfo();
  }

  private MediaProviderInfo createProviderInfo() {
    MediaProviderInfo info = new MediaProviderInfo(PornhubMovieMetadataProvider.ID, "movie_artwork", "Pornhub", "Scraper addon for Pornhub",
        PornhubMovieMetadataProvider.class.getResource("/xyz/ifnotnull/tmm/scraper/pornhub/pornhub_logo.svg"));
    // the ResourceBundle to offer i18n support for scraper options
    info.setResourceBundle(ResourceBundle.getBundle("xyz.ifnotnull.tmm.scraper.pornhub.messages"));

    // create configuration properties
    info.getConfig().addText("text", "", false);
    info.getConfig().addBoolean("boolean", true);
    info.getConfig().addInteger("integer", 10);
    info.getConfig().addSelect("select", new String[] { "A", "B", "C" }, "A");

    // load any existing values from the storage
    info.getConfig().load();

    return info;
  }

  /**
   * Gets a general information about the metadata provider
   *
   * @return the provider info containing metadata of the provider
   */
  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  /**
   * indicates whether this scraper is active or not (private and valid API key OR public to be active)
   *
   * @return true/false
   */
  @Override
  public boolean isActive() {
    return true;
  }

  /**
   * Gets the artwork.
   *
   * @param options
   *     the options
   * @return the artwork
   * @throws ScrapeException
   *     any exception which can be thrown while scraping
   * @throws MissingIdException
   *     indicates that there was no usable id to scrape
   */
  @Override
  public List<MediaArtwork> getArtwork(ArtworkSearchAndScrapeOptions options) throws ScrapeException, MissingIdException {
    logger.debug("getArtwork(): {}", options);
    return options.getMetadata().getMediaArt();
  }
}
