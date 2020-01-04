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
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class parses the Kodi scraper and extracts the functions
 *
 * @author Manuel Laggner, Myron Boyle
 */
class KodiScraperParser {
  private static final Logger LOGGER = LoggerFactory.getLogger(KodiScraperParser.class);

  public KodiScraper parseScraper(KodiScraper scraper, List<File> common) throws Exception {
    LOGGER.debug("Preparing Kodi scraper: " + scraper.getProviderInfo().getName());
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder parser = factory.newDocumentBuilder();

    File scraperFile = new File(scraper.getFolder(), scraper.getScraperXml());
    String xmlFile = FileUtils.readFileToString(scraperFile, "UTF-8");
    xmlFile = KodiUtil.fixXmlHeader(xmlFile);
    xmlFile = KodiUtil.fixXmlAttributes(xmlFile);

    Document xml;
    try {
      InputStream stream = new ByteArrayInputStream(xmlFile.getBytes(StandardCharsets.UTF_8));
      xml = parser.parse(stream);
    }
    catch (SAXException e) {
      LOGGER.warn("Error parsing " + scraperFile + " - trying fallback");
      // eg FilmAffinity.com scraper
      // replace all known entities with their unicode notation
      // this fixes the "entity 'Iacute' was referenced, but not declared" parsing problems, since we do not have to add doctype entity declarations
      // might replace too much; so this is only a fallback
      for (String[] ent : EntityArrays.ISO8859_1_UNESCAPE()) {
        xmlFile = xmlFile.replace(ent[0], ent[1]);
      }
      InputStream stream = new ByteArrayInputStream(xmlFile.getBytes(StandardCharsets.UTF_8));
      xml = parser.parse(stream);
    }

    Element docEl = xml.getDocumentElement();
    NodeList nl = docEl.getChildNodes();

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
          // <RegExp input="$$1" output="&lt;details&gt;&lt;trailer
          // urlencoded=&quot;yes&quot;&gt;\1&lt;/trailer&gt;&lt;/details&gt;" dest="5">
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

  private void readScraperFunctions(KodiScraper scraper, List<File> common) {
    // ALL common files
    for (File file : common) {
      // System.out.println("parsing common file: " + file);

      // just filter only the needed ones!
      for (String imp : scraper.imports) {
        if (file.getPath().contains(imp)) {
          try {
            LOGGER.debug("parsing imports from " + file);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = factory.newDocumentBuilder();
            Document xml = parser.parse(file);
            Element docEl = xml.getDocumentElement();

            // only process xml files with scraperfunctions
            if ("scraperfunctions".equals(docEl.getNodeName())) {
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
          catch (Exception e) {
            LOGGER.error("problem parsing scraper function", e);
          }
        }
      }
    }
  }
}
