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

package org.tinymediamanager.core.tvshow.connector;

import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_BANNER;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_POSTER;
import static org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType.SEASON_THUMB;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.CertificationStyle;
import org.tinymediamanager.core.Constants;
import org.tinymediamanager.core.MediaCertification;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.MediaTrailer;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.filenaming.TvShowNfoNaming;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * this class is a general XML connector which suits as a base class for most xml based connectors
 *
 * @author Manuel Laggner
 */
public abstract class TvShowGenericXmlConnector implements ITvShowConnector {
  protected final String    ORACLE_IS_STANDALONE = "http://www.oracle.com/xml/is-standalone";

  protected final TvShow    tvShow;
  protected TvShowNfoParser parser               = null;

  protected Document        document;
  protected Element         root;

  public TvShowGenericXmlConnector(TvShow tvShow) {
    this.tvShow = tvShow;
  }

  /**
   * get the logger for the impl. class
   *
   * @return the logger
   */
  protected abstract Logger getLogger();

  /**
   * write own tag which are not covered by this generic connector
   */
  protected abstract void addOwnTags();

  @Override
  public void write(List<TvShowNfoNaming> nfoNames) {
    // first of all, get the data from a previous written NFO file,
    // if we do not want clean NFOs
    if (!TvShowModuleManager.SETTINGS.isWriteCleanNfo()) {
      for (MediaFile mf : tvShow.getMediaFiles(MediaFileType.NFO)) {
        try {
          parser = TvShowNfoParser.parseNfo(mf.getFileAsPath());
          break;
        }
        catch (Exception ignored) {
        }
      }
    }

    List<MediaFile> newNfos = new ArrayList<>(1);

    for (TvShowNfoNaming nfoNaming : nfoNames) {
      String nfoFilename = nfoNaming.getFilename(tvShow.getTitle(), "nfo");
      if (StringUtils.isBlank(nfoFilename)) {
        continue;
      }

      try {
        // create the new NFO file according to the specifications
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); // NOSONAR
        document = factory.newDocumentBuilder().newDocument();
        document.setXmlStandalone(true);

        // tmm comment
        Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dat = formatter.format(new Date());
        document.appendChild(document.createComment("created on " + dat + " - tinyMediaManager " + Globals.settings.getVersion()));

        root = document.createElement("tvshow");
        document.appendChild(root);

        // add well known tags
        addTitle();
        addOriginalTitle();
        addShowTitle();
        addSortTitle();
        addYear();
        addRating();
        addUserrating();
        addVotes();
        addOutline();
        addPlot();
        addTagline();
        addRuntime();
        addPoster();
        addSeasonName();
        addSeasonPoster();
        addSeasonBanner();
        addSeasonThumb();
        addFanart();
        addMpaa();
        addCertification();
        addEpisodeguide();
        addId();
        addImdbid();
        addIds();
        addPremiered();
        addStatus();
        addWatched();
        addPlaycount();
        addGenres();
        addStudios();
        addTags();
        addActors();
        addTrailer();
        addDateAdded();

        // add connector specific tags
        addOwnTags();

        // add unsupported tags
        addUnsupportedTags();

        // add tinyMediaManagers own data
        addTinyMediaManagerTags();

        // serialize to string
        Writer out = new StringWriter();
        getTransformer().transform(new DOMSource(document), new StreamResult(out));
        String xml = out.toString().replaceAll("(?<!\r)\n", "\r\n"); // windows conform line endings

        Path f = tvShow.getPathNIO().resolve(nfoFilename);

        // compare old vs new
        boolean changed = true;
        try {
          String xmlOld = Utils.readFileToString(f).replaceAll("\\<\\!\\-\\-.*\\-\\-\\>", ""); // replace xml comments
          String xmlNew = xml.replaceAll("\\<\\!\\-\\-.*\\-\\-\\>", "");
          if (xmlOld.equals(xmlNew)) {
            changed = false;
          }
        }
        catch (Exception e) {
          // ignore
        }

        // write to file
        if (changed) {
          Utils.writeStringToFile(f, xml);
        }
        else {
          getLogger().debug("NFO did not change - do not write it!");
        }
        MediaFile mf = new MediaFile(f);
        mf.gatherMediaInformation(true); // force to update filedate
        newNfos.add(mf);
      }
      catch (Exception e) {
        getLogger().error("write {}: {}", tvShow.getPathNIO().resolve(nfoFilename), e.getMessage());
        MessageManager.instance
            .pushMessage(new Message(Message.MessageLevel.ERROR, tvShow, "message.nfo.writeerror", new String[] { ":", e.getLocalizedMessage() }));
      }
    }

    if (!newNfos.isEmpty()) {
      tvShow.removeAllMediaFiles(MediaFileType.NFO);
      tvShow.addToMediaFiles(newNfos);
    }
  }

  /**
   * add the title in the form <title>xxx</title>
   */
  protected void addTitle() {
    Element title = document.createElement("title");
    title.setTextContent(tvShow.getTitle());
    root.appendChild(title);
  }

  /**
   * add the original title in the form <originaltitle>xxx</originaltitle>
   */
  protected void addOriginalTitle() {
    Element originaltitle = document.createElement("originaltitle");
    originaltitle.setTextContent(tvShow.getOriginalTitle());
    root.appendChild(originaltitle);
  }

  /**
   * add the showtitle in the form <showtitle>xxx</showtitle>
   */
  protected void addShowTitle() {
    Element title = document.createElement("showtitle");
    title.setTextContent(tvShow.getTitle());
    root.appendChild(title);
  }

  /**
   * add the sorttitle in the form <sorttitle>xxx</sorttitle>
   */
  protected void addSortTitle() {
    Element sorttitle = document.createElement("sorttitle");
    sorttitle.setTextContent(tvShow.getSortTitle());
    root.appendChild(sorttitle);
  }

  /**
   * add the year in the form <year>xxx</year>
   */
  protected void addYear() {
    Element year = document.createElement("year");
    year.setTextContent(tvShow.getYear() == 0 ? "" : Integer.toString(tvShow.getYear()));
    root.appendChild(year);
  }

  /**
   * add the rating in the form <rating>xxx</rating> (floating point with one decimal)
   */
  protected void addRating() {
    // get main rating and calculate the rating value to a base of 10
    Float rating10;

    // the default rating
    Map<String, MediaRating> ratings = tvShow.getRatings();
    MediaRating mainMediaRating = ratings.get(TvShowModuleManager.SETTINGS.getPreferredRating());

    // is there any rating which is not the user rating?
    if (mainMediaRating == null) {
      for (MediaRating r : ratings.values()) {
        // skip user ratings here
        if (MediaRating.USER.equals(r.getId())) {
          continue;
        }
        mainMediaRating = r;
      }
    }

    // just create one to not pass null
    if (mainMediaRating == null) {
      mainMediaRating = new MediaRating();
    }

    if (mainMediaRating.getMaxValue() > 0) {
      rating10 = mainMediaRating.getRating() * 10 / mainMediaRating.getMaxValue();
    }
    else {
      rating10 = mainMediaRating.getRating();
    }

    Element rating = document.createElement("rating");
    rating.setTextContent(String.format(Locale.US, "%.1f", rating10));
    root.appendChild(rating);
  }

  /**
   * add the userrating in the form <userrating>xxx</userrating> (floating point with one decimal)
   */
  protected void addUserrating() {
    // get main rating and calculate the rating value to a base of 10
    Float rating10;

    MediaRating mediaRating = tvShow.getRating(MediaRating.USER);

    if (mediaRating.getMaxValue() > 0) {
      rating10 = mediaRating.getRating() * 10 / mediaRating.getMaxValue();
    }
    else {
      rating10 = mediaRating.getRating();
    }

    Element UserRating = document.createElement("userrating");
    UserRating.setTextContent(String.format(Locale.US, "%.1f", rating10));
    root.appendChild(UserRating);
  }

  /**
   * add the votes in the form <votes>xxx</votes> (integer)
   */
  protected void addVotes() {
    Element votes = document.createElement("votes");
    votes.setTextContent(Integer.toString(tvShow.getRating().getVotes()));
    root.appendChild(votes);
  }

  /**
   * add the outline in the form <outline>xxx</outline>
   */
  protected void addOutline() {
    Element outline = document.createElement("outline");
    // FIXME tbc how we should fill that field
    // outline.setTextContent();
    root.appendChild(outline);
  }

  /**
   * add the plot in the form <plot>xxx</plot>
   */
  protected void addPlot() {
    Element plot = document.createElement("plot");
    plot.setTextContent(tvShow.getPlot());
    root.appendChild(plot);
  }

  /**
   * add the tagline in the form <tagline>xxx</tagline>
   */
  protected void addTagline() {
    Element tagline = document.createElement("tagline");
    // FIXME tbc how we should fill that field
    // tagline.setTextContent();
    root.appendChild(tagline);
  }

  /**
   * add the runtime in the form <runtime>xxx</runtime> (integer)
   */
  protected void addRuntime() {
    Element runtime = document.createElement("runtime");
    runtime.setTextContent(Integer.toString(tvShow.getRuntime()));
    root.appendChild(runtime);
  }

  /**
   * add the poster in the form <thumb aspect="poster">xxx</thumb> tags
   */
  protected void addPoster() {
    Element thumb = document.createElement("thumb");

    String posterUrl = tvShow.getArtworkUrl(MediaFileType.POSTER);
    if (StringUtils.isNotBlank(posterUrl)) {
      thumb.setAttribute("aspect", "poster");
      thumb.setTextContent(posterUrl);
      root.appendChild(thumb);
    }
  }

  /**
   * add the season names in multiple <namedseason number="ss">xxx</namedseason> tags
   */
  protected void addSeasonName() {
    for (Map.Entry<Integer, String> entry : tvShow.getSeasonTitles().entrySet()) {
      Element namedseason = document.createElement("namedseason");
      String title = entry.getValue();
      if (StringUtils.isNotBlank(title)) {
        namedseason.setAttribute("number", entry.getKey().toString());
        namedseason.setTextContent(title);
        root.appendChild(namedseason);
      }
    }
  }

  /**
   * add the season posters in multiple <thumb aspect="poster" type="season" season="x">xxx</thumb> tags
   */
  protected void addSeasonPoster() {
    for (Map.Entry<Integer, String> entry : tvShow.getSeasonArtworkUrls(SEASON_POSTER).entrySet()) {
      Element thumb = document.createElement("thumb");
      String posterUrl = entry.getValue();
      if (StringUtils.isNotBlank(posterUrl)) {
        thumb.setAttribute("aspect", "poster");
        thumb.setAttribute("type", "season");
        thumb.setAttribute("season", String.valueOf(entry.getKey()));
        thumb.setTextContent(posterUrl);
        root.appendChild(thumb);
      }
    }
  }

  /**
   * add the season banners in multiple <thumb aspect="banner" type="season" season="x">xxx</thumb> tags
   */
  protected void addSeasonBanner() {
    for (Map.Entry<Integer, String> entry : tvShow.getSeasonArtworkUrls(SEASON_BANNER).entrySet()) {
      Element thumb = document.createElement("thumb");
      String bannerUrl = entry.getValue();
      if (StringUtils.isNotBlank(bannerUrl)) {
        thumb.setAttribute("aspect", "banner");
        thumb.setAttribute("type", "season");
        thumb.setAttribute("season", String.valueOf(entry.getKey()));
        thumb.setTextContent(bannerUrl);
        root.appendChild(thumb);
      }
    }
  }

  /**
   * add the season thumbs in multiple <thumb aspect="thumb" type="season" season="x">xxx</thumb> tags
   */
  protected void addSeasonThumb() {
    for (Map.Entry<Integer, String> entry : tvShow.getSeasonArtworkUrls(SEASON_THUMB).entrySet()) {
      Element thumb = document.createElement("thumb");
      String thumbUrl = entry.getValue();
      if (StringUtils.isNotBlank(thumbUrl)) {
        thumb.setAttribute("aspect", "thumb");
        thumb.setAttribute("type", "season");
        thumb.setAttribute("season", String.valueOf(entry.getKey()));
        thumb.setTextContent(thumbUrl);
        root.appendChild(thumb);
      }
    }
  }

  /**
   * the new fanart in the form <fanart><thumb>xxx</thumb></fanart>
   */
  protected void addFanart() {
    Element fanart = document.createElement("fanart");

    String fanarUrl = tvShow.getArtworkUrl(MediaFileType.FANART);
    if (StringUtils.isNotBlank(fanarUrl)) {
      Element thumb = document.createElement("thumb");
      thumb.setTextContent(fanarUrl);
      fanart.appendChild(thumb);
    }

    root.appendChild(fanart);
  }

  /**
   * add the certification in <mpaa>xxx</mpaa>
   */
  protected void addMpaa() {
    Element mpaa = document.createElement("mpaa");

    if (tvShow.getCertification() != null) {
      if (tvShow.getCertification().getCountry() == CountryCode.US) {
        // if we have US certs, write correct "Rated XX" String
        mpaa.setTextContent(MediaCertification.getMPAAString(tvShow.getCertification()));
      }
      else {
        mpaa.setTextContent(CertificationStyle.formatCertification(tvShow.getCertification(), TvShowModuleManager.SETTINGS.getCertificationStyle()));
      }
    }
    root.appendChild(mpaa);
  }

  /**
   * add the certification in <certification></certification>
   */
  protected void addCertification() {
    Element certification = document.createElement("certification");
    if (tvShow.getCertification() != null) {
      certification
          .setTextContent(CertificationStyle.formatCertification(tvShow.getCertification(), TvShowModuleManager.SETTINGS.getCertificationStyle()));
    }
    root.appendChild(certification);
  }

  /**
   * add the episode guide in <episodeguide>xxx</episodeguide>
   */
  protected void addEpisodeguide() {
    // create the new episodeguide format
    // <episodeguide>
    // <url post="yes"
    // cache="auth.json">https://api.thetvdb.com/login?{&quot;apikey&quot;:&quot;439DFEBA9D3059C6&quot;,&quot;id&quot;:289574}|Content-Type=application/json</url>
    // </episodeguide>
    if (StringUtils.isNotBlank(tvShow.getTvdbId())) {
      Element episodeguide = document.createElement("episodeguide");
      Element url = document.createElement("url");
      url.setAttribute("post", "yes");
      url.setAttribute("cache", "auth.json");
      url.setTextContent(
          "https://api.thetvdb.com/login?{\"apikey\":\"439DFEBA9D3059C6\",\"id\":" + tvShow.getTvdbId() + "}|Content-Type=application/json");
      episodeguide.appendChild(url);
      root.appendChild(episodeguide);
    }
    else if (StringUtils.isNotBlank(tvShow.getIdAsString(Constants.TMDB))) {
      // http://api.themoviedb.org/3/tv/1396?api_key=6a5be4999abf74eba1f9a8311294c267&language=en
      Element episodeguide = document.createElement("episodeguide");
      Element url = document.createElement("url");
      url.setTextContent("http://api.themoviedb.org/3/tv/" + tvShow.getIdAsString(Constants.TMDB)
          + "?api_key=6a5be4999abf74eba1f9a8311294c267&language=" + TvShowModuleManager.SETTINGS.getScraperLanguage().getLanguage());
      episodeguide.appendChild(url);
      root.appendChild(episodeguide);
    }
    // or even import it from the parser
    else if (parser != null && StringUtils.isNotBlank(parser.episodeguide)) {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); // NOSONAR
      try {
        Element episodeguide = document.createElement("episodeguide");

        // parse content of episodeguide into own elements
        Document unsupported = factory.newDocumentBuilder().parse(new ByteArrayInputStream(parser.episodeguide.getBytes("UTF-8")));
        episodeguide.appendChild(document.importNode(unsupported.getFirstChild(), true));

        // and append it
        root.appendChild(episodeguide);
      }
      catch (Exception e) {
        getLogger().warn("could not set episodeguide");
      }
    }
  }

  /**
   * add the tvdb id in <id>xxx</id>
   */
  protected void addId() {
    Element id = document.createElement("id");
    id.setTextContent(tvShow.getTvdbId());
    root.appendChild(id);
  }

  /**
   * add the imdb id in <imdbid>xxx</imdbid>
   */
  protected void addImdbid() {
    Element imdbid = document.createElement("imdbid");
    imdbid.setTextContent(tvShow.getImdbId());
    root.appendChild(imdbid);
  }

  /**
   * add our own id store in the new kodi form<br />
   * <uniqueid type="{scraper}" default="true/false">{id}</uniqueid>
   *
   * imdb should have default="true", but if no imdb ID is available, we must ensure that at least one entry has default="true"
   */
  protected void addIds() {
    String defaultScraper = detectDefaultScraper();

    for (Map.Entry<String, Object> entry : tvShow.getIds().entrySet()) {
      Element uniqueid = document.createElement("uniqueid");
      uniqueid.setAttribute("type", entry.getKey());
      if (defaultScraper.equals(entry.getKey())) {
        uniqueid.setAttribute("default", "true");
      }
      else {
        uniqueid.setAttribute("default", "false");
      }
      uniqueid.setTextContent(entry.getValue().toString());
      root.appendChild(uniqueid);
    }
  }

  /**
   * add the premiered date in <premiered>xxx</premiered>
   */
  protected void addPremiered() {
    Element premiered = document.createElement("premiered");
    if (tvShow.getFirstAired() != null) {
      premiered.setTextContent(new SimpleDateFormat("yyyy-MM-dd").format(tvShow.getFirstAired()));
    }
    root.appendChild(premiered);
  }

  /**
   * add the dateAdded date in <dateadded>xxx</dateadded>
   */
  protected void addDateAdded() {
    Element dateadded = document.createElement("dateadded");
    if (tvShow.getDateAdded() != null) {
      dateadded.setTextContent(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tvShow.getDateAdded()));
    }
    root.appendChild(dateadded);
  }

  /**
   * add the status in <status>xxx</status>
   */
  protected void addStatus() {
    Element status = document.createElement("status");
    status.setTextContent(tvShow.getStatus().getName());
    root.appendChild(status);
  }

  /**
   * add the watched flag in <watched>xxx</watched>
   */
  protected void addWatched() {
    Element watched = document.createElement("watched");
    watched.setTextContent(Boolean.toString(tvShow.isWatched()));
    root.appendChild(watched);
  }

  /**
   * add the playcount in <playcount>xxx</playcount> (integer) we do not have this in tmm, but we might get it from an existing nfo
   */
  protected void addPlaycount() {
    Element playcount = document.createElement("playcount");
    if (tvShow.isWatched() && parser != null && parser.playcount > 0) {
      playcount.setTextContent(Integer.toString(parser.playcount));
    }
    else if (tvShow.isWatched()) {
      playcount.setTextContent("1");
    }
    root.appendChild(playcount);
  }

  /**
   * add genres in <genre>xxx</genre> tags (multiple)
   */
  protected void addGenres() {
    for (MediaGenres mediaGenre : tvShow.getGenres()) {
      Element genre = document.createElement("genre");
      genre.setTextContent(mediaGenre.getLocalizedName(TvShowModuleManager.SETTINGS.getNfoLanguage().toLocale()));
      root.appendChild(genre);
    }
  }

  /**
   * add studios in <studio>xxx</studio> tags (multiple)
   */
  protected void addStudios() {
    String[] studios = tvShow.getProductionCompany().split("\\s*[,\\/]\\s*"); // split on , or / and remove whitespace around
    for (String s : studios) {
      Element studio = document.createElement("studio");
      studio.setTextContent(s);
      root.appendChild(studio);
    }
  }

  /**
   * add tags in <tag>xxx</tag> tags (multiple)
   */
  protected void addTags() {
    for (String t : tvShow.getTags()) {
      Element tag = document.createElement("tag");
      tag.setTextContent(t);
      root.appendChild(tag);
    }
  }

  /**
   * add actors in <actor><name>xxx</name><role>xxx</role><thumb>xxx</thumb></actor>
   */
  protected void addActors() {
    for (Person tvShowActor : tvShow.getActors()) {
      Element actor = document.createElement("actor");

      Element name = document.createElement("name");
      name.setTextContent(tvShowActor.getName());
      actor.appendChild(name);

      Element role = document.createElement("role");
      role.setTextContent(tvShowActor.getRole());
      actor.appendChild(role);

      Element thumb = document.createElement("thumb");
      thumb.setTextContent(tvShowActor.getThumbUrl());
      actor.appendChild(thumb);

      Element profile = document.createElement("profile");
      profile.setTextContent(tvShowActor.getProfileUrl());
      actor.appendChild(profile);

      root.appendChild(actor);
    }
  }

  /**
   * add the trailer url in <trailer>xxx</trailer>
   */
  protected void addTrailer() {
    Element trailer = document.createElement("trailer");
    for (MediaTrailer mediaTrailer : new ArrayList<>(tvShow.getTrailer())) {
      if (mediaTrailer.getInNfo() && !mediaTrailer.getUrl().startsWith("file")) {
        trailer.setTextContent(mediaTrailer.getUrl());
        break;
      }
    }
    root.appendChild(trailer);
  }

  /**
   * add all unsupported tags from the source file to the destination file
   */
  protected void addUnsupportedTags() {
    if (parser != null) {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); // NOSONAR

      for (String unsupportedString : parser.unsupportedElements) {
        try {
          Document unsupported = factory.newDocumentBuilder().parse(new ByteArrayInputStream(unsupportedString.getBytes("UTF-8")));
          root.appendChild(document.importNode(unsupported.getFirstChild(), true));
        }
        catch (Exception e) {
          getLogger().error("import unsupported tags: {}", e.getMessage());
        }
      }
    }
  }

  /**
   * add the missing meta data for tinyMediaManager to this NFO
   */
  protected void addTinyMediaManagerTags() {
    root.appendChild(document.createComment("tinyMediaManager meta data"));
    addUserNote();
  }

  /**
   * add the user note in <user_note>xxx</user_note>
   */
  protected void addUserNote() {
    Element user_note = document.createElement("user_note");
    user_note.setTextContent(tvShow.getNote());
    root.appendChild(user_note);
  }

  /**
   * get any single element by the tag name
   *
   * @param tag
   *          the tag name
   * @return an element or null
   */
  protected Element getSingleElementByTag(String tag) {
    NodeList nodeList = document.getElementsByTagName(tag);
    for (int i = 0; i < nodeList.getLength(); ++i) {
      Node node = nodeList.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        return (Element) node;
      }
    }
    return null;
  }

  /**
   * get the transformer for XML output
   *
   * @return the transformer
   * @throws Exception
   *           any Exception that has been thrown
   */
  protected Transformer getTransformer() throws Exception {
    Transformer transformer = TransformerFactory.newInstance().newTransformer(); // NOSONAR

    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
    // not supported in all JVMs
    try {
      transformer.setOutputProperty(ORACLE_IS_STANDALONE, "yes");
    }
    catch (Exception ignored) {
      // okay, seems we're not on OracleJDK, OPenJDK or AdopOpenJDK
    }
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

    return transformer;
  }

  /**
   * try to detect the default scraper by the given ids
   * 
   * @return the scraper where the default should be set
   */
  private String detectDefaultScraper() {
    // TVDB first
    if (tvShow.getIds().containsKey(MediaMetadata.TVDB)) {
      return MediaMetadata.TVDB;
    }

    // IMDB second
    if (tvShow.getIds().containsKey(MediaMetadata.IMDB)) {
      return MediaMetadata.IMDB;
    }

    // TMDB third
    if (tvShow.getIds().containsKey(MediaMetadata.TMDB)) {
      return MediaMetadata.TMDB;
    }

    // the first found as fallback
    return tvShow.getIds().keySet().stream().findFirst().orElse("");
  }
}
