package org.tinymediamanager.scraper.xbmc;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XbmcScraperParser {
  private static final Logger LOGGER = Logger.getLogger(XbmcScraperParser.class);

  public XbmcScraper parseScraper(XbmcScraper scraper, List<File> common) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder parser = factory.newDocumentBuilder();
    Document xml = parser.parse(new File(scraper.getFolder(), scraper.getScraperXml()));

    // get the first element
    Element element = xml.getDocumentElement();
    // get all child nodes
    NodeList nodes = element.getChildNodes();
    // // print the text content of each child
    // for (int i = 0; i < nodes.getLength(); i++) {
    // System.out.println("<" + nodes.item(i).getNodeName() + "> " + nodes.item(i).getTextContent());
    // }

    Element docEl = xml.getDocumentElement();

    NodeList nl = docEl.getChildNodes();
    int len = nl.getLength();
    for (int i = 0; i < len; i++) {
      Node n = nl.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE) {
        Element el = (Element) n;
        ScraperFunction func = new ScraperFunction();
        func.setName(el.getNodeName());
        func.setClearBuffers(parseBoolean(el.getAttribute("clearbuffers"), true));
        func.setAppendBuffer(parseAppendBuffer(el.getAttribute("dest")));
        func.setDest(parseInt(el.getAttribute("dest")));
        scraper.addFunction(func);

        // functions contain regexp expressions, so let's get those.
        processRegexps(func, el);
      }

    }

    // get all common scraper functions
    readScraperFunctions(scraper, common);

    return scraper;
  }

  private boolean parseAppendBuffer(String attribute) {
    if (attribute == null)
      return false;
    if (attribute.trim().endsWith("+"))
      return true;
    return false;
  }

  private void processRegexps(RegExpContainer container, Element el) {
    NodeList regEls = el.getChildNodes();
    int regElsLen = regEls.getLength();
    for (int k = 0; k < regElsLen; k++) {
      Node nn = regEls.item(k);
      if ("RegExp".equals(nn.getNodeName())) {
        Element expEl = (Element) nn;
        RegExp regexp = new RegExp();
        regexp.setInput(expEl.getAttribute("input"));
        regexp.setOutput(expEl.getAttribute("output"));
        regexp.setAppendBuffer(parseAppendBuffer(expEl.getAttribute("dest")));
        regexp.setDest(parseInt(expEl.getAttribute("dest")));
        regexp.setConditional(expEl.getAttribute("conditional"));
        container.addRegExp(regexp);
        processRegexps(regexp, (Element) nn);
      }
      else if ("expression".equals(nn.getNodeName())) {
        Element expEl = (Element) nn;
        try {
          RegExp regexp = (RegExp) container; // cannot cast - exception see below
          Expression exp = new Expression();
          exp.setExpression(nn.getTextContent());
          exp.setNoClean(expEl.getAttribute("noclean"));
          exp.setRepeat(parseBoolean(expEl.getAttribute("repeat"), false));
          exp.setClear(parseBoolean(expEl.getAttribute("clear"), false));
          regexp.setExpression(exp);
        }
        catch (Exception e) {
          LOGGER.warn("unparseable expression! " + container);
          // happens here (kino.de) - the last empty expression.
          // maybe no RegExp around?
          //
          // <GetTrailer dest="5">
          // <RegExp input="$$1" output="&lt;details&gt;&lt;trailer urlencoded=&quot;yes&quot;&gt;\1&lt;/trailer&gt;&lt;/details&gt;" dest="5">
          // <expression noclean="1">&lt;url&gt;([^&lt;]*)&lt;/url&gt;</expression>
          // </RegExp>
          // <expression noclean="1"/> <------------------
          // </GetTrailer>
        }
      }
      else {
        // skip nodest that we don't know about
        // System.out.println("Skipping Node: " + nn);
      }
    }
  }

  private int parseInt(String attribute) {
    if (attribute == null || attribute.trim().length() == 0)
      return 0;
    if (attribute.endsWith("+")) {
      attribute = attribute.substring(0, attribute.length() - 1);
    }
    return Integer.parseInt(attribute);
  }

  private boolean parseBoolean(String attribute, boolean defaultNull) {
    if (attribute == null || attribute.trim().length() == 0)
      return defaultNull;
    if ("yes".equalsIgnoreCase(attribute))
      return true;
    if ("no".equalsIgnoreCase(attribute))
      return false;
    return Boolean.parseBoolean(attribute);
  }

  private void readScraperFunctions(XbmcScraper scraper, List<File> common) {
    for (File file : common) {
      // System.out.println("parsing common file: " + file);
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = factory.newDocumentBuilder();
        Document xml = parser.parse(file);
        Element docEl = xml.getDocumentElement();

        // only process xml files with scraperfunctions
        if (docEl.getNodeName() == "scraperfunctions") {
          NodeList nl = docEl.getChildNodes();

          // extract all scraperfunctions
          for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
              Element el = (Element) n;
              ScraperFunction func = new ScraperFunction();
              func.setName(el.getNodeName());
              func.setClearBuffers(parseBoolean(el.getAttribute("clearbuffers"), true));
              func.setAppendBuffer(parseAppendBuffer(el.getAttribute("dest")));
              func.setDest(parseInt(el.getAttribute("dest")));
              scraper.addFunction(func);

              // functions contain regexp expressions, so let's get those.
              processRegexps(func, el);
            }
          }
        }
      }
      catch (ParserConfigurationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      catch (SAXException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }
  }

}
