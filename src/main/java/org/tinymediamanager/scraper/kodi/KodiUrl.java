/*
 * Copyright 2012 - 2015 Manuel Laggner
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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.util.CachedUrl;
import org.tinymediamanager.scraper.util.Url;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This represents the "smart" url of the Kodi metadata.
 * 
 * @author Manuel Laggner, Myron Boyle
 * 
 */
class KodiUrl {
  private static final Logger LOGGER = LoggerFactory.getLogger(KodiUrl.class);

  private String      urlString;
  private Url         url;
  private String      functionName;
  private KodiScraper scraper;

  public KodiUrl(Element url) {
    updateFromElement(url);
  }

  public KodiUrl(String url) {
    if (StringUtils.isBlank(url)) {
      return;
    }

    if (url.trim().contains("<url")) {
      // parse <url>url</url> syntax
      try {
        url = StringEscapeUtils.unescapeHtml4(url);
        url = StringEscapeUtils.unescapeXml(url);
        url = StringEscapeUtils.unescapeXml(url);
        url = StringEscapeUtils.unescapeXml(url);
        url = url.replaceAll("\\&", "\\&amp;");

        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = f.newDocumentBuilder();
        Document d = parser.parse(new ByteArrayInputStream(url.getBytes()));
        Element e = (Element) d.getElementsByTagName("url").item(0);
        updateFromElement(e);
      }
      catch (Exception e) {
        throw new RuntimeException("Invalid xml url: " + url, e);
      }
    }
    else {
      this.urlString = url;
    }
  }

  public KodiUrl(String url, KodiScraper kodiScraper) {
    this(url);
    this.scraper = kodiScraper;
  }

  public KodiUrl(Element url, KodiScraper kodiScraper) {
    this(url);
    this.scraper = kodiScraper;
  }

  private void updateFromElement(Element e) {
    urlString = e.getTextContent();
    if (urlString != null)
      urlString = urlString.trim();
    LOGGER.debug("KodiUrl using Url from Xml: " + urlString);
    functionName = e.getAttribute("function");
    // TODO: pull in post, spoof, etc.
  }

  private Url getUrl() throws Exception {
    if (url == null) {
      url = new CachedUrl(urlString);
      // TODO: Add in the referer, etc
    }

    return url;
  }

  public InputStream getInputStream() throws Exception {
    // check if this is a function lookup, and if so, then let's process this as such.
    if (scraper != null && !StringUtils.isEmpty(getFunctionName())) {
      KodiScraperProcessor processor = new KodiScraperProcessor(scraper);
      LOGGER.debug("Processing Url Function: " + getFunctionName() + " with Url: " + urlString);
      // we have a function to process
      // TODO: Set spoof and post attributes...
      KodiUrl xurl = new KodiUrl(urlString);
      String results = processor.executeFunction(getFunctionName(), new String[] { "", xurl.getTextContent() });
      if (results == null)
        results = "";
      return new ByteArrayInputStream(results.getBytes());
    }
    else {
      // TODO: Check for posting, caching, etc
      Url u = getUrl();

      if (urlString.contains(".zip")) {
        LOGGER.debug("Converting ZipFile to Text content for url: " + urlString);
        // turn the zip contents into a text file
        ZipInputStream zis = new ZipInputStream(u.getInputStream());
        ZipEntry ze = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int MAX_LEN = 2048;
        byte buf[] = new byte[MAX_LEN];
        while ((ze = zis.getNextEntry()) != null) {
          LOGGER.debug("Adding Zip Entry: " + ze.getName() + " to Text content");
          int len = 0;
          while ((len = zis.read(buf)) > 0) {
            baos.write(buf, 0, len);
          }
        }
        baos.flush();
        zis.close();
        LOGGER.debug("Returing Text Context as inputstream...");
        return new ByteArrayInputStream(baos.toByteArray());
      }
      else {
        return u.getInputStream();
      }
    }
  }

  public String toExternalForm() {
    return urlString;
  }

  public String getFunctionName() {
    return functionName;
  }

  public String getTextContent() throws Exception {
    return IOUtils.toString(getInputStream());
  }

  @Override
  public String toString() {
    return "KodiUrl[" + url + "]";
  }
}
