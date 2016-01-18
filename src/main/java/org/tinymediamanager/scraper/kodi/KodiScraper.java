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
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.MediaType;
import org.tinymediamanager.scraper.mediaprovider.IMediaProvider;

public class KodiScraper implements IMediaProvider {
  private static final Logger          LOGGER    = LoggerFactory.getLogger(KodiScraper.class);
  private Map<String, ScraperFunction> functions = new TreeMap<String, ScraperFunction>();

  MediaType                            type;
  String                               language;
  String                               provider;
  File                                 addonFolder;
  String                               scraperXml;
  String                               settingsPath;
  MediaProviderInfo                    providerInfo;

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

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
      String id = addon.attr("id");
      String name = addon.attr("name");
      String version = addon.attr("version");
      String summary = "";
      String description = "";
      provider = addon.attr("provider-name");

      // TODO: parse additional info and add to description
      // <disclaimer lang="en_gb"></disclaimer>
      // <language></language>
      // <platform>all</platform>
      // <license></license>
      // <forum></forum>
      // <website></website>
      // <email></email>
      // <source></source>

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
              summary = d.text();
            }
            else if (d.nodeName().equals("description")) {
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
          // else if (point.equals("xbmc.metadata.scraper.albums")) {
          // type = MediaType.ALBUM;
          // }
          // else if (point.equals("xbmc.metadata.scraper.artists")) {
          // type = MediaType.ARTIST;
          // }
          // else if (point.equals("xbmc.metadata.scraper.musicvideos")) {
          // type = MediaType.MUSICVIDEO;
          // }
          // else if (point.equals("xbmc.metadata.scraper.library")) {
          // type = MediaType.LIBRARY;
          // }
        }
      }
      providerInfo = new MediaProviderInfo(id, "Kodi: " + name, "<h3>" + summary + "</h3><br>" + description);
      providerInfo.setVersion(version); // deprecated solely for Kodi, so ok

      // parse settings
      File settingsFile = new File(scraperFolder, "resources/settings.xml");
      if (settingsFile.exists()) {
        settingsPath = settingsFile.getAbsolutePath();
        Document set = Jsoup.parse(settingsFile, "UTF-8", "");
        Elements settings = set.getElementsByTag("setting");
        for (Element el : settings) {
          String setid = el.attr("id");
          if (StringUtils.isEmpty(setid))
            continue;
          String type = el.attr("type");
          String defaultValue = el.attr("default");
          boolean encrypt = false;
          String option = el.attr("option");
          if (option.equalsIgnoreCase("true")) {
            encrypt = true;
          }
          String possibleValues[] = el.attr("values").split("\\|");

          switch (type) {
            case "bool":
              if (defaultValue.equalsIgnoreCase("true") || defaultValue.equalsIgnoreCase("false")) {
                this.providerInfo.getConfig().addBoolean(setid, Boolean.valueOf(defaultValue));
              }
              else {
                LOGGER.warn("This is not a boolean '" + setid + "=" + defaultValue + "' - ignoring");
              }
              break;
            case "select":
            case "labelenum":
              this.providerInfo.getConfig().addSelect(setid, possibleValues, defaultValue);
              break;
            case "enum":
              this.providerInfo.getConfig().addSelectIndex(setid, possibleValues, defaultValue);
              break;
            case "action":
            case "text":
              this.providerInfo.getConfig().addText(setid, defaultValue, encrypt);
              break;

            default:
              break;
          }
        }
        this.providerInfo.getConfig().load(); // load actual values
      } // end parse settings

      File logo = new File(scraperFolder, "icon.png");
      if (logo.exists()) {
        providerInfo.setProviderLogo(logo.toURI().toURL());
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
    result = prime * result + ((providerInfo.getId() == null) ? 0 : providerInfo.getId().hashCode());
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
    if (providerInfo.getId() == null) {
      if (other.providerInfo.getId() != null)
        return false;
    }
    else if (!providerInfo.getId().equals(other.providerInfo.getId()))
      return false;
    return true;
  }

}
