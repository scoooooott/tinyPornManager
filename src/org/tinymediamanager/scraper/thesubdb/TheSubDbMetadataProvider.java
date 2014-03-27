/*
 * Copyright 2012 - 2014 Manuel Laggner
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
package org.tinymediamanager.scraper.thesubdb;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.Globals;
import org.tinymediamanager.ReleaseInfo;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaSearchResult;
import org.tinymediamanager.scraper.util.SubtitleUtils;
import org.tinymediamanager.scraper.util.Url;

/**
 * The Class TheTvDbMetadataProvider.
 * 
 * @author Manuel Laggner
 */
public class TheSubDbMetadataProvider {
  private static final Logger      LOGGER        = LoggerFactory.getLogger(TheSubDbMetadataProvider.class);
  private static final String      USER_AGENT    = "SubDB/1.0 (tinyMediaManager/" + ReleaseInfo.getVersion() + "; http://www.tinymediamanager.org)";
  // private String API_URL = "http://api.thesubdb.com/";
  private String                   API_URL       = "http://sandbox.thesubdb.com/";
  private static MediaProviderInfo providerInfo  = new MediaProviderInfo("subdb", "thsubdb.com",
                                                     "Scraper for thesubdb.com which is able to scrape subtitles");
  private static final String      PAGE_ENCODING = "ISO-8859-1";

  public TheSubDbMetadataProvider() {
  }

  // @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  /**
   * searches for SubDB subtitle
   * 
   * @param mf
   *          the MediaFile
   * @return
   * @throws Exception
   */
  public List<MediaSearchResult> search(MediaFile mf) throws Exception {
    return search(mf.getFile());
  }

  /**
   * searches for SubDB subtitle
   * 
   * @param f
   *          the File
   * @return
   * @throws Exception
   */
  public List<MediaSearchResult> search(File f) throws Exception {
    LOGGER.debug("searching subtitle for " + f);
    List<MediaSearchResult> results = new ArrayList<MediaSearchResult>();

    String hash = SubtitleUtils.computeSubDBHash(f);
    // return an empty search result on hashing error
    if (hash.isEmpty()) {
      return results;
    }

    String desiredLanguage = Globals.settings.getLanguage();

    // search via the api
    Url url = new Url(API_URL + "?action=search&hash=" + hash);
    // Url url = new Url(API_URL + "?action=search&versions&hash=" + hash);
    url.setUserAgent(USER_AGENT);
    InputStream in = url.getInputStream();
    Document doc = Jsoup.parse(in, PAGE_ENCODING, "");
    in.close();

    String result = doc.body().text();
    if (!result.isEmpty()) {
      System.out.println(result);

      String[] lang = result.split(",");
      for (String l : lang) {
        Locale loc = Utils.getLocaleFromLanguage(l);
        MediaSearchResult sr = new MediaSearchResult(providerInfo.getId());
        sr.setId(hash);
        sr.setTitle(loc.getDisplayLanguage());
      }

      if (!result.contains(desiredLanguage)) {
        LOGGER.info("No subtitle for language " + Utils.getLocaleFromLanguage(desiredLanguage).getDisplayLanguage() + " found :(");
      }
    }
    return results;
  }

  /**
   * download subtitle
   * 
   * @param hash
   *          the SubDB file hash
   * @param language
   *          2char language string
   */
  public void download(String hash, String language) {

    Url url = new Url(API_URL + "?action=download&hash=" + hash + "&language=" + language);
    url.setUserAgent(USER_AGENT);

  }

  /**
   * returns all supported languages from SubDB
   * 
   * @return string like "en,es,fr,it,nl,pl,pt,ro,sv,tr"
   * @throws Exception
   */
  public String getLanguages() throws Exception {
    LOGGER.debug("getting subtitle languages");

    // search via the api
    Url url = new Url(API_URL + "?action=languages");
    url.setUserAgent(USER_AGENT);
    InputStream in = url.getInputStream();
    Document doc = Jsoup.parse(in, PAGE_ENCODING, "");
    in.close();

    String result = doc.body().text();
    if (!result.isEmpty()) {
      System.out.println(result);
      String[] lang = result.split(",");
      for (String l : lang) {
        Locale loc = Utils.getLocaleFromLanguage(l);
        System.out.println(loc.getDisplayLanguage());
      }
    }

    return result;
  }
}
