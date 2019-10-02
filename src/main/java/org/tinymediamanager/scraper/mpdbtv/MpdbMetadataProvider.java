/*
 * Copyright 2012 - 2019 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.scraper.mpdbtv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaCastMember;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.tinymediamanager.scraper.entities.MediaRating;
import org.tinymediamanager.scraper.entities.MediaTrailer;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.exceptions.HttpException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.mediaprovider.IMovieMetadataProvider;
import org.tinymediamanager.scraper.mpdbtv.entities.Actor;
import org.tinymediamanager.scraper.mpdbtv.entities.Director;
import org.tinymediamanager.scraper.mpdbtv.entities.DiscArt;
import org.tinymediamanager.scraper.mpdbtv.entities.Fanart;
import org.tinymediamanager.scraper.mpdbtv.entities.Genre;
import org.tinymediamanager.scraper.mpdbtv.entities.HDClearArt;
import org.tinymediamanager.scraper.mpdbtv.entities.HDLogo;
import org.tinymediamanager.scraper.mpdbtv.entities.MovieEntity;
import org.tinymediamanager.scraper.mpdbtv.entities.Poster;
import org.tinymediamanager.scraper.mpdbtv.entities.Producer;
import org.tinymediamanager.scraper.mpdbtv.entities.SearchEntity;
import org.tinymediamanager.scraper.mpdbtv.entities.Studio;
import org.tinymediamanager.scraper.mpdbtv.entities.Trailer;
import org.tinymediamanager.scraper.mpdbtv.services.Controller;
import org.tinymediamanager.scraper.util.ApiKey;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class MpdbMetadataProvider implements IMovieMetadataProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(MpdbMetadataProvider.class);
  private static final MediaProviderInfo providerInfo = createMediaProviderInfo();
  private static final String API_KEY = ApiKey.decryptApikey("DdSGUTZn24ml7rZRBihKb9ea3svKUDnU3GZdhgf+XMrfE8IdLinpy6eAPLrmkZWu");
  private static final String FORMAT = "json";

  private Controller controller;

  public MpdbMetadataProvider() {
    this.controller = new Controller(false);
  }

  private static MediaProviderInfo createMediaProviderInfo() {
    MediaProviderInfo providerInfo = new MediaProviderInfo("mpdbtv", "mpdb.tv",
            "<html><h3>MPDB.tv</h3><br />The MPDB.tv API is a RESTful web service to obtain movie information, all content and images on the site are contributed and maintained by our users. <br /><br />This is a private meta data provider, you may need to become a member there to use this service (more infos at http://www.mpdb.tv/)<br /><br />Available languages: FR</html>\"",
            MpdbMetadataProvider.class.getResource("/mpdbtv.png"));

    providerInfo.setVersion(MpdbMetadataProvider.class);
    providerInfo.getConfig().addText("aboKey", "", false);
    providerInfo.getConfig().addText("username", "", false);
    providerInfo.getConfig().load();

    return providerInfo;
  }

  @Override
  public List<MediaSearchResult> search(MediaSearchOptions mediaSearchOptions) throws ScrapeException {
    LOGGER.debug("search() {} ", mediaSearchOptions);

    List<MediaSearchResult> mediaResult = new ArrayList<>();
    List<SearchEntity> searchResult;

    if (StringUtils.isAnyBlank(getAboKey(), getUserName())) {
      LOGGER.warn("no username/ABO Key found");
      throw new ScrapeException(new HttpException(401, "Unauthorized"));
    }

    LOGGER.info("========= BEGIN MPDB.tv Scraper Search for Movie: {} ", mediaSearchOptions.getQuery());

    try {
      searchResult = controller.getSearchInformation(API_KEY, getEncodedUserName(), getSubscriptionKey(), mediaSearchOptions.getQuery(),
              mediaSearchOptions.getLanguage(), true, FORMAT);
    } catch (IOException e) {
      LOGGER.error("error searching: {} ", e.getMessage());
      throw new ScrapeException(e);
    }

    if (searchResult == null) {
      LOGGER.warn("no result from MPDB.tv");
      return mediaResult;
    }

    for (SearchEntity entity : searchResult) {

      MediaSearchResult result = new MediaSearchResult(MpdbMetadataProvider.providerInfo.getId(), MediaType.MOVIE);
      result.setId(providerInfo.getId(), entity.id);
      result.setOriginalTitle(StringEscapeUtils.unescapeHtml4(entity.original_title));
      if (StringUtils.isEmpty(entity.title)) {
        result.setTitle(StringEscapeUtils.unescapeHtml4(entity.original_title));
      } else {
        result.setTitle(StringEscapeUtils.unescapeHtml4(entity.title));
      }
      result.setYear(entity.year);
      result.setId("imdb_id", entity.id_imdb);
      result.setId("allocine_id", entity.id_allocine);
      result.setUrl(entity.url);
      result.setPosterUrl(entity.posterUrl);

      mediaResult.add(result);
    }

    return mediaResult;
  }

  @Override
  public MediaMetadata getMetadata(MediaScrapeOptions mediaScrapeOptions) throws ScrapeException {

    LOGGER.debug("scrape() {}", mediaScrapeOptions);

    MediaMetadata metadata = new MediaMetadata(MpdbMetadataProvider.providerInfo.getId());
    MovieEntity scrapeResult;

    if (StringUtils.isAnyBlank(getAboKey(), getUserName())) {
      LOGGER.warn("no username/ABO Key found");
      throw new ScrapeException(new HttpException(401, "Unauthorized"));
    }

    LOGGER.info("========= BEGIN MPDB.tv scraping");
    try {
      scrapeResult = controller.getScrapeInformation(API_KEY, getEncodedUserName(), getSubscriptionKey(),
              Integer.parseInt(mediaScrapeOptions.getIdAsString(providerInfo.getId())), mediaScrapeOptions.getLanguage(), null, FORMAT);
    } catch (IOException e) {
      LOGGER.error("error searching: {} ", e.getMessage());
      throw new ScrapeException(e);
    }

    if (scrapeResult == null) {
      LOGGER.warn("no result from MPDB.tv");
      return metadata;
    }

    // Rating
    if (scrapeResult.rating != null) {
      MediaRating rating = new MediaRating(providerInfo.getId());
      rating.setRating(scrapeResult.rating);
      rating.setVoteCount(scrapeResult.ratingVotes);
      rating.setMaxValue(10);

      metadata.addRating(rating);
    }

    // Genres
    ArrayList<MediaGenres> mediaGenres = new ArrayList<>();

    for (Genre genre : scrapeResult.genres) {
      mediaGenres.add(MediaGenres.getGenre(genre.name));
    }
    metadata.setGenres(mediaGenres);

    // Trailers
    ArrayList<MediaTrailer> mediaTrailers = new ArrayList<>();

    for (Trailer trailer : scrapeResult.trailers) {
      MediaTrailer mt = new MediaTrailer();
      mt.setName(scrapeResult.title);
      mt.setUrl(trailer.url);
      mt.setQuality(trailer.quality);

      mediaTrailers.add(mt);
    }
    metadata.setTrailers(mediaTrailers);

    // Studios
    ArrayList<String> productionCompanies = new ArrayList<>();

    for (Studio studio : scrapeResult.studios) {
      productionCompanies.add(studio.name);
    }

    metadata.setProductionCompanies(productionCompanies);

    // Cast
    ArrayList<MediaCastMember> castMembers = new ArrayList<>();

    for (Director director : scrapeResult.directors) {
      MediaCastMember mediaCastMember = new MediaCastMember(MediaCastMember.CastType.DIRECTOR);
      mediaCastMember.setName(director.name);
      mediaCastMember.setPart(director.departement);
      mediaCastMember.setImageUrl(director.thumb);
      mediaCastMember.setCharacter(director.role);
      mediaCastMember.setId(providerInfo.getId(), director.id);

      castMembers.add(mediaCastMember);
    }

    for (Actor actor : scrapeResult.actors) {
      MediaCastMember mediaCastMember = new MediaCastMember(MediaCastMember.CastType.ACTOR);
      mediaCastMember.setId(providerInfo.getId(), actor.id);
      mediaCastMember.setName(actor.name);
      mediaCastMember.setPart(actor.departement);
      mediaCastMember.setImageUrl(actor.thumb);
      mediaCastMember.setCharacter(actor.role);

      castMembers.add(mediaCastMember);
    }

    for (Producer producer : scrapeResult.producers) {
      MediaCastMember mediaCastMember = new MediaCastMember(MediaCastMember.CastType.PRODUCER);
      mediaCastMember.setId(providerInfo.getId(), producer.id);
      mediaCastMember.setName(producer.name);
      mediaCastMember.setPart(producer.departement);
      mediaCastMember.setImageUrl(producer.thumb);
      mediaCastMember.setCharacter(producer.role);

      castMembers.add(mediaCastMember);
    }
    metadata.setCastMembers(castMembers);

    // Poster
    for (Poster poster : scrapeResult.posters) {
      MediaArtwork mediaArtwork = new MediaArtwork(providerInfo.getId(), MediaArtwork.MediaArtworkType.POSTER);
      mediaArtwork.setPreviewUrl(poster.preview);
      mediaArtwork.setDefaultUrl(poster.original);
      mediaArtwork.setLikes(poster.votes);

      metadata.addMediaArt(mediaArtwork);
    }

    // Fanarts
    for (Fanart fanart : scrapeResult.fanarts) {
      MediaArtwork mediaArtwork = new MediaArtwork(providerInfo.getId(), MediaArtwork.MediaArtworkType.BACKGROUND);
      mediaArtwork.setPreviewUrl(fanart.preview);
      mediaArtwork.setDefaultUrl(fanart.original);
      mediaArtwork.setLikes(fanart.votes);

      metadata.addMediaArt(mediaArtwork);
    }

    // DiscArt
    for (DiscArt discArt : scrapeResult.discarts) {
      MediaArtwork mediaArtwork = new MediaArtwork(providerInfo.getId(), MediaArtwork.MediaArtworkType.DISC);
      mediaArtwork.setPreviewUrl(discArt.preview);
      mediaArtwork.setDefaultUrl(discArt.original);
      mediaArtwork.setLikes(discArt.votes);

      metadata.addMediaArt(mediaArtwork);
    }

    // HDClearArt
    for (HDClearArt hdClearArt : scrapeResult.hdcleararts) {
      MediaArtwork mediaArtwork = new MediaArtwork(providerInfo.getId(), MediaArtwork.MediaArtworkType.CLEARART);
      mediaArtwork.setPreviewUrl(hdClearArt.preview);
      mediaArtwork.setDefaultUrl(hdClearArt.original);
      mediaArtwork.setLikes(hdClearArt.votes);

      metadata.addMediaArt(mediaArtwork);
    }

    // HDLogo
    for (HDLogo hdLogo : scrapeResult.hdlogos) {
      MediaArtwork mediaArtwork = new MediaArtwork(providerInfo.getId(), MediaArtwork.MediaArtworkType.CLEARLOGO);
      mediaArtwork.setPreviewUrl(hdLogo.preview);
      mediaArtwork.setDefaultUrl(hdLogo.original);
      mediaArtwork.setLikes(hdLogo.votes);
    }

    metadata.setId("allocine", scrapeResult.idAllocine);
    metadata.setId("imdb", scrapeResult.idImdb);
    metadata.setId("tmdb", scrapeResult.idTmdb);
    metadata.setTagline(scrapeResult.tagline);
    metadata.setReleaseDate(new Date(scrapeResult.firstRelease));
    metadata.setTitle(scrapeResult.title);
    metadata.setOriginalTitle(scrapeResult.originalTitle);
    metadata.setRuntime(scrapeResult.runtime);
    metadata.setPlot(scrapeResult.plot);

    return metadata;
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  private String getAboKey() {
    return providerInfo.getConfig().getValue("aboKey");
  }

  private String getUserName() {
    return providerInfo.getConfig().getValue("username");
  }

  private String getEncodedUserName() {
    return Base64.getUrlEncoder().encodeToString(getUserName().getBytes());
  }

  private String getSubscriptionKey() {
    return DigestUtils.shaHex(getUserName() + API_KEY + getAboKey());
  }

}
