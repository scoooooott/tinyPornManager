/*
 * Copyright 2012 - 2017 Manuel Laggner
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

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.core.entities.Rating;
import org.tinymediamanager.core.tvshow.TvShowModuleManager;
import org.tinymediamanager.core.tvshow.entities.TvShow;
import org.tinymediamanager.core.tvshow.filenaming.TvShowNfoNaming;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;

/**
 * this class is a general XML connector which suits as a base class for most xml based connectors
 *
 * @author Manuel Laggner
 */
public abstract class TvShowGenericXmlConnector implements ITvShowConnector {
  protected final TvShow    tvShow;
  protected TvShowNfoParser parser = null;

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
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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
        addShowTitle();
        addYear();
        addRating();
        addVotes();
        addOutline();
        addPlot();
        addTagline();
        addRuntime();
        addPoster();
        addSeasonPoster();
        addFanart();
        addMpaa();
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

        // add connector specific tags
        addOwnTags();

        // add unsupported tags
        addUnsupportedTags();

        // serialize to string
        Writer out = new StringWriter();
        getTransformer().transform(new DOMSource(document), new StreamResult(out));
        String xml = out.toString().replaceAll("(?<!\r)\n", "\r\n"); // windows conform line endings

        // write to file
        Path f = tvShow.getPathNIO().resolve(nfoFilename);
        Utils.writeStringToFile(f, xml);
        MediaFile mf = new MediaFile(f);
        mf.gatherMediaInformation(true); // force to update filedate
        newNfos.add(mf);
      }
      catch (Exception e) {
        getLogger().error("write " + tvShow.getPathNIO().resolve(nfoFilename) + " :" + e.getMessage());
        MessageManager.instance
            .pushMessage(new Message(Message.MessageLevel.ERROR, tvShow, "message.nfo.writeerror", new String[] { ":", e.getLocalizedMessage() }));
      }
    }

    if (newNfos.size() > 0) {
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
   * add the showtitle in the form <showtitle>xxx</showtitle>
   */
  protected void addShowTitle() {
    Element title = document.createElement("showtitle");
    title.setTextContent(tvShow.getTitle());
    root.appendChild(title);
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
    Rating mainRating = tvShow.getRating();

    if (mainRating.getMaxValue() > 0) {
      rating10 = mainRating.getRating() * 10 / mainRating.getMaxValue();
    }
    else {
      rating10 = mainRating.getRating();
    }

    Element rating = document.createElement("rating");
    rating.setTextContent(String.format(Locale.US, "%.1f", rating10));
    root.appendChild(rating);
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
   * add the season posters in multiple <thumb aspect="poster" type="season" season="x">xxx</thumb> tags
   */
  protected void addSeasonPoster() {
    for (Map.Entry<Integer, String> entry : tvShow.getSeasonPosterUrls().entrySet()) {
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
      if (TvShowModuleManager.SETTINGS.getCertificationCountry() == CountryCode.US) {
        // if we have US certs, write correct "Rated XX" String
        mpaa.setTextContent(Certification.getMPAAString(tvShow.getCertification()));
      }
      else {
        mpaa.setTextContent(CertificationStyle.formatCertification(tvShow.getCertification(), TvShowModuleManager.SETTINGS.getCertificationStyle()));
      }
    }
    root.appendChild(mpaa);
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
    Element tmdbid = document.createElement("imdbid");
    tmdbid.setTextContent(tvShow.getImdbId());
    root.appendChild(tmdbid);
  }

  /**
   * add our own id store in the form <ids><id_provider>id</id_provide></ids>
   */
  protected void addIds() {
    Element ids = document.createElement("ids");
    for (Map.Entry<String, Object> entry : tvShow.getIds().entrySet()) {
      Element id = document.createElement(entry.getKey());
      id.setTextContent(entry.getValue().toString());
      ids.appendChild(id);
    }
    root.appendChild(ids);
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
   * add the status in <status>xxx</status>
   */
  protected void addStatus() {
    Element status = document.createElement("status");
    status.setTextContent(tvShow.getStatus());
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
      genre.setTextContent(mediaGenre.getName());
      root.appendChild(genre);
    }
  }

  /**
   * add studios in <studio>xxx</studio> tags (multiple)
   */
  protected void addStudios() {
    List<String> studios = Arrays.asList(tvShow.getProductionCompany().split("\\s*[,\\/]\\s*")); // split on , or / and remove whitespace around
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

      root.appendChild(actor);
    }
  }

  /**
   * add the trailer url in <trailer>xxx</trailer>
   */
  protected void addTrailer() {
    Element trailer = document.createElement("trailer");
    if (parser != null && StringUtils.isNotBlank(parser.trailer)) {
      trailer.setTextContent(parser.trailer);
    }
    root.appendChild(trailer);
  }

  /**
   * add all unsupported tags from the source file to the destination file
   */
  protected void addUnsupportedTags() {
    if (parser != null) {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

      for (String unsupportedString : parser.unsupportedElements) {
        try {
          Document unsupported = factory.newDocumentBuilder().parse(new ByteArrayInputStream(unsupportedString.getBytes("UTF-8")));
          root.appendChild(document.importNode(unsupported.getFirstChild(), true));
        }
        catch (Exception e) {
          getLogger().error("import unsupported tags: " + e.getMessage());
        }
      }
    }
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
    Transformer transformer = TransformerFactory.newInstance().newTransformer();

    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
    transformer.setOutputProperty(OutputPropertiesFactory.ORACLE_IS_STANDALONE, "yes");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

    return transformer;
  }
}
