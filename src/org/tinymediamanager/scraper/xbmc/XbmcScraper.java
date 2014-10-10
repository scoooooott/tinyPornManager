package org.tinymediamanager.scraper.xbmc;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tinymediamanager.scraper.MediaType;

public class XbmcScraper {
  private Map<String, ScraperFunction> functions = new TreeMap<String, ScraperFunction>();
  private String                       id;
  private String                       name;
  private String                       thumb;
  private String                       scraperXml;
  private String                       language;
  private String                       version;
  private String                       provider;
  private String                       summary;
  private String                       description;
  private String                       settingsPath;
  private MediaType                    type;

  /**
   * instantiates a new scraper and parse info from addon.xml
   * 
   * @param scraperFolder
   */
  public XbmcScraper(File scraperFolder) {
    try {
      File info = new File(scraperFolder, "addon.xml");
      Document doc = Jsoup.parse(info, "UTF-8", "");

      Elements addon = doc.getElementsByTag("addon");

      this.id = addon.attr("id");
      this.name = addon.attr("name");
      this.version = addon.attr("version");
      this.provider = addon.attr("provider-name");

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
              this.summary = d.text();
            }
            else {
              this.description = d.text();
            }
          }
        }
        else if (point.contains("metadata.scraper")) {
          this.setScraperXml(el.attr("library"));
          this.setLanguage(el.attr("language"));

          // TODO: do we have some entity ENUMs ?!?
          // more here http://wiki.xbmc.org/index.php?title=addon.xml#.3Cextension.3E
          if (point.equals("xbmc.metadata.scraper.movies")) {
            System.out.println("I'm a MOVIE scraper");
            this.type = MediaType.MOVIE;
          }
          else if (point.equals("xbmc.metadata.scraper.tvshows")) {
            System.out.println("I'm a TV scraper");
            this.type = MediaType.TV_SHOW;
          }
          else if (point.equals("xbmc.metadata.scraper.albums")) {
            System.out.println("I'm a ALBUM scraper");
          }
          else if (point.equals("xbmc.metadata.scraper.artists")) {
            System.out.println("I'm a ARTIST scraper");
          }
          else if (point.equals("xbmc.metadata.scraper.musicvideos")) {
            System.out.println("I'm a MUSIC VIDEO scraper");
          }
        }
      }

      File settings = new File(scraperFolder, "resources/settings.xml");
      if (settings.exists()) {
        this.settingsPath = settings.getAbsolutePath();
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

  public void addFunction(ScraperFunction func) {
    functions.put(func.getName(), func);
  }

  public ScraperFunction getFunction(String name) {
    return functions.get(name);
  }

  public ScraperFunction[] getFunctions() {
    return functions.values().toArray(new ScraperFunction[functions.size()]);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public String getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
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

  public MediaType getType() {
    return type;
  }

  public void setType(MediaType type) {
    this.type = type;
  }

  /**
   * <p>
   * Uses <code>ReflectionToStringBuilder</code> to generate a <code>toString</code> for the specified object.
   * </p>
   * 
   * @return the String result
   * @see ReflectionToStringBuilder#toString(Object)
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
