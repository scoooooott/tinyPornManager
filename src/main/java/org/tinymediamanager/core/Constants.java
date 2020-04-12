/*
 * Copyright 2012 - 2020 Manuel Laggner
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
package org.tinymediamanager.core;

/**
 * The Class Constants.
 * 
 * @author Manuel Laggner
 */
public final class Constants {
  public static final String BACKUP_FOLDER          = ".deletedByTMM";

  public static final String ACTORS                 = "actors";
  public static final String ADDED_EPISODE          = "addedEpisode";
  public static final String ADDED_MOVIE            = "addedMovie";
  public static final String ADDED_MOVIE_SET        = "addedMovieSet";
  public static final String ADDED_SEASON           = "addedSeason";
  public static final String ADDED_TV_SHOW          = "addedTvShow";
  public static final String AIRED_EPISODE          = "airedEpisode";
  public static final String AIRED_SEASON           = "airedSeason";
  public static final String AUDIO_CODEC            = "audioCodec";
  public static final String BANNER                 = "banner";
  public static final String BANNER_URL             = "bannerUrl";
  public static final String CAST                   = "cast";
  public static final String CHARACTER              = "character";
  public static final String CHARACTERART           = "characterart";
  public static final String CERTIFICATION          = "certification";
  public static final String CLEARART               = "clearart";
  public static final String CLEARLOGO              = "clearlogo";
  public static final String COUNTRY                = "country";
  public static final String DATA_SOURCE            = "dataSource";
  public static final String DATE_ADDED             = "dateAdded";
  public static final String DATE_ADDED_AS_STRING   = "dateAddedAsString";
  public static final String DIRECTORS              = "directors";
  public static final String DIRECTORS_AS_STRING    = "directorsAsString";
  public static final String DISC                   = "disc";
  public static final String DVD_EPISODE            = "dvdEpisode";
  public static final String DVD_ORDER              = "dvdOrder";
  public static final String DVD_SEASON             = "dvdSeason";
  public static final String DISPLAY_EPISODE        = "displayEpisode";
  public static final String DISPLAY_SEASON         = "displaySeason";
  public static final String EDITION                = "edition";
  public static final String EDITION_AS_STRING      = "editionAsString";
  public static final String EPISODE                = "episode";
  public static final String EPISODE_COUNT          = "episodeCount";
  public static final String FANART                 = "fanart";
  public static final String FANART_URL             = "fanartUrl";
  public static final String FIRST_AIRED            = "firstAired";
  public static final String FIRST_AIRED_AS_STRING  = "firstAiredAsString";
  public static final String FRAME_RATE             = "frameRate";
  public static final String GENRE                  = "genre";
  public static final String GENRES_AS_STRING       = "genresAsString";
  public static final String HAS_IMAGES             = "hasImages";
  public static final String HAS_NFO_FILE           = "hasNfoFile";
  public static final String KEYART                 = "keyart";
  public static final String LOGO                   = "logo";
  public static final String MEDIA_FILES            = "mediaFiles";
  public static final String MEDIA_INFORMATION      = "mediaInformation";
  public static final String MEDIA_SOURCE           = "mediaSource";
  public static final String MESSAGES               = "messages";
  public static final String MOVIESET               = "movieset";
  public static final String MOVIESET_TITLE         = "movieSetTitle";
  public static final String NAME                   = "name";
  public static final String NEWLY_ADDED            = "newlyAdded";
  public static final String NFO_FILENAME           = "nfoFilename";
  public static final String ORIGINAL_TITLE         = "originalTitle";
  public static final String ORIGINAL_FILENAME      = "originalFilename";
  public static final String PATH                   = "path";
  public static final String PLOT                   = "plot";
  public static final String POSTER                 = "poster";
  public static final String POSTER_URL             = "posterUrl";
  public static final String PRODUCERS              = "producers";
  public static final String PRODUCTION_COMPANY     = "productionCompany";
  public static final String RATING                 = "rating";
  public static final String RELEASE_DATE           = "releaseDate";
  public static final String RELEASE_DATE_AS_STRING = "releaseDateAsString";
  public static final String REMOVED_EPISODE        = "removedEpisode";
  public static final String REMOVED_MOVIE          = "removedMovie";
  public static final String REMOVED_MOVIE_SET      = "removedMovieSet";
  public static final String REMOVED_TV_SHOW        = "removedTvShow";
  public static final String ROLE                   = "role";
  public static final String RUNTIME                = "runtime";
  public static final String SCRAPED                = "scraped";
  public static final String SEASON                 = "season";
  public static final String SEASON_COUNT           = "seasonCount";
  public static final String SEASON_POSTER          = "seasonPoster";
  public static final String SEASON_BANNER          = "seasonBanner";
  public static final String SEASON_THUMB           = "seasonThumb";
  public static final String SORT_TITLE             = "sortTitle";
  public static final String SPOKEN_LANGUAGES       = "spokenLanguages";
  public static final String STATUS                 = "status";
  public static final String STUDIO                 = "studio";
  public static final String TAG                    = "tag";
  public static final String TAGS_AS_STRING         = "tagsAsString";
  public static final String THUMB                  = "thumb";
  public static final String THUMB_URL              = "thumbUrl";
  public static final String THUMB_PATH             = "thumbPath";
  public static final String TITLE                  = "title";
  public static final String TITLE_FOR_UI           = "titleForUi";
  public static final String TITLE_SORTABLE         = "titleSortable";
  public static final String TOP250                 = "top250";
  public static final String TRAILER                = "trailer";
  public static final String TV_SHOW                = "tvShow";
  public static final String TV_SHOW_COUNT          = "tvShowCount";
  public static final String TV_SHOWS               = "tvShows";
  public static final String VIDEO_CODEC            = "videoCodec";
  public static final String VIDEO_CONTAINER        = "videoContainer";
  public static final String VIDEO_IN_3D            = "videoIn3D";
  public static final String VOTES                  = "votes";
  public static final String WATCHED                = "watched";
  public static final String WRITERS                = "writers";
  public static final String WRITERS_AS_STRING      = "writersAsString";
  public static final String YEAR                   = "year";

  // some hardcoded, well known meta data provider IDs
  // may add new ones in MediaEntity.setId()
  public static final String TMDB                   = "tmdb";
  public static final String TMDB_SET               = "tmdbSet";
  public static final String IMDB                   = "imdb";
  public static final String TVDB                   = "tvdb";
  public static final String TRAKT                  = "trakt";
  public static final String FANART_TV              = "fanarttv";

  private Constants() {
  }
}
