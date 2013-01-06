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
package org.tinymediamanager.scraper.ofdb;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.IMediaTrailerProvider;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaTrailer;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.MetadataUtil;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.scraper.util.ParserUtils;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.tinymediamanager.scraper.util.Url;

/**
 * The Class OfdbMetadataProvider.
 * 
 * @author Myron Boyle (myron0815@gmx.net)
 */
public class OfdbMetadataProvider implements IMediaMetadataProvider, IMediaTrailerProvider {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(OfdbMetadataProvider.class);

    private static final String BASE_URL = "http://www.ofdb.de";

    /** The Constant instance. */
    private static OfdbMetadataProvider instance;

    /** The provider info. */
    private static MediaProviderInfo providerInfo = new MediaProviderInfo("ofdb", "ofdb.de",
            "Scraper for german ofdb.de which is able to scrape movie metadata");

    /**
     * Gets the single instance of OfdbMetadataProvider.
     * 
     * @return single instance of OfdbMetadataProvider
     */
    public static synchronized OfdbMetadataProvider getInstance() {
        if (instance == null) {
            instance = new OfdbMetadataProvider();
        }
        return instance;
    }

    /**
     * Instantiates a new ofdb metadata provider.
     */
    public OfdbMetadataProvider() {
    }

    @Override
    public MediaProviderInfo getProviderInfo() {
        return providerInfo;
    }

    /*
         <meta property="og:title" content="Bourne Vermaächtnis, Das (2012)" />
         <meta property="og:type" content="movie" />
         <meta property="og:url" content="http://www.ofdb.de/film/226745,Das-Bourne-Vermächtnis" />
         <meta property="og:image" content="http://img.ofdb.de/film/226/226745.jpg" />
         <meta property="og:site_name" content="OFDb" />
         <meta property="fb:app_id" content="198140443538429" />
         <script src="http://www.ofdb.de/jscripts/vn/immer_oben.js" type="text/javascript"></script>
    */

    @Override
    public MediaMetadata getMetadata(MediaScrapeOptions options) throws Exception {
        // MediaSearchResult [extraArgs=empty, id=226745, imdbId=null,
        // metadata=null, providerId=ofdb, score=0.0, title=Bourne Vermächtnis, Das,
        // originalTitle=Bourne Legacy, The, type=MOVIE,
        // url=http://www.ofdb.de/film/226745,Das-Bourne-Verm&auml;chtnis,
        // year=2012]

        MediaMetadata md = new MediaMetadata(providerInfo.getId());
        // generic Elements used all over
        Elements el = null;
        // preset values from searchresult (if we have them)
        md.setOriginalTitle(Utils.removeSortableName(options.getResult().getOriginalTitle()));
        md.setTitle(Utils.removeSortableName(options.getResult().getTitle()));
        md.setYear(options.getResult().getYear());

        // http://www.ofdb.de/film/22523,Die-Bourne-Identit%C3%A4t
        Url url;
        try {
            url = new CachedUrl(options.getResult().getUrl());
            InputStream in = url.getInputStream();
            Document doc = Jsoup.parse(in, "UTF-8", "");
            in.close();

            // parse details

            // IMDB ID "http://www.imdb.com/Title?1194173"
            el = doc.getElementsByAttributeValueContaining("href", "imdb.com");
            if (!el.isEmpty()) {
                md.setImdbId("tt" + StrgUtils.substr(el.first().attr("href"), "\\?(\\d+)"));
            }

            // Title Year
            if (StringUtils.isEmpty(md.getYear()) || StringUtils.isEmpty(md.getTitle())) {
                // <meta property="og:title" content="Bourne Vermächtnis, Das (2012)" />
                el = doc.getElementsByAttributeValue("property", "og:title");
                if (!el.isEmpty()) {
                    String[] ty = ParserUtils.parseTitle(el.first().attr("content"));
                    md.setTitle(ty[0]);
                    md.setYear(ty[1]);
                }
            }
            // another year position
            if (StringUtils.isEmpty(md.getYear())) {
                // <a href="view.php?page=blaettern&Kat=Jahr&Text=2012">2012</a>
                el = doc.getElementsByAttributeValueContaining("href", "Kat=Jahr");
                md.setYear(el.first().text());
            }

            // Genre: <a href="view.php?page=genre&Genre=Action">Action</a>
            el = doc.getElementsByAttributeValueContaining("href", "page=genre");
            for (Element g : el) {
                MediaGenres genre = MediaGenres.getGenre(g.text());
                if (genre != null && !md.getGenres().contains(genre)) {
                    md.addGenre(genre);
                }
            }

            // trailer
            Pattern regex = Pattern.compile("return '(.*?)';");
            Matcher m = regex.matcher(doc.toString());
            while (m.find()) {
                String s = m.group(1);
                /*
                 <b>Trailer 1</b><br><i>(xxlarge)</i><br><br>&raquo; 640px<br><br>Download:<br>&raquo; 
                 <a href="http://de.clip-1.filmtrailer.com/9507_31566_a_5.wmv?log_var=72|491100001-1|-">wmv</a><br>&raquo;
                 <a href="http://de.clip-1.filmtrailer.com/9507_31566_a_5.mp4?log_var=72|491100001-1|-">mp4</a><br>&raquo;
                 <a href="http://de.clip-1.filmtrailer.com/9507_31566_a_5.webm?log_var=72|491100001-1|-">webm</a><br>
                */
                String tname = StrgUtils.substr(s, "<b>(.*?)</b>");
                String tpix = StrgUtils.substr(s, "raquo; (.*?)x<br>");
                // String tqual = StrgUtils.substr(s, "<i>\\((.*?)\\)</i>");

                // url + format
                Pattern lr = Pattern.compile("<a href=\"(.*?)\">(.*?)</a>");
                Matcher lm = lr.matcher(s);
                while (lm.find()) {
                    String turl = lm.group(1);
                    String tformat = lm.group(2);
                    MediaTrailer trailer = new MediaTrailer();
                    trailer.setName(tname);
                    trailer.setQuality(tpix + " (" + tformat + ")");
                    trailer.setProvider("filmtrailer");
                    trailer.setUrl(turl);
                    md.addTrailer(trailer);
                }
            }

            // get PlotLink, open url and parse
            // <a href="plot/22523,31360,Die-Bourne-Identität"><b>[mehr]</b></a>
            el = doc.getElementsByAttributeValueMatching("href", "plot\\/\\d+,");
            if (el.isEmpty()) {
                // no full plot found
                return md;
            }
            String plotUrl = BASE_URL + "/" + el.first().attr("href");
            url = new CachedUrl(plotUrl);
            in = url.getInputStream();
            Document plot = Jsoup.parse(in, "UTF-8", "");
            in.close();
            Elements block = plot.getElementsByClass("Blocksatz"); // first Blocksatz
                                                                   // is plot
            String p = block.first().text(); // remove all html stuff
            p = p.substring(p.indexOf("Mal gelesen") + 12); // remove "header"
            // LOGGER.info(p);
            md.setPlot(p);
            md.setTagline(p.substring(0, 150));
        } catch (IOException e) {
            LOGGER.error("Error parsing " + options.getResult().getUrl());
            throw e;
        }

        return md;
    }

    /**
     * Removes all weird characters from search as well some "stopwords" as der|die|das|the|a
     * 
     * @param q
     *            the query string to clean
     * @return
     */
    private String cleanSearch(String q) {
        q = " " + q + " "; // easier regex
        // TODO: doppelte hintereinander funzen so nicht
        q = q.replaceAll("(?i)( a | the | der | die | das |\\(\\d+\\))", " ");
        q = q.replaceAll("[^A-Za-z0-9äöüÄÖÜ ]", " ");
        q = q.replaceAll("  ", "");
        LOGGER.debug(q);
        return q.trim();
    }

    @Override
    public List<MediaSearchResult> search(MediaSearchOptions options) throws Exception {
        LOGGER.debug("OFDB Scraper: start");
        List<MediaSearchResult> resultList = new ArrayList<MediaSearchResult>();
        String searchString = "";
        String imdb = "";

        /*
         * Kat = All | Titel | Person | DTitel | OTitel | Regie | Darsteller | Song
         * | Rolle | EAN| IMDb | Google
         * http://www.ofdb.de//view.php?page=suchergebnis
         * &Kat=xxxxxxxxx&SText=yyyyyyyyyyy
         */
        // detect the search preference (1. imdb, 2. title, 3. all)
        if (StringUtils.isNotEmpty(options.get(MediaSearchOptions.SearchParam.IMDBID))) {
            imdb = options.get(MediaSearchOptions.SearchParam.IMDBID);
            searchString = BASE_URL + "/view.php?page=suchergebnis&Kat=IMDb&SText=" + imdb;
            LOGGER.debug("OFDB Scraper: search with imdbId: " + imdb);
        } else if (StringUtils.isNotEmpty(options.get(MediaSearchOptions.SearchParam.TITLE))) {
            String title = options.get(MediaSearchOptions.SearchParam.TITLE);
            searchString = BASE_URL + "/view.php?page=suchergebnis&Kat=Titel&SText="
                    + URLEncoder.encode(cleanSearch(title), "UTF-8");
            LOGGER.debug("OFDB Scraper: search with title: " + title);
        } else if (StringUtils.isNotEmpty(options.get(MediaSearchOptions.SearchParam.QUERY))) {
            String query = options.get(MediaSearchOptions.SearchParam.QUERY);
            searchString = BASE_URL + "/view.php?page=suchergebnis&Kat=All&SText="
                    + URLEncoder.encode(cleanSearch(query), "UTF-8");
            LOGGER.debug("OFDB Scraper: search for everything: " + query);
        } else {
            LOGGER.debug("OFDB Scraper: empty searchString");
            return resultList;
        }

        Url url = new CachedUrl(searchString);
        InputStream in = url.getInputStream();
        Document doc = Jsoup.parse(in, "UTF-8", "");
        in.close();
        // only look for movie links
        Elements filme = doc.getElementsByAttributeValueMatching("href", "film\\/\\d+,");
        LOGGER.debug("OFDB Scraper: found " + filme.size() + " search results");
        if (filme == null || filme.isEmpty()) {
            return resultList;
        }

        // <a href="film/22523,Die-Bourne-Identität"
        // onmouseover="Tip('<img src=&quot;images/film/22/22523.jpg&quot; width=&quot;120&quot; height=&quot;170&quot;>',SHADOW,true)">Bourne
        // Identität, Die<font size="1"> / Bourne Identity, The</font> (2002)</a>

        for (Element a : filme) {
            try {
                MediaSearchResult sr = new MediaSearchResult(providerInfo.getId());
                if (StringUtils.isNotEmpty(imdb)) {
                    sr.setIMDBId(imdb);
                }
                sr.setId(StrgUtils.substr(a.toString(), "film\\/(\\d+),")); // OFDB ID
                sr.setTitle(StringEscapeUtils.unescapeHtml4(StrgUtils.substr(a.toString(), ">(.*?)<font")));
                LOGGER.debug("OFDB Scraper: found movie " + sr.getTitle());
                sr.setOriginalTitle(StringEscapeUtils.unescapeHtml4(StrgUtils.substr(a.toString(),
                        "> / (.*?)</font")));
                sr.setYear(StrgUtils.substr(a.toString(), "font> \\((.*?)\\)<\\/a"));
                sr.setMediaType(MediaType.MOVIE);
                sr.setUrl(BASE_URL + "/" + StrgUtils.substr(a.toString(), "href=\\\"(.*?)\\\""));
                sr.setPosterUrl(BASE_URL + "/images" + StrgUtils.substr(a.toString(), "images(.*?)\\&quot"));
                // populate extra args
                MetadataUtil.copySearchQueryToSearchResult(options, sr);
                resultList.add(sr);
            } catch (Exception e) {
                LOGGER.warn("OFDB Scraper: error parsing movie result: " + e.getMessage());
            }
        }

        LOGGER.debug("OFDB Scraper: end");
        return resultList;
    }

    @Override
    public List<MediaTrailer> getTrailers(MediaScrapeOptions options) throws Exception {
        /*
        function getTrailerData(ci)
        {
            switch (ci)
            {
                case 'http://de.clip-1.filmtrailer.com/9507_31566_a_1.flv?log_var=72|491100001-1|-' : return '<b>Trailer 1</b><br><i>(small)</i><br><br>&raquo; 160px<br><br>Download:<br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_31566_a_1.wmv?log_var=72|491100001-1|-">wmv</a><br>';
                case 'http://de.clip-1.filmtrailer.com/9507_31566_a_2.flv?log_var=72|491100001-1|-' : return '<b>Trailer 1</b><br><i>(medium)</i><br><br>&raquo; 240px<br><br>Download:<br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_31566_a_2.wmv?log_var=72|491100001-1|-">wmv</a><br>';
                case 'http://de.clip-1.filmtrailer.com/9507_31566_a_3.flv?log_var=72|491100001-1|-' : return '<b>Trailer 1</b><br><i>(large)</i><br><br>&raquo; 320px<br><br>Download:<br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_31566_a_3.wmv?log_var=72|491100001-1|-">wmv</a><br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_31566_a_3.mp4?log_var=72|491100001-1|-">mp4</a><br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_31566_a_3.webm?log_var=72|491100001-1|-">webm</a><br>';
                case 'http://de.clip-1.filmtrailer.com/9507_31566_a_4.flv?log_var=72|491100001-1|-' : return '<b>Trailer 1</b><br><i>(xlarge)</i><br><br>&raquo; 400px<br><br>Download:<br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_31566_a_4.wmv?log_var=72|491100001-1|-">wmv</a><br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_31566_a_4.mp4?log_var=72|491100001-1|-">mp4</a><br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_31566_a_4.webm?log_var=72|491100001-1|-">webm</a><br>';
                case 'http://de.clip-1.filmtrailer.com/9507_31566_a_5.flv?log_var=72|491100001-1|-' : return '<b>Trailer 1</b><br><i>(xxlarge)</i><br><br>&raquo; 640px<br><br>Download:<br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_31566_a_5.wmv?log_var=72|491100001-1|-">wmv</a><br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_31566_a_5.mp4?log_var=72|491100001-1|-">mp4</a><br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_31566_a_5.webm?log_var=72|491100001-1|-">webm</a><br>';
                case 'http://de.clip-1.filmtrailer.com/9507_39003_a_1.flv?log_var=72|491100001-1|-' : return '<b>Trailer 2</b><br><i>(small)</i><br><br>&raquo; 160px<br><br>Download:<br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_39003_a_1.wmv?log_var=72|491100001-1|-">wmv</a><br>';
                case 'http://de.clip-1.filmtrailer.com/9507_39003_a_2.flv?log_var=72|491100001-1|-' : return '<b>Trailer 2</b><br><i>(medium)</i><br><br>&raquo; 240px<br><br>Download:<br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_39003_a_2.wmv?log_var=72|491100001-1|-">wmv</a><br>';
                case 'http://de.clip-1.filmtrailer.com/9507_39003_a_3.flv?log_var=72|491100001-1|-' : return '<b>Trailer 2</b><br><i>(large)</i><br><br>&raquo; 320px<br><br>Download:<br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_39003_a_3.wmv?log_var=72|491100001-1|-">wmv</a><br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_39003_a_3.mp4?log_var=72|491100001-1|-">mp4</a><br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_39003_a_3.webm?log_var=72|491100001-1|-">webm</a><br>';
                case 'http://de.clip-1.filmtrailer.com/9507_39003_a_4.flv?log_var=72|491100001-1|-' : return '<b>Trailer 2</b><br><i>(xlarge)</i><br><br>&raquo; 400px<br><br>Download:<br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_39003_a_4.wmv?log_var=72|491100001-1|-">wmv</a><br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_39003_a_4.mp4?log_var=72|491100001-1|-">mp4</a><br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_39003_a_4.webm?log_var=72|491100001-1|-">webm</a><br>';
                case 'http://de.clip-1.filmtrailer.com/9507_39003_a_5.flv?log_var=72|491100001-1|-' : return '<b>Trailer 2</b><br><i>(xxlarge)</i><br><br>&raquo; 640px<br><br>Download:<br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_39003_a_5.wmv?log_var=72|491100001-1|-">wmv</a><br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_39003_a_5.mp4?log_var=72|491100001-1|-">mp4</a><br>&raquo; <a href="http://de.clip-1.filmtrailer.com/9507_39003_a_5.webm?log_var=72|491100001-1|-">webm</a><br>';
            }
        }
        */
        MediaTrailer trailer = new MediaTrailer();

        return null;
    }
}