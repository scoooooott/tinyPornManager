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
package org.tinymediamanager.scraper.pornhub.v2;

import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.lang3.StringUtils;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.movie.MovieSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.exceptions.MissingIdException;
import org.tinymediamanager.scraper.exceptions.NothingFoundException;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IMovieMetadataProvider;

/**
 * The class {@link Ph2MovieMetadataProvider} is used to provide metadata for movies from pornhub
 *
 * @author Manuel Laggner
 */
public class Ph2MovieMetadataProvider implements IMovieMetadataProvider {

    public static final String ID = "pornhub2";

    public static final String USE_TMDB_FOR_MOVIES = "useTmdbForMovies";
    public static final String USE_TMDB_FOR_TV_SHOWS = "useTmdbForTvShows";

    static final MediaProviderInfo PROVIDER_INFO = createMediaProviderInfo();
    static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(4);

    private static MediaProviderInfo createMediaProviderInfo() {
        MediaProviderInfo providerInfo = new MediaProviderInfo(ID, "pornhub.com2",
            "<html><h3>Pornhub</h3><br />The largest free movie database maintained by the community. It provides metadata and artwork<br />in many different languages. Thus it is the first choice for non english users<br /><br />Available languages: multiple</html>",
            Ph2MovieMetadataProvider.class.getResource("/org/tinymediamanager/scraper/pornhub2.png"));

        // configure/load settings
        providerInfo.getConfig().addBoolean("filterUnwantedCategories", true);
        providerInfo.getConfig().addBoolean(USE_TMDB_FOR_MOVIES, false);
        providerInfo.getConfig().addBoolean(USE_TMDB_FOR_TV_SHOWS, false);
        providerInfo.getConfig().addBoolean("scrapeCollectionInfo", false);
        providerInfo.getConfig().addBoolean("localReleaseDate", true);
        providerInfo.getConfig().addBoolean("scrapeLanguageNames", true);
        providerInfo.getConfig().addBoolean("scrapeKeywordsPage", false);
        providerInfo.getConfig().addInteger("maxKeywordCount", 10);

        providerInfo.getConfig().load();

        return providerInfo;
    }

    static String cleanString(String oldString) {
        if (StringUtils.isEmpty(oldString)) {
            return "";
        }
        // remove non breaking spaces
        String newString = StringUtils.trim(oldString.replace(String.valueOf((char)160), " "));

        // if there is a leading AND trailing quotation marks (e.g. at TV shows) - remove them
        if (newString.startsWith("\"") && newString.endsWith("\"")) {
            newString = StringUtils.stripEnd(StringUtils.stripStart(newString, "\""), "\"");
        }

        // and trim
        return newString;
    }

    static void processMediaArt(MediaMetadata md, MediaArtworkType type, String image) {
        MediaArtwork ma = new MediaArtwork(PROVIDER_INFO.getId(), type);
        ma.setPreviewUrl(image);
        ma.setDefaultUrl(image);
        md.addMediaArt(ma);
    }

    /**
     * Maps scraper Genres to internal TMM genres
     *
     * @param genre
     * @return
     */
    static MediaGenres getTmmGenre(String genre) {
        MediaGenres g = null;
        if (StringUtils.isBlank(genre)) {
            return null;
        }
        // @formatter:off
        else if ("Action".equals(genre)) {
            g = MediaGenres.ACTION;
        }
        else if ("Adult".equals(genre)) {
            g = MediaGenres.EROTIC;
        }
        else if ("Adventure".equals(genre)) {
            g = MediaGenres.ADVENTURE;
        }
        else if ("Animation".equals(genre)) {
            g = MediaGenres.ANIMATION;
        }
        else if ("Biography".equals(genre)) {
            g = MediaGenres.BIOGRAPHY;
        }
        else if ("Comedy".equals(genre)) {
            g = MediaGenres.COMEDY;
        }
        else if ("Crime".equals(genre)) {
            g = MediaGenres.CRIME;
        }
        else if ("Documentary".equals(genre)) {
            g = MediaGenres.DOCUMENTARY;
        }
        else if ("Drama".equals(genre)) {
            g = MediaGenres.DRAMA;
        }
        else if ("Family".equals(genre)) {
            g = MediaGenres.FAMILY;
        }
        else if ("Fantasy".equals(genre)) {
            g = MediaGenres.FANTASY;
        }
        else if ("Film-Noir".equals(genre)) {
            g = MediaGenres.FILM_NOIR;
        }
        else if ("Game-Show".equals(genre)) {
            g = MediaGenres.GAME_SHOW;
        }
        else if ("History".equals(genre)) {
            g = MediaGenres.HISTORY;
        }
        else if ("Horror".equals(genre)) {
            g = MediaGenres.HORROR;
        }
        else if ("Music".equals(genre)) {
            g = MediaGenres.MUSIC;
        }
        else if ("Musical".equals(genre)) {
            g = MediaGenres.MUSICAL;
        }
        else if ("Mystery".equals(genre)) {
            g = MediaGenres.MYSTERY;
        }
        else if ("News".equals(genre)) {
            g = MediaGenres.NEWS;
        }
        else if ("Reality-TV".equals(genre)) {
            g = MediaGenres.REALITY_TV;
        }
        else if ("Romance".equals(genre)) {
            g = MediaGenres.ROMANCE;
        }
        else if ("Sci-Fi".equals(genre)) {
            g = MediaGenres.SCIENCE_FICTION;
        }
        else if ("Short".equals(genre)) {
            g = MediaGenres.SHORT;
        }
        else if ("Sport".equals(genre)) {
            g = MediaGenres.SPORT;
        }
        else if ("Talk-Show".equals(genre)) {
            g = MediaGenres.TALK_SHOW;
        }
        else if ("Thriller".equals(genre)) {
            g = MediaGenres.THRILLER;
        }
        else if ("War".equals(genre)) {
            g = MediaGenres.WAR;
        }
        else if ("Western".equals(genre)) {
            g = MediaGenres.WESTERN;
        }
        // @formatter:on
        if (g == null) {
            g = MediaGenres.getGenre(genre);
        }
        return g;
    }

    @Override
    public SortedSet<MediaSearchResult> search(MovieSearchAndScrapeOptions options)
        throws ScrapeException {
        return (new Ph2MovieParser()).search(options);
    }

    @Override
    public MediaMetadata getMetadata(MovieSearchAndScrapeOptions options)
        throws ScrapeException, MissingIdException, NothingFoundException {
        return (new Ph2MovieParser()).getMovieMetadata(options);
    }

    @Override
    public MediaProviderInfo getProviderInfo() {
        return PROVIDER_INFO;
    }

    @Override
    public String getId() {
        return ID;
    }
}