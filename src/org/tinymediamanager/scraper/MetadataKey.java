/*
 * Copyright 2012 Manuel Laggner
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
package org.tinymediamanager.scraper;

/**
 * The Enum MetadataKey.
 */

public enum MetadataKey {
  /** The media title. */
  MEDIA_TITLE("MediaTitle", ""),
  /** The album. */
  ALBUM("Album", ""),
  /** The artist. */
  ARTIST("Artist", ""),
  /** The album artist. */
  ALBUM_ARTIST("AlbumArtist", ""),
  /** The composer. */
  COMPOSER("Composer", ""),
  /** The track. */
  TRACK("Track", ""),
  /** The total tracks. */
  TOTAL_TRACKS("TotalTracks", ""),
  /** The year. */
  YEAR("Year", ""),
  /** The comment. */
  COMMENT("Comment", ""),
  /** The genre list. */
  GENRE_LIST("GenreList", ""),
  /** The language. */
  LANGUAGE("Language", ""),
  /** The mpaa rating. */
  MPAA_RATING("MPAARating", ""),
  /** The mpaa rating description. */
  MPAA_RATING_DESCRIPTION("MPAARatingDescription", ""),
  /** The user rating. */
  USER_RATING("UserRating", ""),
  /** The running time. */
  RUNNING_TIME("RunningTime", ""),
  /** The duration. */
  DURATION("Duration", ""),
  /** The description. */
  DESCRIPTION("Description", ""),
  /** The cast member list. */
  CAST_MEMBER_LIST("CastMemberList", ""),
  /** The poster art. */
  POSTER_ART("PosterArt", ""),
  /** The background art. */
  BACKGROUND_ART("BackgroundArt", ""),
  /** The banner art. */
  BANNER_ART("BannerArt", ""),
  /** The media art list. */
  MEDIA_ART_LIST("MediaArtList", ""),
  /** The aspect ratio. */
  ASPECT_RATIO("AspectRatio", ""),
  /** The company. */
  COMPANY("Company", ""),
  /** The media provider data id. */
  MEDIA_PROVIDER_DATA_ID("MediaProviderDataID", ""),
  /** The release date. */
  RELEASE_DATE("OriginalAirDate", ""),
  /** The episode. */
  EPISODE("EpisodeNumber", ""),
  /** The episode title. */
  EPISODE_TITLE("EpisodeTitle", ""),
  /** The season. */
  SEASON("SeasonNumber", ""),
  /** The media type. */
  MEDIA_TYPE("MediaType", ""),
  /** The dvd disc. */
  DVD_DISC("Disc", ""),
  /** The metadata provider id. */
  METADATA_PROVIDER_ID("MetadataProviderId", ""),
  /** The imdb id. */
  IMDB_ID("IMDBId", ""),
  /** The iswatched. */
  ISWATCHED("Watched", ""),
  /** The display title. */
  DISPLAY_TITLE("Title", ""),
  /** The plot. */
  PLOT("Plot", ""),
  /** The outline. */
  OUTLINE("Outline", ""),
  /** The original title. */
  ORIGINAL_TITLE("OriginalTitle", ""),
  /** The tagline. */
  TAGLINE("Tagline", ""),
  /** The tmdb id. */
  TMDB_ID("TMDBId", ""),
  /** The votes. */
  VOTE_COUNT("VoteCount", "");

  /** The desc. */
  private String id, desc;

  /**
   * Instantiates a new metadata key.
   * 
   * @param id
   *          the id
   * @param desc
   *          the desc
   */
  MetadataKey(String id, String desc) {
    this.id = id;
    this.desc = desc;
  }

  /**
   * Gets the id.
   * 
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the description.
   * 
   * @return the description
   */
  public String getDescription() {
    return desc;
  }

  /**
   * Value of id.
   * 
   * @param id
   *          the id
   * @return the metadata key
   * @throws RuntimeException
   *           the runtime exception
   */
  public static MetadataKey valueOfId(String id) throws RuntimeException {
    for (MetadataKey k : MetadataKey.values()) {
      if (k.getId().equals(id))
        return k;
    }
    throw new RuntimeException("No Enum in MetadataKey for Id: " + id);
  }
}
