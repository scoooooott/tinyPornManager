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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.scraper.MediaProviderInfo;
import org.tinymediamanager.scraper.entities.MediaType;
import org.tinymediamanager.scraper.interfaces.IMediaProvider;

public class KodiScraper implements IMediaProvider {
  private static final Logger          LOGGER    = LoggerFactory.getLogger(KodiScraper.class);
  private Map<String, ScraperFunction> functions = new TreeMap<>();

  MediaType                            type;
  String                               language;
  String                               provider;
  File                                 addonFolder;
  String                               scraperXml;
  MediaProviderInfo                    providerInfo;
  List<String>                         imports   = new ArrayList<>();

  @Override
  public MediaProviderInfo getProviderInfo() {
    return providerInfo;
  }

  @Override
  public String getId() {
    return providerInfo.getId();
  }

  /**
   * instantiates a new scraper and parse info from addon.xml
   *
   * @param scraperFolder
   */
  public KodiScraper(File scraperFolder) {
    try {

      // =====================================================
      // parse addon.xml
      // =====================================================
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

      // parse addons for correct import
      for (Element el : doc.getElementsByTag("import")) {
        String imp = el.attr("addon");
        if (!imp.isEmpty() && imp.startsWith("metadata.common")) {
          LOGGER.debug("--> found common import: {}", imp);
          imports.add(imp);
        }
      }

      // parse extensions
      for (Element el : doc.getElementsByAttribute("point")) {
        String point = el.attr("point");
        if (point.equals("xbmc.addon.metadata")) {
          Elements desc = el.getElementsByAttributeValue("lang", Locale.getDefault().getLanguage());
          if (desc.isEmpty()) {
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
      if (this.scraperXml.toLowerCase(Locale.ROOT).endsWith(".py")) {
        LOGGER.info("Found a python scraper {}, but we can only load XML based ones - sorry.", scraperFolder);
        providerInfo = new MediaProviderInfo("", name, ""); // set blank ID, so the outer loop ignores
        return;
      }

      providerInfo = new MediaProviderInfo(id, "Kodi: " + name, "<h3>" + summary + "</h3><br>" + description);
      providerInfo.setVersion(version); // deprecated solely for Kodi, so ok

      // =====================================================
      // parse language files, if found
      // =====================================================
      HashMap<String, String> labelmap = new HashMap<>();

      String lang = Locale.getDefault().getDisplayLanguage(Locale.ENGLISH);
      File langFolder = new File(scraperFolder, "resources/language/" + lang);
      if (!langFolder.exists()) {
        lang = (Locale.getDefault().getCountry() + "_" + Locale.getDefault().getLanguage()).toLowerCase(Locale.ROOT);
        langFolder = new File(scraperFolder, "resources/language/resource.language." + lang);
      }

      // put default names
      labelmap.putAll(getLocalizationFromFile(new File(scraperFolder, "resources/language/English")));
      labelmap.putAll(getLocalizationFromFile(new File(scraperFolder, "resources/language/English (GB)")));
      labelmap.putAll(getLocalizationFromFile(new File(scraperFolder, "resources/language/English (US)")));
      labelmap.putAll(getLocalizationFromFile(new File(scraperFolder, "resources/language/resource.language.en_gb")));
      labelmap.putAll(getLocalizationFromFile(new File(scraperFolder, "resources/language/resource.language.en_us")));
      // overwrite with localized ones
      labelmap.putAll(getLocalizationFromFile(langFolder));

      // =====================================================
      // parse default settings and build TMM config
      // =====================================================
      File settingsFile = new File(scraperFolder, "resources/settings.xml");
      if (settingsFile.exists()) {
        Document set = Jsoup.parse(settingsFile, "UTF-8", "");
        Elements settings = set.getElementsByTag("setting");
        for (Element el : settings) {
          String setid = el.attr("id");
          if (StringUtils.isEmpty(setid))
            continue;
          String type = el.attr("type");
          String defaultValue = el.attr("default");
          String possibleValues[] = el.attr("values").split("\\|");
          if (possibleValues.length == 1 && possibleValues[0].isEmpty()) {
            possibleValues = el.attr("lvalues").split("\\|"); // parse label values
          }
          // if it is a labelcode, replace with value
          for (int index = 0; index < possibleValues.length; index++) {
            String code = possibleValues[index];
            if (code.startsWith("3") && code.length() == 5) {
              String labelName = labelmap.get(code);
              if (labelName != null) {
                labelName = labelName.replaceAll("\\[COLOR=(.*?)\\]", "").replaceAll("\\[/COLOR\\]", "");
                possibleValues[index] = labelName;
              }
            }
          }

          String label = el.attr("label");
          String labelName = labelmap.get(label);
          if (labelName != null) {
            // labelName = "<html>" + labelName.replaceAll("\\[COLOR=(.*?)\\]", "<font color=$1>").replaceAll("\\[/COLOR\\]", "</font>") + "</html>";
            // nah, no html, look weird
            labelName = labelName.replaceAll("\\[COLOR=(.*?)\\]", "").replaceAll("\\[/COLOR\\]", "");
          }

          // visible
          boolean visible = true;
          String vis = el.attr("visible");
          if (vis.equalsIgnoreCase("false")) {
            visible = false;
          }

          // hidden|urlencoded
          boolean encrypt = false;
          String option = el.attr("option");
          if (option.toLowerCase(Locale.ROOT).contains("hidden")) {
            encrypt = true;
          }

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
            // case "action":
            case "text":
              this.providerInfo.getConfig().addText(setid, defaultValue, encrypt);
              break;

            default:
              continue; // not a know type, restart
          }

          if (!visible) {
            this.providerInfo.getConfig().getConfigObject(setid).setVisible(visible);
          }
          if (labelName != null) {
            this.providerInfo.getConfig().getConfigObject(setid).setKeyDescription(labelName);
          }
        }
      } // end parse settings

      // =====================================================
      // parse Kodi saved setting values and update TMM config
      // =====================================================
      File savedSettings = new File(KodiUtil.detectKodiUserFolder(), "userdata/addon_data/" + providerInfo.getId() + "/settings.xml");
      if (savedSettings.exists()) {
        Document set = Jsoup.parse(savedSettings, "UTF-8", "");
        Elements settings = set.getElementsByTag("setting");
        for (Element el : settings) {
          String setid = el.attr("id");
          String value = el.attr("value");
          if (providerInfo.getConfig().getConfigKeyValuePairs().keySet().contains(setid)) {
            providerInfo.getConfig().setValue(setid, value);
          }
        }
      }

      // =====================================================
      // load TMM config values
      // =====================================================
      this.providerInfo.getConfig().load();

      File logo = new File(scraperFolder, "icon.png");
      if (logo.exists()) {
        providerInfo.setProviderLogo(logo.toURI().toURL());
      }
      else { // new http://kodi.wiki/view/Add-on_structure#Kodi_v17_Krypton_and_up
        logo = new File(scraperFolder, "resources/icon.png");
        if (logo.exists()) {
          providerInfo.setProviderLogo(logo.toURI().toURL());
        }
      }
    }
    catch (IOException e) {
      LOGGER.error("Unable to generate Kodi scraper for folder {}", scraperFolder, e);
    }
  }

  private HashMap<String, String> getLocalizationFromFile(File langFolder) throws IOException {
    HashMap<String, String> labelmap = new HashMap<>();
    if (langFolder != null) {
      File langFile = new File(langFolder, "strings.xml");
      if (langFile.exists()) {
        // parse XML
        Document set = Jsoup.parse(langFile, "UTF-8", "");
        Elements strings = set.getElementsByTag("string");
        for (Element el : strings) {
          labelmap.put(el.id(), el.text());
        }
      }
      else {
        langFile = new File(langFolder, "strings.po");
        if (langFile.exists()) {
          // parse PO
          String labels = Utils.readFileToString(langFile.toPath());
          Pattern p = Pattern.compile("msgctxt \"#(.*?)\"(?:\\r\\n|\\n|\\r)msgid \"(.*?)\"(?:\\r\\n|\\n|\\r)msgstr \"(.*?)\"");
          Matcher m = p.matcher(labels);
          while (m.find()) {
            // msgctxt "#30030"
            // msgid "Certification prefix"
            // msgstr ""
            labelmap.put(m.group(1), m.group(3).isEmpty() ? m.group(2) : m.group(3));
          }
        }
      }
    }
    return labelmap;
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
