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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tinymediamanager.scraper.MediaType;

public class KodiScraper {
  private Map<String, ScraperFunction> functions = new TreeMap<String, ScraperFunction>();

  String    id;
  String    version;
  String    name;
  String    summary;
  String    description;
  URL       logoUrl;
  MediaType type;
  String    thumb;
  String    language;
  String    provider;
  File      addonFolder;
  String    scraperXml;
  String    settingsPath;

  /**
   * instantiates a new scraper and parse info from addon.xml
   * 
   * @param scraperFolder
   */
  public KodiScraper(File scraperFolder) {
    try {
      File info = new File(scraperFolder, "addon.xml");
      Document doc = Jsoup.parse(info, "UTF-8", "");

      Elements addon = doc.getElementsByTag("addon");

      addonFolder = scraperFolder;
      id = addon.attr("id");
      name = addon.attr("name");
      version = addon.attr("version");
      provider = addon.attr("provider-name");

      for (Element el : doc.getElementsByAttribute("point")) {
        String point = el.attr("point");
        if (point.equals("xbmc.addon.metadata")) {
          Elements desc = el.getElementsByAttributeValue("lang", Locale.getDefault().getLanguage());
          if (desc.size() == 0) {
            // fallback EN
            desc = el.getElementsByAttributeValue("lang", "en");
          }
          for (Element d : desc) {
            if (d.nodeName().equals("summary")) {
              this.setThumb(d.text());
            }
            else {
              description = d.text();
            }
          }
        }
        else if (point.contains("metadata.scraper")) {
          this.scraperXml = el.attr("library");
          this.language = el.attr("language");

          // more here http://wiki.xbmc.org/index.php?title=addon.xml#.3Cextension.3E
          if (point.equals("xbmc.metadata.scraper.movies")) {
            type = MediaType.MOVIE;
          }
          else if (point.equals("xbmc.metadata.scraper.tvshows")) {
            type = MediaType.TV_SHOW;
          }
//          else if (point.equals("xbmc.metadata.scraper.albums")) {
//            type = MediaType.ALBUM;
//          }
//          else if (point.equals("xbmc.metadata.scraper.artists")) {
//            type = MediaType.ARTIST;
//          }
//          else if (point.equals("xbmc.metadata.scraper.musicvideos")) {
//            type = MediaType.MUSICVIDEO;
//          }
//          else if (point.equals("xbmc.metadata.scraper.library")) {
//            type = MediaType.LIBRARY;
//          }
        }
      }

      File settings = new File(scraperFolder, "resources/settings.xml");
      if (settings.exists()) {
        settingsPath = settings.getAbsolutePath();
      }

      File logo = new File(scraperFolder, "icon.png");
      if (logo.exists()) {
        logoUrl = logo.toURI().toURL();
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getSettingsPath() {
    return settingsPath;
  }

  public void setSettingsPath(String settingsPath) {
    this.settingsPath = settingsPath;
  }

  /**
   * no dupes!
   */
  public void addFunction(ScraperFunction func) {
    if (!functions.containsKey(func.getName())) {
      functions.put(func.getName(), func);
    }
  }

  public ScraperFunction getFunction(String name) {
    return functions.get(name);
  }

  public ScraperFunction[] getFunctions() {
    return functions.values().toArray(new ScraperFunction[functions.size()]);
  }

  public String getThumb() {
    return thumb;
  }

  public void setThumb(String thumb) {
    this.thumb = thumb;
  }

  public boolean containsFunction(String functionName) {
    return functions.containsKey(functionName);
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public File getFolder() {
    return addonFolder;
  }

  public void setFolder(File folder) {
    this.addonFolder = folder;
  }

  public String getScraperXml() {
    return scraperXml;
  }

  public void setScraperXml(String scraperXml) {
    this.scraperXml = scraperXml;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    KodiScraper other = (KodiScraper) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    }
    else if (!id.equals(other.id))
      return false;
    return true;
  }

}
