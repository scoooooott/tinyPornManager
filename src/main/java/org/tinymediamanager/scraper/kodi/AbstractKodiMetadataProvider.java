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
package org.tinymediamanager.scraper.kodi;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.UnicodeUnescaper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.entities.MediaGenres;
import org.tinymediamanager.core.entities.MediaRating;
import org.tinymediamanager.core.entities.Person;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaSearchAndScrapeOptions;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.entities.MediaArtwork;
import org.tinymediamanager.scraper.entities.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.exceptions.ScrapeException;
import org.tinymediamanager.scraper.interfaces.IKodiMetadataProvider;
import org.tinymediamanager.scraper.util.DOMUtils;
import org.tinymediamanager.scraper.util.StrgUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

/**
 * The abstract class for the main Kodi plugin parsing logic
 *
 * @author Manuel Laggner
 */
public abstract class AbstractKodiMetadataProvider implements IKodiMetadataProvider {
  private static final Logger          LOGGER       = LoggerFactory.getLogger(AbstractKodiMetadataProvider.class);
  private final UnicodeUnescaper       uu           = new UnicodeUnescaper();
  private final DocumentBuilderFactory factory;

  public KodiScraper                   scraper;
  protected KodiAddonProcessor         processor    = null;
  private String                       baseImageUrl = "";

  public AbstractKodiMetadataProvider(KodiScraper scraper) {
    KodiScraperParser parser = new KodiScraperParser();
    try {
      scraper = parser.parseScraper(scraper, KodiUtil.commonXmls);
    }
    catch (Exception e) {
      LOGGER.error("Failed to Load Kodi Scraper: {}", scraper);
      throw new RuntimeException("Failed to Load Kodi Scraper: " + scraper, e);
    }
    this.scraper = scraper;
    factory = DocumentBuilderFactory.newInstance();
  }

  @Override
  public MediaProviderInfo getProviderInfo() {
    return scraper.getProviderInfo();
  }

  @Override
  public String getId() {
    return scraper.getId();
  }

  protected SortedSet<MediaSearchResult> _search(MediaSearchAndScrapeOptions options) throws ScrapeException {
    // always reset/instantiate on search
    processor = new KodiAddonProcessor(scraper);

    SortedSet<MediaSearchResult> l = new TreeSet<>();
    String arg = options.getSearchQuery();

    // cannot search without any title/query
    if (StringUtils.isBlank(arg)) {
      return l;
    }

    // Kodi wants title and year separated, so let's do that
    String args[] = parseTitle(arg);
    String title = args[0];
    int year = 0;
    if (options.getSearchYear() != 0) {
      year = options.getSearchYear();
    }

    try {
      KodiUrl url = processor.getSearchUrl(title, year > 0 ? String.valueOf(year) : "");
      if (url == null) {
        return l; // error processing XML, nothing found
      }
      String xmlString = processor.getSearchResults(url);

      LOGGER.debug("========= BEGIN Kodi Scraper Search Xml Results: Url: {}", url);
      LOGGER.debug(xmlString);
      LOGGER.debug("========= END Kodi Scraper Search Xml Results: Url: {}", url);

      Document xml = parseXmlString(xmlString);

      NodeList nl = xml.getElementsByTagName("entity");
      for (int i = 0; i < nl.getLength(); i++) {
        try {
          Element el = (Element) nl.item(i);
          NodeList titleList = el.getElementsByTagName("title");
          String t = titleList.item(0).getTextContent();
          NodeList yearList = el.getElementsByTagName("year");
          String y = yearList == null || yearList.getLength() == 0 ? "" : yearList.item(0).getTextContent();
          NodeList urlList = el.getElementsByTagName("url");
          KodiUrl u = new KodiUrl((Element) urlList.item(0));

          MediaSearchResult sr = new MediaSearchResult(scraper.getProviderInfo().getId(), options.getMediaType());
          String id = DOMUtils.getElementValue(el, "id");
          sr.setId(id);
          sr.setUrl(u.toExternalForm());
          sr.setProviderId(scraper.getProviderInfo().getId());

          if (u.toExternalForm().contains("imdb")) {
            sr.setIMDBId(id);
          }

          // String v[] = ParserUtils.parseTitle(t);
          // sr.setTitle(v[0]);
          // sr.setYear(v[1]);
          sr.setTitle(t);
          try {
            sr.setYear(Integer.parseInt(y));
          }
          catch (Exception ignored) {
            // no need to log here
          }

          // calculate score
          sr.calculateScore(options);

          l.add(sr);
        }
        catch (Exception e) {
          LOGGER.error("Error process an xml node!  Ignoring it from the search results.");
        }
      }

      return l;
    }
    catch (Exception e) {
      LOGGER.error("problem searching: {}", e.getMessage());
      throw new ScrapeException(e);
    }
  }

  /**
   * Is i1 != i2 (when >0)
   */
  private boolean yearDiffers(int i1, int i2) {
    return i1 > 0 && i2 > 0 && i1 != i2;
  }

  protected MediaMetadata _getMetadata(MediaSearchAndScrapeOptions options) throws ScrapeException {
    try {
      MediaMetadata md = new MediaMetadata(scraper.getProviderInfo().getId());
      MediaSearchResult result = options.getSearchResult();

      if (result.getIMDBId() != null && result.getIMDBId().contains("tt")) {
        md.setId(MediaMetadata.IMDB, result.getIMDBId());
      }

      String id = result.getIdAsString(result.getProviderId());
      String xmlDetails = processor.getDetails(new KodiUrl(result.getUrl()), id);

      // save scraper ID
      if (!StringUtils.isEmpty(id)) {
        md.setId(scraper.getProviderInfo().getId(), result.getId());
      }

      // workaround: replace problematic sequences
      xmlDetails = xmlDetails.replace("&nbsp;", " ");
      processXmlContent(xmlDetails, md, result);

      // try to parse an imdb id from the url
      if (!StringUtils.isEmpty(result.getUrl()) && md.getId(MediaMetadata.IMDB) != null) {
        md.setId(MediaMetadata.IMDB, parseIMDBID(result.getUrl()));
      }

      return md;
    }
    catch (Exception e) {
      LOGGER.error("problem scraping: {}", e.getMessage());
      throw new ScrapeException(e);
    }
  }

  private String parseIMDBID(String url) {
    if (url == null)
      return null;
    Pattern p = Pattern.compile("/(tt[0-9]+)/");
    Matcher m = p.matcher(url);
    if (m.find()) {
      return m.group(1);
    }
    return "";
  }

  /**
   * W3C no easy method to get complete string out of node/element
   *
   * @param node
   * @return String
   */
  protected String innerXml(Node node) {
    DOMImplementationLS lsImpl = (DOMImplementationLS) node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
    LSSerializer lsSerializer = lsImpl.createLSSerializer();
    lsSerializer.getDomConfig().setParameter("xml-declaration", false);
    NodeList childNodes = node.getChildNodes();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < childNodes.getLength(); i++) {
      sb.append(lsSerializer.writeToString(childNodes.item(i)));
    }
    return sb.toString();
  }

  protected Document parseXmlString(String xmlString) throws Exception {
    DocumentBuilder parser = factory.newDocumentBuilder();

    String xml = xmlString;
    // xml = Utils.replaceAcutesHTML(xml);
    xml = uu.translate(xml);
    xml = StringEscapeUtils.unescapeHtml4(xml);
    xml = StringEscapeUtils.unescapeXml(xml);
    xml = StringEscapeUtils.unescapeXml(xml);
    xml = StringEscapeUtils.unescapeXml(xml);
    xml = xml.replaceAll("\\&", "\\&amp;");
    // xml = StringEscapeUtils.escapeXml(xml);

    Document doc = null;
    for (String charset : new String[] { "UTF-8", "ISO-8859-1", "US-ASCII" }) {
      try {
        doc = parser.parse(new ByteArrayInputStream(xml.getBytes(charset)));
        break;
      }
      catch (Throwable t) {
        LOGGER.error("Failed to parse xml using charset: " + charset + " - " + t.getMessage());
      }
    }

    if (doc == null) {
      LOGGER.error("Unabled to parse xml string");
      LOGGER.error(xml);
      throw new Exception("Unable to parse xml!");
    }

    return doc;
  }

  protected void addMetadata(MediaMetadata md, Element details) {
    LOGGER.debug("Processing <details> node....");

    NodeList subDetails = details.getElementsByTagName("details");

    String title = getInfoFromScraperFunctionOrBase("title", details, subDetails);
    if (StringUtils.isNotBlank(title)) {
      md.setTitle(title);
    }

    String originalTitle = getInfoFromScraperFunctionOrBase("originaltitle", details, subDetails);
    if (StringUtils.isNotBlank(originalTitle)) {
      md.setOriginalTitle(originalTitle);
    }

    String plot = getInfoFromScraperFunctionOrBase("plot", details, subDetails);
    if (StringUtils.isNotBlank(plot)) {
      md.setPlot(plot);
    }

    String year = getInfoFromScraperFunctionOrBase("year", details, subDetails);
    if (StringUtils.isNotBlank(year)) {
      try {
        md.setYear(Integer.parseInt(year));
      }
      catch (Exception ignored) {
      }
    }
    String aired = getInfoFromScraperFunctionOrBase("aired", details, subDetails);
    if (StringUtils.isNotBlank(aired)) {
      try {
        md.setReleaseDate(StrgUtils.parseDate(aired));
      }
      catch (ParseException ignored) {
      }
    }
    String premiered = getInfoFromScraperFunctionOrBase("premiered", details, subDetails);
    if (StringUtils.isNotBlank(premiered)) {
      try {
        md.setReleaseDate(StrgUtils.parseDate(premiered));
      }
      catch (ParseException ignored) {
      }
    }
    if (md.getYear() == 0 && md.getReleaseDate() != null) {
      // fallback - if we have no year set (like UniversalScraper)
      LocalDate localDate = md.getReleaseDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      md.setYear(localDate.getYear());
    }

    String tagline = getInfoFromScraperFunctionOrBase("tagline", details, subDetails);
    if (StringUtils.isNotBlank(tagline)) {
      md.setTagline(tagline);
    }

    // <rating>8.0</rating>
    // or
    // <ratings>
    // <rating max="10" name="themoviedb" default="true">
    // <value>5.7</value>
    // <votes>661</votes>
    // </rating>
    // </ratings>

    NodeList nlr = details.getElementsByTagName("rating");
    for (int i = 0; i < nlr.getLength(); i++) {
      Element rating = (Element) nlr.item(i);

      String id = rating.getAttribute("name");
      if (id == null || id.isEmpty()) {
        id = getProviderInfo().getId();
      }
      MediaRating rat = new MediaRating(id);

      float value = 0;
      Element val = DOMUtils.getElementByTagName(rating, "value");
      if (val == null) {
        // no value tag? must be old rating element...
        value = NumberUtils.toFloat(rating.getTextContent().trim());

        String votes = getInfoFromScraperFunctionOrBase("votes", details, subDetails);
        if (StringUtils.isNotBlank(votes)) {
          try {
            rat.setVotes(Integer.parseInt(votes));
          }
          catch (NumberFormatException ignored) {
            LOGGER.trace("unparsable votecount: {}", votes);
          }
        }
      }
      else {
        value = NumberUtils.toFloat(val.getTextContent().trim());
        rat.setVotes(DOMUtils.getElementIntValue(rating, "votes"));
      }
      rat.setRating(value);

      int maxValue = NumberUtils.toInt(rating.getAttribute("max"));
      if (maxValue > 0) {
        rat.setMaxValue(maxValue);
      }
      else {
        // try an educated guess of value...
        if (value > 10) {
          rat.setMaxValue(100);
        }
      }

      md.addRating(rat);
    }

    String set = getInfoFromScraperFunctionOrBase("set", details, subDetails);
    if (StringUtils.isNotBlank(set)) {
      md.setCollectionName(set);
    }

    String country = getInfoFromScraperFunctionOrBase("country", details, subDetails);
    if (StringUtils.isNotBlank(country)) {
      md.setCountries(Arrays.asList(country)); // just add first
    }

    String studio = getInfoFromScraperFunctionOrBase("studio", details, subDetails);
    if (StringUtils.isNotBlank(studio)) {
      md.setProductionCompanies(Arrays.asList(studio)); // just add first
    }

    String runtime = getInfoFromScraperFunctionOrBase("runtime", details, subDetails);
    if (StringUtils.isNotBlank(runtime)) {
      try {
        md.setRuntime(Integer.parseInt(runtime));
      }
      catch (NumberFormatException ignored) {
      }
    }

    // finds all <details> elements w/o subitems - this could be our image base url?!
    for (int i = 0; i < subDetails.getLength(); i++) {
      Element el = (Element) subDetails.item(i);
      Node n = el.getFirstChild();
      if (n != null && !n.hasChildNodes() && el.getTextContent().startsWith("http")) {
        baseImageUrl = el.getTextContent();
      }
    }

    // fanarts
    NodeList nl = details.getElementsByTagName("fanart");
    for (int i = 0; i < nl.getLength(); i++) {
      Element fanart = (Element) nl.item(i);
      String url = fanart.getAttribute("url");
      NodeList thumbs = fanart.getElementsByTagName("thumb");
      if (thumbs != null && thumbs.getLength() > 0) {
        processMediaArt(md, MediaArtworkType.BACKGROUND, "Backgrounds", thumbs, url);
      }
      else {
        if (!StringUtils.isEmpty(url)) {
          processMediaArt(md, MediaArtworkType.BACKGROUND, "Background", url);
        }
      }
    }

    // thumbs
    nl = details.getElementsByTagName("thumb");
    for (int i = 0; i < nl.getLength(); i++) {
      Element poster = (Element) nl.item(i);
      if (poster.getParentNode().getNodeName().equals("details"))
        processMediaArt(md, MediaArtworkType.POSTER, "Poster", poster, baseImageUrl);
    }

    // actors
    nl = details.getElementsByTagName("actor");
    for (int i = 0; i < nl.getLength(); i++) {
      Element actor = (Element) nl.item(i);
      Person cm = new Person(Person.Type.ACTOR);
      cm.setName(DOMUtils.getElementValue(actor, "name"));
      cm.setRole(DOMUtils.getElementValue(actor, "role"));
      String pic = DOMUtils.getElementValue(actor, "thumb");
      if (StringUtils.isNotBlank(pic)) {
        cm.setThumbUrl(pic.startsWith("http") ? pic : baseImageUrl + pic);
      }
      md.addCastMember(cm);
    }

    // directors
    nl = details.getElementsByTagName("director");
    for (int i = 0; i < nl.getLength(); i++) {
      Element el = (Element) nl.item(i);
      Person cm = new Person(Person.Type.DIRECTOR);
      cm.setName(StringUtils.trim(el.getTextContent()));
      String pic = DOMUtils.getElementValue(el, "thumb");
      if (StringUtils.isNotBlank(pic)) {
        cm.setThumbUrl(pic.startsWith("http") ? pic : baseImageUrl + pic);
      }
      md.addCastMember(cm);
    }

    // credits
    nl = details.getElementsByTagName("credits");
    for (int i = 0; i < nl.getLength(); i++) {
      Element el = (Element) nl.item(i);
      Person cm = new Person(Person.Type.WRITER);
      cm.setName(StringUtils.trim(el.getTextContent()));
      String pic = DOMUtils.getElementValue(el, "thumb");
      if (StringUtils.isNotBlank(pic)) {
        cm.setThumbUrl(pic.startsWith("http") ? pic : baseImageUrl + pic);
      }
      md.addCastMember(cm);
    }

    // genres
    nl = details.getElementsByTagName("genre");
    for (int i = 0; i < nl.getLength(); i++) {
      Element el = (Element) nl.item(i);
      String g = StringUtils.trim(el.getTextContent());
      if (g != null && !g.isEmpty()) {
        MediaGenres genre = MediaGenres.getGenre(g);
        if (genre != null) {
          md.addGenre(genre);
        }
      }
    }
  }

  /**
   * first search the tag in any <details> (from a scraperfunction), then in base
   *
   * @param tag
   *          the tag to search for
   * @param subDetails
   *          a node list
   * @return the found info or an empty String
   */
  private String getInfoFromScraperFunctionOrBase(String tag, Element details, NodeList subDetails) {
    String info = "";

    for (int i = 0; i < subDetails.getLength(); i++) {
      Element subDetail = (Element) subDetails.item(i);
      NodeList nl = subDetail.getElementsByTagName(tag);
      for (int j = 0; j < nl.getLength();) {
        Element el = (Element) nl.item(j);
        info = el.getTextContent();
        break;
      }
      if (!StringUtils.isBlank(info))
        break;
    }

    if (StringUtils.isBlank(info)) {
      info = DOMUtils.getElementValue(details, tag);
    }

    return info;
  }

  private void processMediaArt(MediaMetadata md, MediaArtworkType type, String label, NodeList els, String baseUrl) {
    for (int i = 0; i < els.getLength(); i++) {
      Element e = (Element) els.item(i);
      String image = e.getTextContent();
      if (image != null)
        image = image.trim();
      if (baseUrl != null) {
        baseUrl = baseUrl.trim();
        image = baseUrl + image;
      }
      processMediaArt(md, type, label, image);
    }
  }

  private void processMediaArt(MediaMetadata md, MediaArtworkType type, String label, Element e, String baseUrl) {
    String image = e.getTextContent();
    if (image != null)
      image = image.trim();
    if (baseUrl != null) {
      baseUrl = baseUrl.trim();
      image = baseUrl + image;
    }

    processMediaArt(md, type, label, image);

    // Some scrapers respond with multiple <thumb> tags.
    // since we do not know what is a poster, or a fanart, lets add them to both ;)
    if (type == MediaArtworkType.POSTER) {
      processMediaArt(md, MediaArtworkType.BACKGROUND, label, image);
    }
  }

  private void processMediaArt(MediaMetadata md, MediaArtworkType type, String label, String image) {
    MediaArtwork ma = new MediaArtwork(md.getProviderId(), type);
    ma.setPreviewUrl(image);
    ma.setDefaultUrl(image);
    md.addMediaArt(ma);
  }

  /**
   * return a 2 element array. 0 = title; 1=date
   * <p>
   * parses the title in the format Title YEAR or Title (YEAR)
   *
   * @param title
   *          the title
   * @return the string[]
   */
  private String[] parseTitle(String title) {
    String v[] = { "", "" };
    if (title == null)
      return v;

    Pattern p = Pattern.compile("(.*)\\s+\\(?([0-9]{4})\\)?", Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(title);
    if (m.find()) {
      v[0] = m.group(1);
      v[1] = m.group(2);
    }
    else {
      v[0] = title;
    }
    return v;
  }

  /**
   * add leadingZero if only 1 char
   *
   * @param num
   *          the number
   * @return the string with a leading 0
   */
  protected String lz(int num) {
    return String.format("%02d", num);
  }

  /**
   * converts an w3c dom Element to String
   *
   * @param el
   * @return String or NULL
   */
  protected String elementToString(Element el) {
    try {
      DOMImplementationLS lsImpl = (DOMImplementationLS) el.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
      LSSerializer serializer = lsImpl.createLSSerializer();
      serializer.getDomConfig().setParameter("xml-declaration", false);
      return serializer.writeToString(el);
    }
    catch (Exception e) {
      LOGGER.error("Could not parse XML element!");
    }
    return null;
  }

  /**
   * Parses the movie/show XML, generated by the GetDetails method<br>
   *
   * @param xmlDetails
   * @param md
   * @param result
   * @throws Exception
   */
  protected abstract void processXmlContent(String xmlDetails, MediaMetadata md, MediaSearchResult result) throws Exception;
}
