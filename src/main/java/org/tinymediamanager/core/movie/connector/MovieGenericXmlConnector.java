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

package org.tinymediamanager.core.movie.connector;

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

import org.apache.commons.lang3.LocaleUtils;
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
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.core.movie.entities.MovieTrailer;
import org.tinymediamanager.core.movie.filenaming.MovieNfoNaming;
import org.tinymediamanager.scraper.entities.Certification;
import org.tinymediamanager.scraper.entities.CountryCode;
import org.tinymediamanager.scraper.entities.MediaGenres;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * this class is a general XML connector which suits as a base class for most xml based connectors
 *
 * @author Manuel Laggner
 */
public abstract class MovieGenericXmlConnector implements IMovieConnector {
  protected final String   ORACLE_IS_STANDALONE = "http://www.oracle.com/xml/is-standalone";

  protected final Movie    movie;
  protected MovieNfoParser parser = null;

  protected Document       document;
  protected Element        root;

  public MovieGenericXmlConnector(Movie movie) {
    this.movie = movie;
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
  public void write(List<MovieNfoNaming> nfoNames) {

    // first of all, get the data from a previous written NFO file,
    // if we do not want clean NFOs
    if (!MovieModuleManager.SETTINGS.isWriteCleanNfo()) {
      for (MediaFile mf : movie.getMediaFiles(MediaFileType.NFO)) {
        try {
          parser = MovieNfoParser.parseNfo(mf.getFileAsPath());
          break;
        }
        catch (Exception ignored) {
        }
      }
    }

    List<MediaFile> newNfos = new ArrayList<>(1);

    for (MovieNfoNaming nfoNaming : nfoNames) {
      String nfoFilename = movie.getNfoFilename(nfoNaming);
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

        root = document.createElement("movie");
        document.appendChild(root);

        // add well known tags
        addTitle();
        addOriginaltitle();
        addSorttitle();
        addYear();
        addRating();
        addVotes();
        addSet();
        addOutline();
        addPlot();
        addTagline();
        addRuntime();
        addThumb();
        addFanart();
        addMpaa();
        addCertification();
        addId();
        addTmdbid();
        addIds();
        addCountry();
        addPremiered();
        addWatched();
        addPlaycount();
        addGenres();
        addStudios();
        addCredits();
        addDirectors();
        addTags();
        addActors();
        addProducers();
        addTrailer();
        addLanguages();

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

        // write to file
        Path f = movie.getPathNIO().resolve(nfoFilename);
        Utils.writeStringToFile(f, xml);
        MediaFile mf = new MediaFile(f);
        mf.gatherMediaInformation(true); // force to update filedate
        newNfos.add(mf);
      }
      catch (Exception e) {
        getLogger().error("write " + movie.getPathNIO().resolve(nfoFilename) + " :" + e.getMessage());
        MessageManager.instance
            .pushMessage(new Message(Message.MessageLevel.ERROR, movie, "message.nfo.writeerror", new String[] { ":", e.getLocalizedMessage() }));
      }
    }

    if (newNfos.size() > 0) {
      movie.removeAllMediaFiles(MediaFileType.NFO);
      movie.addToMediaFiles(newNfos);
    }
  }

  /**
   * add the title in the form <title>xxx</title>
   */
  protected void addTitle() {
    Element title = document.createElement("title");
    title.setTextContent(movie.getTitle());
    root.appendChild(title);
  }

  /**
   * add the originaltitle in the form <originaltitle>xxx</originaltitle>
   */
  protected void addOriginaltitle() {
    Element originaltitle = document.createElement("originaltitle");
    originaltitle.setTextContent(movie.getOriginalTitle());
    root.appendChild(originaltitle);
  }

  /**
   * add the sorttitle in the form <sorttitle>xxx</sorttitle>
   */
  protected void addSorttitle() {
    Element sorttitle = document.createElement("sorttitle");
    sorttitle.setTextContent(movie.getSortTitle());
    root.appendChild(sorttitle);
  }

  /**
   * add the year in the form <year>xxx</year>
   */
  protected void addYear() {
    Element year = document.createElement("year");
    year.setTextContent(movie.getYear() == 0 ? "" : Integer.toString(movie.getYear()));
    root.appendChild(year);
  }

  /**
   * add the rating in the form <rating>xxx</rating> (floating point with one decimal)
   */
  protected void addRating() {
    // get main rating and calculate the rating value to a base of 10
    Float rating10;
    Rating mainRating = movie.getRating();

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
    votes.setTextContent(Integer.toString(movie.getRating().getVotes()));
    root.appendChild(votes);
  }

  /**
   * add the set information in the form <set>xxx</set>
   */
  protected void addSet() {
    Element set = document.createElement("set");
    set.setTextContent(movie.getMovieSetTitle());
    root.appendChild(set);
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
    plot.setTextContent(movie.getPlot());
    root.appendChild(plot);
  }

  /**
   * add the tagline in the form <tagline>xxx</tagline>
   */
  protected void addTagline() {
    Element tagline = document.createElement("tagline");
    tagline.setTextContent(movie.getTagline());
    root.appendChild(tagline);
  }

  /**
   * add the runtime in the form <runtime>xxx</runtime> (integer)
   */
  protected void addRuntime() {
    Element runtime = document.createElement("runtime");
    runtime.setTextContent(Integer.toString(movie.getRuntime()));
    root.appendChild(runtime);
  }

  /**
   * add the thumb (poster) url in the form <thumb>xxx</thumb>
   */
  protected void addThumb() {
    Element thumb = document.createElement("thumb");
    thumb.setTextContent(movie.getArtworkUrl(MediaFileType.POSTER));
    root.appendChild(thumb);
  }

  /**
   * add the fanart url in the form <fanart>xxx</fanart>
   */
  protected void addFanart() {
    Element fanart = document.createElement("fanart");
    fanart.setTextContent(movie.getArtworkUrl(MediaFileType.FANART));
    root.appendChild(fanart);
  }

  /**
   * add the certification in <mpaa>xxx</mpaa>
   */
  protected void addMpaa() {
    Element mpaa = document.createElement("mpaa");

    if (movie.getCertification() != null) {
      if (MovieModuleManager.SETTINGS.getCertificationCountry() == CountryCode.US) {
        // if we have US certs, write correct "Rated XX" String
        mpaa.setTextContent(Certification.getMPAAString(movie.getCertification()));
      }
      else {
        mpaa.setTextContent(CertificationStyle.formatCertification(movie.getCertification(), MovieModuleManager.SETTINGS.getCertificationStyle()));
      }
    }
    root.appendChild(mpaa);
  }

  /**
   * add the certification in <certification></certification>
   */
  protected void addCertification() {
    Element certification = document.createElement("certification");
    if (movie.getCertification() != null) {
      certification
          .setTextContent(CertificationStyle.formatCertification(movie.getCertification(), MovieModuleManager.SETTINGS.getCertificationStyle()));
    }
    root.appendChild(certification);
  }

  /**
   * add the imdb id in <id>xxx</id>
   */
  protected void addId() {
    Element id = document.createElement("id");
    id.setTextContent(movie.getImdbId());
    root.appendChild(id);
  }

  /**
   * add the tmdb id in <tmdbid>xxx</tmdbid>
   */
  protected void addTmdbid() {
    Element tmdbid = document.createElement("tmdbid");
    if (movie.getTmdbId() > 0) {
      tmdbid.setTextContent(Integer.toString(movie.getTmdbId()));
    }
    root.appendChild(tmdbid);
  }

  /**
   * add our own id store in the form <ids><id_provider>id</id_provide></ids>
   */
  protected void addIds() {
    Element ids = document.createElement("ids");
    for (Map.Entry<String, Object> entry : movie.getIds().entrySet()) {
      Element id = document.createElement(entry.getKey());
      id.setTextContent(entry.getValue().toString());
      ids.appendChild(id);
    }
    root.appendChild(ids);
  }

  /**
   * add the country in <country>xxx</country>
   */
  protected void addCountry() {
    Element country = document.createElement("country");
    country.setTextContent(movie.getCountry());
    root.appendChild(country);
  }

  /**
   * add the premiered date in <premiered>xxx</premiered>
   */
  protected void addPremiered() {
    Element premiered = document.createElement("premiered");
    if (movie.getReleaseDate() != null) {
      premiered.setTextContent(new SimpleDateFormat("yyyy-MM-dd").format(movie.getReleaseDate()));
    }
    root.appendChild(premiered);
  }

  /**
   * add the watched flag in <watched>xxx</watched>
   */
  protected void addWatched() {
    Element watched = document.createElement("watched");
    watched.setTextContent(Boolean.toString(movie.isWatched()));
    root.appendChild(watched);
  }

  /**
   * add the playcount in <playcount>xxx</playcount> (integer) we do not have this in tmm, but we might get it from an existing nfo
   */
  protected void addPlaycount() {
    Element playcount = document.createElement("playcount");
    if (movie.isWatched() && parser != null && parser.playcount > 0) {
      playcount.setTextContent(Integer.toString(parser.playcount));
    }
    else if (movie.isWatched()) {
      playcount.setTextContent("1");
    }
    root.appendChild(playcount);
  }

  /**
   * add genres in <genre>xxx</genre> tags (multiple)
   */
  protected void addGenres() {
    for (MediaGenres mediaGenre : movie.getGenres()) {
      Element genre = document.createElement("genre");
      genre.setTextContent(mediaGenre.getLocalizedName(LocaleUtils.toLocale(MovieModuleManager.SETTINGS.getNfoLanguage().name())));
      root.appendChild(genre);
    }
  }

  /**
   * add studios in <studio>xxx</studio> tags (multiple)
   */
  protected void addStudios() {
    List<String> studios = Arrays.asList(movie.getProductionCompany().split("\\s*[,\\/]\\s*")); // split on , or / and remove whitespace around
    for (String s : studios) {
      Element studio = document.createElement("studio");
      studio.setTextContent(s);
      root.appendChild(studio);
    }
  }

  /**
   * add credits in <credits>xxx</credits> tags (mulitple)
   */
  protected void addCredits() {
    for (Person writer : movie.getWriters()) {
      Element element = document.createElement("credits");
      element.setTextContent(writer.getName());
      root.appendChild(element);
    }
  }

  /**
   * add directors in <director>xxx</director> tags (mulitple)
   */
  protected void addDirectors() {
    for (Person director : movie.getDirectors()) {
      Element element = document.createElement("director");
      element.setTextContent(director.getName());
      root.appendChild(element);
    }
  }

  /**
   * add tags in <tag>xxx</tag> tags (multiple)
   */
  protected void addTags() {
    for (String t : movie.getTags()) {
      Element tag = document.createElement("tag");
      tag.setTextContent(t);
      root.appendChild(tag);
    }
  }

  /**
   * add actors in <actor><name>xxx</name><role>xxx</role><thumb>xxx</thumb></actor>
   */
  protected void addActors() {
    for (Person movieActor : movie.getActors()) {
      Element actor = document.createElement("actor");

      Element name = document.createElement("name");
      name.setTextContent(movieActor.getName());
      actor.appendChild(name);

      Element role = document.createElement("role");
      role.setTextContent(movieActor.getRole());
      actor.appendChild(role);

      Element thumb = document.createElement("thumb");
      thumb.setTextContent(movieActor.getThumbUrl());
      actor.appendChild(thumb);

      root.appendChild(actor);
    }
  }

  /**
   * add producers in <producer><name>xxx</name><role>xxx</role><thumb>xxx</thumb></producer>
   */
  protected void addProducers() {
    for (Person movieProducer : movie.getProducers()) {
      Element producer = document.createElement("producer");

      Element name = document.createElement("name");
      name.setTextContent(movieProducer.getName());
      producer.appendChild(name);

      Element role = document.createElement("role");
      role.setTextContent(movieProducer.getRole());
      producer.appendChild(role);

      Element thumb = document.createElement("thumb");
      thumb.setTextContent(movieProducer.getThumbUrl());
      producer.appendChild(thumb);

      root.appendChild(producer);
    }
  }

  /**
   * add spoken languages in <languages>xxx</languages>
   */
  protected void addLanguages() {
    Element languages = document.createElement("languages");
    languages.setTextContent(movie.getSpokenLanguages());
    root.appendChild(languages);
  }

  /**
   * add the trailer url in <trailer>xxx</trailer>
   */
  protected void addTrailer() {
    Element trailer = document.createElement("trailer");
    for (MovieTrailer movieTrailer : new ArrayList<>(movie.getTrailer())) {
      if (movieTrailer.getInNfo() && !movieTrailer.getUrl().startsWith("file")) {
        trailer.setTextContent(movieTrailer.getDownloadUrl());
        break;
      }
    }
    root.appendChild(trailer);
  }

  /**
   * add the media source <source>xxx</source>
   */
  protected void addSource() {
    Element source = document.createElement("source");
    source.setTextContent(movie.getMediaSource().name());
    root.appendChild(source);
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
   * add the missing meta data for tinyMediaManager to this NFO
   */
  protected void addTinyMediaManagerTags() {
    root.appendChild(document.createComment("tinyMediaManager meta data"));
    addSource();
    addEdition();
  }

  /**
   * add the edition in <edition>xxx</edition>
   */
  protected void addEdition() {
    Element edition = document.createElement("edition");
    edition.setTextContent(movie.getEdition().name());
    root.appendChild(edition);
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
    transformer.setOutputProperty(ORACLE_IS_STANDALONE, "yes");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

    return transformer;
  }

}
