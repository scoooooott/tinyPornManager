package org.tinymediamanager.scraper.xbmc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.tinymediamanager.scraper.CastMember;
import org.tinymediamanager.scraper.IHasFindByIMDBID;
import org.tinymediamanager.scraper.IMediaMetadataProvider;
import org.tinymediamanager.scraper.MediaArt;
import org.tinymediamanager.scraper.MediaArtifactType;
import org.tinymediamanager.scraper.MediaGenres;
import org.tinymediamanager.scraper.MediaMetadata;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.MetadataKey;
import org.tinymediamanager.scraper.MetadataUtil;
import org.tinymediamanager.scraper.ProviderInfo;
import org.tinymediamanager.scraper.SearchQuery;
import org.tinymediamanager.scraper.util.DOMUtils;
import org.tinymediamanager.scraper.util.ParserUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XbmcMetadataProvider implements IMediaMetadataProvider, IHasFindByIMDBID {
  private static final Logger                 LOGGER                  = Logger.getLogger(XbmcMetadataProvider.class);
  private static final DocumentBuilderFactory factory                 = DocumentBuilderFactory.newInstance();

  private ProviderInfo                        info;
  private XbmcScraper                         scraper;
  private MediaType[]                         supportedSearchTypes    = null;

  private static final String                 IMDB_TITLE_URL          = "http://%s/title/%s/";
  private static final String                 IMDB_DOMAIN             = "www.imdb.com";
  private static final String                 IMDB_RUNNING_TIME_REGEX = "([0-9]+)(\\s+min)?";

  private static Pattern                      mpaaRatingParser        = Pattern.compile("Rated\\s+([^ ]+).*");

  public XbmcMetadataProvider(String providerXml) {
    XbmcScraperParser parser = new XbmcScraperParser();
    XbmcScraper scr;
    try {
      scr = parser.parseScraper(new File(providerXml));
    }
    catch (Exception e) {
      LOGGER.error("Failed to Load XBMC Scraper: " + providerXml);
      throw new RuntimeException("Failed to Load XBMC Scraper: " + providerXml, e);
    }

    init(scr);
  }

  public XbmcMetadataProvider(XbmcScraper scraper) {
    init(scraper);
  }

  private void init(XbmcScraper scraper) {
    this.scraper = scraper;

    ProviderInfo in = new ProviderInfo(scraper.getId(), scraper.getName(), scraper.getDescription(), scraper.getThumb());
    info = in;

    String content = scraper.getContent();
    if (!StringUtils.isEmpty(content)) {
      List<MediaType> types = new ArrayList<MediaType>();
      Pattern p = Pattern.compile("([^,]+)");
      Matcher m = p.matcher(content);
      while (m.find()) {
        String type = m.group(1).trim();
        LOGGER.debug("Provider:  " + scraper.getId() + "; content type: " + type);
        if ("movies".equalsIgnoreCase(type)) {
          LOGGER.debug("Using Movies for Provider:  " + scraper.getId() + "; content type: " + type);
          types.add(MediaType.MOVIE);
        }
        else if ("tvshows".equalsIgnoreCase(type)) {
          LOGGER.debug("Using TV for Provider:  " + scraper.getId() + "; content type: " + type);
          types.add(MediaType.TV);
        }
        else if ("music".equalsIgnoreCase(type)) {
          types.add(MediaType.MUSIC);
        }
        else {
          LOGGER.debug("Unknown XBMC Scraper type: " + type);
        }
      }

      supportedSearchTypes = types.toArray(new MediaType[types.size()]);
    }
    else {
      LOGGER.warn("No Content Type for provider: " + scraper.getId());
      supportedSearchTypes = new MediaType[0];
    }
  }

  public ProviderInfo getInfo() {
    return info;
  }

  public MediaMetadata getMetaData(MediaSearchResult result) throws Exception {
    LOGGER.debug("Xbmc: getMetadata(): " + result);

    MediaMetadata md = new MediaMetadata();
    updateMDValue(md, MetadataKey.METADATA_PROVIDER_ID, getInfo().getId());
    md.setString(MetadataKey.MEDIA_PROVIDER_DATA_ID, result.getId());

    if (result.getIMDBId() != null && result.getIMDBId().contains("tt")) {
      updateMDValue(md, MetadataKey.IMDB_ID, result.getId());
    }

    XbmcMovieProcessor processor = new XbmcMovieProcessor(scraper);
    String xmlDetails = processor.getDetails(new XbmcUrl(result.getUrl()), result.getId());

    // workaround: replace problematic sequences
    xmlDetails = xmlDetails.replace("&nbsp;", " ");

    if (result.getMediaType() == MediaType.TV) {
      md.set(MetadataKey.MEDIA_TYPE, MetadataUtil.TV_MEDIA_TYPE);
      processXmlContentForTV(xmlDetails, md, result);
    }
    else {
      processXmlContent(xmlDetails, md);
    }

    // try to parse an imdb id from the url
    if (!StringUtils.isEmpty(result.getUrl()) && StringUtils.isEmpty(md.getIMDBID())) {
      md.setIMDBID(parseIMDBID(result.getUrl()));
    }

    return md;
  }

  public List<MediaSearchResult> search(SearchQuery query) throws Exception {
    List<MediaSearchResult> l = new ArrayList<MediaSearchResult>();
    String arg = query.get(SearchQuery.Field.QUERY);

    // xbmc wants title and year separated, so let's do that
    String args[] = ParserUtils.parseTitle(arg);
    String title = args[0];
    String year = query.get(SearchQuery.Field.YEAR);

    XbmcMovieProcessor processor = new XbmcMovieProcessor(scraper);
    XbmcUrl url = processor.getSearchUrl(title, year);
    String xmlString = processor.getSearchResults(url);

    LOGGER.debug("========= BEGIN XBMC Scraper Search Xml Results: Url: " + url);
    LOGGER.debug(xmlString);
    LOGGER.debug("========= End XBMC Scraper Search Xml Results: Url: " + url);

    Document xml = parseXmlString(xmlString);

    NodeList nl = xml.getElementsByTagName("entity");
    for (int i = 0; i < nl.getLength(); i++) {
      try {
        Element el = (Element) nl.item(i);
        NodeList titleList = el.getElementsByTagName("title");
        String t = titleList.item(0).getTextContent();
        NodeList yearList = el.getElementsByTagName("year");
        String y = yearList.item(0).getTextContent();
        NodeList urlList = el.getElementsByTagName("url");
        XbmcUrl u = new XbmcUrl((Element) urlList.item(0));

        MediaSearchResult sr = new MediaSearchResult();
        String id = DOMUtils.getElementValue(el, "id");
        sr.setId(id);
        sr.setUrl(u.toExternalForm());
        sr.setProviderId(getInfo().getId());
        sr.getExtra().put("mediatype", query.getMediaType().name());

        // populate extra args
        MetadataUtil.copySearchQueryToSearchResult(query, sr);

        if (u.toExternalForm().indexOf("imdb") != -1) {
          sr.addExtraArg("xbmcprovider", "imdb");
          sr.addExtraArg("imdbid", id);
          sr.setIMDBId(id);
        }
        else if (u.toExternalForm().indexOf("thetvdb.com") != -1) {
          sr.addExtraArg("xbmcprovider", "tvdb");
          sr.addExtraArg("tvdbid", id);
        }

        // String v[] = ParserUtils.parseTitle(t);
        // sr.setTitle(v[0]);
        // sr.setYear(v[1]);
        sr.setTitle(t);
        sr.setYear(y);
        sr.setScore(MetadataUtil.calculateScore(arg, t));
        l.add(sr);
      }
      catch (Exception e) {
        LOGGER.error("Error process an xml node!  Ignoring it from the search results.");
      }
    }

    return l;
  }

  public MediaMetadata getMetadataForIMDBId(String imdbid) {
    if (getInfo().getId().contains("imdb")) {
      MediaSearchResult sr = new MediaSearchResult();
      sr.setIMDBId(imdbid);
      sr.setId(imdbid);
      sr.setUrl(createDetailUrl(imdbid));
      try {
        return getMetaData(sr);
      }
      catch (Exception e) {
        LOGGER.warn("Failed to search by IMDB URL: " + sr.getUrl(), e);
      }
    }
    return null;
  }

  private void processXmlContent(String xmlDetails, MediaMetadata md) throws Exception {
    if (xmlDetails == null || StringUtils.isEmpty(xmlDetails)) {
      LOGGER.warn("Cannot process empty Xml Contents.");
      return;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("******* BEGIN XML ***********");
      LOGGER.debug(xmlDetails);
      LOGGER.debug("******* END XML ***********");
    }

    Document xml = parseXmlString(xmlDetails);
    addMetadata(md, xml.getDocumentElement());
  }

  private void processXmlContentForTV(String xmlDetails, MediaMetadata md, MediaSearchResult result) throws Exception {
    LOGGER.debug("*** PROCESSING TV ***");
    if (xmlDetails == null || StringUtils.isEmpty(xmlDetails)) {
      LOGGER.warn("Cannot process empty Xml Contents.");
      return;
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("******* BEGIN XML ***********");
      LOGGER.debug(xmlDetails);
      LOGGER.debug("******* END XML ***********");
    }

    Document xml = parseXmlString(xmlDetails);

    addMetadata(md, xml.getDocumentElement());

    LOGGER.debug("Fetching Episode Guide url");

    // now check for episode and guide url
    String episodeUrl = DOMUtils.getElementValue(xml.getDocumentElement(), "episodeguide");
    if (StringUtils.isEmpty(episodeUrl)) {
      LOGGER.error("No Episode Data!");
    }
    else {
      if (!StringUtils.isEmpty(result.getExtra().get(SearchQuery.Field.SEASON.name()))) {
        int findEpisode = NumberUtils.toInt(result.getExtra().get(SearchQuery.Field.EPISODE.name()));
        int findSeason = NumberUtils.toInt(result.getExtra().get(SearchQuery.Field.SEASON.name()));
        int findDisc = NumberUtils.toInt(result.getExtra().get(SearchQuery.Field.DISC.name()));

        XbmcUrl url = new XbmcUrl(episodeUrl);
        // Call get Episode List
        XbmcMovieProcessor processor = new XbmcMovieProcessor(scraper);

        if (findEpisode > 0) {
          String epListXml = processor.getEpisodeList(url);

          LOGGER.debug("******** BEGIN EPISODE LIST XML ***********");
          LOGGER.debug(epListXml);
          LOGGER.debug("******** END EPISODE LIST XML ***********");

          Document epListDoc = parseXmlString(epListXml);

          NodeList nl = epListDoc.getElementsByTagName("episode");
          int s = nl.getLength();
          int season, ep;
          String id = null;
          String epUrl = null;
          for (int i = 0; i < s; i++) {
            Element el = (Element) nl.item(i);
            season = DOMUtils.getElementIntValue(el, "season");
            ep = DOMUtils.getElementIntValue(el, "epnum");
            if (season == findSeason && ep == findEpisode) {
              id = DOMUtils.getElementValue(el, "id");
              epUrl = DOMUtils.getElementValue(el, "url");
              break;
            }
          }

          if (id == null) {
            throw new Exception("Could Not Find Seaons and Episode for: " + findSeason + "x" + findEpisode);
          }

          LOGGER.debug("We have an episdoe id for season and episode... fetching details...");

          processor = new XbmcMovieProcessor(scraper);
          xmlDetails = processor.getEpisodeDetails(new XbmcUrl(epUrl), id);

          LOGGER.debug("******** BEGIN EPISODE DETAILS XML ***********");
          LOGGER.debug(xmlDetails);
          LOGGER.debug("******** END EPISODE DETAILS XML ***********");

          // update again, using the episode specific data
          xml = parseXmlString(xmlDetails);
          Element el = xml.getDocumentElement();
          addMetadata(md, el);

          // add/update tv specific stuff
          String plot = DOMUtils.getElementValue(el, "plot");
          if (!StringUtils.isEmpty(plot)) {
            md.setDescription(plot);
          }

          md.set(MetadataKey.EPISODE, String.valueOf(findEpisode));
          md.set(MetadataKey.RELEASE_DATE, DOMUtils.getElementValue(el, "aired"));
          md.set(MetadataKey.EPISODE_TITLE, DOMUtils.getElementValue(el, "title"));
        }
        else if (findDisc > 0) {
          md.set(MetadataKey.DVD_DISC, String.format("%1$02d", findDisc));
        }

        if (findSeason > 0) {
          md.set(MetadataKey.SEASON, String.valueOf(findSeason));
        }
      }
    }

  }

  private void addMetadata(MediaMetadata md, Element details) {
    LOGGER.debug("Processing <details> node....");

    NodeList subDetails = details.getElementsByTagName("details");

    NodeList nl = details.getElementsByTagName("fanart");
    for (int i = 0; i < nl.getLength(); i++) {
      Element fanart = (Element) nl.item(i);
      String url = fanart.getAttribute("url");
      NodeList thumbs = fanart.getElementsByTagName("thumb");
      if (thumbs != null && thumbs.getLength() > 0) {
        processMediaArt(md, MediaArtifactType.BACKGROUND, "Backgrounds", thumbs, url);
      }
      else {
        if (!StringUtils.isEmpty(url)) {
          processMediaArt(md, MediaArtifactType.BACKGROUND, "Background", url);
        }
      }
    }

    nl = details.getElementsByTagName("thumb");
    for (int i = 0; i < nl.getLength(); i++) {
      Element poster = (Element) nl.item(i);
      if (poster.getParentNode().getNodeName() == "details")
        processMediaArt(md, MediaArtifactType.POSTER, "Poster", poster, null);
    }

    nl = details.getElementsByTagName("actor");
    for (int i = 0; i < nl.getLength(); i++) {
      Element actor = (Element) nl.item(i);
      CastMember cm = new CastMember();
      cm.setType(CastMember.ACTOR);
      cm.setName(DOMUtils.getElementValue(actor, "name"));
      cm.setPart(DOMUtils.getElementValue(actor, "role"));
      md.addCastMember(cm);
    }

    nl = details.getElementsByTagName("director");
    for (int i = 0; i < nl.getLength(); i++) {
      Element el = (Element) nl.item(i);
      CastMember cm = new CastMember();
      cm.setType(CastMember.DIRECTOR);
      cm.setName(StringUtils.trim(el.getTextContent()));
      LOGGER.debug("Adding Director: " + cm.getName());
      cm.setPart("Director");
      md.addCastMember(cm);
    }

    nl = details.getElementsByTagName("credits");
    for (int i = 0; i < nl.getLength(); i++) {
      Element el = (Element) nl.item(i);
      CastMember cm = new CastMember();
      cm.setType(CastMember.WRITER);
      cm.setName(StringUtils.trim(el.getTextContent()));
      cm.setPart("Writer");
      md.addCastMember(cm);
    }

    nl = details.getElementsByTagName("genre");
    for (int i = 0; i < nl.getLength(); i++) {
      Element el = (Element) nl.item(i);
      MediaGenres genre = MediaGenres.getGenre(StringUtils.trim(el.getTextContent()));
      if (genre != null) {
        md.addGenre(genre);
      }
    }

    // first search plot in any <details> (from a scraperfunction), then in base
    // <details>
    String plot = null;
    for (int i = 0; i < subDetails.getLength(); i++) {
      Element subDetail = (Element) subDetails.item(i);
      nl = subDetail.getElementsByTagName("plot");
      for (int j = 0; j < nl.getLength();) {
        Element el = (Element) nl.item(j);
        plot = el.getTextContent();
        break;
      }
      if (plot != null)
        break;
    }
    if (plot != null) {
      md.setPlot(plot);
    }
    else {
      updateMDValue(md, MetadataKey.PLOT, DOMUtils.getElementValue(details, "plot"));
    }

    updateMDValue(md, MetadataKey.COMPANY, DOMUtils.getElementValue(details, "studio"));
    updateMDValue(md, MetadataKey.DESCRIPTION, DOMUtils.getMaxElementValue(details, "plot"));
    updateMDValue(md, MetadataKey.MPAA_RATING, parseMPAARating(DOMUtils.getElementValue(details, "mpaa")));
    updateMDValue(md, MetadataKey.MPAA_RATING_DESCRIPTION, DOMUtils.getElementValue(details, "mpaa"));

    updateMDValue(md, MetadataKey.RELEASE_DATE, DOMUtils.getElementValue(details, "year"));
    updateMDValue(md, MetadataKey.RUNNING_TIME, MetadataUtil.parseRunningTime(DOMUtils.getElementValue(details, "runtime"), IMDB_RUNNING_TIME_REGEX));
    updateMDValue(md, MetadataKey.MEDIA_TITLE, DOMUtils.getElementValue(details, "title"));
    updateMDValue(md, MetadataKey.USER_RATING, DOMUtils.getElementValue(details, "rating"));
    updateMDValue(md, MetadataKey.YEAR, DOMUtils.getElementValue(details, "year"));
    updateMDValue(md, MetadataKey.OUTLINE, DOMUtils.getElementValue(details, "outline"));
  }

  private void processMediaArt(MediaMetadata md, MediaArtifactType type, String label, NodeList els, String baseUrl) {
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

  private void processMediaArt(MediaMetadata md, MediaArtifactType type, String label, Element e, String baseUrl) {

    String image = e.getTextContent();
    if (image != null)
      image = image.trim();
    if (baseUrl != null) {
      baseUrl = baseUrl.trim();
      image = baseUrl + image;
    }
    processMediaArt(md, type, label, image);
  }

  private void processMediaArt(MediaMetadata md, MediaArtifactType type, String label, String image) {
    MediaArt ma = new MediaArt();
    ma.setDownloadUrl(image);
    ma.setLabel(label);
    ma.setProviderId(getInfo().getId());
    ma.setType(type);
    md.addMediaArt(ma);
  }

  /**
   * only update if the existing value is null or empty
   * 
   * @param md
   * @param key
   * @param value
   */
  private void updateMDValue(MediaMetadata md, MetadataKey key, String value) {
    if (md.get(key) == null && !StringUtils.isEmpty(value)) {
      md.set(key, value);
    }
  }

  /**
   * added because some xml strings are not parsable using utf-8
   * 
   * @param xml
   * @return
   * @throws Exception
   */
  private Document parseXmlString(String xml) throws Exception {
    DocumentBuilder parser = factory.newDocumentBuilder();
    Document doc = null;
    for (String charset : new String[] { "UTF-8", "ISO-8859-1", "US-ASCII" }) {
      try {
        doc = parser.parse(new ByteArrayInputStream(xml.getBytes(charset)));
        break;
      }
      catch (Throwable t) {
        LOGGER.error("Failed to parse xml using charset: " + charset, t);
      }
    }

    if (doc == null) {
      LOGGER.error("Unabled to parse xml string");
      LOGGER.error(xml);
      throw new Exception("Unable to parse xml!");
    }

    return doc;
  }

  public MediaType[] getSupportedSearchTypes() {
    return supportedSearchTypes;
  }

  /**
   * Given a string like, "Rated PG-13 for..." it tries to return PG-13, or the
   * entire string, if cannot find it.
   * 
   * @param imdbString
   * @return
   */
  public static String parseMPAARating(String imdbString) {
    if (imdbString != null) {
      Matcher m = mpaaRatingParser.matcher(imdbString);
      if (m.find()) {
        return m.group(1);
      }
      else {
        return imdbString;
      }
    }
    return null;
  }

  public static String parseIMDBID(String url) {
    if (url == null)
      return null;
    Pattern p = Pattern.compile("/(tt[0-9]+)/");
    Matcher m = p.matcher(url);
    if (m.find()) {
      return m.group(1);
    }
    return null;
  }

  public static String createDetailUrl(String imdbid) {
    return String.format(IMDB_TITLE_URL, IMDB_DOMAIN, imdbid);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.tinymediamanager.scraper.IHasFindByIMDBID#searchByImdbId(java.lang.
   * String)
   */
  @Override
  public MediaSearchResult searchByImdbId(String imdbid) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
